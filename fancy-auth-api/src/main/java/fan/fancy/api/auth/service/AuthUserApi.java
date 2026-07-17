package fan.fancy.api.auth.service;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.toolkit.http.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 认证服务用户 API.
 *
 * @author Fan
 */
@FeignClient(name = "fancy-authorization-server", path = "/auth/users")
public interface AuthUserApi {

    /**
     * 绑定认证账号（业务用户创建后调用）.
     */
    @PostMapping("/bind")
    Response<Void> bind(@RequestBody AuthBindRequest request);

    /**
     * 修改密码.
     */
    @PutMapping("/{userId}/password")
    Response<Void> changePassword(@PathVariable("userId") Long userId,
                                  @RequestBody ChangePasswordRequest request);
}