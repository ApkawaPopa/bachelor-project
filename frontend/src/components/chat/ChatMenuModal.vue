<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <div class="chat-menu__header">
        <img v-if="chatImages.length" :src="chatImages[chatImages.length-1]" class="avatar avatar--profile"
             @click="$emit('openChatGallery')"/>
        <div v-else class="avatar avatar--placeholder avatar--profile"></div>
        <h3>{{ chatName }}</h3>
      </div>

      <div class="chat-menu__actions">
        <button class="btn" @click="$refs.profileImg.click()">Выбрать фото</button>
        <input ref="profileImg" accept="image/*" hidden type="file" @change="onImageSelect"/>
        <button class="btn btn--danger" @click="$emit('chat-delete')">Удалить чат</button>
      </div>

      <div class="chat-menu__users">
        <div v-for="user in users" :key="user.id" class="user-row" @click="$emit('openUserProfile', user.username)">
          <img v-if="user.url" :src="user.url" class="avatar"/>
          <div v-else class="avatar avatar--placeholder"></div>
          <span>{{ user.username }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits(['loadChatPicture', 'close', 'chat-delete', 'openChatGallery', 'openUserProfile']);

const props = defineProps({
  activeChatId: {type: Number, default: -1},
  chatName: {type: String, required: true},
  chatSymmetricKey: {type: Object, required: true},
  users: {type: Array, required: true},
  stringValues: {type: Array, required: true},
  imageValues: {type: Array, required: true},
  chatImages: {type: Array, default: () => []},
});

const onImageSelect = (event) => {
  const image = Array.from(event.target.files)[0];
  emit('loadChatPicture', image, props.chatSymmetricKey);
};
</script>

<style scoped>
.chat-menu__header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}

.chat-menu__actions {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
}

.chat-menu__users {
  overflow-y: auto;
  max-height: 200px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2);
}

.user-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  cursor: pointer;
}
</style>