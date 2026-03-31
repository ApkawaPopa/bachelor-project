import {computed, onUnmounted, ref} from 'vue';
import * as StompJs from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {useCrypto} from './useCrypto';
import {useApi} from './useApi';
import {useAuth} from './useAuth';
import {useFileUpload} from './useFileUpload';
import {useFileDownload} from "@/composables/useFileDownload.js";

export function useChat() {
    const {
        base64ToArrayBuffer, importSymmetricKey, rsaDecrypt,
        decryptMessageContent, encryptMessageContent, importPublicKeyFromJWK,
        rsaEncrypt, arrayBufferToBase64, generateAESKey
    } = useCrypto();
    const {get, post, del} = useApi();
    const auth = useAuth();
    const {uploadFile} = useFileUpload();
    const {downloadAndDecrypt} = useFileDownload();

    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
    const WS_URL = import.meta.env.VITE_WS_URL;

    const chats = ref([]);
    const activeChatId = ref(-1);
    const chatMessages = ref(new Map());
    const connectionStatus = ref('disconnected');
    const stompClient = ref(null);

    const activeMessages = computed(() => chatMessages.value.get(activeChatId.value) || []);

    function getTextDate(data){
        if(data.getHours()>=7)data.setUTCHours(data.getHours());
        else data.setUTCHours(data.getHours()+24);
        let date = data.getHours().toString() + ":";
        if(data.getHours() < 10)date = "0" + date;
        if(data.getMinutes() < 10)date += "0" + data.getMinutes().toString();
        else date += data.getMinutes().toString();
        return date
    }

    const loadChats = async () => {
        const data = await get('/api/v1/user/chats');
        const loadedChats = [];
        for (const item of data.data) {
            const data = new Date(item.sortingDate);
            if(data.getHours()>=7)data.setUTCHours(data.getHours());
            else data.setUTCHours(data.getHours()+24);
            const currentData = new Date();
            let date = "";
            if (currentData.getFullYear() == data.getFullYear() &&
                currentData.getMonth() == data.getMonth() &&
                currentData.getDate() == data.getDate()){
                date = getTextDate(new Date(item.sortingDate))
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
                createdAt: getTextDate(new Date(item.sortingDate)),
                unreadMessages: item.unreadMessagesCount,
                userCount: item.userCount
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

            let date = getTextDate(new Date(item.createdAt))

            const fileKeysWithNames = [];
            const imgs = [];
            if (item.fileKeys && item.fileKeys.length) {
                for (const fk of item.fileKeys) {
                    try {
                        const decryptedName = await decryptMessageContent(fk.filename, chat.symmetricKey);
                        if (decryptedName.endsWith(".jpg") || decryptedName.endsWith(".png")) {
                            const blob = await downloadAndDecrypt(fk.fileKey, chat.symmetricKey);
                            const url = URL.createObjectURL(blob);
                            imgs.push({
                                url: url
                            });
                        } else {
                            fileKeysWithNames.push({
                                fileKey: fk.fileKey,
                                filename: decryptedName
                            });
                        }
                    }catch(err){
                        console.log("Catch error while loading chat[" + chatId.toString() + "], message[" + item.id.toString() + "] with content:" + decryptedContent, err)
                    }
                }
            }
            messages.push({
                ...item,
                content: decryptedContent,
                receivers: item.receivers || [],
                fileKeys: fileKeysWithNames,
                images: imgs,
                time: date,
            });
        }
        chatMessages.value.set(chatId, messages);
    };

    const subscribeToChatTopics = (chatId, symmetricKey) => {
        if (!stompClient.value) return;

        stompClient.value.subscribe(`/topic/chat/${chatId}`, async (message) => {
            const {data} = JSON.parse(message.body);
            const decryptedContent = await decryptMessageContent(data.content, symmetricKey);

            const date = getTextDate(new Date(data.createdAt));

            const fileKeysWithNames = [];
            const imgs = [];
            if (data.fileKeys && data.fileKeys.length) {
                for (const fk of data.fileKeys) {
                    const decryptedName = await decryptMessageContent(fk.filename, symmetricKey);
                    if(decryptedName.endsWith(".jpg")||decryptedName.endsWith(".png")) {
                        const blob = await downloadAndDecrypt(fk.fileKey, symmetricKey);
                        const url = URL.createObjectURL(blob);
                        imgs.push({
                            url: url
                        });
                    }else{
                        fileKeysWithNames.push({
                            fileKey: fk.fileKey,
                            filename: decryptedName
                        });
                    }
                }
            }
            const newMessage = {
                ...data,
                content: decryptedContent,
                receivers: data.receivers || [],
                fileKeys: fileKeysWithNames,
                images: imgs,
                time: date,
            };

            const messages = chatMessages.value.get(chatId);
            if (messages) {
                messages.push(newMessage);
                chatMessages.value.set(chatId, messages);
            }

            const chatIdx = chats.value.findIndex(c => c.id === chatId);
            if (chatIdx !== -1) {
                let date = getTextDate(new Date(data.createdAt));
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

        stompClient.value.subscribe(`/topic/chat/${chatId}/deleted`, (message) => {
            const {data} = JSON.parse(message.body);
            const messages = chatMessages.value.get(chatId);
            if (!messages) return;
            const messag = messages.findIndex(c => c.id === data)
            if (messag===null) return;
            messages.splice(messag, 1)
            const chat = chats.value.find(c => c.id === chatId)
            if(messag == messages.length){
                if(messag == 0){
                    chat.lastMessage = ""
                    chat.time = chat.createdAt
                }else{
                    chat.lastMessage = messages[messag-1].content
                    chat.time = messages[messag-1].time
                }
            }
        });

        stompClient.value.subscribe(`/topic/chat/${chatId}/edited`, async (message) => {
            const {data} = JSON.parse(message.body);
            const chat = chats.value.find(c => c.id === chatId);
            if (!chat) return;
            const decryptContent = await decryptMessageContent(data.content, chat.symmetricKey);
            const messages = chatMessages.value.get(chatId);
            if (!messages) return;
            const messag = messages.find(c => c.id === data.id)
            if (!messag) return;
            const messagInd = messages.findIndex(c => c == messag)
            if(messagInd == messages.length - 1)chat.lastMessage = decryptContent
            messag.content = decryptContent
        });

        stompClient.value.subscribe(`/user/queue/unread`, (unread) => {
            const {data} = JSON.parse(unread.body);
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

            stompClient.value.subscribe('/user/queue/chat/created', async (message) => {
                const {data} = JSON.parse(message.body);
                const encryptedKey = base64ToArrayBuffer(data.encryptedSymmetricKey);
                const rawSymKey = await rsaDecrypt(auth.privateKey.value, encryptedKey);
                const symmetricKey = await importSymmetricKey(rawSymKey);
                const newChat = {
                    id: data.id,
                    name: data.name,
                    lastMessage: '',
                    symmetricKey,
                    time: getTextDate(new Date(data.createdAt)),
                    createdAt: getTextDate(new Date(data.createdAt)),
                    userCount: data.userCount
                };
                chats.value.unshift(newChat);
                chatMessages.value.set(data.id, []);
                subscribeToChatTopics(data.id, symmetricKey);
            });

            stompClient.value.subscribe('/user/queue/chat/deleted', async (message) => {
                const {data} = JSON.parse(message.body);
                const chatsInd = chats.value.findIndex(c => c.id == data);
                chatMessages.value.delete(data);
                chats.value.splice(chatsInd, 1);
                if(activeChatId.value==data)activeChatId.value = -1;
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

    const sendMessage = async (text, files = []) => {
        if (!stompClient.value || connectionStatus.value !== 'connected' || activeChatId.value === -1) return;
        const chat = chats.value.find(c => c.id === activeChatId.value);
        if (!chat) return;

        const fileKeys = [];
        for (const fileItem of files) {
            try {
                const {key} = await uploadFile(fileItem.file, chat.symmetricKey);
                fileKeys.push(key);
            } catch (err) {
                console.error('File upload failed', err);
                throw err; // прерываем отправку
            }
        }

        const encryptedContent = await encryptMessageContent(text, chat.symmetricKey);
        stompClient.value.publish({
            destination: `/app/chat/${activeChatId.value}/message/post`,
            body: JSON.stringify({
                content: encryptedContent,
                fileKeys: fileKeys
            })
        });
    };

    const deleteMessage = (id) => {
        if (!stompClient.value || connectionStatus.value !== 'connected' || activeChatId.value === -1) return;
        stompClient.value.publish({
            destination: `/app/chat/${activeChatId.value}/message/${id}/delete`,
            body: "",
        });
    };

    const editMessage = async (id, newContent) => {
        if (!stompClient.value || connectionStatus.value !== 'connected' || activeChatId.value === -1) return;
        const chat = chats.value.find(c => c.id === activeChatId.value);
        if (!chat) return;
        const encryptedNewContent = await encryptMessageContent(newContent, chat.symmetricKey);
        stompClient.value.publish({
            destination: `/app/chat/${activeChatId.value}/message/${id}/edit`,
            body: JSON.stringify({content: encryptedNewContent}),
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

    const deleteChat = async (chatId) => {
        const data = await del(`/api/v1/chat/${chatId}`);
        console.log('Chat deleted', data);
    }

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
        deleteMessage,
        editMessage,
        createChat,
        deleteChat,
        connect,
    };
}