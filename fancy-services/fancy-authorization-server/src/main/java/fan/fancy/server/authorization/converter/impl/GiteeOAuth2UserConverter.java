package fan.fancy.server.authorization.converter.impl;

import fan.fancy.server.authorization.converter.OAuth2UserConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * 转换通过码云登录的用户信息（占位，第三方登录用户创建流程暂未实现）.
 *
 * @author vains
 */
@Component("gitee")
public class GiteeOAuth2UserConverter implements OAuth2UserConverter {

    @Override
    public OAuth2User convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        return oAuth2User;
    }
}