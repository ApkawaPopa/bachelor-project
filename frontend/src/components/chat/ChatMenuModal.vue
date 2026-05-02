<template>
  <div id="ChatMenu" @click="$emit('close')">
    <div id="ChatMenuBody" @click.stop>
      <div id="ChatMenuHeader">
        <p id="ChatAvatar"/>
        <p id="ChatName">{{ chatName }}</p>
      </div>
      <button id="ChatMenuDelete" @click="$emit('chat-delete')">Удалить чат?</button>
      <div id="ChatMenuSafe">
        <div id="ChatMenuSafeImage">
          <div v-for="image in imageValues" class="ChatMenuSafeImageRow">
            <p v-for="pixel in image"
               :style="{'background-color':'rgba(' + pixel[0].toString() + ',' + pixel[1].toString() + ',' + pixel[2].toString() + ',' + pixel[3].toString() + ')'}"
               class="ChatMenuSafeImagePixel">
              0</p>
          </div>
        </div>
        <div id="ChatMenuSafeStrings">
          <div v-for="string in stringValues" class="ChatMenuSafeString">
            <p v-for="item in string" class="ChatMenuSafeItem">{{ item }}</p>
          </div>
        </div>
        <p id="ChatMenuSafeText">Это изображение и текст созданы на основе ключей шифрования. Если они совпадают у всех
          участников, чат полностью приватен.</p>
      </div>
      <div id="ChatMenuUsers">
        <div v-for="user in users" :key="user.id" class="ChatMenuUser">
          <img :src="user.url" class="ChatMenuUserAvatar">
          <p class="ChatMenuUserName">
            {{ user.username }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  activeChatId: {type: Number, default: -1},
  chatName: {type: String, required: true},
  users: {type: Array, required: true},
  stringValues: {type: Array, required: true},
  imageValues: {type: Array, required: true},
});
</script>

<style scoped>
@media (orientation: portrait) {
  #ChatMenu {
    width: 100vw;
    height: 100%;
    left: 0;
    top: 0;
    background-color: rgba(50, 50, 50, 50%);
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  #ChatMenuBody {
    border: 1px solid white;
    width: 80vw;
    height: 80vh;
    background-color: black;
  }

  #ChatMenuHeader {
    padding-top: 1vh;
    item-direction: column;
    border-bottom: 1px solid white;
  }

  #ChatAvatar {
    margin: 0;
    height: 10vh;
    width: 10vh;
    margin-left: calc(50% - 5vh);
    background-color: white;
    border-radius: 100%;
  }

  #ChatName {
    padding-top: 1vh;
    color: white;
    margin: 0;
    font-weight: bold;
    height: 2.5vh;
    font-size: 2vh;
    font-family: "Arial";
    text-align: center;
  }

  #ChatMenuDelete {
    width: 30vw;
    border: 1px solid white;
    border-top: 0px;
    border-radius: 0 0 12px 12px;
    background-color: black;
    font-weight: bold;
    height: 2.5vh;
    font-size: 2vh;
    font-family: "Arial";
    color: white;
    padding: 0;
    margin: 0;
    margin-left: calc(50% - 15vw);
  }

  #ChatMenuSafe {
    margin-top: 1vh;
    margin-left: calc(50% - 35vw);
    width: 70vw;
    height: 37.5vh;
  }

  #ChatMenuSafeStrings {
    height: 11.25vh;
  }

  .ChatMenuSafeString {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-rows: 1fr;
  }

  .ChatMenuSafeItem {
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 2vh;
    font-family: "Arial";
    text-align: center;
  }

  #ChatMenuSafeImage {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-rows: 1fr;
    background-color: white;
    width: 15vh;
    height: 15vh;
    margin-left: calc(50% - 7.5vh);
  }

  #ChatMenuSafeText {
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 1.5vh;
    height: 11.25vh;
    overflow-y: auto;
    font-family: "Arial";
    text-align: center;
  }

  .ChatMenuSafeImageRow {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-rows: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-columns: 1fr;
  }

  .ChatMenuSafeImagePixel {
    margin: 0;
    color: rgba(255, 255, 255, 0);
    font-size: 0;
  }

  #ChatMenuUsers {
    border: 1px solid white;
    border-radius: 12px 12px 0 0;
    padding: 0 2%;
    border-bottom: 0;
    width: 86%;
    height: calc(23.5vh - 1px);
    margin: 1vh 5% 0 5%;
    overflow-y: auto;
  }

  #ChatMenuUsers::-webkit-scrollbar {
    width: 0;
  }

  .ChatMenuUser {
    height: 4vh;
    padding: 4px 2px;
    border-bottom: 1px solid white;
  }

  .ChatMenuUserAvatar {
    float: left;
    margin: 0;
    width: 4vh;
    height: 4vh;
    background-color: white;
    border-radius: 100%;
  }

  .ChatMenuUserName {
    float: right;
    padding-top: 1vh;
    width: calc(100% - 4vh - 1px);
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 2vh;
    font-family: "Arial";
  }
}
</style>

<style scoped>
@media (orientation: landscape) {
  #ChatMenu {
    width: 100vw;
    height: 100vh;
    left: 0;
    top: 0;
    background-color: rgba(50, 50, 50, 50%);
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  #ChatMenuBody {
    border: 1px solid white;
    width: 40vw;
    height: 90vh;
    background-color: black;
  }

  #ChatMenuUsers {
    border: 1px solid white;
    width: 90%;
    height: calc(24.5vh - 2px - 2vh);
    margin: 1vh 5%;
    overflow-y: auto;
  }

  #ChatMenuUsers::-webkit-scrollbar {
    width: 0;
  }

  .ChatMenuUser {
    height: 4vh;
    padding: 4px 2px;
    border-bottom: 1px solid white;
  }

  .ChatMenuUserAvatar {
    float: left;
    margin: 0;
    width: 4vh;
    height: 4vh;
    background-color: white;
    border-radius: 100%;
  }

  .ChatMenuUserName {
    float: right;
    padding-top: 1vh;
    width: calc(100% - 4vh - 1px);
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 2vh;
    font-family: "Arial";
  }

  #ChatMenuHeader {
    padding-top: 1vh;
    item-direction: column;
  }

  #ChatAvatar {
    margin: 0;
    height: 10vh;
    width: 10vh;
    margin-left: calc(50% - 5vh);
    background-color: white;
    border-radius: 100%;
  }

  #ChatName {
    padding-top: 1vh;
    color: white;
    margin: 0;
    font-weight: bold;
    height: 2.5vh;
    font-size: 2vh;
    font-family: "Arial";
    text-align: center;
  }

  #ChatMenuDelete {
    width: 30vw;
    border: 1px solid white;
    background-color: black;
    font-weight: bold;
    height: 2.5vh;
    font-size: 2vh;
    font-family: "Arial";
    color: white;
    padding: 0;
    margin: 0;
    margin-left: calc(max(1vh, 1vw) * 5 - 5px);
  }

  #ChatMenuSafe {
    margin-top: 1vh;
    margin-left: calc(max(1vh, 1vw) * 5 - 5px);
    padding: 5px;
    width: 30vw;
    height: calc(47.5vh - 12px);
  }

  .ChatMenuSafeString {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-rows: 1fr;
  }

  .ChatMenuSafeItem {
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 2vh;
    font-family: "Arial";
    text-align: center;
  }

  #ChatMenuSafeImage {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-rows: 1fr;
    background-color: white;
    width: 15vh;
    height: 15vh;
    margin-left: calc(15vw - 7.5vh);
  }

  #ChatMenuSafeStrings {
    height: calc(32.5vh - 12px - 15vh);
  }

  #ChatMenuSafeText {
    color: white;
    margin: 0;
    font-weight: bold;
    font-size: 2vh;
    height: 15vh;
    overflow-y: auto;
    font-family: "Arial";
    text-align: center;
  }

  .ChatMenuSafeImageRow {
    display: grid;
    grid-auto-columns: 1fr;
    grid-template-rows: 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr;
    grid-template-columns: 1fr;
  }

  .ChatMenuSafeImagePixel {
    margin: 0;
    color: rgba(255, 255, 255, 0);
    font-size: 0;
  }
}
</style>