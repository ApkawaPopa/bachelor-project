package minchakov.arkadii.amina.interceptor;

import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.repository.WebSocketTokenRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final ObjectMapper objectMapper;
    private final WebSocketTokenRepository webSocketTokenRepository;

    public WebSocketHandshakeInterceptor(ObjectMapper objectMapper, WebSocketTokenRepository webSocketTokenRepository) {
        this.objectMapper = objectMapper;
        this.webSocketTokenRepository = webSocketTokenRepository;
    }

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) throws Exception {
        var query = request.getURI().getQuery();
        if (query == null || query.indexOf('&') != query.lastIndexOf('&') || !query.startsWith("token=")) {
            var r = new ApiResponse<>(400, "Only 'token' query parameter required", null);
            response.setStatusCode(r.getStatusCode());
            response.getBody().write(objectMapper.writeValueAsBytes(r.getBody()));

            return false;
        }

        var tokenUUIDStr = query.substring(6);

        UUID tokenUUID;
        try {
            tokenUUID = UUID.fromString(tokenUUIDStr);
        } catch (IllegalArgumentException e) {
            send400("Not a valid UUID token: " + tokenUUIDStr, response);
            return false;
        }

        var tokenOpt = webSocketTokenRepository.getWebSocketTokenById(tokenUUID);
        if (tokenOpt.isEmpty()) {
            send400("Token " + tokenUUIDStr + " not found", response);
            return false;
        }

        var token = tokenOpt.get();
        attributes.put("user", token.getUser());
        webSocketTokenRepository.delete(token);

        return true;
    }

    private void send400(String message, ServerHttpResponse response) throws IOException {
        var r = new ApiResponse<>(400, message, null);
        response.setStatusCode(r.getStatusCode());
        response.getBody().write(objectMapper.writeValueAsBytes(r.getBody()));
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        @Nullable Exception exception
    ) {
    }
}
