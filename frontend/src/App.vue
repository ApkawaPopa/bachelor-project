<script setup>
import {onMounted, ref} from 'vue';

import AuthForm from '@/components/auth/AuthForm.vue';

import ChatList from '@/components/chat/ChatList.vue';
import ChatWindow from '@/components/chat/ChatWindow.vue';
import AddChatModal from '@/components/chat/AddChatModal.vue';
import ChatMenuModal from "@/components/chat/ChatMenuModal.vue";
import ProfileModal from "@/components/profile/ProfileModal.vue";

import {useCrypto} from '@/composables/useCrypto';
import {useAuth} from '@/composables/useAuth';
import {useApi} from "./composables/useApi.js";
import {useChat} from '@/composables/useChat';
import {useUsers} from "@/composables/useUsers.js";
import {useFileDownload} from "@/composables/useFileDownload.js";
import {useFileUpload} from "@/composables/useFileUpload.js";
import ImageCarouselModal from "@/components/common/ImageCarouselModal.vue";

const {sha256} = useCrypto();
const {isAuthenticated, username, restoreSession, login, register, logout} = useAuth();
const {
  chats,
  activeChatId,
  activeMessages,
  loadChats,
  selectChat,
  sendMessage,
  deleteMessage,
  editMessage,
  createChat,
  deleteChat,
  connect
} = useChat();
const {loadUserProfilePicture, getUserKeysByChatId, getUsersByChatId} = useUsers();
const {get, post} = useApi();
const {download, downloadAndDecryptByUrl} = useFileDownload();
const {uploadFile} = useFileUpload();

const showGallery = ref(false);
const galleryImages = ref([]);
const galleryInitialIndex = ref(0);
const selectedProfileUsername = ref('');
const chatImages = ref([]);

const openGallery = (images, index = 0) => {
  galleryImages.value = images;
  galleryInitialIndex.value = index;
  showGallery.value = true;
};

const isChatCreateOpen = ref(false);
const isChatMenuOpen = ref(false);
const isProfileMenuOpen = ref(false);

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
  isChatMenuOpen.value = true;
  const chat = chats.value.find(c => c.id === activeChatId.value);
  if (chat) {
    if (chat.pictureUrls) {
      const images = [];
      for (const picUrl of chat.pictureUrls) {
        try {
          const blob = await downloadAndDecryptByUrl(picUrl, chat.symmetricKey);
          images.push(URL.createObjectURL(blob));
        } catch (e) {
          console.error(e);
        }
      }
      chatImages.value = images;
    } else {
      chatImages.value = [];
    }
  }
  users.value = await getUserKeysByChatId(activeChatId.value);
  ImageValues.value = [];
  let keys = activeChatId.value.toString();
  for (const user of users.value) {
    keys += user.publicKey;
  }
  const keysHash = await sha256(keys);
  const keysHash2 = await sha256(keysHash);
  for (let i = 0; i < 64; i += 16) {
    let keySlice = "";
    let keySlices = [];
    keySlice = keysHash.slice(i, i + 16).toUpperCase();
    for (let k = 0; k < 16; k += 2) keySlices[k / 2] = keySlice.slice(k, k + 2);
    StringValues.value[i / 16] = keySlices;
  }
  let idx = 0;
  let keySliceRGB = [];
  for (let i = 0; i < 64; i += 4) {
    let keySlice = "";
    keySlice = keysHash.slice(i, i + 4);
    let keySlice2 = "";
    keySlice2 = keysHash2.slice(i, i + 4);

    let keySum = 0;
    let keySum2 = 0;

    let keySliceCodes = [];
    for (let k = 0; k < 4; k++) {
      keySliceCodes[k] = keySlice.codePointAt(k);
      keySliceCodes[k + 4] = keySlice2.codePointAt(k);

      keySum += keySliceCodes[k];
      keySum2 += keySliceCodes[k + 4];
    }
    keySliceCodes[8] = Math.abs(keySum - keySum2);

    for (let f = 0; f < 8; f++) {
      for (let k = f + 1; k <= 8; k++) {
        if (keySliceCodes[k] < keySliceCodes[f]) keySliceRGB[idx++] = keySliceCodes[k] / keySliceCodes[f] * 255;
        else keySliceRGB[idx++] = keySliceCodes[f] / keySliceCodes[k] * 255;
      }
    }
  }
  for (let i = 0; i < 576; i += 48) {
    let keySliceRGBsub = [];
    for (let k = i; k < i + 48; k += 4) {
      keySliceRGBsub.push(keySliceRGB.slice(k, k + 4));
    }
    ImageValues.value.push(keySliceRGBsub);
  }

  const resp = await getUsersByChatId(activeChatId.value);
  for (const item of resp) {
    const {data} = await get(`/api/v1/user/${item.username}/pictures`);
    if (data.length > 0) {
      const urle = await download(data[data.length - 1]);
      users.value[users.value.findIndex(c => c.username === item.username)].url = urle;
    }
  }
};

const handleChatDelete = () => {
  deleteChat(activeChatId.value);
  isChatMenuOpen.value = false;
};

const handleProfileOpen = () => {
  selectedProfileUsername.value = username.value;
  isProfileMenuOpen.value = true;
};

const handleCloseProfile = () => {
  isProfileMenuOpen.value = false;
  selectedProfileUsername.value = '';
};

const handleUserProfileOpen = (usernameParam) => {
  selectedProfileUsername.value = usernameParam;
  isProfileMenuOpen.value = true;
};

const handleUploadProfilePicture = async (image) => {
  await loadUserProfilePicture(image);
};

const handleLoadChatPic = async (image, symmetricKey) => {
  const {key} = await uploadFile(image, symmetricKey);
  const chatImage = await post(`/api/v1/chat/${activeChatId.value}/picture`, key);
  if (!chatImage) {
    throw new Error(`Load chat image failed`);
  }
};
</script>

<template>
  <div v-if="!isAuthenticated" id="autorazeBlocker">
    <AuthForm @login="handleLogin" @register="handleRegister"/>
  </div>
  <div v-else id="ForChats">
    <div id="sideMenu">
      <ChatList
          :active-chat-id="activeChatId"
          :chats="chats"
          @select-chat="selectChat"
          @open-add-chat="isChatCreateOpen = true"
          @open-profile="handleProfileOpen"
      />
    </div>
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
        :current-user="username"
        @close="isChatCreateOpen = false"
        @create="handleCreateChat"
    />
    <ChatMenuModal
        v-if="isChatMenuOpen"
        :active-chat-id="activeChatId"
        :chat-name="chats.find(c => c.id === activeChatId)?.name"
        :chat-symmetric-key="chats.find(c => c.id === activeChatId)?.symmetricKey"
        :users="users"
        :string-values="StringValues"
        :image-values="ImageValues"
        :chat-images="chatImages"
        @chat-delete="handleChatDelete"
        @close="isChatMenuOpen = false"
        @loadChatPicture="handleLoadChatPic"
        @openChatGallery="openGallery(chatImages, chatImages.length - 1)"
        @openUserProfile="handleUserProfileOpen"
    />
    <!-- Собственный профиль -->
    <ProfileModal
        :editable="true"
        :onUploadPicture="handleUploadProfilePicture"
        :username="selectedProfileUsername"
        :visible="isProfileMenuOpen && selectedProfileUsername === username"
        @close="handleCloseProfile"
        @quitProfile="logout"
    />
    <!-- Чужой профиль -->
    <ProfileModal
        :editable="false"
        :username="selectedProfileUsername"
        :visible="isProfileMenuOpen && selectedProfileUsername !== username"
        @close="handleCloseProfile"
    />
    <ImageCarouselModal
        :images="galleryImages"
        :initialIndex="galleryInitialIndex"
        :visible="showGallery"
        @close="showGallery = false"
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

#sideMenu {
  width: calc(max(1vh, 1vw) * 25);
  height: 100vh;
  float: left;
  border-right: 1px solid white;
}
</style>