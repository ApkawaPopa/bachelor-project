package minchakov.arkadii.amina;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jWTUtil;
    private final UserRepository userRepository;
    private final List<String> TOKEN_GET_PATHS = List.of("/api/v1/auth/login", "/api/v1/auth/register");

    public JWTFilter(JWTUtil jWTUtil, UserRepository userRepository) {
        this.jWTUtil = jWTUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        System.out.println("Зашли в фильтр JWT");

        if (TOKEN_GET_PATHS.stream().anyMatch(path -> path.startsWith(request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Выполняем его");

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                var decodedJwtToken = jWTUtil.verifyJwtToken(jwtToken);

                var username = decodedJwtToken.getClaim("username").asString();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        response.sendError(500, "User not found with verified JWT Token");
                        return;
                    }

                    var user = userOpt.get();

                    var authenticationToken = new CustomAuthenticationToken(user);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }

                filterChain.doFilter(request, response);
                return;
            } catch (JWTVerificationException e) {
                System.out.println("Невалидный токен найден");
                response.sendError(401, "Invalid JWT Token");
                return;
            }
        }

        System.out.println("Нет токена или неправильный формат заголовка");
        response.sendError(401, "No 'Authorization' header found in proper format");
    }
}
