import {computed, onUnmounted, ref} from 'vue';
import * as StompJs from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {useCrypto} from './useCrypto';
import {useApi} from './useApi';
import {useAuth} from './useAuth';

export function useChat() {
    const {
        base64ToArrayBuffer, importSymmetricKey, rsaDecrypt,
        decryptMessageContent, encryptMessageContent, importPublicKeyFromJWK,
        rsaEncrypt, arrayBufferToBase64, generateAESKey
    } = useCrypto();
    const {get, post} = useApi();
    const auth = useAuth();

    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
    const WS_URL = import.meta.env.VITE_WS_URL;

    const chats = ref([]);
    const activeChatId = ref(-1);
    const chatMessages = ref(new Map());
    const connectionStatus = ref('disconnected');
    const stompClient = ref(null);

    const activeMessages = computed(() => chatMessages.value.get(activeChatId.value) || []);

    const loadChats = async () => {
        const data = await get('/api/v1/user/chats');
        const loadedChats = [];
        for (const item of data.data) {
            const data = new Date(item.sortingDate);
            data.setUTCHours(data.getHours());
            const currentData = new Date();
            let date = "";
            if (currentData.getFullYear() == data.getFullYear() &&
                currentData.getMonth() == data.getMonth() &&
                currentData.getDate() == data.getDate()){
                date = data.getHours().toString() + ":";
                if(data.getMinutes() < 10)date += "0" + data.getMinutes().toString();
                else date += data.getMinutes().toString();
            }else if (currentData.getFullYear() == data.getFullYear() &&
                currentData.getMonth() == data.getMonth() &&
                Math.abs(currentData.getDate() - data.getDate()) < 8){
                const DayOfNedelya = new Array("пн", "вт", "ср", "чт", "пт", "сб", "вс")
                date = DayOfNedelya[data.getDay() - 1];
            }else if (currentData.getFullYear() == data.getFullYear()) {
                const Month = new Array("янв.", "фев.", "мар.", "апр.", "мая", "июнь", "июль", "авг.", "сен.", "окт.", "ноя.", "дек.")
                date = data.getDate().toString() + " " + Month[data.getMonth()];
            }else{
                date = data.getDate().toString() + "." + data.getMonth().toString() + "." + data.getFullYear().toString()[2] + data.getFullYear().toString()[3];
            }
            const encryptedKey = base64ToArrayBuffer(item.encryptedSymmetricKey);
            const rawSymKey = await rsaDecrypt(auth.privateKey.value, encryptedKey);
            const symmetricKey = await importSymmetricKey(rawSymKey);
            let lastMessage = '';
            if (item.messageContent) {
                lastMessage = await decryptMessageContent(item.messageContent, symmetricKey);
            }
            loadedChats.push({
                id: item.id,
                name: item.name,
                lastMessage,
                symmetricKey,
                time: date,
                unreadMessages: item.unreadMessagesCount,
            });
        }
        chats.value = loadedChats;
    };

    const loadChatHistory = async (chatId) => {
        const data = await get(`/api/v1/chat/${chatId}/message`);
        const chat = chats.value.find(c => c.id === chatId);
        if (!chat) return;
        const messages = [];
        for (const item of data.data) {
            const decryptedContent = await decryptMessageContent(item.content, chat.symmetricKey);
            messages.push({
                ...item,
                content: decryptedContent,
                receivers: item.receivers || [],
            });
        }
        chatMessages.value.set(chatId, messages);
    };

    const subscribeToChatTopics = (chatId, symmetricKey) => {
        if (!stompClient.value) return;

        stompClient.value.subscribe(`/topic/chat/${chatId}`, async (message) => {
            const {data} = JSON.parse(message.body);
            const decryptedContent = await decryptMessageContent(data.content, symmetricKey);
            const newMessage = {
                ...data,
                content: decryptedContent,
                receivers: data.receivers || [],
            };
            const messages = chatMessages.value.get(chatId);
            if (messages) {
                messages.push(newMessage);
                chatMessages.value.set(chatId, messages);
            }

            const chatIdx = chats.value.findIndex(c => c.id === chatId);
            if (chatIdx !== -1) {
                let dateOrigin = new Date(data.createdAt);
                dateOrigin.setUTCHours(dateOrigin.getHours());
                let date = dateOrigin.getHours().toString() + ":";
                if(dateOrigin.getMinutes() < 10)date += "0" + dateOrigin.getMinutes().toString();
                else date += dateOrigin.getMinutes().toString();
                chats.value[chatIdx].time = date;
                chats.value[chatIdx].lastMessage = decryptedContent;
                const [chat] = chats.value.splice(chatIdx, 1);
                chats.value.unshift(chat);
            }

            if (chatId === activeChatId.value) {
                stompClient.value.publish({
                    destination: `/app/chat/${chatId}/message/${data.id}/receive`,
                });
            }
        });

        stompClient.value.subscribe(`/topic/chat/${chatId}/received`, (message) => {
            const {data} = JSON.parse(message.body);
            const messages = chatMessages.value.get(chatId);
            if (messages) {
                const targetMsg = messages.find(m => m.id === data.id);
                if (targetMsg) {
                    targetMsg.receivers.push(data);
                }
            }
        });

        stompClient.value.subscribe(`/user/queue/unread`, (unread) => {
            const {data} = JSON.parse(unread.body);
            console.log(data);
            const chat = chats.value[chats.value.findIndex(c => c.id == data.chatId)];
            if (chat) {
                chat.unreadMessages = data.unreadMessagesCount;
            }
        });
    };

    const connect = async () => {
        const wsTokenData = await post('/api/v1/auth/ws-token', {});
        const wsToken = wsTokenData.data;

        stompClient.value = new StompJs.Client({
            webSocketFactory: () => new SockJS(`https://${API_BASE_URL}${WS_URL}?token=${wsToken}`),
            connectHeaders: {Authorization: `Bearer ${auth.jwtToken.value}`},
            debug: (str) => console.log(str),
        });

        stompClient.value.onConnect = () => {
            connectionStatus.value = 'connected';
            console.log('WebSocket connected');

            stompClient.value.subscribe('/user/queue/error', (msg) => console.error('STOMP error', msg.body));

            stompClient.value.subscribe('/user/queue/chat', async (message) => {
                const {data} = JSON.parse(message.body);
                const encryptedKey = base64ToArrayBuffer(data.encryptedSymmetricKey);
                const rawSymKey = await rsaDecrypt(auth.privateKey.value, encryptedKey);
                const symmetricKey = await importSymmetricKey(rawSymKey);
                const newChat = {
                    id: data.id,
                    name: data.name,
                    lastMessage: '',
                    symmetricKey,
                };
                chats.value.unshift(newChat);
                chatMessages.value.set(data.id, []);
                subscribeToChatTopics(data.id, symmetricKey);
            });

            chats.value.forEach(chat => {
                subscribeToChatTopics(chat.id, chat.symmetricKey);
            });
        };

        stompClient.value.onStompError = (frame) => {
            console.error('Broker error', frame.headers['message'], frame.body);
            connectionStatus.value = 'error';
        };

        stompClient.value.onWebSocketClose = () => {
            connectionStatus.value = 'disconnected';
            console.log('WebSocket closed, reconnecting in 5s');
            setTimeout(() => connect(), 5000);
        };

        stompClient.value.activate();
    };

    const sendMessage = async (text) => {
        if (!stompClient.value || connectionStatus.value !== 'connected' || activeChatId.value === -1) return;
        const chat = chats.value.find(c => c.id === activeChatId.value);
        if (!chat) return;
        const encryptedContent = await encryptMessageContent(text, chat.symmetricKey);
        stompClient.value.publish({
            destination: `/app/chat/${activeChatId.value}/message/post`,
            body: JSON.stringify({content: encryptedContent}),
        });
    };

    const selectChat = async (chatId) => {
        activeChatId.value = chatId;
        if (!chatMessages.value.has(chatId)) {
            await loadChatHistory(chatId);
        }
        const messages = chatMessages.value.get(chatId) || [];
        messages.forEach(msg => {
            if (!msg.receivers.some(r => r.username === auth.username.value)) {
                stompClient.value?.publish({
                    destination: `/app/chat/${chatId}/message/${msg.id}/receive`,
                });
            }
        });
    };

    const createChat = async (chatName, participants) => {
        const allUsernames = participants.map(p => ({username: p.username}));
        const keysData = await post('/api/v1/user/keys', {usernames: allUsernames});
        const usersWithKeys = keysData.data;

        const rawSymmetricKey = await crypto.subtle.exportKey('raw', await generateAESKey());
        const usersDetails = [];

        for (const user of usersWithKeys) {
            const publicKey = await importPublicKeyFromJWK(JSON.parse(user.publicKey));
            const encryptedKey = await rsaEncrypt(publicKey, rawSymmetricKey);
            usersDetails.push({
                username: user.username,
                encryptedSymmetricKey: arrayBufferToBase64(encryptedKey),
            });
        }

        const response = await post('/api/v1/chat', {
            chatName,
            usersDetails,
        });
        console.log('Chat created', response.data);
    };

    onUnmounted(() => {
        if (stompClient.value) {
            stompClient.value.deactivate();
        }
    });

    return {
        chats,
        activeChatId,
        chatMessages,
        activeMessages,
        connectionStatus,
        loadChats,
        selectChat,
        sendMessage,
        createChat,
        connect,
    };
}