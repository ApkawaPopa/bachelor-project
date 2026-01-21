import os

is_needed = {
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\.gitignore': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\index.html': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\jsconfig.json': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\localhost.crt': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\localhost.pfx': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\package.json': 1,
    # # r'C:\Users\ark13\MPU\Диплом\amina\frontend\package-lock.json': 1,
    # r'C:\Users\ark13\MPU\Диплом\amina\frontend\vite.config.js': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\frontend\\src\\App.vue': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\frontend\\src\\main.js': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\frontend\\src\\assets\\base.css': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\frontend\\src\\assets\\logo.svg': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\frontend\\src\\assets\\main.css': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\AminaBackendApplication.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\ApplicationConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\SecurityConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\StompConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\AuthController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\ChatController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\ExampleController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\MessageController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\StompMessageController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\UserController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\advice\\GlobalControllerAdvice.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\AddChatDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ChatCreateDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ChatCreateUserDetailsDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\GetMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\GetMessageReceiverDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\GetUsersKeysInDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\GetUsersKeysOutDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\GetUsersKeysUsernameDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\InSendMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ListChatUsersUserDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ListUserChatsChatDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\LoginDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\MessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\OutAuthDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\OutReceiveMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\OutSendMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ReadChatDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ReadChatMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\RegisterDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\RestResponse.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\RestResponseBody.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\StompResponse.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\UpdateMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\AppException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\BadRequestException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\InternalServerErrorException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\JwtAuthenticationException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\NotFoundException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\filter\\JWTFilter.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\interceptor\\WebSocketHandshakeInterceptor.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Chat.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Example.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Message.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\MessageReceiver.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\MessageReceiverId.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\User.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\UserChat.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\UserChatId.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\WebSocketToken.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\ChatRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\ExampleRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\MessageReceiverRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\MessageRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\UserChatRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\UserRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\WebSocketTokenRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\AuthService.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\ChatService.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\CrudService.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\CrudServiceImpl.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\MessageService.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\service\\UserService.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\util\\AppAuthenticationToken.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\util\\JWTUtil.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\ChatCreateOwnUsernameConstraint.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\ChatCreateOwnUsernameValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\ChatCreateUsernameConstraint.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\ChatCreateUsernameValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\GetUsersKeysUsernameConstraint.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\GetUsersKeysUsernameValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\MessageDTOValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\RegisterDTOUsernameValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\UsernameConstraint.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\application.properties': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\custom.properties': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\db-init.sql': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\http-client.env.json': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\keystore.p12': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\requests.http': 1,
    r'C:\Users\ark13\MPU\Диплом\amina\backend\Dockerfile': 1,
    r'C:\Users\ark13\MPU\Диплом\amina\backend\pom.xml': 1
}


def print_files_content(directory):
    for root, _, files in os.walk(directory):
        for file_name in files:
            if not is_needed.get(root + '\\' + file_name):
                continue
            file_path = os.path.join(root, file_name)
            try:
                with open(file_path, 'r', encoding='utf-8') as file:
                    content = file.readlines()
                print(f'{os.path.join(root, file_name)[len(directory) + 1:]}:')
                # print(content)
                for l in content:
                    if not (l.isspace() or l.startswith('import ')):
                        print(l, end='')
                print()
            except Exception as e:
                print(f'Error reading {file_path}: {e}')


if __name__ == '__main__':
    print_files_content(
        directory=r'C:\Users\ark13\MPU\Диплом\amina'
    )
