<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import SockJS from 'sockjs-client'
import {SHA256} from 'crypto-js'
import * as StompJs from '@stomp/stompjs'

//SockJS
const socket = ref(null);
const connectionStatus = ref('disconnected');

//chat
const haventJWT = ref(true)
const name = ref('')
const jwtToken = ref('')
const apiData = ref(null)

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const WS_URL = import.meta.env.VITE_WS_URL

const chats = ref([])

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

function arrayBufferToBase64(buffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

function base64ToArrayBuffer(base64) {
  const binaryString = atob(base64);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes.buffer;
}

onMounted(async () => {
  jwtToken.value = localStorage.getItem("jwtToken")
  if (jwtToken.value) {
    haventJWT.value = false
    name.value = localStorage.getItem("username")
    await goIn()
  }
})

let privateKey = null

async function goIn() {
  let privat = new Uint8Array(base64ToArrayBuffer(localStorage.getItem("private")))
  //console.log(privat.slice(0, 1)) - version
  let salt = privat.slice(1, 17)
  let iv = privat.slice(17, 29)
  let encryptPrivateKey = privat.slice(29, privat.length)

  const keyMaterial = await window.crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(localStorage.getItem("password")),
      "PBKDF2",
      false,
      ["deriveKey"]
  )

  let aesKey = await window.crypto.subtle.deriveKey(
      {
        name: "PBKDF2",
        salt: salt,
        iterations: 100000,
        hash: "SHA-256"
      },
      keyMaterial,
      {name: "AES-GCM", length: 256},
      true,
      ["decrypt"]
  )

  const decryptedPrivateKey = await window.crypto.subtle.decrypt(
      {
        name: "AES-GCM",
        iv: iv
      },
      aesKey,
      encryptPrivateKey
  )

  privateKey = await window.crypto.subtle.importKey(
      "pkcs8",
      decryptedPrivateKey,
      {
        name: "RSA-OAEP",
        hash: "SHA-256"
      },
      true,
      ["decrypt"]
  )

  await getData(`https://${API_BASE_URL}/api/v1/user/chats`, {
    method: "GET",
    headers: {"Authorization": "Bearer " + jwtToken.value}
  }).then(async (data) => {
    for (const item of data.data) {
      let symKey = await window.crypto.subtle.decrypt(
          {name: "RSA-OAEP"},
          privateKey,
          base64ToArrayBuffer(item.encryptedSymmetricKey)
      )
      let symmetricKey = await window.crypto.subtle.importKey(
          "raw",
          symKey,
          {name: "AES-GCM", length: 256},
          true,
          ["encrypt", "decrypt"]
      )
      let decryptedMessage = ""
      if (item.messageContent != "") {
        let decodedBlob = base64ToArrayBuffer(item.messageContent)
        let iv = decodedBlob.slice(1, 13)
        let content = decodedBlob.slice(13)
        decryptedMessage = new TextDecoder().decode(await window.crypto.subtle.decrypt({
          "name": "AES-GCM",
          "iv": iv
        }, symmetricKey, content))
      }
      chats.value.push({id: item.id, name: item.name, lastMessage: decryptedMessage, symmetricKey: symmetricKey})
    }
  })

  await getData(`https://${API_BASE_URL}/api/v1/auth/ws-token`, {
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
  //socket.value = new SockJS("https://" + address + "/ws/chat?token=" + token)
  socket.value = new StompJs.Client({
    webSocketFactory: () => new SockJS(`https://${API_BASE_URL}${WS_URL}?token=${token}`),
    connectHeaders: {
      "Authorization": "Bearer " + jwtToken.value
    }
  })

  socket.value.onConnect = () => {
    connectionStatus.value = "connected"
    console.log("Successfully connected")
    socket.value.subscribe("/user/queue/error", message => console.log("ERROR:", message.body))
    socket.value.subscribe("/user/queue/chat", async (message) => {
      console.log("createdChat", JSON.parse(message.body))
      let symKey = await window.crypto.subtle.decrypt(
          {name: "RSA-OAEP"},
          privateKey,
          base64ToArrayBuffer(JSON.parse(message.body).data.encryptedSymmetricKey)
      )
      let symmetricKey = await window.crypto.subtle.importKey(
          "raw",
          symKey,
          {name: "AES-GCM", length: 256},
          true,
          ["encrypt", "decrypt"]
      )
      let chatId = JSON.parse(message.body).data.id
      let pisk = chats.value.push({
        id: chatId,
        name: JSON.parse(message.body).data.name,
        lastMessage: "",
        symmetricKey: symmetricKey
      })
      chatMessages.value.push({id: chatId, message: new Proxy(Array(), Array())})
      let topChat = chats.value.splice(pisk - 1, 1)
      chats.value = topChat.concat(chats.value)

      socket.value.subscribe("/topic/chat/" + chatId, async (message2) => {
        let messageBody = JSON.parse(message2.body).data
        let chatIdx = chats.value.findIndex(c => c.id == chatId)
        let decodedBlob = base64ToArrayBuffer(messageBody.content)
        let iv = decodedBlob.slice(1, 13)
        let content = decodedBlob.slice(13)
        let decryptedMessage = new TextDecoder().decode(await window.crypto.subtle.decrypt({
          "name": "AES-GCM",
          "iv": iv
        }, chats.value[chatIdx].symmetricKey, content))
        messageBody.content = decryptedMessage
        messageBody.receivers = Array()
        chats.value[chatIdx].lastMessage = messageBody.content
        let currentChatMessages = chatMessages.value.find(x => x.id == chatId)
        if (currentChatMessages) {
          currentChatMessages.message.push(messageBody)
        }
        let topChat = chats.value.splice(chatIdx, 1)
        chats.value = topChat.concat(chats.value)

        if (chatId == activeChatId.value) {
          socket.value.publish({
            destination: "/app/chat/" + activeChatId.value + "/message/" + messageBody.id + "/receive",
          })
        }
      })

      socket.value.subscribe("/topic/chat/" + chatId + "/received", async (message2) => {
        let messageBody = JSON.parse(message2.body).data
        let currentChatMessages = chatMessages.value.find(x => x.id == chatId)
        if (currentChatMessages) {
          let receivedMessage = currentChatMessages.message.find(x => x.id == messageBody.id)
          if (receivedMessage) {
            delete messageBody.id;
            receivedMessage.receivers.push(messageBody);
          }
        }
      })
    })

    chats.value.forEach((item) => {
      socket.value.subscribe("/topic/chat/" + item.id, async (message) => {
        let messageBody = JSON.parse(message.body).data
        let chatIdx = chats.value.findIndex(c => c.id == item.id)
        let decodedBlob = base64ToArrayBuffer(messageBody.content)
        let iv = decodedBlob.slice(1, 13)
        let content = decodedBlob.slice(13)
        let decryptedMessage = new TextDecoder().decode(await window.crypto.subtle.decrypt({
          "name": "AES-GCM",
          "iv": iv
        }, chats.value[chatIdx].symmetricKey, content))
        messageBody.content = decryptedMessage
        messageBody.receivers = Array()
        chats.value[chatIdx].lastMessage = messageBody.content
        let currentChatMessages = chatMessages.value.find(x => x.id == item.id)
        if (currentChatMessages) {
          currentChatMessages.message.push(messageBody)
        }
        let topChat = chats.value.splice(chatIdx, 1)
        chats.value = topChat.concat(chats.value)

        if (item.id == activeChatId.value) {
          socket.value.publish({
            destination: "/app/chat/" + activeChatId.value + "/message/" + messageBody.id + "/receive",
          })
        }
      })
      socket.value.subscribe("/topic/chat/" + item.id + "/received", async (message) => {
        let messageBody = JSON.parse(message.body).data
        let currentChatMessages = chatMessages.value.find(x => x.id == item.id)
        if (currentChatMessages) {
          let receivedMessage = currentChatMessages.message.find(x => x.id == messageBody.id)
          if (receivedMessage) {
            delete messageBody.id
            receivedMessage.receivers.push(messageBody)
          }
        }
      })

      console.log("Subscribe to:" + item.id)
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

    setTimeout(() => goIn(), 5000);
  }
}

async function generateKeyPair() {
  const keyPair = await window.crypto.subtle.generateKey(
      {
        name: "RSA-OAEP",
        modulusLength: 2048,
        publicExponent: new Uint8Array([1, 0, 1]),
        hash: "SHA-256",
      },
      true,
      ["encrypt", "decrypt"]
  )
  return {
    publicKey: keyPair.publicKey,
    privateKey: keyPair.privateKey
  }
}

async function generateSymmetricKey() {
  const key = await window.crypto.subtle.generateKey(
      {
        name: "AES-GCM",
        length: 256
      },
      true,
      ["encrypt", "decrypt"]
  )
  return key
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

async function reg() {
  console.log("Регистрация пользователя:" + userLogin.value + " с паролем:" + userPassword.value + "/" + userPasswordConfirmed.value)
  passCheck()
  if (passNot.value) return
  console.log("Отправка запроса на сервер")
  let keyPair = await generateKeyPair()
  let exPublic = await window.crypto.subtle.exportKey("jwk", keyPair.publicKey)

  let exPrivate = await window.crypto.subtle.exportKey(
      "pkcs8",
      keyPair.privateKey
  )

  let salt = window.crypto.getRandomValues(new Uint8Array(16))
  let iv = window.crypto.getRandomValues(new Uint8Array(12))

  let keyForPrivate = await window.crypto.subtle.importKey(
      "raw",
      new TextEncoder().encode(userPassword.value),
      {name: "PBKDF2"},
      false,
      ["deriveKey"]
  );

  let aesKey = await window.crypto.subtle.deriveKey(
      {
        name: "PBKDF2",
        salt: salt,
        iterations: 100000,
        hash: "SHA-256"
      },
      keyForPrivate,
      {name: "AES-GCM", length: 256},
      true,
      ["encrypt", "decrypt"]
  )
  let encryptPrivate = await window.crypto.subtle.encrypt(
      {
        name: "AES-GCM",
        iv: iv
      },
      aesKey,
      exPrivate
  )

  let version = new Uint8Array([0x01])

  let totalLength = version.length + salt.length + iv.length + encryptPrivate.byteLength
  let result = new Uint8Array(totalLength)

  let offset = 0;
  result.set(version, offset)
  offset += version.length
  result.set(salt, offset)
  offset += salt.length
  result.set(iv, offset)
  offset += iv.length
  result.set(new Uint8Array(encryptPrivate), offset)
  let kok = arrayBufferToBase64(result)
  regLog(`https://${API_BASE_URL}/api/v1/auth/register`,
      {
        "username": userLogin.value,
        "passwordHash": SHA256(userPassword.value).toString(),
        "publicKey": JSON.stringify(exPublic),
        "encryptedPrivateKey": kok
      }).then((regData) => {
    console.log("resp:", regData, regData.code == 200)
    if (regData.code === 200 || regData.code === 201) {
      haventJWT.value = false
      name.value = userLogin.value
      jwtToken.value = regData.data.jwt
      localStorage.setItem("public", regData.data.publicKey)
      localStorage.setItem("private", regData.data.encryptedPrivateKey)
      localStorage.setItem("jwtToken", regData.data.jwt)
      localStorage.setItem("username", userLogin.value)
      localStorage.setItem("password", userPassword.value)
      goIn()
    } else {
      console.log("Ошибка запроса:", regData)
    }
  })
}

function login() {
  console.log("Вход в пользователя:" + userLogin.value + " с паролем:" + userPassword.value)
  console.log("Хеш:", SHA256(userPassword.value).toString())
  regLog(`https://${API_BASE_URL}/api/v1/auth/login`,
      {
        "username": userLogin.value,
        "passwordHash": SHA256(userPassword.value).toString()
      }).then((regData) => {
    console.log("resp:", regData, regData.code == 200)
    if (regData.code === 200 || regData.code === 201) {
      haventJWT.value = false
      name.value = userLogin.value
      jwtToken.value = regData.data.jwt
      localStorage.setItem("public", regData.data.publicKey)
      localStorage.setItem("private", regData.data.encryptedPrivateKey)
      localStorage.setItem("jwtToken", regData.data.jwt)
      localStorage.setItem("username", userLogin.value)
      localStorage.setItem("password", userPassword.value)
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
const chatHeaderName = ref("")

async function selectChat(a, chatName) {
  activeChatId.value = a
  chatHeaderName.value = chatName
  let chatIdx = chatMessages.value.findIndex(c => c.id == a)
  let chatInd = chats.value.findIndex(c => c.id == a)
  if (chatIdx == -1) {
    await getData(`https://${API_BASE_URL}/api/v1/chat/${activeChatId.value}/message`, {
      method: "GET",
      headers: {"Authorization": "Bearer " + jwtToken.value}
    }).then(async (data) => {
      let messages = new Proxy(Array(), Array())
      for (const item of data.data) {
        let decodedBlob = base64ToArrayBuffer(item.content)
        let iv = decodedBlob.slice(1, 13)
        let content = decodedBlob.slice(13)
        let decryptedMessage = new TextDecoder().decode(await window.crypto.subtle.decrypt({
          "name": "AES-GCM",
          "iv": iv
        }, chats.value[chatInd].symmetricKey, content))
        item.content = decryptedMessage
        messages.push(item)
      }
      chatMessages.value.push({id: activeChatId.value, message: messages})
    })
  }
  if (chatIdx == -1) chatIdx = chatMessages.value.findIndex(c => c.id == a)
  let currentChatMessages = chatMessages.value[chatIdx].message
  currentChatMessages.forEach(message => {
    if (!message.receivers.some(c => c.username == name.value)) {
      socket.value.publish({destination: "/app/chat/" + a + "/message/" + message.id + "/receive"})
    }
  })
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
  if (name.value == username.value) return;

  let symmetricKey = await window.crypto.subtle.exportKey("raw", await generateSymmetricKey())
  let users = []

  await getData(`https://${API_BASE_URL}/api/v1/user/keys`, {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + jwtToken.value,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "usernames": [{"username": name.value}, {"username": username.value}]
    })
  }).then(async (data) => {
    for (const item of data.data) {
      let key = await window.crypto.subtle.importKey(
          "jwk",
          JSON.parse(item.publicKey),
          {name: "RSA-OAEP", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256"},
          true,
          ["encrypt"])

      let enSyKey = arrayBufferToBase64(await window.crypto.subtle.encrypt({name: "RSA-OAEP"}, key, symmetricKey))
      users.push({"username": item.username, "encryptedSymmetricKey": enSyKey})
    }
  })
  await getData(`https://${API_BASE_URL}/api/v1/chat`, {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + jwtToken.value,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "chatName": chatName.value,
      "usersDetails": users
    })
  }).then((data) => {
    console.log("Single Chat Creation:", data.data)
  })
}

async function addGroupChatF() {
  let symmetricKey = await window.crypto.subtle.exportKey("raw", await generateSymmetricKey())
  let users = []
  let userN = [{"username": name.value}]
  usersList.value.forEach((item) => {
    userN.push({"username": item.name})
  })

  await getData(`https://${API_BASE_URL}/api/v1/user/keys`, {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + jwtToken.value,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "usernames": userN
    })
  }).then(async (data) => {
    for (const item of data.data) {
      let key = await window.crypto.subtle.importKey(
          "jwk",
          JSON.parse(item.publicKey),
          {name: "RSA-OAEP", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256"},
          true,
          ["encrypt"])

      let enSyKey = arrayBufferToBase64(await window.crypto.subtle.encrypt({name: "RSA-OAEP"}, key, symmetricKey))
      users.push({"username": item.username, "encryptedSymmetricKey": enSyKey})
    }
  })
  await getData(`https://${API_BASE_URL}/api/v1/chat`, {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + jwtToken.value,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "chatName": chatName.value,
      "usersDetails": users
    })
  }).then((data) => {
    console.log(data.data)
  })
}

const message = ref("")

async function sendMessage() {
  if (socket.value && connectionStatus.value === 'connected') {
    let chatik = chats.value[chats.value.findIndex(c => c.id == activeChatId.value)]
    let key = chatik.symmetricKey

    let iv = window.crypto.getRandomValues(new Uint8Array(12))

    let version = new Uint8Array([0x01])

    let contentMessage = await window.crypto.subtle.encrypt({
      "name": "AES-GCM",
      "iv": iv
    }, key, new TextEncoder().encode(message.value))

    let totalLength = version.length + iv.length + contentMessage.byteLength
    let result = new Uint8Array(totalLength)

    let offset = 0;
    result.set(version, offset)
    offset += version.length
    result.set(iv, offset)
    offset += iv.length
    result.set(new Uint8Array(contentMessage), offset)
    let kok = arrayBufferToBase64(result)

    socket.value.publish({
      destination: "/app/chat/" + activeChatId.value + "/message/post",
      body: JSON.stringify({"content": kok})
    })
  }
  message.value = ''
}

const usersList = ref([])

const userToGroup = ref('')

function addUser() {
  if (userToGroup.value == "") return;
  usersList.value.push({name: userToGroup.value})
  userToGroup.value = ''
}

function removeUser(user) {
  usersList.value = usersList.value.filter((t) => t !== user)
}
</script>

<template>
  <div v-if="haventJWT" id="autorazeBlocker">
    <div id="autorizationForm" :class="{selected: !isAuth, unselected: isAuth}">
      <div id="changeAutorization">
        <button id="LoginButton" @click="isAuth=true">Вход</button>
        <button id="RegButton" @click="isAuth=false">Регистрация</button>
        <p v-if="passNot || passCh" id="error">{{ errorMessage }}</p>
      </div>
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
               type="password"
               required
               @input="passCheck">
        <button class="inBut">Зарегистрироваться</button>
      </form>
    </div>
  </div>

  <div v-if="!haventJWT" id="ForChats">
    <div id="chatSelector">
      <div v-for="chat in chats"
           class="chat"
           :class="{selected: activeChatId === chat.id}"
           @click="selectChat(chat.id, chat.name)">
        <p class="chatAvatar"/>
        <p class="chatName">{{ chat.name }}</p>
        <p class="chatLastMessage">{{ chat.lastMessage }}</p>
      </div>
    </div>
    <div v-if="activeChatId != -1" id="chatChatting">
      <div id="chatHeader">
        <button id="chatHeaderLeave" @click="activeChatId = -1">🡨</button>
        <p id="chatHeaderName">{{ chatHeaderName }}</p>
      </div>
      <ul class="messages">
        <li v-for="chats in chatMessages" :key="chats.id" class="messageChat">
          <li v-for="message in chats.message"
              v-if="chats.id == activeChatId"
              :class="{messageUs: message.sender == name}"
              class="message">
            <div class="message-wrapper" :class="{isMe: message.sender == name}">
              <p v-if="message.sender != name" class="messageSender">{{ message.sender }}</p>
              <p class="messageContent">{{ message.content }}</p>
              <p v-if="message.sender == name" class="messageStatus">{{ message.receivers.length >= 2 ? "🤝" : "👋" }}</p>
            </div>
          </li>
        </li>
      </ul>
      <form id="chatInput" @submit.prevent="sendMessage">
        <input id="chatInputInputer" v-model="message" placeholder="Сообщение" required>
        <button id="chatInputSend">▶</button>
      </form>
    </div>
    <button id="addChat" @click="isChatMenu=true">+</button>
    <div v-if="isChatMenu" id="addChatMenu" @click="isChatMenu=false">
      <div id="addChatSettings" :class="{selected: isGroup, unselected: !isGroup}" @click.stop>
        <div id="addChatTopHeader">
          <button id="selectUser"
                  class="topHeaderButton"
                  @click="changeAddChatMode(false)">Пользователь
          </button>
          <button id="selectGroup"
                  class="topHeaderButton"
                  @click="changeAddChatMode(true)">Группа
          </button>
        </div>
        <form v-if="isGroup" id="addChatForm" :class="haveError" @submit.prevent="addGroupChatF">
          <input v-model="chatName" name="chatName" placeholder="Название группы" required>
          <div id="addUserToGroup">
            <p class="defText">Добавить пользователя:</p>
            <div id="addUserToGroupDiv">
              <input v-model="userToGroup" name="user" placeholder="Имя пользователя"/>
              <button type="button" @click="addUser">Добавить</button>
            </div>
            <p class="defText">Список пользователей:</p>
          </div>
          <div v-for="user in usersList" class="addUserToGroupBottom">
            <p>{{ user.name }}</p>
            <button type="button" @click="removeUser(user)">X</button>
          </div>
          <button class="inBut" type="submit">Создать группу</button>
        </form>
        <form v-else id="addChatForm" :class="haveError" @submit.prevent="addChatF">
          <input v-model="chatName" placeholder="Название чата" required>
          <input v-model="username" placeholder="Имя пользователя" required>
          <button class="inBut" type="submit">Создать чат</button>
        </form>
      </div>
    </div>
  </div>
</template>

<style>
#addUserToGroup {
  display: grid;
  grid-template-columns: 1fr;
}

.addUserToGroupBottom {
  border: 1px solid black;
  display: grid;
  grid-template-columns: 4fr 1fr;
}

.addUserToGroupBottom p {
  height: 50%;
  font-size: 100%;
  margin: 0;
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  color: black;
}

.addUserToGroupBottom button {
  border: 0px;
  padding: 0px;
  background-color: black;
  color: white;
  font-weight: bold;
  font-family: "Arial";
}

#addUserToGroupDiv {
  display: grid;
  grid-template-columns: 3fr 1fr;
}

#addUserToGroupDiv button {
  padding: 0;
  border: 0;
  color: white;
  background-color: black;
}

.defText {
  margin: 0;
  text-align: center;
  font-weight: bold;
  font-family: "Arial";
  color: black;
}

#chatInputSend {
  height: 4vh;
  width: 4vh;
  border: 0;
  padding: 0;
  margin-bottom: 0.5vh;
  margin-left: 1vh;
  margin-right: 1vw;
  border-radius: 100%;
  background-color: white;
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  color: black;
}

#chatInputInputer {
  background-color: black;
  color: white;
  height: 100%;
  padding: 0;
  border: 0;
  padding-left: 1vw;
  width: calc(100% - 5vh - 1px - 1vw);
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  vertical-align: bottom;
}

#chatInputInputer:focus {
  outline: none !important;
}

#chatInput {
  margin: 0;
  height: 5vh;
  border-top: 1px solid white;
}

#chatHeader {
  margin: 0;
  height: 5vh;
  border-bottom: 1px solid white;
}

#chatHeaderLeave {
  padding: 0;
  border: 0;
  margin-left: 1vw;
  height: 5vh;
  width: 4vh;
  background-color: black;
  color: white;
  font-weight: 999;
  font-size: 4vh;
  font-family: "Arial";
}

#chatHeaderName {
  float: right;
  margin: 0;
  width: calc(74vw - 5vh);
  margin-top: 1vh;
  height: 4vh;
  color: white;
  font-weight: bold;
  font-size: 3vh;
  font-family: "Arial";
}

#chatChatting {
  margin-left: 25vw;
  width: 75vw;
  min-height: 100vh;
}

.messages {
  margin: 0;
  padding: 0;
  padding-left: 1vw;
  padding-right: 1vw;
  height: calc(90vh - 2px);
  align-items: end;
}

.messageChat {
  list-style: none;
  max-height: 100%;
  overflow-y: auto;
  -ms-overflow-style: none;
}

.messageChat::-webkit-scrollbar {
  width: 0;
}

.message {
  list-style: none;
  font-weight: bold;
  font-size: 1.75vh;
  font-family: "Arial";
  margin-bottom: 5px;
}

.messageUs {
  text-align: right;
}

.message-wrapper {
  display: inline-flex;
  flex-direction: column;
  max-width: 80%;
  min-width: min-content;
  background-color: black;
  padding: 3px;
  border-radius: 12px 12px 12px 0;
  border: 1px solid white;
}

.isMe {
  border-radius: 12px 12px 0 12px;
}

.messageSender {
  color: grey;
  padding-bottom: 2px;
}

.messageContent {
  color: white;
}

.messageStatus {
  text-align: left;
  margin: 0;
}

.messageSender, .messageContent {
  width: fit-content;
  max-width: 100%;
  margin: 0;
  box-sizing: border-box;
  word-break: break-word; /* Переносит длинные слова */
  overflow-wrap: break-word; /* Альтернатива для переноса */
  hyphens: auto;
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
  background-color: black;
  border-right: 1px solid white;
  width: calc(max(1vh, 1vw) * 25);
  height: 100vh;
  overflow: auto;
}

#chatSelector::-webkit-scrollbar {
  width: 0;
}

.chat {
  width: 25vw;
  height: 6vh;
  background-color: black;
  border-bottom: 1px solid white;
}

.chat .chatAvatar {
  float: left;
  width: 5vh;
  height: 5vh;
  margin: 0;
  margin-left: 0.5vh;
  margin-top: 0.5vh;
  background-color: white;
  border-radius: 100%;
}

.chat .chatName {
  overflow: hidden;
  float: right;
  margin: 0;
  width: calc(25vw - 6vh);
  color: white;
  font-weight: bold;
  font-size: 1.75vh;
  height: 2vh;
  padding-bottom: 0.5vh;
  padding-top: 0.5vh;
  font-family: "Arial";
}

.chat .chatLastMessage {
  overflow: hidden;
  float: right;
  margin: 0;
  width: calc(25vw - 6vh);
  color: white;
  font-weight: bold;
  font-size: 1.75vh;
  height: 2vh;
  padding-bottom: 0.5vh;
  padding-top: 0.5vh;
  font-family: "Arial";
}

.chat.selected .chatAvatar {
  background-color: black;
}

.chat.selected .chatName {
  color: black;
}

.chat.selected .chatLastMessage {
  color: black;
}

#addChat {
  position: fixed;
  top: calc(100% - calc(max(1vh, 1vw) * 5.5));
  left: calc(max(1vh, 1vw) * 19.5);
  width: calc(max(1vh, 1vw) * 5);
  height: calc(max(1vh, 1vw) * 5);
  border-radius: 100%;
  border: 0;
  background-color: white;
  color: black;
  font-size: calc(max(1vh, 1vw) * 4);
  padding: 0;
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

#addChatTopHeader {
  width: calc(max(1vh, 1vw) * 30);
  height: calc(max(1vh, 1vw) * 6);
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr;
}

.topHeaderButton {
  text-align: center;
  border: 0;
  padding: 0;
}

#selectGroup {
  background-color: white;
  color: black;
  font-size: 100%;
  font-weight: bold;
  font-family: "Arial";
}

#selectUser {
  background-color: black;
  color: white;
  font-size: 100%;
  font-weight: bold;
  font-family: "Arial";
}

#addChatForm {
  margin: 0;
  width: calc(max(1vh, 1vw) * 28);
  height: calc(max(1vh, 1vw) * 37.5);
  padding-left: calc(max(1vh, 1vw) * 1);
  display: grid;
  grid-template-columns: 1fr;
  grid-row-gap: calc(max(1vh, 1vw) * 0.5);
}

#addChatSettings {
  border: 1px solid white;
  margin-left: calc(50vw - (calc(max(1vh, 1vw) * 30) / 2));
  margin-top: calc(50vh - (calc(max(1vh, 1vw) * 45) / 2));
  width: calc(max(1vh, 1vw) * 30);
  height: calc(max(1vh, 1vw) * 45);
}

#addChatSettings input {
  //font-size: 100%;
  font-weight: bold;
  font-family: "Arial";
  text-align: center;
  padding: 0;
}

#addChatSettings.selected input {
  background-color: white;
  border: 1px solid black;
  color: black;
}

#addChatSettings.unselected input {
  background-color: black;
  border: 1px solid white;
  color: white;
}

#addChatSettings.selected .inBut {
  background-color: black;
  border: 0;
  padding: 0;
  color: white;
}

#addChatSettings.unselected .inBut {
  background-color: white;
  border: 0;
  padding: 0;
  color: black;
}
</style>
<style>
#autorazeBlocker {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: black;
}

#autorizationForm {
  border: 1px solid white;
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
}

#RegButton {
  background-color: rgb(255, 255, 255);
  padding: 0;
  border: 0;
  color: rgb(0, 0, 0);
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
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
  height: calc(max(1vh, 1vw) * 14.5);
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(3, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.5);
  padding-left: calc(max(1vh, 1vw) * 2.5);
  text-align: center;
}

#reg {
  margin: 0;
  width: 80%;
  height: calc(max(1vh, 1vw) * 14.5);
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(4, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.25);
  padding-left: calc(max(1vh, 1vw) * 2.5);
  text-align: center;
}

.error {
  padding-top: calc(max(1vh, 1vw) * 1.5);
}

.noError {
  padding-top: calc(max(1vh, 1vw) * 0.75);
}

#autorizationForm.unselected input {
  background: rgb(0, 0, 0);
  color: white;
  font-weight: bold;
  font-family: "Arial";
  width: calc(max(1vh, 1vw) * 20);
  font-size: 100%;

  text-align: center;
  border: 1px solid white;
  padding: 0px;
}

#autorizationForm.selected input {
  background: white;
  color: black;
  font-weight: bold;
  font-family: "Arial";
  width: calc(max(1vh, 1vw) * 20);
  font-size: 100%;

  text-align: center;
  border: 2px solid rgb(0, 0, 0);
  padding: 0px;
}

#autorizationForm.selected .inBut {
  background-color: black;
  border: 0;
  padding: 0;
  color: white;
}

#autorizationForm.unselected .inBut {
  background-color: white;
  border: 0;
  padding: 0;
  color: black;
}

.inBut {
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
}

#error {
  width: calc(max(1vh, 1vw) * 25);
  font-size: calc(max(1vh, 1vw) * 1.25);
  margin: 0;
  margin-top: calc(max(1vh, 1vw) * 4);
  color: red;
  font-family: "Arial";
  position: fixed;
}

.unselected {
  background: black;
}

.selected {
  background: white;
}
</style>