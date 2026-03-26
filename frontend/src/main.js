import './assets/main.css'

import {createApp} from 'vue'
import App from './App.vue'

if (typeof window !== 'undefined') {
    window.global = window
}

createApp(App).mount('#app')