<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import SockJS from 'sockjs-client'
import {SHA256} from 'crypto-js';
import * as StompJs from '@stomp/stompjs';

//SockJS
const socket = ref(null);
const connectionStatus = ref('disconnected');

//chat
let id = 0
const haventJWT = ref(true)
const name = ref('')
const jwtToken = ref('')
const apiData = ref(null)

let address = "192.168.1.44:56234"

const chats = ref([])

async function postData(Paddress, data) {
  const response = await fetch(
      Paddress,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          "Authorization": "Bearer " + jwtToken.value
        },
        body: JSON.stringify(data)
      }
  )
  return await response.json()
}

async function regLog(Paddress, data) {
  const response = await fetch(
      Paddress,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data)
      }
  )
  return await response.json()
}

async function getData(Paddress, data) {
  const response = await fetch(Paddress, data)
  apiData.value = await response.json()
  return apiData.value
}

onMounted(async () => {
  jwtToken.value = localStorage.getItem("jwtToken")
  if (jwtToken.value) {
    haventJWT.value = false
    name.value = localStorage.getItem("username")
    await goIn()
  }
})

async function goIn() {
  await getData("http://" + address + "/api/v1/user/chats", {
    method: "GET",
    headers: {"Authorization": "Bearer " + jwtToken.value}
  }).then((data) => {
    data.data.forEach((item) => {
      chats.value.push({id: item.id, name: item.name, lastMessage: item.messageContent})
    })
  })

  await getData("http://" + address + "/api/v1/auth/ws-token", {
    method: "POST",
    headers: {"Authorization": "Bearer " + jwtToken.value}
  }).then((data) => {
    connect(data.data)
  })
}

onUnmounted(() => {
  if (socket.value) socket.value.close()
})

const connect = (token) => {
  //socket.value = new SockJS("http://" + address + "/ws/chat?token=" + token)
  socket.value = new StompJs.Client({
    webSocketFactory: () => new SockJS("http://" + address + "/ws?token=" + token),
    connectHeaders: {
      "Authorization": "Bearer " + jwtToken.value
    }
  })

  socket.value.onConnect = () => {
    connectionStatus.value = "connected"
    console.log("Successfully connected")
    socket.value.subscribe("/queue/errors", message => console.log("ERROR:", message.body),
        {"Authorization": "Bearer " + jwtToken.value})
    chats.value.forEach((item) => {
      socket.value.subscribe("/topic/chat/" + item.id, (message) => {
            let chatId = -1
            for (var id = 0; id < chatMessages.value.length; id++) {
              if (chatMessages.value[id].id == item.id) {
                chatId = id
                break
              }
            }
            if (chatId != -1) {
              chatMessages.value[id].message.push(JSON.parse(message.body))
              console.log(chatMessages.value)
            }
          },
          {"Authorization": "Bearer " + jwtToken.value})
      console.log("Subscribe to:" + item.id)
      //socket.value.publish({destination: "/topic/chat/" + item.id + "/message/post", body:"test"})
    })
  }

  socket.value.activate()

  socket.value.onopen = () => {
    connectionStatus.value = "opened"
    console.log("Successfully opened")
  }

  socket.value.onclose = () => {
    connectionStatus.value = "disconnected"
    console.log("Connection closed")

    //setTimeout(() => connect(), 5000);
  }
}

const passNot = ref(false)
const passCh = ref(false)
const isAuth = ref(false)

const haveError = ref('noError')

const userLogin = ref('')
const userPassword = ref('')
const userPasswordConfirmed = ref('')
const errorMessage = ref('')

function passCheck() {
  passNot.value = !(userPassword.value == userPasswordConfirmed.value)
  errCheck()
}

function errCheck() {
  if (passCh.value) errorMessage.value = "Неправильный логин или пароль "
  if (passNot.value) errorMessage.value = "Пароли не совпадают"
  if (passNot.value || passCh.value) haveError.value = "error"
  else haveError.value = "noError"
}

function reg() {
  console.log("Регистрация пользователя:" + userLogin.value + " с паролем:" + userPassword.value + "/" + userPasswordConfirmed.value)
  passCheck()
  if (passNot.value) return
  console.log("Отправка запроса на сервер")
  regLog("http://" + address + "/api/v1/auth/register",
      {
        "username": userLogin.value,
        "passwordHash": SHA256(userPassword.value).toString(),
        "publicKey": "0123456789012345678901234567890123456789012345678901234567890123",
        "encryptedPrivateKey": "0123456789012345678901234567890123456789012345678901234567890123"
      }).then((regData) => {
    console.log("resp:", regData, regData.code == 200)
    if (regData.code === 200 || regData.code === 201) {
      haventJWT.value = false
      name.value = userLogin.value
      jwtToken.value = regData.data
      localStorage.setItem("jwtToken", regData.data)
      localStorage.setItem("username", userLogin.value)
      goIn()
    } else {
      console.log("Ошибка запроса:", regData)
    }
  })
}

function login() {
  console.log("Вход в пользователя:" + userLogin.value + " с паролем:" + userPassword.value)
  console.log("Хеш:", SHA256(userPassword.value).toString())
  regLog("http://" + address + "/api/v1/auth/login",
      {
        "username": userLogin.value,
        "passwordHash": SHA256(userPassword.value).toString()
      }).then((regData) => {
    console.log("resp:", regData, regData.code == 200)
    if (regData.code === 200 || regData.code === 201) {
      haventJWT.value = false
      name.value = userLogin.value
      jwtToken.value = regData.data
      localStorage.setItem("jwtToken", regData.data)
      localStorage.setItem("username", userLogin.value)
      goIn()
      passCh.value = false
      errCheck()
    } else {
      console.log("Ошибка запроса:", regData)
      if (regData.code === 401) {
        passCh.value = true
        errCheck()
      }
    }
  })
}

const activeChatId = ref(-1)
const isChatMenu = ref(false)
const chatMessages = ref([])

async function selectChat(a) {
  activeChatId.value = a
  let estChat = false
  for (var i = 0; i < chatMessages.value.length; i++) {
    if (chatMessages.value[i].id == activeChatId.value) {
      estChat = true
      break
    }
  }
  if (!estChat) {
    await getData("http://" + address + "/api/v1/chat/" + activeChatId.value + "/message", {
      method: "GET",
      headers: {"Authorization": "Bearer " + jwtToken.value}
    }).then((data) => {
      chatMessages.value.push({id: activeChatId.value, message: data.data})
    })
  }
  console.log(chatMessages.value)
}

const addChatMode = ref('selected')
const isGroup = ref(true)

function changeAddChatMode(isBGroup) {
  isGroup.value = isBGroup
  if (isGroup.value) {
    addChatMode.value = "unselected"
  } else {
    addChatMode.value = "selected"
  }
}

const chatName = ref('')
const username = ref('')

async function addChatF() {
  await getData("http://" + address + "/api/v1/chat", {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + jwtToken.value,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "chatName": chatName.value,
      "usernames": [
        name.value,
        username.value
      ]
    })
  }).then((data) => {
    console.log(data.data)
    chats.value.push({id: data.data, name: chatName.value, lastMessage: ""})
  })
}

const message = ref("")

function sendMessage() {
  if (socket.value && connectionStatus.value === 'connected') {
    console.log("Sended:", {textMessage: message.value})
    socket.value.publish({
      destination: "/app/chat/" + activeChatId.value + "/message/post",
      body: JSON.stringify({"content": message.value}),
      headers: {"Authorization": "Bearer " + jwtToken.value}
    })
    console.log({
      destination: "/app/chat/" + activeChatId.value + "/message/post",
      body: {"content": message.value},
      headers: {"Authorization": "Bearer " + jwtToken.value}
    })
  }
  message.value = ''
}
</script>

<template>
  <div v-if="haventJWT" id="autorazeBlocker" :class="{selected: isAuth, unselected: !isAuth}">
    <div id="autorizationForm" :class="{selected: !isAuth, unselected: isAuth}">
      <div id="changeAutorization">
        <button id="LoginButton" @click="isAuth=true">Вход</button>
        <button id="RegButton" @click="isAuth=false">Регистрация</button>
      </div>
      <p v-if="passNot || passCh" id="error">{{ errorMessage }}</p>
      <form :class="haveError" id="login" v-if=isAuth @submit.prevent="login">
        <input v-model="userLogin" required placeholder="Логин">
        <input type="password" v-model="userPassword" required placeholder="Пароль">
        <button class="inBut">Войти</button>
      </form>
      <form :class="haveError" id="reg" v-else @submit.prevent="reg">
        <input v-model="userLogin" required placeholder="Логин">
        <input type="password" v-model="userPassword" required placeholder="Пароль">
        <input v-model="userPasswordConfirmed"
               placeholder="Подтвердите пароль"
               required
               type="password"
               @input="passCheck">
        <button class="inBut">Зарегистрироваться</button>
      </form>
    </div>
  </div>

  <div v-if="!haventJWT" id="ForChats">
    <div id="chatSelector">
      <div v-for="chat in chats"
           :class="{selected: activeChatId === chat.id}"
           class="chat"
           @click="selectChat(chat.id)">
        <p class="chatAvatar"/>
        <p class="chatName">{{ chat.name }}</p>
        <p class="chatLastMessage">{{ chat.lastMessage }}</p>
      </div>
    </div>
    <div v-if="activeChatId != -1" id="chatChatting">
      <ul class="messages">
        <li v-for="chats in chatMessages" :key="chats.id" class="message">
          <li v-for="message in chats.message" v-if="chats.id == activeChatId" class="message">
            {{ message.sender }} : {{ message.content }}
          </li>
        </li>
      </ul>
      <form id="chatInput" @submit.prevent="sendMessage">
        <input v-model="message" placeholder="Сообщение" required>
        <button>=></button>
      </form>
    </div>
    <button id="addChat" @click="isChatMenu=true">+</button>
    <div v-if="isChatMenu" id="addChatMenu" @click="isChatMenu=false">
      <div id="addChatSettings" @click.stop>
        <div id="addChatTopHeader">
          <button class="topHeaderButton"
                  style="background-color: white;color:black;border-radius: 15px 0 0 0;"
                  @click="changeAddChatMode(false)">Пользователь
          </button>
          <button class="topHeaderButton"
                  style="background-color: black;color:white;border-radius: 0 15px 0 0;"
                  @click="changeAddChatMode(true)">Группа
          </button>
        </div>
        <form id="addChatForm" :class="haveError" @submit.prevent="addChatF">
          <input v-model="chatName" name="username" placeholder="Название группы" required>
          <input v-model="username" name="username" placeholder="Имя пользователя" required>
          <button class="inBut" type="submit">Создать группу</button>
        </form>
      </div>
    </div>
  </div>
</template>

<style>
#chatChatting {
  margin-left: 25vw;
  width: 75vw;
}

.messages {
  margin: 0;
  padding: 0;
}

.message {
  color: white;
  list-style: none;
}

#ForChats {
  background-color: rgb(0, 0, 0);
  width: 100vw;
  min-height: 100vh;
  height: auto;
  overflow: auto;
}

#chatSelector {
  float: left;
  background-color: rgb(75, 75, 75);
  width: 25vw;
  min-height: 100vh;
}

.chat {
  width: 25vw;
  height: 6vh;
  background-color: rgb(100, 100, 100);
}

.chatAvatar {
  float: left;
  width: 5vh;
  height: 5vh;
  margin: 0;
  margin-left: 0.5vh;
  margin-top: 0.5vh;
  background-color: rgb(125, 125, 125);
  border-radius: 100%;
}

.chatName {
  float: right;
  margin: 0;
  width: calc(25vw - 6vh);
  color: white;
  font-weight: bold;
  font-size: 1.75vh;
  height: 2vh;
  margin-bottom: 0.5vh;
  margin-top: 0.5vh;
  font-family: "Arial";
}

.chatLastMessage {
  float: right;
  margin: 0;
  width: calc(25vw - 6vh);
  color: rgb(175, 175, 175);
  font-weight: bold;
  margin-bottom: 0.5vh;
  margin-top: 0.5vh;
  height: 2vh;
  font-family: "Arial";
}

#addChat {
  position: fixed;
  top: calc(100% - 5.5vw);
  left: calc(25vw - 5.5vw);
  width: 5vw;
  height: 5vw;
  border-radius: 100%;
  border: 0;
  background-color: white;
  color: rgb(100, 100, 100);
  font-size: 4vw;
  text-align: center;
}

#addChatMenu {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(50, 50, 50, 50%);
}

#addChatSettings {
  border-radius: 15px;
  margin-left: calc(50vw - (25vw / 2));
  margin-top: calc(50vh - (50vh / 2));
  width: 25vw;
  height: 50vh;
  background-color: rgb(75, 75, 75);
}

#addChatTopHeader {
  width: 25vw;
  height: 6vh;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr;
}

.topHeaderButton {
  text-align: center;
  border: 0;
  padding: 0;
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
}

#addChatForm {
  margin: 0;
  width: 25vw;
  height: 44vh;
  border-radius: 0 0 15px 15px;
}
</style>
<style>
#autorazeBlocker {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
}

#autorizationForm {
  border-radius: 20px 20px 15px 15px;
  width: calc(max(1vh, 1vw) * 25);
  height: calc(max(1vh, 1vw) * 20);
  margin-left: calc(50vw - (max(1vh, 1vw) * 12.5));
  margin-top: calc(50vh - (max(1vh, 1vw) * 10));
}

#LoginButton {
  background-color: rgb(0, 0, 0);
  padding: 0;
  border: 0;
  color: white;
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
  border-radius: 15px 0 0 0;
}

#RegButton {
  background-color: rgb(255, 255, 255);
  padding: 0;
  border: 0;
  color: rgb(0, 0, 0);
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
  border-radius: 0 15px 0 0;
}

#changeAutorization {
  height: 20%;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr;
  text-align: center;
}

#login {
  margin: 0;
  width: 80%;
  height: 70%;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(3, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.5);
  padding-left: 10%;
  text-align: center;
}

#reg {
  margin: 0;
  width: 80%;
  height: 70%;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(4, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.25);
  padding-left: 10%;
}

.error {
  padding-top: 10%;
}

.noError {
  padding-top: 5%;
}

#autorizationForm.unselected input {
  background: rgb(0, 0, 0);
  color: white;
  font-weight: bold;
  font-size: 100%;
  width: 100%;
  border-radius: 15px;
  font-family: "Arial";

  text-align: center;
  border: 2px solid rgb(255, 255, 255);
  padding: 0px;
}

#autorizationForm.selected input {
  background: rgb(255, 255, 255);
  color: rgb(0, 0, 0);
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
  width: 100%;
  border-radius: 15px;

  text-align: center;
  border: 2px solid rgb(0, 0, 0);
  padding: 0px;
}

#autorizationForm.selected .inBut {
  background-color: rgb(0, 0, 0);
  border-radius: 15px;
  border: 0;
  color: rgb(255, 255, 255);
}

#autorizationForm.unselected .inBut {
  background-color: rgb(255, 255, 255);
  border-radius: 15px;
  border: 0;
  color: rgb(0, 0, 0);
}

.inBut {
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
}

#error {
  position: absolute;
  top: 21%;
  left: 50%;
  transform: translate(-50%);
  text-align: center;
  width: 100%;
  height: 10%;
  font-size: 75%;
  margin: 0;
  color: red;
  font-family: "Arial";
}

.unselected {
  background: rgb(0, 0, 0);
}

.selected {
  background: rgb(255, 255, 255);
}
</style>