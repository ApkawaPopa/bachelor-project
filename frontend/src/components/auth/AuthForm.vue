<template>
  <div class="auth-form modal-content">
    <div class="auth-form__tabs">
      <button :class="{'btn--primary': isLoginMode}" class="btn" @click="setMode(true)">Вход</button>
      <button :class="{'btn--primary': !isLoginMode}" class="btn" @click="setMode(false)">Регистрация</button>
    </div>
    <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

    <form v-if="isLoginMode" class="auth-form__fields" @submit.prevent="handleLogin">
      <input v-model="loginForm.username" class="input" placeholder="Логин" required/>
      <input v-model="loginForm.password" class="input" placeholder="Пароль" required type="password"/>
      <button class="btn" type="submit">Войти</button>
    </form>

    <form v-else class="auth-form__fields" @submit.prevent="handleRegister">
      <input v-model="registerForm.username" class="input" placeholder="Логин" required/>
      <input v-model="registerForm.password" class="input" placeholder="Пароль" type="password"
             @input="checkPasswordsMatch"/>
      <input v-model="registerForm.passwordConfirm" class="input" placeholder="Подтвердите пароль" type="password"
             @input="checkPasswordsMatch"/>
      <button :disabled="!passwordsMatch" class="btn" type="submit">Зарегистрироваться</button>
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
.auth-form {
  width: 100%;
  max-width: 320px;
  text-align: center;
}

.auth-form__tabs {
  display: flex;
  gap: var(--space-2);
  justify-content: center;
  flex-wrap: wrap;
}

.auth-form__tabs .btn {
  flex: 1 0 0;
  white-space: nowrap;
  text-align: center;
}

.auth-form__fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.error-msg {
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}
</style>