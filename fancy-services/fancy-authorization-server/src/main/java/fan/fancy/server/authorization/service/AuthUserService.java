package fan.fancy.server.authorization.service;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;

/**
 * 认证服务（bind / changePassword）.
 *
 * @author Fan
 */
public interface AuthUserService {

    /**
     * 绑定认证账号.
     */
    void bind(AuthBindRequest request);

    /**
     * 修改密码.
     *
     * @param userId     业务用户ID
     * @param identifier 当前身份标识（用户名）
     * @param request    请求体
     */
    void changePassword(Long userId, String identifier, ChangePasswordRequest request);
}