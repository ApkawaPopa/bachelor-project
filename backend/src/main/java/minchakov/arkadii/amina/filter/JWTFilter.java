package minchakov.arkadii.amina.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import minchakov.arkadii.amina.dto.ApiResponseBody;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.util.JWTUtil;
import minchakov.arkadii.amina.util.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jWTUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JWTFilter(JWTUtil jWTUtil, UserRepository userRepository, ObjectMapper objectMapper) {
        this.jWTUtil = jWTUtil;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        System.out.println("Зашли в фильтр JWT: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            try {
                var decodedJwtToken = jWTUtil.verifyJwt(jwtToken);

                var username = decodedJwtToken.getSubject();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("С валидным токеном нет юзера");
                        var r = new ApiResponseBody<>(500, "User not found with verified JWT Token", null);
                        response.setStatus(r.code());
                        response.getWriter().write(objectMapper.writeValueAsString(r));
                        return;
                    } else {
                        var user = userOpt.get();
                        var authenticationToken = new JwtAuthenticationToken(user);
                        var newContext = SecurityContextHolder.createEmptyContext();
                        newContext.setAuthentication(authenticationToken);
                        SecurityContextHolder.setContext(newContext);
                    }
                }
                System.out.println("Успех");
            } catch (JWTVerificationException e) {
                System.out.println("Невалидный токен найден");
                var r = new ApiResponseBody<>(401, "Invalid JWT Token", null);
                response.setStatus(r.code());
                response.getWriter().write(objectMapper.writeValueAsString(r));
                SecurityContextHolder.clearContext();
                return;
            }
        } else {
            SecurityContextHolder.clearContext();
        }

        System.out.println("Передача остальным фильтрам");
        filterChain.doFilter(request, response);
    }
}
