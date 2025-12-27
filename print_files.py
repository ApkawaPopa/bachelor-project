import os

is_needed = {
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\AminaBackendApplication.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\ApplicationConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\SecurityConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\config\\WebSocketConfig.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\AuthController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\ChatController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\ExampleController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\MessageController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\MessageWebSocketHandler.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\UserController.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\controller\\advice\\GlobalControllerAdvice.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ApiResponse.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ApiResponseBody.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ChatCreateDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\LoginDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\MessageCreateDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ReadChatDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\ReadChatMessageDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\dto\\RegisterDTO.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\exception\\DtoValidationException.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\filter\\JWTFilter.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\interceptor\\WebSocketHandshakeInterceptor.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Chat.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Example.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\Message.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\User.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\model\\WebSocketToken.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\ChatRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\ExampleRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\MessageRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\UserRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\repository\\WebSocketTokenRepository.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\util\\JwtAuthenticationToken.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\util\\JWTUtil.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\java\\minchakov\\arkadii\\amina\\validator\\ChatCreateDTOValidator.java': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\application.properties': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\custom.properties': 1,
    'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\db-init.sql': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\requests.http': 1,
    # 'C:\\Users\\ark13\\MPU\\Диплом\\amina\\backend\\src\\main\\resources\\static\\index.html': 1,
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
                    content = file.read()
                print(f'{os.path.join(root, file_name)[len(directory) + 1:]}:\n```')
                print(content)
                print('```\n')
            except Exception as e:
                print(f'Error reading {file_path}: {e}')


if __name__ == '__main__':
    print_files_content(
        directory=r'C:\Users\ark13\MPU\Диплом\amina\backend\src'
    )
