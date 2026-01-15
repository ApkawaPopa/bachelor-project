package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.LoginDTO;
import minchakov.arkadii.amina.dto.OutAuthDTO;
import minchakov.arkadii.amina.dto.RegisterDTO;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public RestResponse<OutAuthDTO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        var response = authService.register(registerDTO);
        return RestResponse.success(response);
    }

    @PostMapping("/login")
    public RestResponse<OutAuthDTO> login(@RequestBody LoginDTO loginDTO) {
        var response = authService.login(loginDTO);
        return RestResponse.success(response);
    }

    @PostMapping("/ws-token")
    public RestResponse<String> getWebSocketToken(@AuthenticationPrincipal User currentUser) {
        var wsToken = authService.createWebSocketToken(currentUser);
        return RestResponse.created(wsToken);
    }
}
