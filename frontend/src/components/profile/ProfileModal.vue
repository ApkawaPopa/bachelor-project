<!-- frontend/src/components/profile/ProfileModal.vue -->
<template>
  <div v-if="visible" class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <input
          v-if="editable"
          ref="fileInput"
          accept="image/*"
          autocomplete="off"
          style="display: none"
          type="file"
          @change="onFileSelected"
      />
      <img
          v-if="profileImages.length"
          :src="profileImages[profileImages.length - 1]"
          class="avatar"
          @click="openGallery"
      />
      <p v-else class="avatar-placeholder" @click="openGallery"></p>
      <p class="username">{{ username }}</p>
      <div v-if="editable" class="buttons">
        <button class="button primary" @click="$refs.fileInput.click()">Выбрать фото</button>
        <button class="button danger" @click="$emit('quitProfile')">Выйти</button>
      </div>
    </div>
    <ImageCarouselModal
        :images="profileImages"
        :initialIndex="profileImages.length - 1"
        :visible="showCarousel"
        @close="showCarousel = false"
    />
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';
import {useApi} from '@/composables/useApi';
import {useFileDownload} from '@/composables/useFileDownload';
import ImageCarouselModal from '@/components/common/ImageCarouselModal.vue';

const props = defineProps({
  username: {type: String, required: true},
  visible: {type: Boolean, default: false},
  editable: {type: Boolean, default: false},
  // колбэк для загрузки нового фото (только при editable)
  onUploadPicture: {type: Function, default: null}
});

const emit = defineEmits(['close', 'quitProfile']);

const {get} = useApi();
const {download} = useFileDownload();
const profileImages = ref([]);
const showCarousel = ref(false);
const fileInput = ref(null);

const loadProfilePictures = async () => {
  try {
    const resp = await get(`/api/v1/user/${props.username}/pictures`);
    const images = [];
    for (const url of resp.data) {
      const blobUrl = await download(url);
      images.push(blobUrl);
    }
    profileImages.value = images;
  } catch (e) {
    console.error('Failed to load profile pictures', e);
    profileImages.value = [];
  }
};

const onFileSelected = async (event) => {
  const file = event.target.files[0];
  if (!file || !props.onUploadPicture) return;
  try {
    await props.onUploadPicture(file);
    // После загрузки перезапрашиваем все фото
    await loadProfilePictures();
  } catch (err) {
    console.error('Upload failed', err);
  }
  // сброс input
  if (fileInput.value) fileInput.value.value = '';
};

const openGallery = () => {
  if (profileImages.value.length) showCarousel.value = true;
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    showCarousel.value = false;
    loadProfilePictures();
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(50, 50, 50, 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 500;
}

.modal-content {
  border: 1px solid white;
  width: calc(max(1vh, 1vw) * 30);
  height: calc(max(1vh, 1vw) * 45);
  background-color: black;
  text-align: center;
}

.avatar {
  display: block;
  margin: calc(max(1vh, 1vw) * 2.5) auto 0;
  background-color: white;
  height: calc(max(1vh, 1vw) * 12.5);
  width: calc(max(1vh, 1vw) * 12.5);
  border-radius: 100%;
  border: 1px solid white;
  cursor: pointer;
  font-weight: bold;
  font-family: "Arial";
}

.avatar-placeholder {
  display: block;
  margin: calc(max(1vh, 1vw) * 2.5) auto 0;
  background-color: white;
  height: calc(max(1vh, 1vw) * 12.5);
  width: calc(max(1vh, 1vw) * 12.5);
  border-radius: 100%;
  border: 1px solid white;
  cursor: pointer;
}

.username {
  margin: 0;
  height: calc(max(1vh, 1vw) * 3);
  font-size: calc(max(1vh, 1vw) * 2.5);
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  color: white;
}

.buttons {
  margin-top: calc(max(1vh, 1vw) * 18.75);
  width: calc(max(1vh, 1vw) * 30);
  height: calc(max(1vh, 1vw) * 7.5);
}

.button {
  font-weight: bold;
  font-family: "Arial";
  font-size: calc(max(1vh, 1vw) * 2);
  padding: 0;
  height: 100%;
  width: calc(max(1vh, 1vw) * 14.25);
  border: 1px solid white;
  background-color: black;
  cursor: pointer;
}

.primary {
  float: left;
  margin-left: calc(max(1vh, 1vw) * 0.5);
  color: white;
}

.danger {
  float: right;
  margin-right: calc(max(1vh, 1vw) * 0.5);
  border-color: red;
  color: red;
}
</style>