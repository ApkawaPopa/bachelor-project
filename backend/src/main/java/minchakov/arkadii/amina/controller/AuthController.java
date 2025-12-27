package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.exception.DtoValidationException;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.WebSocketToken;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.repository.WebSocketTokenRepository;
import minchakov.arkadii.amina.util.JWTUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
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
    private final PasswordEncoder passwordEncoder;

    public AuthController(
        ModelMapper modelMapper,
        UserRepository userRepository,
        JWTUtil jwtUtil,
        WebSocketTokenRepository webSocketTokenRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.webSocketTokenRepository = webSocketTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid RegisterDTO registerDTO, BindingResult result) {
        if (result.hasErrors()) {
            throw new DtoValidationException(result);
        }

        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            return new ApiResponse<>(400, "Username is already taken", null);
        }

        var hashedPassword = registerDTO.getPasswordHash();
        var hashedPasswordHash = passwordEncoder.encode(hashedPassword);
        registerDTO.setPasswordHash(hashedPasswordHash);

        var userToRegister = modelMapper.map(registerDTO, User.class);
        var savedUser = userRepository.save(userToRegister);

        var jwtToken = jwtUtil.createJwtToken(savedUser);

        return new ApiResponse<>(201, "Created", jwtToken);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginDTO loginDTO) {
        var foundUserOpt = userRepository.findByUsername(loginDTO.getUsername());
        if (foundUserOpt.isPresent()) {
            var hashedOncePassword = loginDTO.getPasswordHash();

            var foundUser = foundUserOpt.get();
            if (passwordEncoder.matches(hashedOncePassword, foundUser.getPasswordHash())) {
                var jwtToken = jwtUtil.createJwtToken(foundUser);
                return new ApiResponse<>(200, "Success", jwtToken);
            }
        }

        return new ApiResponse<>(401, "Wrong login or password", null);
    }

    @PostMapping("/ws-token")
    public ApiResponse<String> getWebSocketToken(@AuthenticationPrincipal User currentUser) {
        var tokenUUID = UUID.randomUUID();

        try {
            webSocketTokenRepository.deleteByUserId((currentUser.getId()));
            webSocketTokenRepository.save(new WebSocketToken(tokenUUID, currentUser));

            return new ApiResponse<>(201, "Created", tokenUUID.toString());
        } catch (NullPointerException e) {
            return new ApiResponse<>(500, "Error while trying to get user from security session", null);
        }
    }
}
