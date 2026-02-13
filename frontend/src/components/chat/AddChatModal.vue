<template>
  <div id="addChatMenu" @click="$emit('close')">
    <div id="addChatSettings" :class="{ selected: isGroup, unselected: !isGroup }" @click.stop>
      <div id="addChatTopHeader">
        <button
            id="selectUser"
            :class="{ active: !isGroup }"
            class="topHeaderButton"
            @click="isGroup = false"
        >Пользователь
        </button>
        <button
            id="selectGroup"
            :class="{ active: isGroup }"
            class="topHeaderButton"
            @click="isGroup = true"
        >Группа
        </button>
      </div>

      <form v-if="!isGroup" id="addChatForm" @submit.prevent="createSingleChat">
        <input v-model="singleChatName" placeholder="Название чата" required/>
        <input v-model="singleUsername" placeholder="Имя пользователя" required/>
        <button class="inBut" type="submit">Создать чат</button>
      </form>

      <form v-else id="addChatForm" @submit.prevent="createGroupChat">
        <input v-model="groupName" placeholder="Название группы" required/>
        <div id="addUserToGroup">
          <p class="defText">Добавить пользователя:</p>
          <div id="addUserToGroupDiv">
            <input v-model="newParticipant" placeholder="Имя пользователя"/>
            <button type="button" @click="addParticipant">Добавить</button>
          </div>
          <p class="defText">Список пользователей:</p>
        </div>
        <div v-for="user in participants" :key="user.username" class="addUserToGroupBottom">
          <p>{{ user.username }}</p>
          <button type="button" @click="removeParticipant(user)">X</button>
        </div>
        <button :disabled="participants.length === 0" class="inBut" type="submit">Создать группу</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';

const props = defineProps({
  currentUser: {type: String, required: true},
});
const emit = defineEmits(['close', 'create']);

const isGroup = ref(false);

// Личный чат
const singleChatName = ref('');
const singleUsername = ref('');

// Группа
const groupName = ref('');
const participants = ref([]);
const newParticipant = ref('');

const addParticipant = () => {
  const name = newParticipant.value.trim();
  if (name && !participants.value.some(p => p.username === name)) {
    participants.value.push({username: name});
    newParticipant.value = '';
  }
};

const removeParticipant = (user) => {
  participants.value = participants.value.filter(p => p.username !== user.username);
};

const createSingleChat = () => {
  if (!singleChatName.value || !singleUsername.value) return;
  emit('create', {
    name: singleChatName.value,
    participants: [
      {username: props.currentUser},
      {username: singleUsername.value.trim()}
    ]
  });
};

const createGroupChat = () => {
  if (!groupName.value || participants.value.length === 0) return;
  emit('create', {
    name: groupName.value,
    participants: [
      {username: props.currentUser},
      ...participants.value
    ]
  });
};
</script>

<style scoped>
#addChatMenu {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(50, 50, 50, 50%);
  display: flex;
  align-items: center;
  justify-content: center;
}

#addChatSettings {
  border: 1px solid white;
  width: calc(max(1vh, 1vw) * 30);
  min-height: calc(max(1vh, 1vw) * 45);
  background-color: black;
}

#addChatTopHeader {
  width: 100%;
  height: calc(max(1vh, 1vw) * 6);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
}

.topHeaderButton {
  text-align: center;
  border: 0;
  padding: 0;
  font-weight: bold;
  font-family: "Arial";
  font-size: 100%;
  cursor: pointer;
}

#selectGroup.active {
  background-color: white;
  color: black;
}

#selectGroup:not(.active) {
  background-color: black;
  color: white;
}

#selectUser.active {
  background-color: white;
  color: black;
}

#selectUser:not(.active) {
  background-color: black;
  color: white;
}

#addChatForm {
  margin: 0;
  width: calc(100% - 2 * 1vh);
  padding: 1vh;
  display: grid;
  grid-template-columns: 1fr;
  grid-row-gap: 1vh;
}

#addChatForm input {
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  padding: 0.5vh;
}

#addChatSettings.selected input {
  background-color: white;
  border: 1px solid black;
  color: black;
}

#addChatSettings.unselected input {
  background-color: black;
  border: 1px solid white;
  color: white;
}

#addChatSettings.selected .inBut {
  background-color: black;
  color: white;
  border: 0;
  padding: 0.5vh;
}

#addChatSettings.unselected .inBut {
  background-color: white;
  color: black;
  border: 0;
  padding: 0.5vh;
}

.inBut {
  font-weight: bold;
  font-family: "Arial";
  cursor: pointer;
}

#addUserToGroup {
  display: grid;
  grid-template-columns: 1fr;
}

#addUserToGroupDiv {
  display: grid;
  grid-template-columns: 3fr 1fr;
}

#addUserToGroupDiv button {
  padding: 0.5vh;
  border: 0;
  color: white;
  background-color: black;
  cursor: pointer;
}

.addUserToGroupBottom {
  border: 1px solid black;
  display: grid;
  grid-template-columns: 4fr 1fr;
}

.addUserToGroupBottom p {
  height: 100%;
  font-size: 100%;
  margin: 0;
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  color: black;
  background: white;
}

.addUserToGroupBottom button {
  border: 0px;
  padding: 0px;
  background-color: black;
  color: white;
  font-weight: bold;
  font-family: "Arial";
  cursor: pointer;
}

.defText {
  margin: 0;
  text-align: center;
  font-weight: bold;
  font-family: "Arial";
  color: white;
}
</style>