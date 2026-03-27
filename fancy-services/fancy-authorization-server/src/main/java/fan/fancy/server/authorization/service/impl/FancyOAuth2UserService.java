package fan.fancy.server.authorization.service.impl;

import fan.fancy.iam.api.pojo.bo.UserBO;
import fan.fancy.iam.api.service.UserRpcService;
import fan.fancy.server.authorization.converter.OAuth2UserConverterManager;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * {@link OAuth2User} 服务.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class FancyOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserConverterManager userConverterManager;

    private final UserRpcService userRpcService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 获取三方用户信息
        OAuth2User oAuth2User = super.loadUser(userRequest);
        UserBO userBO = userConverterManager.convert(userRequest, oAuth2User);
        // 保存用户信息, 存在则更新, 不存在则新增
//        userRpcService.saveUser(userBO);
        return oAuth2User;
    }
}
