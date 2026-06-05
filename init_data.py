import requests
import urllib3
from urllib3.exceptions import InsecureRequestWarning

urllib3.disable_warnings(InsecureRequestWarning)

BASE_URL = "https://localhost:56234"
USERS_COUNT = 50
CHATS_COUNT = 10
USERS_PER_CHAT = 5


def register_user(username, password):
    resp = requests.post(
        f"{BASE_URL}/api/v1/auth/register",
        json={
            "username": username,
            "passwordHash": password,
            "publicKey": "dummy" * 16,  # 64 символа
            "encryptedPrivateKey": "dummy" * 16
        },
        verify=False)
    if resp.status_code != 200:
        print(f"[FAIL] Register {username}: {resp.text}")
        return None
    print(f"[OK] Registered {username}")
    return resp.json()


def login_user(username, password):
    resp = requests.post(f"{BASE_URL}/api/v1/auth/login",
                         json={"username": username, "passwordHash": password},
                         verify=False)
    if resp.status_code != 200:
        raise Exception(f"Login failed for {username}: {resp.text}")
    print(f"[OK] Login {username}")
    return resp.json()["data"]["jwt"]


def create_chat(jwt, chat_name, participants):
    users_details = [{"username": p, "encryptedSymmetricKey": "dummy"} for p in participants]
    resp = requests.post(f"{BASE_URL}/api/v1/chat",
                         json={"chatName": chat_name, "usersDetails": users_details},
                         headers={"Authorization": f"Bearer {jwt}"},
                         verify=False)
    if resp.status_code not in (200, 201):
        print(f"[FAIL] Create chat {chat_name}: {resp.text}")
        return None
    chat_id = resp.json()['data']
    print(f"[OK] Chat {chat_name} created, id={chat_id}")
    return chat_id


def get_chat_users(jwt, chat_id):
    resp = requests.get(f"{BASE_URL}/api/v1/chat/{chat_id}/users",
                        headers={"Authorization": f"Bearer {jwt}"},
                        verify=False)
    if resp.status_code != 200:
        print(f"[FAIL] Get users for chat {chat_id}: {resp.text}")
        return []
    users = [u['username'] for u in resp.json()['data']]
    print(f"[OK] Chat {chat_id} users: {users}")
    return users


def main():
    # 1. Создаём пользователей
    users = [f"user_{i}" for i in range(1, USERS_COUNT + 1)]
    for u in users:
        register_user(u, "pass" * 16)  # 64 символа ("pass" * 16)

    # 2. Создаём чаты
    for chat_idx in range(CHATS_COUNT):
        start = chat_idx * USERS_PER_CHAT
        participants = users[start:start + USERS_PER_CHAT]
        creator = participants[0]
        jwt = login_user(creator, "pass" * 16)
        chat_id = create_chat(jwt, f"Chat_{chat_idx + 1}", participants)
        if chat_id:
            get_chat_users(jwt, chat_id)
        print("---")


if __name__ == "__main__":
    main()
