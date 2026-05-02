<template>
  <div id="addChatMenu" @click="$emit('close')">
    <div id="addChatSettings" @click.stop>
      <form id="addChatForm" @submit.prevent="createGroupChat">
        <input id="groupName" v-model="groupName" placeholder="Название группы" required/>
        <p class="defText">Добавить пользователя:</p>
        <div id="addUserToGroupDiv">
          <input id="addUserInput" v-model="newParticipant" placeholder="Имя пользователя"/>
          <button type="button" @click="addParticipant">Добавить</button>
        </div>
        <p class="defText">Список пользователей:</p>
        <div id="users">
          <div v-for="user in participants" :key="user.username" class="addUserToGroupBottom">
            <button @click="removeParticipant(user)" class="addUsername">{{ user.username }}</button>
            <button @click="removeParticipant(user)">X</button>
          </div>
        </div>
        <button v-if="participants.length > 0" class="inBut" type="submit">Создать группу</button>
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
  height: calc(max(1vh, 1vw) * 45);
  background-color: black;
}

#addChatForm {
  margin: 0;
  height: calc(max(1vh, 1vw) * 45 - 2% - 2px);
  width: calc(100% - 2 * 2%);
  padding: 2%;
}

#groupName {
  height: 10%;
  width: 100%;
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  padding: 0;
  background-color: black;
  border: 1px solid white;
  color: white;
}

.defText {
  padding-top:5%;
  height:5%;
  margin: 0;
  text-align: center;
  vertical-align:middle;
  font-weight: bold;
  font-family: "Arial";
  color: white;
}

#addUserToGroupDiv {
  height:10%;
  display: grid;
  grid-template-columns: 3fr 1fr;
}

.inBut {
  font-weight: bold;
  font-family: "Arial";
  cursor: pointer;
}

#addUserInput {
  height: 100%;
  width: 100%;
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  padding: 0;
  background-color: black;
  border: 1px solid white;
  color: white;
}

#addUserToGroupDiv button {
  padding: 0.5vh;
  font-weight: bold;
  font-family: "Arial";
  border: 1px solid white;
  border-left:0px;
  color: white;
  background-color: black;
  cursor: pointer;
}

.addUserToGroupBottom {
  height: 15%;
  border: 1px solid black;
  display: grid;
  grid-template-columns: 4fr 1fr;
}

.addUserToGroupBottom button {
  padding: 0px;
  background-color: black;
  color: white;
  font-weight: bold;
  font-family: "Arial";
  border:1px solid white;
  border-left: 0px;
  cursor: pointer;
}


.addUserToGroupBottom .addUsername {
  border-left: 1px solid white;
  cursor: default;
}

#users {
  margin-bottom: 5%;
  height: 52%;
  overflow-y:auto;
}

#addChatSettings .inBut {
  background-color: black;
  color: white;
  border: 1px solid white;
  padding: 0.5vh;
  height:8%;
  width:100%;
}

#users::-webkit-scrollbar {
  background: black;
  border:1px solid white;
}

#users::-webkit-scrollbar-thumb {
  background: white;
}
</style>
