// src/composables/useCrypto.js
export function useCrypto() {
    // ---- Утилиты преобразования ----
    const arrayBufferToBase64 = (buffer) => {
        let binary = '';
        const bytes = new Uint8Array(buffer);
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    };

    const base64ToArrayBuffer = (base64) => {
        const binaryString = atob(base64);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes.buffer;
    };

    // ---- Хеширование ----
    const sha256 = async (message) => {
        const msgUint8 = new TextEncoder().encode(message);
        const hashBuffer = await crypto.subtle.digest('SHA-256', msgUint8);
        return Array.from(new Uint8Array(hashBuffer))
            .map(b => b.toString(16).padStart(2, '0'))
            .join('');
    };

    // ---- Генерация ключей ----
    const generateRSAKeyPair = async () => {
        return await crypto.subtle.generateKey(
            {
                name: 'RSA-OAEP',
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256',
            },
            true,
            ['encrypt', 'decrypt']
        );
    };

    const generateAESKey = async () => {
        return await crypto.subtle.generateKey(
            {name: 'AES-GCM', length: 256},
            true,
            ['encrypt', 'decrypt']
        );
    };

    // ---- Экспорт/импорт ключей ----
    const exportPublicKeyAsJWK = async (publicKey) => {
        return await crypto.subtle.exportKey('jwk', publicKey);
    };

    const exportPrivateKeyAsPKCS8 = async (privateKey) => {
        return await crypto.subtle.exportKey('pkcs8', privateKey);
    };

    const importPublicKeyFromJWK = async (jwk) => {
        return await crypto.subtle.importKey(
            'jwk',
            jwk,
            {name: 'RSA-OAEP', hash: 'SHA-256'},
            true,
            ['encrypt']
        );
    };

    const importPrivateKeyFromPKCS8 = async (pkcs8) => {
        return await crypto.subtle.importKey(
            'pkcs8',
            pkcs8,
            {name: 'RSA-OAEP', hash: 'SHA-256'},
            true,
            ['decrypt']
        );
    };

    const importSymmetricKey = async (rawKey) => {
        return await crypto.subtle.importKey(
            'raw',
            rawKey,
            {name: 'AES-GCM', length: 256},
            true,
            ['encrypt', 'decrypt']
        );
    };

    // ---- Шифрование/дешифрование RSA ----
    const rsaEncrypt = async (publicKey, data) => {
        return await crypto.subtle.encrypt({name: 'RSA-OAEP'}, publicKey, data);
    };

    const rsaDecrypt = async (privateKey, data) => {
        return await crypto.subtle.decrypt({name: 'RSA-OAEP'}, privateKey, data);
    };

    // ---- Шифрование/дешифрование AES-GCM ----
    const aesEncrypt = async (key, iv, data) => {
        return await crypto.subtle.encrypt({name: 'AES-GCM', iv}, key, data);
    };

    const aesDecrypt = async (key, iv, data) => {
        return await crypto.subtle.decrypt({name: 'AES-GCM', iv}, key, data);
    };

    // ---- Функции для работы с зашифрованным приватным ключом ----
    const deriveAESKeyFromPassword = async (password, salt, iterations = 100000) => {
        const keyMaterial = await crypto.subtle.importKey(
            'raw',
            new TextEncoder().encode(password),
            'PBKDF2',
            false,
            ['deriveKey']
        );
        return await crypto.subtle.deriveKey(
            {
                name: 'PBKDF2',
                salt,
                iterations,
                hash: 'SHA-256',
            },
            keyMaterial,
            {name: 'AES-GCM', length: 256},
            true,
            ['encrypt', 'decrypt']
        );
    };

    const encryptPrivateKeyWithPassword = async (privateKeyPkcs8, password) => {
        const salt = crypto.getRandomValues(new Uint8Array(16));
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const aesKey = await deriveAESKeyFromPassword(password, salt);
        const encryptedPrivate = await aesEncrypt(aesKey, iv, privateKeyPkcs8);

        const version = new Uint8Array([0x01]);
        const result = new Uint8Array(
            version.length + salt.length + iv.length + encryptedPrivate.byteLength
        );
        let offset = 0;
        result.set(version, offset);
        offset += version.length;
        result.set(salt, offset);
        offset += salt.length;
        result.set(iv, offset);
        offset += iv.length;
        result.set(new Uint8Array(encryptedPrivate), offset);
        return arrayBufferToBase64(result);
    };

    const decryptPrivateKeyWithPassword = async (encryptedPrivateBase64, password) => {
        const blob = new Uint8Array(base64ToArrayBuffer(encryptedPrivateBase64));
        const version = blob[0]; // можно проверить
        const salt = blob.slice(1, 17);
        const iv = blob.slice(17, 29);
        const encryptedPrivate = blob.slice(29);
        const aesKey = await deriveAESKeyFromPassword(password, salt);
        const decrypted = await aesDecrypt(aesKey, iv, encryptedPrivate);
        return await importPrivateKeyFromPKCS8(decrypted);
    };

    // ---- Вспомогательная функция для дешифровки сообщения ----
    const decryptMessageContent = async (encryptedContentBase64, symmetricKey) => {
        const blob = base64ToArrayBuffer(encryptedContentBase64);
        const iv = new Uint8Array(blob.slice(1, 13));
        const content = blob.slice(13);
        const decrypted = await aesDecrypt(symmetricKey, iv, content);
        return new TextDecoder().decode(decrypted);
    };

    const encryptMessageContent = async (plainText, symmetricKey) => {
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const version = new Uint8Array([0x01]);
        const content = new TextEncoder().encode(plainText);
        const encrypted = await aesEncrypt(symmetricKey, iv, content);
        const result = new Uint8Array(version.length + iv.length + encrypted.byteLength);
        let offset = 0;
        result.set(version, offset);
        offset += version.length;
        result.set(iv, offset);
        offset += iv.length;
        result.set(new Uint8Array(encrypted), offset);
        return arrayBufferToBase64(result);
    };

    return {
        arrayBufferToBase64,
        base64ToArrayBuffer,
        sha256,
        generateRSAKeyPair,
        generateAESKey,
        exportPublicKeyAsJWK,
        exportPrivateKeyAsPKCS8,
        importPublicKeyFromJWK,
        importPrivateKeyFromPKCS8,
        importSymmetricKey,
        rsaEncrypt,
        rsaDecrypt,
        aesEncrypt,
        aesDecrypt,
        deriveAESKeyFromPassword,
        encryptPrivateKeyWithPassword,
        decryptPrivateKeyWithPassword,
        decryptMessageContent,
        encryptMessageContent,
    };
}