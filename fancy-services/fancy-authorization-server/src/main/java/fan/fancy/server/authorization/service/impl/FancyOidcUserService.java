package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.iam.pojo.bo.UserBO;
import fan.fancy.api.iam.service.UserApiService;
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

    private final UserApiService userApiService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 获取三方用户信息
        OidcUser oidcUser = super.loadUser(userRequest);
        UserBO userBO = userConverterManager.convert(userRequest, oidcUser);
        // 保存用户信息, 存在则更新, 不存在则新增
        userApiService.createUser(userBO);
        return oidcUser;
    }
}
