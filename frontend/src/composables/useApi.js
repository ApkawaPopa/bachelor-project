// src/composables/useApi.js
import {useStorage} from './useStorage';

export function useApi() {
    const {loadAuthData} = useStorage();
    const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

    const request = async (endpoint, options = {}) => {
        const token = loadAuthData()?.jwt;
        const url = `https://${API_BASE_URL}${endpoint}`;
        const response = await fetch(url, {
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
    // можно добавить put, delete и т.д.

    return {get, post, request};
}