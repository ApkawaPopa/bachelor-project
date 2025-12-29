package minchakov.arkadii.amina.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import minchakov.arkadii.amina.util.JWTUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

//@Component
public class AuthInterceptor implements ChannelInterceptor {

    private final JWTUtil jWTUtil;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public AuthInterceptor(JWTUtil jWTUtil, SimpMessagingTemplate simpMessagingTemplate) {
        this.jWTUtil = jWTUtil;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new RuntimeException("Accessor was null when validating message authorization");
        }

        var authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return handleError(accessor, "No 'Authorization' header found");
        }

        var authHeader = authHeaders.getFirst();
        if (!authHeader.startsWith("Bearer ")) {
            return handleError(accessor, "'Authtorization' header has invalid format");
        }

        var token = authHeader.substring(7);
        try {
            var jwt = jWTUtil.verifyJwt(token);
            accessor.setUser(jwt::getSubject);
            return message;
        } catch (JWTVerificationException e) {
            return handleError(accessor, "Invalid JWT token: " + e.getMessage());
        }
    }

    private Message<?> handleError(StompHeaderAccessor accessor, String message) {
        return null;
    }
}
