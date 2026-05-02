import {ref} from 'vue';
import {useApi} from './useApi';
import {useAuth} from "@/composables/useAuth.js";

const users = ref([]);

export function useUsers() {
    const {get, post} = useApi();

    const getUsersByChatId = async (chatId) => {
        const {data} = await get(`/api/v1/chat/${chatId}/users`);
        users.value = data;
        users.value.sort((a, b) => a.id - b.id);
        return users.value;
    }

    const getUserKeysByChatId = async (chatId) => {
        const data = await getUsersByChatId(chatId);
        const keys = await post(`/api/v1/user/keys`, {"usernames": data});
        return keys.data
    }

    const loadUserProfilePicture = async (image) => {
        const response = await post('/api/v1/s3/upload-urls', [image.name]);
        const {url, key} = response.data[0];

        const uploadResponse = await fetch(url, {
            method: 'PUT',
            body: await image.arrayBuffer(),
            headers: {'Content-Type': 'application/octet-stream'}
        });
        if (!uploadResponse.ok) {
            throw new Error(`Upload failed: ${uploadResponse.status}`);
        }
        const profileImage = await post(`/api/v1/user/pictures`, key);
        if (!profileImage) {
            throw new Error(`Load profile image failed: ${profileImage.status}`);
        }
    }

    return {getUsersByChatId, getUserKeysByChatId, loadUserProfilePicture};
}