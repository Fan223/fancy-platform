package fan.fancy.server.authorization.service.impl;

import fan.fancy.server.authorization.converter.OAuth2UserConverterManager;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * {@link OidcUser} 服务类.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class FancyOidcUserService extends OidcUserService {

    private final OAuth2UserConverterManager userConverterManager;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 获取三方用户信息
        OidcUser oidcUser = super.loadUser(userRequest);
        // 第三方登录用户的业务用户创建流程将由 IAM 服务在收到 OIDC 用户信息后通过 auth-api 触发（后续实现）。
        // 当前阶段：仅做身份转换，不调用任何业务服务。
        userConverterManager.convert(userRequest, oidcUser);
        return oidcUser;
    }
}
