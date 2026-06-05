import {useStorage} from './useStorage';

export function useApi() {
    const {loadAuthData} = useStorage();

    const request = async (endpoint, options = {}) => {
        const token = loadAuthData()?.jwt;
        const response = await fetch(endpoint, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(token && {Authorization: `Bearer ${token}`}),
                ...options.headers,
            },
        });
        const data = await response.json();
        if (!response.ok) {
            throw {status: response.status, data};
        }
        return data;
    };

    const get = (endpoint) => request(endpoint, {method: 'GET'});
    const post = (endpoint, body) => request(endpoint, {method: 'POST', body: JSON.stringify(body)});
    const del = (endpoint) => request(endpoint, {method: 'DELETE'});

    return {get, post, del, request};
}