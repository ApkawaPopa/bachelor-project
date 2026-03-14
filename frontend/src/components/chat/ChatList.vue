<template>
  <div id="chatSelector">
    <div
        v-for="chat in chats"
        :key="chat.id"
        :class="{ selected: activeChatId === chat.id }"
        class="chat"
        @click="selectChat(chat.id)"
    >
      <p class="chatAvatar"/>
      <p class="chatName">{{ chat.name }}</p>
      <p class="chatTimer">{{ chat.time }}</p>
      <p v-if="chat.unreadMessages>0" class="chatMessagesCount">{{ chat.unreadMessages }}</p>
      <p class="chatLastMessage">{{ chat.lastMessage }}</p>
    </div>
    <button id="addChat" @click="$emit('open-add-chat')">+</button>
  </div>
</template>

<script setup>
defineProps({
  chats: {type: Array, required: true},
  activeChatId: {type: Number, default: -1},
});
const emit = defineEmits(['select-chat', 'open-add-chat']);

const selectChat = (chatId) => {
  emit('select-chat', chatId);
};
</script>

<style scoped>
#chatSelector {
  float: left;
  background-color: black;
  border-right: 1px solid white;
  width: calc(max(1vh, 1vw) * 25);
  height: 100vh;
  overflow: auto;
}

#chatSelector::-webkit-scrollbar {
  width: 0;
}

.chat {
  width: 25vw;
  height: 6vh;
  background-color: black;
  border-bottom: 1px solid white;
  cursor: pointer;
}

.chat .chatAvatar {
  float: left;
  width: 5vh;
  height: 5vh;
  margin: 0.5vh 0 0 0.5vh;
  background-color: white;
  border-radius: 100%;
}

.chat .chatName {
  overflow: hidden;
  float: left;
  margin: 0;
  width: calc(25vw - 12vh - 3px);
  color: white;
  font-weight: bold;
  font-size: 1.75vh;
  height: 2vh;
  padding: 0.5vh 0;
  padding-left: 0.5vh;
  font-family: "Arial";
}

.chat .chatLastMessage {
  overflow: hidden;
  float: left;
  margin: 0;
  width: calc(25vw - 12vh - 3px);
  color: white;
  font-weight: bold;
  font-size: 1.75vh;
  height: 2vh;
  padding: 0.5vh 0;
  padding-left: 0.5vh;
  font-family: "Arial";
}
.chat .chatMessagesCount{
  overflow: hidden;
  float: right;
  text-align: center;
  margin: 0;
  color: black;
  font-weight: bold;
  font-size: 2vh;
  height: 1.5vh;
  width: 3.25vh;
  padding-top:0.5vh;
  padding-bottom:1.25vh;
  margin-right:0.25vh;
  margin-top:0.25vh;
  font-family: "Arial";
  background-color: white;
  border-radius: 100%;
}
.chat .chatTimer{
  overflow: hidden;
  float: right;
  text-align: center;
  margin: 0;
  color: white;
  font-weight: bold;
  font-size: 1.5vh;
  height: 2vh;
  width: 6vh;
  margin-right:0.25vh;
  margin-top:0.25vh;
  font-family: "Arial";
}

.chat.selected .chatAvatar {
  background-color: black;
}

.chat.selected .chatName{
  color: black;
}

.chat.selected .chatLastMessage{
  color: black;
}

.chat.selected .chatTimer{
  color: black;
}

.chat.selected {
  background-color: white;
}

#addChat {
  position: fixed;
  top: calc(100% - calc(max(1vh, 1vw) * 5.5));
  left: calc(max(1vh, 1vw) * 19.5);
  width: calc(max(1vh, 1vw) * 5);
  height: calc(max(1vh, 1vw) * 5);
  border-radius: 100%;
  border: 0;
  background-color: white;
  color: black;
  font-size: calc(max(1vh, 1vw) * 4);
  padding: 0;
  text-align: center;
  cursor: pointer;
}
</style>