package minchakov.arkadii.amina.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import minchakov.arkadii.amina.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class JWTUtil {

    @Value("${jwt-secret}")
    private String JWT_SECRET;

    public String createJwt(User user) {
        return JWT.create().withSubject(user.getUsername())
                  .withExpiresAt(LocalDateTime.now().plusDays(31).atZone(ZoneId.systemDefault()).toInstant())
                  .sign(Algorithm.HMAC256(JWT_SECRET));
    }

    public DecodedJWT verifyJwt(String jwtToken) {
        var jwtVerifier = JWT.require(Algorithm.HMAC256(JWT_SECRET)).build();
        return jwtVerifier.verify(jwtToken);
    }
}
