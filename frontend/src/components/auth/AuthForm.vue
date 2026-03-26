<template>
  <div id="autorizationForm" :class="{ selected: !isLoginMode, unselected: isLoginMode }">
    <div id="changeAutorization">
      <button id="LoginButton" @click="setMode(true)">Вход</button>
      <button id="RegButton" @click="setMode(false)">Регистрация</button>
      <p v-if="errorMessage" id="error">{{ errorMessage }}</p>
    </div>

    <form v-if="isLoginMode" id="login" :class="errorClass" @submit.prevent="handleLogin">
      <input v-model="loginForm.username" placeholder="Логин" required/>
      <input v-model="loginForm.password" placeholder="Пароль" required type="password"/>
      <button class="inBut" type="submit">Войти</button>
    </form>

    <form v-else id="reg" :class="errorClass" @submit.prevent="handleRegister">
      <input v-model="registerForm.username" placeholder="Логин" required/>
      <input v-model="registerForm.password" placeholder="Пароль" required type="password"
             @input="checkPasswordsMatch"/>
      <input v-model="registerForm.passwordConfirm" placeholder="Подтвердите пароль" required type="password"
             @input="checkPasswordsMatch"/>
      <button :disabled="!passwordsMatch" class="inBut" type="submit">Зарегистрироваться</button>
    </form>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue';

const emit = defineEmits(['login', 'register']);

const isLoginMode = ref(true);
const errorMessage = ref('');

const loginForm = ref({username: '', password: ''});
const registerForm = ref({username: '', password: '', passwordConfirm: ''});
const passwordsMatch = ref(true);

const setMode = (mode) => {
  isLoginMode.value = mode;
  errorMessage.value = '';
  loginForm.value = {username: '', password: ''};
  registerForm.value = {username: '', password: '', passwordConfirm: ''};
  passwordsMatch.value = true;
};

const checkPasswordsMatch = () => {
  passwordsMatch.value = registerForm.value.password === registerForm.value.passwordConfirm;
  errorMessage.value = passwordsMatch.value ? '' : 'Пароли не совпадают';
};

const errorClass = computed(() => (errorMessage.value ? 'error' : 'noError'));

const handleLogin = () => {
  emit('login', loginForm.value.username, loginForm.value.password);
};

const handleRegister = () => {
  if (!passwordsMatch.value) return;
  emit('register', registerForm.value.username, registerForm.value.password);
};
</script>

<style scoped>
#autorizationForm {
  border: 1px solid white;
  width: calc(max(1vh, 1vw) * 25);
  height: calc(max(1vh, 1vw) * 20);
  margin-left: calc(50vw - (max(1vh, 1vw) * 12.5));
  margin-top: calc(50vh - (max(1vh, 1vw) * 10));
}

#changeAutorization {
  height: 20%;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: 1fr;
  text-align: center;
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

#login, #reg {
  margin: 0;
  width: 80%;
  padding-left: calc(max(1vh, 1vw) * 2.5);
  text-align: center;
}

#login {
  height: calc(max(1vh, 1vw) * 14.5);
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(3, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.5);
}

#reg {
  height: calc(max(1vh, 1vw) * 14.5);
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(4, 1fr);
  grid-row-gap: calc(max(1vh, 1vw) * 0.25);
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
  color: white;
}

#autorizationForm.unselected .inBut {
  background-color: white;
  color: black;
}

.inBut {
  font-weight: bold;
  font-size: 100%;
  font-family: "Arial";
  border: 0;
  padding: 0;
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