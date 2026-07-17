package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} 实现类.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserIdentityService userIdentityService;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserIdentityDO identity = userIdentityService.getByIdentifier(IdentityType.USERNAME, username);
        if (identity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(username, identity.getCredentialHash(),
                AuthorityUtils.NO_AUTHORITIES);
    }
}