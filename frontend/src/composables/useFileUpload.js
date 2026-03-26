import {useApi} from './useApi';
import {useCrypto} from './useCrypto';

export function useFileUpload() {
    const {post} = useApi();
    const {encryptMessageContent, encryptFile} = useCrypto();

    /**
     * Загружает файл на S3, шифруя содержимое и имя файла симметричным ключом чата.
     * @param {File} file - файл для загрузки
     * @param {CryptoKey} symmetricKey - симметричный ключ чата
     * @returns {Promise<{key: number, encryptedName: string}>} - ключ объекта S3 и зашифрованное имя
     */
    const uploadFile = async (file, symmetricKey) => {
        // 1. Шифрование имени файла
        const encryptedFilename = await encryptMessageContent(file.name, symmetricKey);

        // 2. Получение подписанного URL на загрузку (передаём зашифрованное имя)
        const response = await post('/api/v1/s3/upload-urls', [encryptedFilename]);
        const {url, key} = response.data[0];

        // 3. Шифрование содержимого файла
        const fileBuffer = await file.arrayBuffer();
        const encryptedBuffer = await encryptFile(fileBuffer, symmetricKey);

        // 4. Загрузка на S3
        const uploadResponse = await fetch(url, {
            method: 'PUT',
            body: encryptedBuffer,
            headers: {'Content-Type': 'application/octet-stream'}
        });
        if (!uploadResponse.ok) {
            throw new Error(`Upload failed: ${uploadResponse.status}`);
        }

        return {key, encryptedName: encryptedFilename};
    };

    return {uploadFile};
}