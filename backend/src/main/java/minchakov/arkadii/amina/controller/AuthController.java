package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.exception.DtoValidationException;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid RegisterDTO registerDTO, BindingResult errors) {
        if (errors.hasErrors()) {
            throw new DtoValidationException(errors);
        }

        var jwt = authService.register(registerDTO);
        return new ApiResponse<>(201, "Created", jwt);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginDTO loginDTO) {
        var jwt = authService.login(loginDTO);
        return new ApiResponse<>(200, "Success", jwt);
    }

    @PostMapping("/ws-token")
    public ApiResponse<String> getWebSocketToken(@AuthenticationPrincipal User currentUser) {
        var wsToken = authService.createWebSocketToken(currentUser);
        return new ApiResponse<>(201, "Created", wsToken);
    }
}
