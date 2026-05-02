import {ref} from 'vue';
import {useCrypto} from './useCrypto';
import {useStorage} from './useStorage';
import {useApi} from './useApi';

const isAuthenticated = ref(false);
const username = ref('');
const jwtToken = ref('');
const privateKey = ref(null);

export function useAuth() {
    const {
        sha256, generateRSAKeyPair, exportPublicKeyAsJWK, exportPrivateKeyAsPKCS8,
        encryptPrivateKeyWithPassword, decryptPrivateKeyWithPassword
    } = useCrypto();
    const {saveAuthData, loadAuthData, clearAuth} = useStorage();
    const {post} = useApi();

    const restoreSession = async () => {
        const data = loadAuthData();
        if (data.jwt && data.encryptedPrivateKey && data.password) {
            try {
                privateKey.value = await decryptPrivateKeyWithPassword(data.encryptedPrivateKey, data.password);
                username.value = data.username;
                jwtToken.value = data.jwt;
                isAuthenticated.value = true;
                return true;
            } catch (e) {
                console.error('Failed to restore session', e);
                clearAuth();
                return false;
            }
        }
        return false;
    };

    const login = async (loginUsername, password) => {
        const passwordHash = await sha256(password);
        const response = await post('/api/v1/auth/login', {
            username: loginUsername,
            passwordHash,
        });
        if (response.code === 200 || response.code === 201) {
            const {jwt, publicKey, encryptedPrivateKey} = response.data;
            privateKey.value = await decryptPrivateKeyWithPassword(encryptedPrivateKey, password);
            username.value = loginUsername;
            jwtToken.value = jwt;
            isAuthenticated.value = true;
            saveAuthData({
                jwt,
                username: loginUsername,
                publicKey,
                encryptedPrivateKey,
                password,
            });
            return {success: true};
        }
        return {success: false, error: response};
    };

    const register = async (loginUsername, password) => {
        const passwordHash = await sha256(password);
        const keyPair = await generateRSAKeyPair();
        const publicKeyJwk = await exportPublicKeyAsJWK(keyPair.publicKey);
        const privateKeyPkcs8 = await exportPrivateKeyAsPKCS8(keyPair.privateKey);
        const encryptedPrivateKey = await encryptPrivateKeyWithPassword(privateKeyPkcs8, password);
        const response = await post('/api/v1/auth/register', {
            username: loginUsername,
            passwordHash,
            publicKey: JSON.stringify(publicKeyJwk),
            encryptedPrivateKey,
        });
        if (response.code === 200 || response.code === 201) {
            const {jwt} = response.data;
            privateKey.value = keyPair.privateKey;
            username.value = loginUsername;
            jwtToken.value = jwt;
            isAuthenticated.value = true;
            saveAuthData({
                jwt,
                username: loginUsername,
                publicKey: JSON.stringify(publicKeyJwk),
                encryptedPrivateKey,
                password,
            });
            return {success: true};
        }
        return {success: false, error: response};
    };

    const logout = () => {
        console.log('Logged out!');
        clearAuth();
        isAuthenticated.value = false;
        username.value = '';
        jwtToken.value = '';
        privateKey.value = null;
    };

    return {
        isAuthenticated,
        username,
        jwtToken,
        privateKey,
        restoreSession,
        login,
        register,
        logout,
    };
}