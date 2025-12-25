package minchakov.arkadii.amina.controller;

import jakarta.transaction.Transactional;
import minchakov.arkadii.amina.JWTUtil;
import minchakov.arkadii.amina.dto.ApiResponseEntity;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.WebSocketToken;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.repository.WebSocketTokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@Transactional
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final WebSocketTokenRepository webSocketTokenRepository;

    public AuthController(
        ModelMapper modelMapper,
        UserRepository userRepository,
        JWTUtil jwtUtil,
        WebSocketTokenRepository webSocketTokenRepository
    ) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webSocketTokenRepository = webSocketTokenRepository;
    }

    @PostMapping("/register")
    public ApiResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        // TODO: Валидация
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            return new ApiResponseEntity<>(400, "Username is already taken", null);
        }
        var userToRegister = modelMapper.map(registerDTO, User.class);
        var savedUser = userRepository.save(userToRegister);
        var jwtToken = jwtUtil.createJwtToken(savedUser);
        return new ApiResponseEntity<>(201, "Created", jwtToken);
    }

    @PostMapping("/login")
    public ApiResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        var foundUserOpt = userRepository.findByUsername(loginDTO.getUsername());
        if (foundUserOpt.isPresent()) {
            var foundUser = foundUserOpt.get();
            if (foundUser.getPasswordHash().equals(loginDTO.getPasswordHash())) {
                var jwtToken = jwtUtil.createJwtToken(foundUser);
                return new ApiResponseEntity<>(200, "Success", jwtToken);
            }
        }
        return new ApiResponseEntity<>(401, "Wrong login or password", null);
    }

    @PostMapping("/ws-token")
    public ApiResponseEntity<String> getWebSocketToken() {
        var tokenUUID = UUID.randomUUID();
        try {
            System.out.println(SecurityContextHolder.getContext());
            var tokenUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("Удаляем бай юзер:" + tokenUser.getId());
            webSocketTokenRepository.deleteByUserId((tokenUser.getId()));
            System.out.println("Делаем новый токен");
            webSocketTokenRepository.save(new WebSocketToken(tokenUUID, tokenUser));
            return new ApiResponseEntity<>(201, "Created", tokenUUID.toString());
        } catch (ClassCastException | NullPointerException e) {
            return new ApiResponseEntity<>(500, "Error while trying to get user from security session", null);
        }
    }
}
