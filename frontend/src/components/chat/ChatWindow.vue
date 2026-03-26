<template>
  <div id="chatChatting">
    <div id="chatHeader">
      <button id="chatHeaderLeave" @click="$emit('leave-chat')">🡨</button>
      <p id="chatHeaderName">{{ chatName }}</p>
    </div>

    <ul class="messages">
      <li v-for="message in messages"
          :key="message.id"
          :class="{ messageUs: message.sender === currentUser }"
          class="message">
        <div :class="{ isMe: message.sender === currentUser }" class="message-wrapper">
          <p v-if="message.sender !== currentUser" class="messageSender">{{ message.sender }}</p>
          <p class="messageContent">{{ message.content }}</p>
          <!-- Блок вложений -->
          <div v-if="message.fileKeys && message.fileKeys.length" class="message-attachments">
            <div v-for="file in message.fileKeys" :key="file.fileKey" class="attachment"
                 @click="downloadFile(file.fileKey, file.filename)">
              📎 {{ file.filename }}
            </div>
          </div>
          <p v-if="message.sender === currentUser" class="messageStatus">
            {{ message.receivers.length >= 2 ? '🤝' : (message.receivers.length === 1 ? '👋' : '🕚') }}</p>
        </div>
      </li>
    </ul>

    <div v-if="selectedFiles.length" id="file-attachments">
      <div v-for="(file, idx) in selectedFiles" :key="idx" class="file-item">
        <span>{{ file.name }} ({{ formatSize(file.size) }})</span>
        <button @click="removeFile(idx)">❌</button>
      </div>
    </div>

    <form id="chatInput" @submit.prevent="handleSend">
      <input id="chatInputInputer" v-model="newMessage" placeholder="Сообщение" required/>
      <input ref="fileInput" multiple style="display: none" type="file" @change="onFileSelect"/>
      <button class="attach-button" type="button" @click="$refs.fileInput.click()">📎</button>
      <button id="chatInputSend" type="submit">▶</button>
    </form>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useFileDownload} from '@/composables/useFileDownload';

const props = defineProps({
  messages: {type: Array, required: true},
  chatName: {type: String, default: ''},
  currentUser: {type: String, required: true},
  activeChatId: {type: Number, required: true},
  chatSymmetricKey: {type: Object, required: true}
});
const emit = defineEmits(['send-message', 'leave-chat']);

const newMessage = ref('');
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
  fileInput.value.value = '';
};

const removeFile = (idx) => {
  selectedFiles.value.splice(idx, 1);
};

const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1048576).toFixed(1) + ' MB';
};

const handleSend = async () => {
  if (!newMessage.value.trim() && selectedFiles.value.length === 0) return;
  emit('send-message', newMessage.value, selectedFiles.value);
  newMessage.value = '';
  selectedFiles.value = [];
};
</script>

<style scoped>
#chatChatting {
  margin-left: 25vw;
  width: 75vw;
  min-height: 100vh;
}

#chatHeader {
  margin: 0;
  height: 5vh;
  border-bottom: 1px solid white;
}

#chatHeaderLeave {
  padding: 0;
  border: 0;
  margin-left: 1vw;
  height: 5vh;
  width: 4vh;
  background-color: black;
  color: white;
  font-weight: 999;
  font-size: 4vh;
  font-family: "Arial";
  cursor: pointer;
}

#chatHeaderName {
  float: right;
  margin: 0;
  width: calc(74vw - 5vh);
  margin-top: 1vh;
  height: 4vh;
  color: white;
  font-weight: bold;
  font-size: 3vh;
  font-family: "Arial";
}

.messages {
  margin: 0;
  padding: 0 1vw;
  height: calc(90vh - 2px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.messages::-webkit-scrollbar {
  width: 0;
}

.message {
  list-style: none;
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  margin-bottom: 5px;
  width: 100%;
}

.messageUs {
  text-align: right;
}

.message-wrapper {
  display: inline-flex;
  flex-direction: column;
  max-width: 80%;
  min-width: min-content;
  background-color: black;
  padding: 3px;
  border-radius: 12px 12px 12px 0;
  border: 1px solid white;
}

.isMe {
  border-radius: 12px 12px 0 12px;
}

.messageSender {
  color: grey;
  padding-bottom: 2px;
}

.messageContent {
  color: white;
}

.messageStatus {
  text-align: left;
  margin: 0;
}

.messageSender, .messageContent {
  width: fit-content;
  max-width: 100%;
  margin: 0;
  box-sizing: border-box;
  word-break: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
}

#chatInput {
  margin: 0;
  height: 5vh;
  border-top: 1px solid white;
}

#chatInputInputer {
  background-color: black;
  color: white;
  height: 100%;
  padding: 0 0 0 1vw;
  border: 0;
  width: calc(100% - 5vh - 1px - 1vw);
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  vertical-align: bottom;
}

#chatInputInputer:focus {
  outline: none;
}

#chatInputSend {
  height: 4vh;
  width: 4vh;
  border: 0;
  padding: 0;
  margin-bottom: 0.5vh;
  margin-left: 1vh;
  margin-right: 1vw;
  border-radius: 100%;
  background-color: white;
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  color: black;
  cursor: pointer;
}

.message-attachments {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.attachment {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background-color 0.2s;
}

.attachment:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

#file-attachments {
  border-top: 1px solid #444;
  padding: 8px;
  background-color: #222;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-item {
  background-color: #333;
  border-radius: 4px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-item button {
  background: none;
  border: none;
  color: #ff6b6b;
  cursor: pointer;
}

.attach-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin: 0 8px;
}
</style>