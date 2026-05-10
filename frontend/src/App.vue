<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue';

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
  connect,
  getChatImages,
  resetAll: chatResetAll,
} = useChat();
const {loadUserProfilePicture, getUserKeysByChatId, getUsersByChatId} = useUsers();
const {get, post} = useApi();
const {download, downloadAndDecryptByUrl} = useFileDownload();
const {uploadFile} = useFileUpload();

const showGallery = ref(false);
const galleryImages = ref([]);
const galleryInitialIndex = ref(0);
const selectedProfileUsername = ref('');

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

const profileModalKey = ref(0);

const activeChatImages = computed(() => {
  if (activeChatId.value === -1) return [];
  return getChatImages(activeChatId.value);
});

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < 768);

const onResize = () => {
  windowWidth.value = window.innerWidth;
};

onMounted(async () => {
  window.addEventListener('resize', onResize);
  const restored = await restoreSession();
  if (restored) {
    await loadChats();
    await connect();
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', onResize);
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
  try {
    const newPictureUrl = await loadUserProfilePicture(image);
    const response = await fetch(newPictureUrl);
    const blob = await response.blob();
    const blobUrl = URL.createObjectURL(blob);

    profileModalKey.value++;

    if (isChatMenuOpen.value && users.value) {
      const currentUserEntry = users.value.find(u => u.username === username.value);
      if (currentUserEntry) {
        currentUserEntry.url = blobUrl;
      }
    }
  } catch (err) {
    console.error('Upload profile picture failed', err);
  }
};

const handleLoadChatPic = async (image, symmetricKey) => {
  const {key} = await uploadFile(image, symmetricKey);
  const chatImage = await post(`/api/v1/chat/${activeChatId.value}/picture`, key);
  if (!chatImage) {
    throw new Error(`Load chat image failed`);
  }
};

const performLogout = async () => {
  showGallery.value = false;
  galleryImages.value = [];
  galleryInitialIndex.value = 0;
  selectedProfileUsername.value = '';
  isChatCreateOpen.value = false;
  isChatMenuOpen.value = false;
  isProfileMenuOpen.value = false;
  users.value = [];
  StringValues.value = [];
  ImageValues.value = [];
  profileModalKey.value = 0;

  await chatResetAll();

  logout();
};
</script>

<template>
  <div v-if="!isAuthenticated" class="auth-screen">
    <AuthForm @login="handleLogin" @register="handleRegister"/>
  </div>
  <div v-else class="app-layout">
    <div :class="{ 'sidebar--hidden': isMobile && activeChatId !== -1 }" class="sidebar">
      <ChatList
          :active-chat-id="activeChatId"
          :chats="chats"
          @select-chat="selectChat"
          @open-add-chat="isChatCreateOpen = true"
          @open-profile="handleProfileOpen"
      />
    </div>
    <div v-if="!isMobile || activeChatId !== -1" class="chat-area">
      <ChatWindow v-if="activeChatId !== -1"
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
      <div v-else class="empty-state">Выберите чат</div>
    </div>
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
        :chat-images="activeChatImages"
        @chat-delete="handleChatDelete"
        @close="isChatMenuOpen = false"
        @loadChatPicture="handleLoadChatPic"
        @openChatGallery="openGallery(activeChatImages, activeChatImages.length - 1)"
        @openUserProfile="handleUserProfileOpen"
    />
    <ProfileModal
        :key="profileModalKey"
        :editable="true"
        :onUploadPicture="handleUploadProfilePicture"
        :username="selectedProfileUsername"
        :visible="isProfileMenuOpen && selectedProfileUsername === username"
        @close="handleCloseProfile"
        @quitProfile="performLogout"
    />
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

<style scoped>
.app-layout {
  display: flex;
  height: 100dvh;
  background: var(--color-bg);
  overflow: hidden;
}

.sidebar {
  width: 300px;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; /* чтобы не переполнялся */
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  opacity: 0.6;
}

.auth-screen {
  height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
}

@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  .sidebar--hidden {
    display: none;
  }

  .chat-area {
    width: 100%;
    flex: 1;
    min-height: 0;
  }
}
</style>