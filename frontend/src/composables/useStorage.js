export function useStorage() {
    const getItem = (key) => localStorage.getItem(key);
    const setItem = (key, value) => localStorage.setItem(key, value);
    const removeItem = (key) => localStorage.removeItem(key);

    const saveAuthData = ({jwt, username, publicKey, encryptedPrivateKey, password}) => {
        setItem('jwtToken', jwt);
        setItem('username', username);
        setItem('public', publicKey);
        setItem('private', encryptedPrivateKey);
        setItem('password', password); // Пароль хранить небезопасно, лучше запрашивать при старте
    };

    const loadAuthData = () => ({
        jwt: getItem('jwtToken'),
        username: getItem('username'),
        publicKey: getItem('public'),
        encryptedPrivateKey: getItem('private'),
        password: getItem('password'),
    });

    const clearAuth = () => {
        removeItem('jwtToken');
        removeItem('username');
        removeItem('public');
        removeItem('private');
        removeItem('password');
    };

    return {
        getItem,
        setItem,
        removeItem,
        saveAuthData,
        loadAuthData,
        clearAuth,
    };
}