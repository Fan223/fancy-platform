package fan.fancy.server.authorization.controller;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.server.authorization.service.AuthUserService;
import fan.fancy.toolkit.http.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证服务用户接口.
 *
 * @author Fan
 */
@RestController
@RequestMapping("/auth/users")
@AllArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;

    /**
     * 绑定认证账号（内部接口，需 X-Internal-Token 校验）.
     */
    @PostMapping("/bind")
    public Response<Void> bind(@RequestBody @Valid AuthBindRequest request) {
        authUserService.bind(request);
        return Response.success();
    }

    /**
     * 修改密码（需登录）.
     */
    @PutMapping("/{userId}/password")
    public Response<Void> changePassword(@PathVariable("userId") Long userId,
                                         @RequestBody @Valid ChangePasswordRequest request,
                                         Authentication authentication) {
        authUserService.changePassword(userId, authentication.getName(), request);
        return Response.success();
    }
}