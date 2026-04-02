package fan.fancy.server.authorization.service.impl;

import fan.fancy.iam.api.pojo.bo.UserBO;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

/**
 * {@link UserDetailsService} 实现类.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final RestClient restClient;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserBO userBO = restClient.get()
                .uri("http://localhost:10200/iam/users/auth/" + username)
                .retrieve()
                .body(UserBO.class);
        if (userBO == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(username, passwordEncoder.encode(userBO.getUserIdentities().getFirst().getCredential()), new ArrayList<>());
    }
}
