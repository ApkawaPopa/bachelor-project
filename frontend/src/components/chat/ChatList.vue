<template>
  <div class="chat-list-wrapper">
    <button class="btn profile-btn" @click="$emit('open-profile')">Профиль</button>
    <div class="chat-list">
      <div
          v-for="chat in chats" :key="chat.id"
          :class="{ active: activeChatId === chat.id }" class="chat-item"
          @click="$emit('select-chat', chat.id)"
      >
        <img v-if="chat.image" :src="chat.image" class="avatar"/>
        <div v-else class="avatar avatar--placeholder"></div>
        <div class="chat-item__info">
          <div class="chat-item__header">
            <span class="chat-item__name">{{ chat.name }}</span>
            <span class="chat-item__time">{{ chat.time }}</span>
          </div>
          <div class="chat-item__footer">
            <span class="chat-item__message">{{ chat.lastMessage }}</span>
            <span v-if="chat.unreadMessages>0" class="chat-item__unread">{{ chat.unreadMessages }}</span>
          </div>
        </div>
      </div>
      <button class="btn add-chat-btn" @click="$emit('open-add-chat')">+</button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  chats: {type: Array, required: true},
  activeChatId: {type: Number, default: -1},
});
const emit = defineEmits(['select-chat', 'open-add-chat', 'open-profile']);

const selectChat = (chatId) => {
  emit('select-chat', chatId);
};
</script>

<style scoped>
.profile-btn {
  border-bottom: 1px solid var(--color-border);
  padding: var(--space-3);
  width: 100%;
  flex-shrink: 0;
}

.chat-list-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item__info {
  flex: 1;
  margin-left: var(--space-3);
  min-width: 0;
}

.chat-item__header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: var(--font-size-sm);
}

.chat-item__name {
  font-weight: var(--font-weight-bold);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item__time {
  font-size: var(--font-size-xs);
  opacity: 0.7;
  margin-left: var(--space-2);
}

.chat-item__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-1);
}

.chat-item__message {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: var(--font-size-xs);
  opacity: 0.8;
}

.chat-item__unread {
  background: var(--color-text);
  color: var(--color-text-inverse);
  border-radius: var(--radius-full);
  padding: 0 var(--space-1);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  min-width: 1.5em;
  text-align: center;
}

.add-chat-btn {
  position: absolute;
  bottom: var(--space-4);
  right: var(--space-4);
  width: 3rem;
  height: 3rem;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xl);
  padding: 0;
  z-index: 50;
}
</style>