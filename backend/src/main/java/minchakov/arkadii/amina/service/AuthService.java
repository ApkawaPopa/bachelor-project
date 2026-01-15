package minchakov.arkadii.amina.service;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.OutAuthDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.WebSocketToken;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.repository.WebSocketTokenRepository;
import minchakov.arkadii.amina.util.JWTUtil;
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
    private final JWTUtil jwtUtil;
    private final WebSocketTokenRepository webSocketTokenRepository;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JWTUtil jwtUtil,
        WebSocketTokenRepository webSocketTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.webSocketTokenRepository = webSocketTokenRepository;
    }

    public OutAuthDTO register(@Valid RegisterDTO registerDTO) {
        var userToRegister = new User(
            registerDTO.username(),
            registerDTO.passwordHash(), registerDTO.publicKey(), registerDTO.encryptedPrivateKey()
        );

        var hashedPassword = userToRegister.getPasswordHash();
        var hashedPasswordHash = passwordEncoder.encode(hashedPassword);
        userToRegister.setPasswordHash(hashedPasswordHash);
        var savedUser = userRepository.save(userToRegister);

        return new OutAuthDTO(
            jwtUtil.createJwt(savedUser),
            savedUser.getUsername(),
            savedUser.getPublicKey(),
            savedUser.getEncryptedPrivateKey()
        );
    }

    public OutAuthDTO login(LoginDTO loginDTO) {
        var foundUserOpt = userRepository.findByUsername(loginDTO.username());
        if (foundUserOpt.isPresent()) {
            var hashedOncePassword = loginDTO.passwordHash();
            var foundUser = foundUserOpt.get();
            if (passwordEncoder.matches(hashedOncePassword, foundUser.getPasswordHash())) {
                return new OutAuthDTO(
                    jwtUtil.createJwt(foundUser),
                    foundUser.getUsername(),
                    foundUser.getPublicKey(),
                    foundUser.getEncryptedPrivateKey()
                );
            }
        }
        throw new BadCredentialsException("Wrong login or password");
    }

    public String createWebSocketToken(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new InternalServerErrorException("Current user is invalid while creating websocket token");
        }

        var tokenUUID = UUID.randomUUID();
        webSocketTokenRepository.deleteByUserId(currentUser.getId());
        webSocketTokenRepository.save(new WebSocketToken(tokenUUID, currentUser));
        return tokenUUID.toString();
    }
}
