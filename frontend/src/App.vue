<script setup>
import {onMounted, ref} from 'vue';
import AuthForm from '@/components/auth/AuthForm.vue';
import ChatList from '@/components/chat/ChatList.vue';
import ChatWindow from '@/components/chat/ChatWindow.vue';
import AddChatModal from '@/components/chat/AddChatModal.vue';
import {useAuth} from '@/composables/useAuth';
import {useChat} from '@/composables/useChat';

const {isAuthenticated, username, restoreSession, login, register} = useAuth();
const {chats, activeChatId, activeMessages, loadChats, selectChat, sendMessage, createChat, connect} = useChat();

const isChatMenuOpen = ref(false);

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
  isChatMenuOpen.value = false;
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
        @open-add-chat="isChatMenuOpen = true"
    />
    <ChatWindow
        v-if="activeChatId !== -1"
        :chat-name="chats.find(c => c.id === activeChatId)?.name"
        :current-user="username"
        :messages="activeMessages"
        @send-message="sendMessage"
        @leave-chat="activeChatId = -1"
    />
    <AddChatModal
        v-if="isChatMenuOpen"
        :current-user="username"
        @close="isChatMenuOpen = false"
        @create="handleCreateChat"
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