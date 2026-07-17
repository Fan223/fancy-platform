package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserIdentityService userIdentityService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_throwsWhenIdentityNotFound() {
        when(userIdentityService.getByIdentifier(any(), anyString())).thenReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_returnsUserWithHashedCredential() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setUserId(42L);
        identity.setCredentialHash("$2a$10$hashed");
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(identity);

        UserDetails details = userDetailsService.loadUserByUsername("fan");

        assertThat(details.getUsername()).isEqualTo("fan");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
    }
}