<template>
  <div v-if="visible" class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <input v-if="editable" ref="fileInput" accept="image/*" hidden type="file" @change="onFileSelected"/>
      <img v-if="profileImages.length" :src="profileImages[profileImages.length-1]" class="avatar avatar--profile"
           @click="openGallery"/>
      <div v-else class="avatar avatar--placeholder avatar--profile"></div>
      <h3>{{ username }}</h3>
      <div v-if="editable" class="profile-actions">
        <button class="btn" @click="$refs.fileInput.click()">Выбрать фото</button>
        <button class="btn btn--danger" @click="$emit('quitProfile')">Выйти</button>
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
}, {immediate: true});
</script>

<style scoped>
.profile-actions {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  margin-top: var(--space-4);
}

h3 {
  text-align: center;
}
</style>