import {ref} from 'vue';
import {useApi} from './useApi';

export function useUsers(){
    const {get} = useApi();

    const getUsersByChatId = async (chatId) => {
        const {data} = await get(`/api/v1/chat/${chatId}/users`);
        return data;
    }

    return {getUsersByChatId};
}