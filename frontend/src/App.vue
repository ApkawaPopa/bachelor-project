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
import {useCrypto} from "@/composables/useCrypto.js";

const {sha256} = useCrypto();
const {isAuthenticated, username, restoreSession, login, register} = useAuth();
const {chats, activeChatId, activeMessages, loadChats, selectChat, sendMessage, deleteMessage, editMessage, createChat, deleteChat, connect} = useChat();
const {getUserKeysByChatId} = useUsers();

const isChatCreateOpen = ref(false);
const isChatMenuOpen = ref(false);

const users = ref([]);
const StringValues = ref([]);
const ImageValues = ref([]);

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
  users.value = await getUserKeysByChatId(activeChatId.value);
  ImageValues.value = []
  let keys = activeChatId.value.toString()
  for(const user of users.value){
    keys += user.publicKey;
  }
  const keysHash = await sha256(keys);
  const keysHash2 = await sha256(keysHash);
  for(let i=0;i<64;i+=16){
    let keySlice = "";
    let keySlices = []
    keySlice = keysHash.slice(i, i+16).toUpperCase();
    for(let k=0;k<16;k+=2)keySlices[k/2] = keySlice.slice(k, k+2)
    StringValues.value[i/16] = keySlices
  }
  let idx = 0;
  let keySliceRGB = [];
  for(let i=0;i<64;i+=4){
    let keySlice = "";
    keySlice = keysHash.slice(i, i+4);
    let keySlice2 = "";
    keySlice2 = keysHash2.slice(i, i+4);

    let keySum = 0;
    let keySum2 = 0;

    let keySliceCodes = [];
    for(let k=0;k<4;k++){
      keySliceCodes[k] = keySlice.codePointAt(k);
      keySliceCodes[k+4] = keySlice2.codePointAt(k);

      keySum += keySliceCodes[k];
      keySum2 += keySliceCodes[k+4];
    }
    keySliceCodes[8] = Math.abs(keySum-keySum2)

    for(let f=0;f<8;f++) {
      for(let k=f+1;k<=8;k++) {
        if(keySliceCodes[k] < keySliceCodes[f])keySliceRGB[idx++] = keySliceCodes[k] / keySliceCodes[f] * 255;
        else keySliceRGB[idx++] = keySliceCodes[f] / keySliceCodes[k] * 255;
      }
    }
  }
  for(let i=0;i<576;i+=48){
    let keySliceRGBsub = [];
    for(let k=i;k<i+48;k+=4){
      keySliceRGBsub.push(keySliceRGB.slice(k, k+4))
    }
    ImageValues.value.push(keySliceRGBsub)
  }
  isChatMenuOpen.value = true;
};

const handleChatDelete = () => {
  deleteChat(activeChatId.value);
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
        :string-values="StringValues"
        :image-values="ImageValues"
        @chat-delete="handleChatDelete"
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
  height: 100%;
  overflow: unset;
}
</style>