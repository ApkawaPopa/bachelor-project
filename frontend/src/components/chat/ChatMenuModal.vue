<template>
  <div id="ChatMenu" @click="$emit('close')">
    <div id="ChatMenuBody" @click.stop>
      <div id="ChatMenuHeader">
        <p id="ChatAvatar"/>
        <p id="ChatName">{{chatName}}</p>
      </div>
      <div id="ChatMenuUsers">
        <div class="ChatMenuUser" v-for="user in users" :key="user.id">
          <p class="ChatMenuUserAvatar"/>
          <p class="ChatMenuUserName">
            {{user.username}}
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
});
</script>

<style scoped>
#ChatMenu{
  width:100vw;
  height:100vh;
  left:0;
  top:0;
  background-color: rgba(50, 50, 50, 50%);
  position:fixed;
  display: flex;
  align-items: center;
  justify-content: center;
}

#ChatMenuBody {
  border: 1px solid white;
  width: calc(max(1vh, 1vw) * 30);
  min-height: calc(max(1vh, 1vw) * 45);
  max-height: 80vh;
  background-color: black;
  display: grid;
  grid-template-columns: 1fr;
  grid-row-gap: 1vh;
}

#ChatMenuUsers{
  border:1px solid white;
  border-radius: 12px;
  width:90%;
  margin:5%;
  max-height: 40vh;
  overflow-y: auto;
}

#ChatMenuUsers::-webkit-scrollbar {
  width: 0;
}

.ChatMenuUser {
  height:4vh;
  padding:4px 2px;
  border-bottom:1px solid white;
}

.ChatMenuUserAvatar{
  float:left;
  margin:0;
  width:4vh;
  height:4vh;
  background-color:white;
  border-radius:100%;
}

.ChatMenuUserName{
  float:right;
  padding-top:1vh;
  width:calc(100% - 4vh - 1px);
  color:white;
  margin:0;
  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
}

#ChatMenuHeader{
  padding-top:1vh;
  item-direction:column;
}

#ChatAvatar{
  margin:0;
  height:10vh;
  width:10vh;
  margin-left:calc(50% - 5vh);
  background-color:white;
  border-radius:100%;
}

#ChatName{
  padding-top:1vh;
  color:white;
  margin:0;
  font-weight: bold;
  font-size: 2vh;
  font-family: "Arial";
  text-align:center;
}
</style>