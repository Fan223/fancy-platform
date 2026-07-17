package fan.fancy.api.auth.pojo.dto;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绑定认证账号请求.
 *
 * @author Fan
 */
@Data
public class AuthBindRequest {

    /**
     * 业务用户ID（雪花ID，由 IAM 生成）.
     */
    @NotNull
    private Long userId;

    /**
     * 身份类型.
     */
    @NotNull
    private IdentityType identityType;

    /**
     * 身份标识（用户名/手机/邮箱/第三方 openid）.
     */
    @NotBlank
    private String identifier;

    /**
     * 明文密码（仅绑定时一次性传输，认证侧立即 BCrypt 哈希入库）.
     */
    @NotBlank
    private String credential;
}