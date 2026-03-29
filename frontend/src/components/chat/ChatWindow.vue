<template>
  <div id="messageMenu" v-if="menu.visible" :style="{top:menu.y, left:menu.x, 'border-radius':menu.radius}">
    <button id="deleteMessage" @click.stop="handleDelete">Удалить</button>
    <button id="editMessage" @click.stop="handleEdit">Изменить</button>
  </div>
  <div id="chatChatting">
    <div id="chatHeader">
      <button id="chatHeaderLeave" @click.stop="$emit('leave-chat')">🡨</button>
      <div id="chatHeaderController" @click="$emit('open-chat-menu')">
        <p id="chatHeaderName">{{ chatName }}</p>
        <p id="chatHeaderUserCounter">{{ userCount }} участников</p>
      </div>
    </div>
    <ul class="messages" :style="{height:isEditing ? 'calc(95vh - 5.5vh - 2px - 5vh - 1px)':'calc(95vh - 5.5vh - 2px)'}">
      <li v-for="message in messages"
          :key="message.id"
          :class="{ messageUs: message.sender === currentUser }"
          class="message"
          @click.stop="showMenu(message, message.sender === currentUser)">
        <div :class="{ isMe: message.sender === currentUser }" class="message-wrapper">
          <p v-if="message.sender !== currentUser" class="messageSender">{{ message.sender }}</p>
          <div class="messageImages" v-if="message.images && message.images.length">
            <img class="messageImagesImage" v-for="image in message.images" :src="image.url">
          </div>
          <p class="messageContent">
            {{ message.content }}
          </p>
          <div v-if="message.fileKeys && message.fileKeys.length" class="message-attachments">
            <div v-for="file in message.fileKeys" :key="file.fileKey" class="attachment"
                 @click="downloadFile(file.fileKey, file.filename)">📄 {{ file.filename }}
            </div>
          </div>
          <div class="messageInfo">
            <p v-if="message.sender === currentUser" class="messageStatus">
              {{message.receivers.length >= 2 ? '🤝' : (message.receivers.length === 1 ? '👋' : '🕚')}}
            </p>
            <p class="messageTime">
              {{message.time}}
            </p>
          </div>
        </div>
      </li>
    </ul>

    <div v-if="isEditing" id="chatEdit">
      <p id="chatEditHeader">Редактирование</p>
      <button id="chatEditCancel" @click="cancelEdit">✖</button>
    </div>

    <div v-if="selectedFiles.length" id="file-attachments">
      <div v-for="(file, idx) in selectedFiles" :key="idx" class="file-item">
        <span>{{ file.name }}<br>{{ formatSize(file.size) }}</span>
        <button @click="removeFile(idx)">✖</button>
      </div>
    </div>

    <form id="chatInput" @submit.prevent="handleSend">
      <input ref="fileInput" multiple style="display: none" type="file" @change="onFileSelect" autocomplete="off"/>
      <input id="chatInputInputer" v-model="newMessage" placeholder="Сообщение" autocomplete="off"/>
      <button id="chatInputAttach" type="button" @click="!isEditing ? $refs.fileInput.click() : ''">🧷</button>
      <button id="chatInputSend" type="submit">💬</button>
    </form>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue';
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
  if(isEditing.value){
    if(newMessage.value !== menu.message.content)emit('edit-message', menu.message.id, newMessage.value);
  }else{
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
  x: "",
  y: "",
  style: "",
  message: {},
  radius: ""
})

const showMenu = (message, isMe) => {
  if(!isMe)return;
  const target = event.target.closest(".message")
  const position = target.children[0].getBoundingClientRect()
  menu.visible = true
  if(isMe){
    menu.x = "calc(" + String(position.x - 5 - 2) + "px - 10vw)"
    menu.radius = "12px 0 12px 12px"
  }else{
    menu.x = String(position.x + position.width + 5) + "px"
    menu.radius = "0 12px 12px 12px"
  }
  if(position.height > window.innerHeight){
    if(position.height - Math.abs(position.y) > window.innerHeight) menu.y = String(window.innerHeight / 2) + "px"
    else menu.y = String((position.height + position.y) / 2) + "px"
  }
  else menu.y = String(position.y + position.height / 2) + "px"
  menu.message = message
}

document.addEventListener("click", () => {
  menu.visible = false
})
</script>

<style scoped>
#messageMenu{
  position:absolute;
  border:1px solid white;
  width:10vw;
  background-color: black;
}

#deleteMessage, #editMessage{
  width:100%;
  height:5vh;
  border:0px;
  padding:0px;
  color:white;
  background-color:rgba(0,0,0,0);
  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
  cursor: pointer;
}

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

#chatHeaderController{
  float: right;
  width: calc(74vw - 5vh);
  height: 5vh;
}

#chatHeaderName {
  height: 2.5vh;
  margin: 0;
  margin-top: 0.25vh;
  color: white;
  font-weight: bold;
  font-size: 2.25vh;
  font-family: "Arial";
  text-align: center;
}

#chatHeaderUserCounter {
  height: 1.5vh;
  margin: 0;
  width: calc(74vw - 5vh);
  margin-top: 0.75vh;
  color: white;
  font-weight: bold;
  font-size: 1.25vh;
  font-family: "Arial";
  text-align: center;
}

.messages {
  margin: 0;
  padding: 0.5vh 1vw 0 1vw;
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

.messageInfo {
  display: inline-flex;
  flex-direction: row-reverse;
}

.isMe {
  border-radius: 12px 12px 0 12px;
}

.messageImages {
  max-width: 60vw;
}

.messageImagesImage{
  border-radius:6px;
  max-width: 60vw;
}

.messageSender {
  color: white;
  padding-bottom: 2px;
}

.messageContent {
  color: white;
}

.messageStatus {
  text-align: left;
  color:white;
  margin: 0;
}

.messageTime {
  text-align: right;
  color:white;
  margin:0;
}

.messageSender, .messageContent {
  width: fit-content;
  max-width: 100%;
  margin: 0;
  box-sizing: border-box;
  word-break: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  font-weight: bold;
  font-family: "Arial";
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
  width: calc(100% - 5vh - 1px - 1vw - 5vh);
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

#chatInputAttach {
  height: 4vh;
  width: 4vh;
  border: 0;
  padding: 0;
  margin-bottom: 0.5vh;
  margin-left: 1vh;
  //margin-right: 1vw;
  border-radius: 100%;
  background-color: white;
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  color: black;
  cursor: pointer;
}

#chatEdit {
  margin: 0;
  height: 5vh;
  border-top: 1px solid white;
}

#chatEditHeader{
  margin:0;
  background-color: black;
  color: white;
  height: 3.5vh;
  padding-top:1.5vh;
  padding-left: 1vw;
  border: 0;
  width: calc(100% - 5vh - 1px - 2vw);
  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
  vertical-align: center;
  float:left;
}

#chatEditCancel{
  height: 4vh;
  width: 4vh;
  border: 0;
  padding: 0;
  margin-top: 0.5vh;
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
  float:right;
}

.message-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.attachment {
  background-color: black;
  color:white;
  border:1px solid white;
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
  background-color: grey;
}

#file-attachments {
  border-top: 1px solid white;
  border-left: 1px solid white;
  padding: 8px;
  width: calc(75vw - 8px - 8px - 1px);
  background-color: black;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  position: absolute;
  transform: translateY(-100%);
}

.file-item {
  background-color: black;
  color:white;
  border:1px solid white;
  border-radius: 4px;
  padding: 4px 8px;
  display: flex;
  align-items: center;

  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
  gap:8px;
}

.file-item button {
  background: none;
  border: none;
  cursor: pointer;
  padding:0;
  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
  border:1px solid white;
  border-radius:100%;
  width:2em;
  height:2em;
  color:white;
}
</style>