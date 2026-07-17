package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceImplTest {

    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUserServiceImpl authUserService;

    private AuthBindRequest sampleBindRequest() {
        AuthBindRequest req = new AuthBindRequest();
        req.setUserId(7L);
        req.setIdentityType(IdentityType.USERNAME);
        req.setIdentifier("fan");
        req.setCredential("plain");
        return req;
    }

    @Test
    void bind_hashesCredentialAndInserts() {
        when(passwordEncoder.encode("plain")).thenReturn("$2a$10$hashed");
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(null);

        authUserService.bind(sampleBindRequest());

        ArgumentCaptor<UserIdentityDO> captor = ArgumentCaptor.forClass(UserIdentityDO.class);
        verify(userIdentityService).save(captor.capture());
        UserIdentityDO saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(7L);
        assertThat(saved.getIdentityType()).isEqualTo(IdentityType.USERNAME);
        assertThat(saved.getIdentifier()).isEqualTo("fan");
        assertThat(saved.getCredentialHash()).isEqualTo("$2a$10$hashed");
    }

    @Test
    void bind_rejectsDuplicateIdentifier() {
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(new UserIdentityDO());

        try {
            authUserService.bind(sampleBindRequest());
        } catch (IllegalStateException e) {
            // expected
        }

        verify(userIdentityService, never()).save(any());
    }

    @Test
    void changePassword_rejectsWrongOldCredential() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setId(1L);
        identity.setUserId(7L);
        identity.setCredentialHash("$2a$10$old");
        when(userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan")).thenReturn(identity);
        when(passwordEncoder.matches("wrong", "$2a$10$old")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldCredential("wrong");
        req.setNewCredential("new");

        try {
            authUserService.changePassword(7L, "fan", req);
        } catch (IllegalArgumentException e) {
            // expected
        }

        verify(userIdentityService, never()).save(any());
    }

    @Test
    void changePassword_updatesHashOnSuccess() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setId(1L);
        identity.setUserId(7L);
        identity.setCredentialHash("$2a$10$old");
        when(userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan")).thenReturn(identity);
        when(passwordEncoder.matches("old", "$2a$10$old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("$2a$10$new");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldCredential("old");
        req.setNewCredential("new");

        authUserService.changePassword(7L, "fan", req);

        assertThat(identity.getCredentialHash()).isEqualTo("$2a$10$new");
        verify(userIdentityService).save(identity);
    }
}