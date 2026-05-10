<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <input v-model="groupName" class="input" placeholder="Название группы" required/>
      <div class="add-user">
        <input v-model="newParticipant" class="input" placeholder="Имя пользователя"/>
        <button class="btn" @click="addParticipant">Добавить</button>
      </div>
      <div :class="{ 'has-users': participants.length > 0 }" class="participants-wrapper">
        <div class="participants">
          <div v-for="user in participants" :key="user.username" class="participant-row">
            <span>{{ user.username }}</span>
            <button class="btn" @click="removeParticipant(user)">✖</button>
          </div>
        </div>
      </div>
      <button v-if="participants.length > 0" class="btn btn--primary" @click="createGroupChat">Создать</button>
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
.add-user {
  display: flex;
  gap: var(--space-2);
}

.participants-wrapper {
  border: none;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.participants-wrapper.has-users {
  border: 1px solid var(--color-border);
}

.participants {
  max-height: 200px;
  overflow-y: auto;
  padding: var(--space-2);
}

.participant-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-1) 0;
}
</style>