<script setup>
  import {ref,onMounted, onUnmounted } from 'vue'
  import SockJS from 'sockjs-client'
  import { SHA256 } from 'crypto-js';

  //SockJS
  const socket = ref(null);
  const connectionStatus = ref('disconnected');

  //chat
  let id = 0
  const messageText = ref('')
  const haventJWT = ref(true)
  const messages = ref([])
  const name = ref('')
  const jwtToken = ref('')
  const apiData = ref(null)

  let address = "192.168.224.66:25565"

  function sendMessage(){
    messages.value.push({ id: id++, text: messageText.value, owner: name})

    if (socket.value && connectionStatus.value === 'connected') {
      console.log("Sended")
      socket.value.send(JSON.stringify({
        senderId: 0,
        chatId: 0,
        textMessage: messageText.value,
        owner: name.value
      }))
    }

    messageText.value = ''
  }

  async function postData(Paddress, data) {
    const response = await fetch(
        Paddress,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            "Authorization":"Bearer " + jwtToken.value
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
    const response = await fetch(Paddress,data)
    apiData.value = await response.json()
    return apiData.value
  }

  onMounted(async () => {
    jwtToken.value = localStorage.getItem("jwtToken")
    if(jwtToken.value){
      haventJWT.value = false
      name.value = localStorage.getItem("username")
      await goIn()
    }
  })

  async function goIn(){
    await getData("http://" + address + "/api/v1/auth/ws-token", {method:"POST", headers:{"Authorization":"Bearer " + jwtToken.value}}).then((data) => {
      connect(data.data)
    })

    await getData("http://" + address + "/api/v1/example", {method:"GET", headers:{"Authorization":"Bearer " + jwtToken.value}}).then((data) => {
      data.data.forEach((item, index) => {
        messages.value.push({ id: item.id, text: item.textMessage, owner: item.owner})
      })
    })
  }

  onUnmounted(() => {
    if(socket.value)socket.value.close()
  })

  const connect = (token) => {
    socket.value = new SockJS("http://" + address + "/ws/chat?token=" + token)

    socket.value.onopen = () => {
      connectionStatus.value = "connected"
      console.log("Successfully connected")
    }

    socket.value.onmessage = (e) => {
      console.log("onmessage" + e.data)
      let dat = JSON.parse(e.data)
      if(dat.owner != name.value){
        messages.value.push({ id: id++, text: dat.textMessage, owner: dat.owner})
      }else{
        postData("http://" + address + "/api/v1/example", {textMessage:dat.textMessage, owner:dat.owner})
      }
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
  const LoginClass = ref('unselected')
  const RegClass = ref('selected')

  const haveError = ref('noError')

  function change(auth){
    isAuth.value=auth
    if(auth){
      passNot.value = false
      errCheck()
      LoginClass.value="selected"
      RegClass.value="unselected"
    }else{
      passCh.value = false
      passCheck()
      LoginClass.value="unselected"
      RegClass.value="selected"
    }
  }

  const userLogin = ref('')
  const userPassword = ref('')
  const userPasswordConfirmed = ref('')
  function passCheck(){
    passNot.value = !(userPassword.value==userPasswordConfirmed.value)
    errCheck()
  }
  function errCheck(){
    if(passNot.value || passCh.value)haveError.value = "error"
    else haveError.value = "noError"
  }
  function reg(){
    console.log("Регистрация пользователя:" + userLogin.value + " с паролем:" + userPassword.value + "/" + userPasswordConfirmed.value)
    passCheck()
    if(passNot.value)return
    console.log("Отправка запроса на сервер")
    regLog("http://" + address + "/api/v1/auth/register",
        {
          "username": userLogin.value,
          "passwordHash": SHA256(userPassword.value).toString(),
          "publicKey": "public",
          "encryptedPrivateKey": "private"
        }).then((regData) => {
      console.log("resp:", regData, regData.code == 200)
      if(regData.code === 200 || regData.code === 201){
        haventJWT.value = false
        localStorage.setItem("jwtToken", regData.data)
        localStorage.setItem("username", userLogin.value)
        goIn()
      }else{
        console.log("Ошибка запроса:", regData)
      }
    })
  }
  function login(){
    console.log("Вход в пользователя:" + userLogin.value + " с паролем:" + userPassword.value)
    console.log("Хеш:", SHA256(userPassword.value).toString())
    regLog("http://" + address + "/api/v1/auth/login",
        {
          "username": userLogin.value,
          "passwordHash": SHA256(userPassword.value).toString()
        }).then((regData) => {
      console.log("resp:", regData, regData.code == 200)
      if(regData.code === 200 || regData.code === 201){
        haventJWT.value = false
        localStorage.setItem("jwtToken", regData.data)
        localStorage.setItem("username", userLogin.value)
        name.value = userLogin.value
        goIn()
        passCh.value = false
        errCheck()
      }else{
        console.log("Ошибка запроса:", regData)
        if(regData.code === 401){
          passCh.value = true
          errCheck()
        }
      }
    })
  }
</script>

<template>
    <div v-if="haventJWT" id="autorizationForm">
      <div id="changeAutorization">
        <button :class="LoginClass" style="border-radius: 15px 0px 0px 0px" @click="change(true)">Вход</button>
        <button :class="RegClass" style="border-radius: 0px 15px 0px 0px" @click="change(false)">Регистрация</button>
      </div>
      <form :class="haveError" id="login" v-if=isAuth @submit.prevent="login">
        <input v-model="userLogin" required placeholder="Логин">
        <input type="password" v-model="userPassword" required placeholder="Пароль">
        <button class="inBut">Войти</button>
      </form>
      <form :class="haveError" id="reg" v-else @submit.prevent="reg">
        <input v-model="userLogin" required placeholder="Логин">
        <input type="password" v-model="userPassword" required placeholder="Пароль">
        <input @input="passCheck" type="password" v-model="userPasswordConfirmed" required placeholder="Подтвердите пароль">
        <button class="inBut">Зарегестрироваться</button>
      </form>
      <p id="passCheck" v-if="passNot">Пароли не совпадают</p>
      <p id="logCheck" v-if="passCh">Неправильный логин или пароль</p>
    </div>
    <ul class="messages">
      <li v-for="message in messages" :key="message.id">{{message.owner}}: {{message.text}}</li>
    </ul>
    <br>
    <form @submit.prevent="sendMessage">
      <input v-model="messageText" required placeholder="Сообщение">
      <button>=></button>
    </form>
</template>

<style>
  #autorizationForm{
    border-radius: 15px;
    background-color:rgb(51,51,51);
    width:25vw;
    height:30vh;
    position:absolute;
    top:50%;
    left:50%;
    transform: translate(-50%, -50%);
  }
  #autorizationForm button{
    border:0px;
    padding:0px;
    color:white;
    font-weight: bold;
    font-size: 100%;
    font-family: "Arial";
  }
  .inBut{
    background-color:rgb(35,35,35);
    border-radius: 15px;
  }
  #changeAutorization{
    height: 20%;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: 1fr;
    text-align: center;
  }

  #login{
    //background-color:green;
    width:80%;
    height:70%;
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: repeat(3, 1fr);
    grid-row-gap: 6%;
    position:relative;
    left:50%;
    transform: translate(-50%);
    text-align: center;
  }

  .error{
    top:10%;
  }

  .noError{
    top:5%;
  }

  #reg{
    //background-color:green;
    width:80%;
    height:70%;
    display: grid;
    grid-template-columns: 1fr;
    grid-template-rows: repeat(4, 1fr);
    grid-row-gap: 3%;
    position:relative;
    left:50%;
    transform: translate(-50%);
    text-align: center;
  }

  #autorizationForm input{
    background: rgb(35,35,35);
    color:white;
    font-weight: bold;
    font-size:100%;
    width:100%;
    border-radius: 15px;
    font-family: "Arial";

    text-align: center;
    border:1px solid rgb(127,127,127);
    padding:0px;
  }
  #passCheck{
    position:absolute;
    top:21%;
    left:50%;
    transform: translate(-50%);
    text-align: center;
    font-size: 10px;
    color:red;
  }
  #logCheck{
    position:absolute;
    top:21%;
    left:50%;
    transform: translate(-50%);
    text-align: center;
    font-size: 10px;
    width:80%;
    color:red;
  }
  .unselected {
    background: rgb(35,35,35);
  }
  .selected {
    background-color:rgb(51,51,51);
  }
  .messages {
    list-style: none;
    padding:0px;
  }
</style>