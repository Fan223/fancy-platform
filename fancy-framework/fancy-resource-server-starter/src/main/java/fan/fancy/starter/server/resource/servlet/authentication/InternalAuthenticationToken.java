package fan.fancy.starter.server.resource.servlet.authentication;

import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;

/**
 * 内部认证 Token.
 *
 * @author Fan
 */
@EqualsAndHashCode(callSuper = false)
public class InternalAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -9026796118649268020L;

    private final @Nullable Object principal;

    public InternalAuthenticationToken(@Nullable Object principal) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return this.principal;
    }
}
