package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.AuthUserService;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fan
 */
@Service
@AllArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private final UserIdentityService userIdentityService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void bind(AuthBindRequest request) {
        if (userIdentityService.getByIdentifier(request.getIdentityType(), request.getIdentifier()) != null) {
            throw new IllegalStateException("identifier already bound: " + request.getIdentifier());
        }
        UserIdentityDO identity = new UserIdentityDO();
        identity.setUserId(request.getUserId());
        identity.setIdentityType(request.getIdentityType());
        identity.setIdentifier(request.getIdentifier());
        identity.setCredentialHash(passwordEncoder.encode(request.getCredential()));
        userIdentityService.save(identity);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String identifier, ChangePasswordRequest request) {
        UserIdentityDO identity = userIdentityService.getByIdentifier(IdentityType.USERNAME, identifier);
        if (identity == null || !identity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("identity not found for userId=" + userId);
        }
        if (!passwordEncoder.matches(request.getOldCredential(), identity.getCredentialHash())) {
            throw new IllegalArgumentException("old credential mismatch");
        }
        identity.setCredentialHash(passwordEncoder.encode(request.getNewCredential()));
        userIdentityService.save(identity);
    }
}