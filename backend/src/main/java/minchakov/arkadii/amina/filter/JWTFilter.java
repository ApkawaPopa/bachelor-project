package minchakov.arkadii.amina.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import minchakov.arkadii.amina.exception.JwtAuthenticationException;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.util.AppAuthenticationToken;
import minchakov.arkadii.amina.util.JWTUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jWTUtil;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JWTFilter(
        JWTUtil jWTUtil,
        UserRepository userRepository,
        HandlerExceptionResolver handlerExceptionResolver
    ) {
        this.jWTUtil = jWTUtil;
        this.userRepository = userRepository;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            try {
                var decodedJwtToken = jWTUtil.verifyJwt(jwtToken);
                var username = decodedJwtToken.getSubject();
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        handlerExceptionResolver.resolveException(
                            request,
                            response,
                            null,
                            new BadCredentialsException("User not found with verified JWT Token")
                        );
                        SecurityContextHolder.clearContext();
                        return;
                    } else {
                        var user = userOpt.get();
                        var authenticationToken = new AppAuthenticationToken(user);
                        var newContext = SecurityContextHolder.createEmptyContext();
                        newContext.setAuthentication(authenticationToken);
                        SecurityContextHolder.setContext(newContext);
                    }
                }
            } catch (JWTVerificationException e) {
                handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new JwtAuthenticationException("Invalid JWT Token")
                );
                SecurityContextHolder.clearContext();
                return;
            }
        } else {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
