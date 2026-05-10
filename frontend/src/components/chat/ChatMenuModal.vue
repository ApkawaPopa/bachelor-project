<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <!-- Заголовок с аватаром -->
      <div class="chat-menu__header">
        <img v-if="chatImages.length" :src="chatImages[chatImages.length-1]" class="avatar avatar--profile"
             @click="$emit('openChatGallery')"/>
        <div v-else class="avatar avatar--placeholder avatar--profile"></div>
        <h3>{{ chatName }}</h3>
      </div>

      <!-- Кнопки действий -->
      <div class="chat-menu__actions">
        <button class="btn" @click="$refs.profileImg.click()">Выбрать фото</button>
        <input ref="profileImg" accept="image/*" hidden type="file" @change="onImageSelect"/>
        <button class="btn btn--danger" @click="$emit('chat-delete')">Удалить чат</button>
      </div>

      <!-- Safety-секция: возвращённая картинка, строки и подпись -->
      <div class="chat-menu__safety">
        <!-- Пиксельная картинка -->
        <div class="safety-image">
          <div v-for="(imageRow, rowIdx) in imageValues" :key="'img-row-'+rowIdx" class="safety-image__row">
            <div v-for="(pixel, pixIdx) in imageRow" :key="'pix-'+rowIdx+'-'+pixIdx"
                 :style="{ backgroundColor: `rgba(${pixel[0]},${pixel[1]},${pixel[2]},${pixel[3]})` }"
                 class="safety-image__pixel">
            </div>
          </div>
        </div>

        <!-- Строки -->
        <div class="safety-strings">
          <div v-for="(stringRow, sIdx) in stringValues" :key="'str-row-'+sIdx" class="safety-strings__row">
            <span v-for="(item, iIdx) in stringRow" :key="'str-'+sIdx+'-'+iIdx" class="safety-strings__item">
              {{ item }}
            </span>
          </div>
        </div>

        <!-- Пояснительный текст -->
        <p class="safety-text">
          Это изображение и текст созданы на основе ключей шифрования. Если они совпадают у всех участников, чат
          полностью приватен.
        </p>
      </div>

      <!-- Список участников -->
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

/* Safety section */
.chat-menu__safety {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
}

.safety-image {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(12, 1fr);
  width: 180px;
  height: 180px;
  background: #fff; /* белый фон для наглядности */
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  margin: 0 auto;
}

.safety-image__row {
  display: contents;
}

.safety-image__pixel {
  width: 100%;
  height: 100%;
}

.safety-strings {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  width: 100%;
}

.safety-strings__row {
  display: flex;
  justify-content: center;
  gap: var(--space-1);
}

.safety-strings__item {
  font-family: var(--font-family);
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-sm);
  color: var(--color-text);
  min-width: 2ch;
  text-align: center;
}

.safety-text {
  font-size: var(--font-size-xs);
  text-align: center;
  opacity: 0.7;
  max-width: 300px;
  line-height: 1.4;
}

/* Users list */
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