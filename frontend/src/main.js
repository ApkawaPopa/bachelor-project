import './assets/theme.css'

import {createApp} from 'vue'
import App from './App.vue'

if (typeof window !== 'undefined') {
    window.global = window
}

if (localStorage.getItem('prototype-mode') === 'true') {
    document.body.classList.add('prototype-mode')
}

createApp(App).mount('#app')