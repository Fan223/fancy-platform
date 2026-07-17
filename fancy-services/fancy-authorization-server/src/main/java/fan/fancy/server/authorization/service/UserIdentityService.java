package fan.fancy.server.authorization.service;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;

/**
 * 用户认证身份服务.
 *
 * @author Fan
 */
public interface UserIdentityService {

    /**
     * 根据身份类型 + identifier 查询.
     */
    UserIdentityDO getByIdentifier(IdentityType identityType, String identifier);

    /**
     * 插入.
     */
    void save(UserIdentityDO userIdentity);
}