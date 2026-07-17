package fan.fancy.server.authorization.service.impl;

import fan.fancy.server.authorization.converter.OAuth2UserConverterManager;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * {@link OAuth2User} 服务（占位，第三方登录用户创建流程暂未实现）.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class FancyOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserConverterManager userConverterManager;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 第三方登录用户的业务用户创建流程暂未实现（spec §8 范围之外）。
        return userConverterManager.convert(userRequest, oAuth2User);
    }
}