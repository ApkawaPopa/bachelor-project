<template>
  <div class="chat-window">
    <div class="chat-header">
      <button class="btn" @click="$emit('leave-chat')">←</button>
      <div class="chat-header__info" @click="$emit('open-chat-menu')">
        <div class="chat-header__name">{{ chatName }}</div>
        <div class="chat-header__participants">{{ userCount }} участников</div>
      </div>
    </div>

    <div ref="messagesContainer" class="messages">
      <div v-for="message in messages"
           :key="message.id"
           :class="{'message--own': message.sender === currentUser}"
           class="message">
        <div class="message-bubble"
             @click.stop="(e) => showMenu(message, message.sender === currentUser, e)">
          <div v-if="message.sender !== currentUser" class="message-bubble__sender">{{ message.sender }}</div>
          <div v-if="message.images?.length" class="message-images">
            <img v-for="img in message.images" :src="img.url" class="message-image"/>
          </div>
          <div>{{ message.content }}</div>
          <div v-if="message.fileKeys?.length" class="message-attachments">
            <div v-for="file in message.fileKeys" :key="file.fileKey" class="attachment"
                 @click="downloadFile(file.fileKey, file.filename)">
              📄 {{ file.filename }}
            </div>
          </div>
          <div class="message-bubble__meta">
            <span v-if="message.sender === currentUser" class="message-status">
              {{ message.receivers.length >= 2 ? '🤝' : (message.receivers.length === 1 ? '👋' : '🕚') }}
            </span>
            <span>{{ message.time }}</span>
          </div>
        </div>

        <div v-if="menu.visible && message.sender === currentUser" :style="{ top: menu.y, right: menu.x }"
             class="context-menu">
          <button class="btn btn--danger" @click="handleDelete(message.id)">Удалить</button>
          <button class="btn" @click="handleEdit(message)">Изменить</button>
        </div>
      </div>
    </div>

    <div v-if="isEditing" class="edit-bar">
      <span>Редактирование</span>
      <button class="btn" @click="cancelEdit">✖</button>
    </div>

    <div v-if="selectedFiles.length" class="file-preview">
      <div v-for="(file, idx) in selectedFiles" :key="idx" class="file-preview__item">
        <span>{{ file.name }} ({{ formatSize(file.size) }})</span>
        <button class="btn" @click="removeFile(idx)">✖</button>
      </div>
    </div>

    <form class="chat-input" @submit.prevent="handleSend">
      <input ref="fileInput" hidden multiple type="file" @change="onFileSelect"/>
      <button class="btn" type="button" @click="!isEditing && $refs.fileInput.click()">🧷</button>
      <input v-model="newMessage" autocomplete="off" class="input" placeholder="Сообщение"/>
      <button class="btn" type="submit">💬</button>
    </form>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, reactive, ref} from 'vue';
import {useFileDownload} from '@/composables/useFileDownload';

const props = defineProps({
  messages: {type: Array, required: true},
  chatName: {type: String, default: ''},
  currentUser: {type: String, required: true},
  userCount: {type: Number, default: 0},
  activeChatId: {type: Number, required: true},
  chatSymmetricKey: {type: Object, required: true}
});

const emit = defineEmits(['send-message', 'delete-message', 'edit-message', 'leave-chat', 'open-chat-menu']);

const newMessage = ref('');
const isEditing = ref(false);

const handleEdit = () => {
  isEditing.value = true;
  newMessage.value = menu.message.content;
}
const selectedFiles = ref([]);
const fileInput = ref(null);

const {downloadAndDecrypt} = useFileDownload();

const downloadFile = async (fileKey, filename) => {
  try {
    const blob = await downloadAndDecrypt(fileKey, props.chatSymmetricKey);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } catch (err) {
    console.error('Failed to download file', err);
  }
};

const onFileSelect = (event) => {
  const files = Array.from(event.target.files);
  selectedFiles.value.push(...files.map(f => ({
    file: f,
    name: f.name,
    size: f.size,
    status: 'pending'
  })));
  fileInput.value = '';
};

const removeFile = (idx) => {
  selectedFiles.value.splice(idx, 1);
};

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1048576).toFixed(1) + ' MB';
};

const handleSend = () => {
  if (!newMessage.value.trim() && selectedFiles.value.length === 0) return;
  if (isEditing.value) {
    if (newMessage.value !== menu.message.content) emit('edit-message', menu.message.id, newMessage.value);
  } else {
    emit('send-message', newMessage.value, selectedFiles.value);
  }
  newMessage.value = '';
  isEditing.value = false;
  selectedFiles.value = [];
};

const cancelEdit = () => {
  newMessage.value = '';
  isEditing.value = false;
}

const handleDelete = () => {
  menu.visible = false
  emit('delete-message', menu.message.id)
}

const menu = reactive({
  visible: false,
  x: '0px',
  y: '0px',
  message: {}
});

const messagesContainer = ref(null);

const hideMenu = () => {
  if (menu.visible) {
    menu.visible = false;
  }
};

onMounted(() => {
  if (messagesContainer.value) {
    messagesContainer.value.addEventListener('scroll', hideMenu, {passive: true});
  }
});

onUnmounted(() => {
  if (messagesContainer.value) {
    messagesContainer.value.removeEventListener('scroll', hideMenu);
  }
});

const showMenu = (message, isMe, e) => {
  if (!isMe) return;
  const rect = e.currentTarget.getBoundingClientRect();
  menu.visible = true;
  menu.x = Math.ceil(rect.width) + 12 + 5 + 1 + 'px';
  menu.y = rect.top + rect.height / 2 + 'px';
  menu.message = message;
};

document.addEventListener("click", () => {
  menu.visible = false
})
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.chat-header__info {
  flex: 1;
  text-align: center;
  cursor: pointer;
}

.chat-header__name {
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
}

.chat-header__participants {
  font-size: var(--font-size-xs);
  opacity: 0.7;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-3);
}

.context-menu {
  position: fixed;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md) 0 var(--radius-md) var(--radius-md);
  padding: var(--space-1);
  display: flex;
  flex-direction: column;
  gap: 2px;
  z-index: 300;
}

.context-menu .btn {
  border: none;
  padding: var(--space-1) var(--space-3);
}

.edit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-2) var(--space-3);
  border-top: 1px solid var(--color-border);
}

.file-preview {
  border-top: 1px solid var(--color-border);
  padding: var(--space-2);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.chat-input {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2);
  border-top: 1px solid var(--color-border);
}

.chat-input .input {
  flex: 1;
}
</style>