import {ref} from 'vue';
import {useApi} from './useApi';

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

    return {getUsersByChatId, getUserKeysByChatId};
}