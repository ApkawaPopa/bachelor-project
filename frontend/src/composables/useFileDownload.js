import {useApi} from './useApi';
import {useCrypto} from './useCrypto';

export function useFileDownload() {
    const {post} = useApi();
    const {decryptFile} = useCrypto();

    /**
     * Скачивает и расшифровывает файл по его ключу.
     * @param {number} fileKey - идентификатор объекта S3
     * @param {CryptoKey} symmetricKey - симметричный ключ чата
     * @returns {Promise<Blob>} - расшифрованный файл в виде Blob
     */
    const downloadAndDecrypt = async (fileKey, symmetricKey) => {
        const response = await post('/api/v1/s3/download-urls', [fileKey]);
        const url = response.data[0];
        const fetchResponse = await fetch(url);
        if (!fetchResponse.ok) {
            throw new Error(`Download failed: ${fetchResponse.status}`);
        }
        const encryptedBuffer = await fetchResponse.arrayBuffer();
        const decryptedBuffer = await decryptFile(encryptedBuffer, symmetricKey);
        return new Blob([decryptedBuffer], {type: 'application/octet-stream'});
    };

    const download = async (url) => {
        const fetchResponse = await fetch(url);
        if (!fetchResponse.ok) {
            throw new Error(`Download failed: ${fetchResponse.status}`);
        }
        const buf = await fetchResponse.arrayBuffer();
        const urlBlob = URL.createObjectURL(new Blob([buf], {type: 'application/octet-stream'}));
        return urlBlob;
    }

    return {downloadAndDecrypt, download};
}