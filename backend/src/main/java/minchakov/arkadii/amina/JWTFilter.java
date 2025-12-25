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

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jWTUtil;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtil jWTUtil, UserRepository userRepository) {
        this.jWTUtil = jWTUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        System.out.println("Зашли в фильтр JWT: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                var decodedJwtToken = jWTUtil.verifyJwtToken(jwtToken);

                var username = decodedJwtToken.getClaim("username").asString();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("С валидным токеном нет юзера");
                        response.sendError(500, "User not found with verified JWT Token");
                    } else {
                        var user = userOpt.get();
                        var authenticationToken = new CustomAuthenticationToken(user);
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
                System.out.println("Успех");
            } catch (JWTVerificationException e) {
                System.out.println("Невалидный токен найден");
                response.sendError(401, "Invalid JWT Token");
                SecurityContextHolder.clearContext();
            }
        } else {
            SecurityContextHolder.clearContext();
        }

        System.out.println("Передача остальным фильтрам");
        filterChain.doFilter(request, response);
    }
}
