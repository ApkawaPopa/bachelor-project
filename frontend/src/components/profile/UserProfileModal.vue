<template>
  <div v-if="visible" class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <img v-if="profileImages.length" :src="profileImages[profileImages.length - 1]"
           class="avatar" @click="showCarousel = true"/>
      <p v-else class="avatar-placeholder"></p>
      <p class="username">{{ username }}</p>
    </div>
    <ImageCarouselModal :images="profileImages" :initialIndex="profileImages.length - 1"
                        :visible="showCarousel" @close="showCarousel = false"/>
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';
import {useApi} from '@/composables/useApi';
import {useFileDownload} from '@/composables/useFileDownload';
import ImageCarouselModal from '@/components/common/ImageCarouselModal.vue';

const props = defineProps({
  username: {type: String, required: true},
  visible: {type: Boolean, default: false}
});
const emit = defineEmits(['close']);

const {get} = useApi();
const {download} = useFileDownload();
const profileImages = ref([]);
const showCarousel = ref(false);

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
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 500;
}

.modal-content {
  background: black;
  border: 1px solid white;
  padding: 20px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: white;
  cursor: pointer;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: white;
  display: inline-block;
}

.username {
  color: white;
  font-weight: bold;
  margin-top: 10px;
}
</style>