package minchakov.arkadii.amina;

import minchakov.arkadii.amina.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final User user;

    public CustomAuthenticationToken(User user) {
        super(List.of());
        super.setAuthenticated(true);
        this.user = user;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable User getPrincipal() {
        return user;
    }
}
