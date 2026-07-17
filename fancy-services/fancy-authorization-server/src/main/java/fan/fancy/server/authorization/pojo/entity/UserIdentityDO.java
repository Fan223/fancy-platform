package fan.fancy.server.authorization.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fancy.starter.mybatis.plus.entity.MetaDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户认证身份实体.
 *
 * @author Fan
 */
@TableName("user_identity")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserIdentityDO extends MetaDO {

    /**
     * 业务用户ID.
     */
    private Long userId;

    /**
     * 身份类型.
     */
    private IdentityType identityType;

    /**
     * 身份标识.
     */
    private String identifier;

    /**
     * 凭据哈希（BCrypt）.
     */
    private String credentialHash;
}