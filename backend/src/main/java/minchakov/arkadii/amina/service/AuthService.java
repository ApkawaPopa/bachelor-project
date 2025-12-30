package minchakov.arkadii.amina.service;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.WebSocketToken;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.repository.WebSocketTokenRepository;
import minchakov.arkadii.amina.util.JWTUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JWTUtil jwtUtil;
    private final WebSocketTokenRepository webSocketTokenRepository;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        ModelMapper modelMapper,
        JWTUtil jwtUtil,
        WebSocketTokenRepository webSocketTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtUtil = jwtUtil;
        this.webSocketTokenRepository = webSocketTokenRepository;
    }

    public String register(@Valid RegisterDTO registerDTO) {
        var userToRegister = modelMapper.map(registerDTO, User.class);

        var hashedPassword = userToRegister.getPasswordHash();
        var hashedPasswordHash = passwordEncoder.encode(hashedPassword);
        userToRegister.setPasswordHash(hashedPasswordHash);

        var savedUser = userRepository.save(userToRegister);

        return jwtUtil.createJwt(savedUser);
    }

    public String login(LoginDTO loginDTO) {
        var foundUserOpt = userRepository.findByUsername(loginDTO.username());

        if (foundUserOpt.isPresent()) {
            var hashedOncePassword = loginDTO.passwordHash();

            var foundUser = foundUserOpt.get();

            if (passwordEncoder.matches(hashedOncePassword, foundUser.getPasswordHash())) {
                return jwtUtil.createJwt(foundUser);
            }
        }

        throw new BadCredentialsException("Wrong login or password");
    }

    public String createWebSocketToken(User currentUser) {
        var tokenUUID = UUID.randomUUID();

        try {
            webSocketTokenRepository.deleteByUserId((currentUser.getId()));
            webSocketTokenRepository.save(new WebSocketToken(tokenUUID, currentUser));
            return tokenUUID.toString();
        } catch (NullPointerException e) {
            throw new RuntimeException("Error while creating websocket token");
        }
    }
}
