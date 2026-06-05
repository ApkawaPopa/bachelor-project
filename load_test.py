import asyncio
import json
import random
import ssl
import statistics
import string
import time
from collections import defaultdict

import aiohttp
import websockets

BASE_URL = "https://localhost:56234"
USERS_COUNT = 50
USERS_PER_CHAT = 5
CHATS_COUNT = USERS_COUNT // USERS_PER_CHAT  # =10
INTERVAL_SEC = 5.0  # отправка каждые 5 секунд
TEST_DURATION_SEC = 300  # 5 минут

ssl_ctx = ssl.create_default_context()
ssl_ctx.check_hostname = False
ssl_ctx.verify_mode = ssl.CERT_NONE


# ---------- STOMP helpers ----------
def stomp_connect_frame():
    return b"CONNECT\r\naccept-version:1.2\r\nhost:localhost\r\nheart-beat:10000,10000\r\n\r\n\x00"


def stomp_subscribe_frame(sub_id, destination):
    body = f"SUBSCRIBE\r\nid:{sub_id}\r\ndestination:{destination}\r\nack:auto\r\n\r\n\x00"
    return body.encode()


def stomp_send_frame(destination, body_dict):
    body_str = json.dumps(body_dict)
    frame = f"SEND\r\ndestination:{destination}\r\ncontent-length:{len(body_str)}\r\n\r\n{body_str}\x00"
    return frame.encode()


def parse_stomp_frame(data):
    if isinstance(data, str):
        data = data.encode('utf-8')
    delimiter = b'\r\n\r\n' if b'\r\n\r\n' in data else b'\n\n'
    parts = data.split(delimiter, 1)
    if len(parts) != 2:
        return None, None, None
    header_part, body = parts
    lines = header_part.split(b'\r\n') if b'\r\n' in header_part else header_part.split(b'\n')
    if not lines:
        return None, None, None
    command = lines[0].decode().strip()
    headers = {}
    for line in lines[1:]:
        if b':' in line:
            k, v = line.split(b':', 1)
            headers[k.decode().strip()] = v.decode().strip()
    if body and body[-1] == 0:
        body = body[:-1]
    return command, headers, body


# ---------- HTTP helpers ----------
async def http_post(session, endpoint, json_data, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    async with session.post(f"{BASE_URL}{endpoint}", json=json_data, headers=headers, ssl=False) as resp:
        return await resp.json()


async def login(session, username, password):
    data = await http_post(session, "/api/v1/auth/login", {"username": username, "passwordHash": password})
    if data.get("code") != 200:
        raise Exception(f"Login failed: {data}")
    return data["data"]["jwt"]


async def get_ws_token(session, jwt):
    data = await http_post(session, "/api/v1/auth/ws-token", {}, token=jwt)
    if data.get("code") not in (200, 201):
        raise Exception(f"WS token failed: {data}")
    return data["data"]


async def check_user_in_chat(session, jwt, chat_id, username):
    headers = {"Authorization": f"Bearer {jwt}"}
    async with session.get(f"{BASE_URL}/api/v1/chat/{chat_id}/users", headers=headers, ssl=False) as resp:
        if resp.status != 200:
            return False
        data = await resp.json()
        users_in_chat = [u['username'] for u in data['data']]
        return username in users_in_chat


def random_string(length=75):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))


async def user_worker(user_id, stats, stop_event):
    username = f"user_{user_id}"
    password = "pass" * 16
    chat_id = ((user_id - 1) // USERS_PER_CHAT) + 37
    offset = (user_id - 1) % USERS_PER_CHAT  # 0..4
    print(f"[{username}] Starting, chat_id={chat_id}, offset={offset}")

    async with aiohttp.ClientSession() as session:
        try:
            login_start = time.time()
            jwt = await login(session, username, password)
            stats["login_time"].append(time.time() - login_start)

            if not await check_user_in_chat(session, jwt, chat_id, username):
                print(f"[{username}] NOT a member of chat {chat_id}, aborting")
                stats["errors"]["chat_membership"] += 1
                return

            ws_token_start = time.time()
            ws_token = await get_ws_token(session, jwt)
            ws_url = f"wss://localhost:56234/ws?token={ws_token}"
            async with websockets.connect(ws_url, ssl=ssl_ctx, subprotocols=['v12.stomp']) as websocket:
                await websocket.send(stomp_connect_frame())
                resp = await websocket.recv()
                cmd, _, _ = parse_stomp_frame(resp)
                if cmd != "CONNECTED":
                    raise Exception(f"CONNECT failed: {resp[:100]}")
                ws_connect_time = time.time() - ws_token_start
                stats["ws_connect_time"].append(ws_connect_time)
                stats["connections_success"] += 1
                print(f"[{username}] STOMP connected in {ws_connect_time * 1000:.1f}ms")

                await websocket.send(stomp_subscribe_frame(f"sub-{chat_id}", f"/topic/chat/{chat_id}"))
                await websocket.send(stomp_subscribe_frame("error-queue", "/user/queue/error"))
                await asyncio.sleep(1)  # даём время на регистрацию подписки

                async def receiver():
                    while not stop_event.is_set():
                        try:
                            msg = await asyncio.wait_for(websocket.recv(), timeout=1.0)
                            receive_time = time.time()
                            cmd, headers, body = parse_stomp_frame(msg)
                            if cmd == "MESSAGE" and headers.get("destination") == f"/topic/chat/{chat_id}":
                                try:
                                    body_json = json.loads(body.decode())
                                    content = body_json.get("data").get("content")
                                    parts = content.split(":", 1)
                                    if len(parts) >= 1:
                                        sent_ts = float(parts[0])
                                        latency = receive_time - sent_ts
                                        stats["rtt"].append(latency)
                                except Exception:
                                    pass
                                stats["received_msgs"] += 1
                            elif cmd == "ERROR":
                                err_body = body.decode(errors='replace') if body else ''
                                print(f"[{username}] STOMP error: {err_body}")
                                stats["errors"]["stomp"] += 1
                        except asyncio.TimeoutError:
                            continue
                        except websockets.exceptions.ConnectionClosed:
                            stats["connections_dropped"] += 1
                            break

                recv_task = asyncio.create_task(receiver())

                # Начальная задержка согласно offset
                if offset > 0:
                    await asyncio.sleep(offset)

                # Цикл отправки каждые 5 секунд
                while not stop_event.is_set():
                    send_time = time.time()
                    content = f"{send_time}:{random_string(75)}"
                    payload = {"content": content, "fileKeys": []}
                    await websocket.send(stomp_send_frame(f"/app/chat/{chat_id}/message/post", payload))
                    stats["sent_msgs"] += 1
                    stats["send_time"].append(time.time() - send_time)
                    await asyncio.sleep(INTERVAL_SEC)

                recv_task.cancel()

        except Exception as e:
            print(f"[{username}] Error: {e}")
            stats["errors"]["general"] += 1
            stats["connections_failed"] += 1


async def periodic_stats_printer(stats, stop_event, interval=10):
    while not stop_event.is_set():
        await asyncio.sleep(interval)
        now = time.time()
        sent_rate = stats["sent_msgs"] / (now - start_time)
        recv_rate = stats["received_msgs"] / (now - start_time)
        expected_recv = stats["sent_msgs"] * USERS_PER_CHAT
        loss = expected_recv - stats["received_msgs"]
        loss_pct = 100 * loss / expected_recv if expected_recv > 0 else 0
        print(f"\n--- Interim stats after {now - start_time:.0f}s ---")
        print(f"  Sent: {stats['sent_msgs']} ({sent_rate:.1f} msg/s)")
        print(f"  Received: {stats['received_msgs']} ({recv_rate:.1f} msg/s)")
        print(f"  Expected received: {expected_recv} (loss: {loss} ({loss_pct:.1f}%))")
        print(f"  Errors: {dict(stats['errors'])}")
        print(f"  Active connections: {stats['connections_success'] - stats['connections_dropped']}")
        if stats['rtt']:
            p50 = statistics.median(stats['rtt'])
            p95 = statistics.quantiles(stats['rtt'], n=20)[18] if len(stats['rtt']) >= 20 else max(stats['rtt'])
            p99 = statistics.quantiles(stats['rtt'], n=100)[98] if len(stats['rtt']) >= 100 else max(stats['rtt'])
            print(f"  RTT (ms) — avg: {statistics.mean(stats['rtt']) * 1000:.1f}, "
                  f"p50: {p50 * 1000:.1f}, p95: {p95 * 1000:.1f}, p99: {p99 * 1000:.1f}")


async def auto_stop(stop_event, duration):
    await asyncio.sleep(duration)
    print(f"\n--- Test duration ({duration}s) reached, stopping... ---")
    stop_event.set()


async def main():
    stats = {
        "sent_msgs": 0,
        "received_msgs": 0,
        "rtt": [],
        "send_time": [],
        "login_time": [],
        "ws_connect_time": [],
        "connections_success": 0,
        "connections_failed": 0,
        "connections_dropped": 0,
        "errors": defaultdict(int)
    }
    stop_event = asyncio.Event()
    workers = [user_worker(i, stats, stop_event) for i in range(1, USERS_COUNT + 1)]
    printer = asyncio.create_task(periodic_stats_printer(stats, stop_event))
    stopper = asyncio.create_task(auto_stop(stop_event, TEST_DURATION_SEC))

    global start_time
    start_time = time.time()
    print("Starting load test...")

    try:
        await asyncio.gather(*workers)
    except KeyboardInterrupt:
        stop_event.set()
        print("\nStopping early due to interrupt...")
    finally:
        printer.cancel()
        stopper.cancel()
        duration = time.time() - start_time
        print(f"\n=== Final Results after {duration:.1f}s ===")
        print(f"Sent messages: {stats['sent_msgs']}")
        print(f"Received messages: {stats['received_msgs']}")
        expected_recv = stats['sent_msgs'] * USERS_PER_CHAT
        loss = expected_recv - stats['received_msgs']
        loss_pct = 100 * loss / expected_recv if expected_recv > 0 else 0
        print(f"Expected received (5x sent): {expected_recv}, loss: {loss} ({loss_pct:.1f}%)")
        print(
            f"Connections: success={stats['connections_success']}, failed={stats['connections_failed']}, dropped={stats['connections_dropped']}")
        print(f"Errors: {dict(stats['errors'])}")
        if stats['rtt']:
            avg_rtt = statistics.mean(stats['rtt']) * 1000
            p50 = statistics.median(stats['rtt']) * 1000
            p95 = statistics.quantiles(stats['rtt'], n=20)[18] * 1000 if len(stats['rtt']) >= 20 else max(
                stats['rtt']) * 1000
            p99 = statistics.quantiles(stats['rtt'], n=100)[98] * 1000 if len(stats['rtt']) >= 100 else max(
                stats['rtt']) * 1000
            print(f"RTT (ms) — avg: {avg_rtt:.1f}, p50: {p50:.1f}, p95: {p95:.1f}, p99: {p99:.1f}")
        if stats['send_time']:
            avg_send = statistics.mean(stats['send_time']) * 1000
            print(f"Send operation time (ms): avg {avg_send:.1f}")
        if stats['login_time']:
            avg_login = statistics.mean(stats['login_time']) * 1000
            print(f"Login time (ms): avg {avg_login:.1f}")
        if stats['ws_connect_time']:
            avg_ws = statistics.mean(stats['ws_connect_time']) * 1000
            print(f"WebSocket+STOMP handshake time (ms): avg {avg_ws:.1f}")


if __name__ == "__main__":
    start_time = 0
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nTest interrupted by user")
