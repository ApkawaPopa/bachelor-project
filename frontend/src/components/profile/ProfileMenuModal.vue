<script setup>
  import {ref} from 'vue';
  const props = defineProps({
    currentUser: {type: String, required: true},
    profileImage: {type: Array, required: true},
  });
  const emit = defineEmits(['loadProfilePicture', 'close', 'quitProfile']);

  const profileImg = ref(null);

  const onImageSelect = (event) => {
    const image = Array.from(event.target.files)[0];
    emit('loadProfilePicture', image);
  };
</script>

<template>
  <div id="profileMenu" @click="$emit('close')">
    <div id="profile" @click.stop>
      <input ref="profileImg" autocomplete="off" style="display: none" accept="image/*" type="file" @change="onImageSelect"/>
      <img v-if="profileImage.length > 0" :src="profileImage[profileImage.length - 1]" id="profilePicture">
      <p v-else id="profilePicture"/>
      <p id="profileName">{{ currentUser }}</p>
      <div id="profileButtons">
        <button id="profilePictureSelect" @click="$refs.profileImg.click()">Выбрать фото</button>
        <button id="profileQuit" @click="$emit('quitProfile')">Выйти</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
  #profileMenu {
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

  #profile {
    border: 1px solid white;
    width: calc(max(1vh, 1vw) * 30);
    height: calc(max(1vh, 1vw) * 45);
    background-color: black;
  }

  #profilePicture {
    margin:0;
    color:white;
    background-color: white;
    font-weight: bold;
    font-family: "Arial";
    margin-top: calc(max(1vh, 1vw) * 2.5);
    margin-left: calc(max(1vh, 1vw) * 8.75);
    height: calc(max(1vh, 1vw) * 12.5);
    width: calc(max(1vh, 1vw) * 12.5);
    border-radius: 100%;
    border: 1px solid white;
  }

  .imageDiv {
    width:calc(max(1vh, 1vw) * 29.5);
    height:calc(max(1vh, 1vw) * 29.5);
    float: inline-end;
  }

  #profileName {
    margin:0;
    height: calc(max(1vh, 1vw) * 3);
    font-size: calc(max(1vh, 1vw) * 2.5);
    font-weight: bold;
    font-family: "Arial";
    text-align: center;
    color: white;
  }

  #profileButtons {
    margin-top: calc(max(1vh, 1vw) * 18.75);
    width: calc(max(1vh, 1vw) * 30);
    height: calc(max(1vh, 1vw) * 7.5);
  }

  #profilePictureSelect {
    float:left;
    font-weight: bold;
    font-family: "Arial";
    text-align: center;
    margin-left: calc(max(1vh, 1vw) * 0.5);
    width: calc(max(1vh, 1vw) * 14.25);
    height: calc(max(1vh, 1vw) * 7.5);
    font-size: calc(max(1vh, 1vw) * 2);
    background-color: black;
    border: 1px solid white;
    color:white;
    padding:0;
  }

  #profileQuit {
    float:right;
    font-weight: bold;
    font-family: "Arial";
    text-align: center;
    margin-right: calc(max(1vh, 1vw) * 0.5);
    width: calc(max(1vh, 1vw) * 14.25);
    height: calc(max(1vh, 1vw) * 7.5);
    font-size: calc(max(1vh, 1vw) * 2);
    background-color: black;
    border: 1px solid red;
    color:red;
    padding:0;
  }
</style>