<template>
  <div v-if="visible" class="carousel-overlay" @click="$emit('close')">
    <div class="carousel-container" @click.stop>
      <button :disabled="currentIndex === 0" class="carousel-btn left" @click="prev">❮</button>
      <img v-if="images.length" :src="images[currentIndex]" class="carousel-image"/>
      <button :disabled="currentIndex === images.length - 1" class="carousel-btn right" @click="next">❯</button>
      <div class="carousel-indicator">{{ currentIndex + 1 }} / {{ images.length }}</div>
    </div>
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';

const props = defineProps({
  images: {type: Array, required: true},
  initialIndex: {type: Number, default: 0},
  visible: {type: Boolean, default: false}
});

const emit = defineEmits(['close']);

const currentIndex = ref(props.initialIndex);

watch(() => props.visible, (newVal) => {
  if (newVal) currentIndex.value = props.initialIndex;
});

const prev = () => {
  if (currentIndex.value > 0) currentIndex.value--;
};
const next = () => {
  if (currentIndex.value < props.images.length - 1) currentIndex.value++;
};
</script>

<style scoped>
.carousel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.carousel-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
}

.carousel-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 3rem;
  padding: 0 20px;
  cursor: pointer;
  user-select: none;
}

.carousel-btn:disabled {
  opacity: 0.3;
  cursor: default;
}

.carousel-indicator {
  position: absolute;
  bottom: 10px;
  color: white;
  font-size: 1.2rem;
}
</style>