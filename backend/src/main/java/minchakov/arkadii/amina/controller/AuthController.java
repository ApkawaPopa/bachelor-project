package minchakov.arkadii.amina.controller;

import jakarta.transaction.Transactional;
import minchakov.arkadii.amina.JWTUtil;
import minchakov.arkadii.amina.dto.ApiResponseEntity;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Transactional
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    public AuthController(ModelMapper modelMapper, UserRepository userRepository, JWTUtil jwtUtil) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ApiResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        // TODO: Валидация
        var userToRegister = modelMapper.map(registerDTO, User.class);
        var savedUser = userRepository.save(userToRegister);
        var jwtToken = jwtUtil.createJwtToken(savedUser);
        return new ApiResponseEntity<>(200, "Created", jwtToken);
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
}
