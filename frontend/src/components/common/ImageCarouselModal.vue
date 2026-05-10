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