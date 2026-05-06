import os

is_needed = {
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\AminaBackendApplication.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\config\ApplicationConfig.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\config\S3Config.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\config\SecurityConfig.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\config\StompConfig.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\AuthController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\ChatController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\ExampleController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\MessageController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\S3Controller.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\StompMessageController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\UserController.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\controller\advice\GlobalControllerAdvice.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\AddChatDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ChatCreateDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ChatCreateUserDetailsDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ChatCreationEvent.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ChatDeletionEvent.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ChatMessageEvent.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetMessageFileDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetMessageReceiverDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetSignedPutUrlsOutDto.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetUsersKeysInDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetUsersKeysOutDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\GetUsersKeysUsernameDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\InEditMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\InSendMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ListChatUsersUserDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ListUserChatsChatDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\LoginDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\MessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\OutAuthDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\OutEditMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\OutReceiveMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\OutSendMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\OutSendMessageFileDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ReadChatDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\ReadChatMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\RegisterDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\RestResponse.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\RestResponseBody.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\StompResponse.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\UnreadMessagesCountDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\dto\UpdateMessageDTO.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\exception\AppException.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\exception\BadRequestException.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\exception\InternalServerErrorException.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\exception\JwtAuthenticationException.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\exception\NotFoundException.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\filter\JWTFilter.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\interceptor\WebSocketHandshakeInterceptor.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\jobs\CleanUnconfirmedObjectsJob.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\Chat.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\Example.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\Message.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\MessageReceiver.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\MessageReceiverId.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\S3Object.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\User.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\UserChat.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\UserChatId.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\model\WebSocketToken.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\ChatRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\ExampleRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\MessageReceiverRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\MessageRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\S3ObjectRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\UserChatRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\UserRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\repository\WebSocketTokenRepository.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\AuthService.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\ChatService.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\CrudService.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\CrudServiceImpl.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\MessageService.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\S3Service.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\service\UserService.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\util\AppAuthenticationToken.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\util\ChatEventPublisher.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\util\JWTUtil.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\ChatCreateOwnUsernameConstraint.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\ChatCreateOwnUsernameValidator.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\ChatCreateUsernameConstraint.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\ChatCreateUsernameValidator.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\GetUsersKeysUsernameConstraint.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\GetUsersKeysUsernameValidator.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\MessageDTOValidator.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\RegisterDTOUsernameValidator.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\java\minchakov\arkadii\amina\validator\UsernameConstraint.java',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\application.properties',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\custom.properties',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\db-init.sql',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\http-client.env.json',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\keystore.p12',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\requests.http',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\minio\init-minio.sh',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\minio\minio.license',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\minio\private.key',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\minio\public.crt',
    # # r'C:\Users\ark13\MPU\Диплом\amina\backend\src\main\resources\templates\index.html',
    #
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\Dockerfile',
    # r'C:\Users\ark13\MPU\Диплом\amina\backend\pom.xml',
    #
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\App.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\main.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\assets\base.css',
    # # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\assets\logo.svg',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\assets\main.css',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\auth\AuthForm.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\chat\AddChatModal.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\chat\ChatList.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\chat\ChatMenuModal.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\chat\ChatWindow.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\profile\ProfileMenuModal.vue',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useApi.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useAuth.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useChat.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useCrypto.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useFileDownload.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useFileUpload.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useStorage.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useUsers.js',
    #
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\vite.config.js',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\package.json',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\.env',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\jsconfig.json',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\index.html',
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\Dockerfile',
    #
    # r'C:\Users\ark13\MPU\Диплом\amina\docker-compose.env',
    # r'C:\Users\ark13\MPU\Диплом\amina\docker-compose.yml',

    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\chat\ChatMenuModal.vue',
    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\profile\ProfileMenuModal.vue',
    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\profile\UserProfileModal.vue',
    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\composables\useChat.js',
    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\App.vue',
    r'C:\Users\ark13\MPU\Диплом\amina\frontend\src\components\common\ImageCarouselModal.vue',
}


def print_files_content(directory):
    for root, _, files in os.walk(directory):
        for file_name in files:
            if not (root + '\\' + file_name) in is_needed:
                continue
            file_path = os.path.join(root, file_name)
            try:
                with open(file_path, 'r', encoding='utf-8') as file:
                    content = file.readlines()
                print(f'{os.path.join(root, file_name)[len(directory) + 1:]}:')
                # print(content)
                print('```')
                for l in content:
                    print(l, end='')
                    # if not (l.isspace() or l.startswith('import ') or l.strip().startswith('//') or l.strip().startswith('#')):
                    #     print(l, end='')
                print('\n```\n')
                # print()
            except Exception as e:
                print(f'Error reading {file_path}: {e}')


if __name__ == '__main__':
    print_files_content(
        directory=r'C:\Users\ark13\MPU\Диплом\amina'
    )
