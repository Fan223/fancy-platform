package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.mapper.UserIdentityMapper;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Fan
 */
@Service
@AllArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityMapper userIdentityMapper;

    @Override
    public UserIdentityDO getByIdentifier(IdentityType identityType, String identifier) {
        return userIdentityMapper.selectByIdentifier(identityType.getCode(), identifier);
    }

    @Override
    public void save(UserIdentityDO userIdentity) {
        userIdentityMapper.insert(userIdentity);
    }
}