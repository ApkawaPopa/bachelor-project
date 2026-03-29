<script setup>
import {onMounted, ref} from 'vue';

import AuthForm from '@/components/auth/AuthForm.vue';

import ChatList from '@/components/chat/ChatList.vue';
import ChatWindow from '@/components/chat/ChatWindow.vue';
import AddChatModal from '@/components/chat/AddChatModal.vue';
import ChatMenuModal from "@/components/chat/ChatMenuModal.vue";

import {useAuth} from '@/composables/useAuth';
import {useChat} from '@/composables/useChat';
import {useUsers} from "@/composables/useUsers.js";

const {isAuthenticated, username, restoreSession, login, register} = useAuth();
const {chats, activeChatId, activeMessages, loadChats, selectChat, sendMessage, deleteMessage, editMessage, createChat, connect} = useChat();
const {getUsersByChatId} = useUsers();

const isChatCreateOpen = ref(false);
const isChatMenuOpen = ref(false);

const users = ref([]);

onMounted(async () => {
  const restored = await restoreSession();
  if (restored) {
    await loadChats();
    await connect();
  }
});

const handleLogin = async (user, pass) => {
  const result = await login(user, pass);
  if (result.success) {
    await loadChats();
    await connect();
  }
  // Здесь можно добавить уведомление об ошибке, если !result.success
};

const handleRegister = async (user, pass) => {
  const result = await register(user, pass);
  if (result.success) {
    await loadChats();
    await connect();
  }
};

const handleCreateChat = async ({name, participants}) => {
  await createChat(name, participants);
  isChatCreateOpen.value = false;
};

const handleChatMenu = async () => {
  users.value = await getUsersByChatId(activeChatId.value);
  isChatMenuOpen.value = true;
};
</script>

<template>
  <div v-if="!isAuthenticated" id="autorazeBlocker">
    <AuthForm @login="handleLogin" @register="handleRegister"/>
  </div>
  <div v-else id="ForChats">
    <ChatList
        :active-chat-id="activeChatId"
        :chats="chats"
        @select-chat="selectChat"
        @open-add-chat="isChatCreateOpen = true"
    />
    <ChatWindow
        v-if="activeChatId !== -1"
        :active-chat-id="activeChatId"
        :chat-name="chats.find(c => c.id === activeChatId)?.name"
        :chat-symmetric-key="chats.find(c => c.id === activeChatId)?.symmetricKey"
        :current-user="username"
        :messages="activeMessages"
        :userCount="chats.find(c => c.id === activeChatId)?.userCount"
        @send-message="sendMessage"
        @delete-message="deleteMessage"
        @edit-message="editMessage"
        @leave-chat="activeChatId = -1"
        @open-chat-menu="handleChatMenu"
    />
    <AddChatModal
        v-if="isChatCreateOpen"
        :active-chat-id="activeChatId"
        :current-user="username"
        @close="isChatCreateOpen = false"
        @create="handleCreateChat"
    />
    <ChatMenuModal
        v-if="isChatMenuOpen"
        :active-chat-id="activeChatId"
        :chat-name="chats.find(c => c.id === activeChatId)?.name"
        :users="users"
        @close="isChatMenuOpen = false"
    />
  </div>
</template>

<style>
#autorazeBlocker {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: black;
}

#ForChats {
  background-color: rgb(0, 0, 0);
  width: 100vw;
  min-height: 100vh;
  height: auto;
  overflow: auto;
}
</style>