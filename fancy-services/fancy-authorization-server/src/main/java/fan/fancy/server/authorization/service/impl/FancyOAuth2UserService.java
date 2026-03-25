package fan.fancy.server.authorization.service.impl;

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
public class FancyOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 获取三方用户信息
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return oAuth2User;
    }
}
