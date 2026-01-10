package minchakov.arkadii.amina.util;

import minchakov.arkadii.amina.model.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class AppAuthenticationToken extends AbstractAuthenticationToken {

    private final User user;

    public AppAuthenticationToken(User user) {
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

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public @Nullable User getDetails() {
        return user;
    }
}
