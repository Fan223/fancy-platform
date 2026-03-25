package fan.fancy.server.authorization.converter.impl;

import fan.fancy.iam.api.bo.UserBO;
import fan.fancy.server.authorization.converter.OAuth2UserConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 转换通过Github登录的用户信息
 *
 * @author vains
 */
@Component("github")
public class GithubOAuth2UserConverter implements OAuth2UserConverter {

    @Override
    public UserBO convert(OAuth2User oAuth2User) {
        // 获取三方用户信息
        Map<String, Object> attributes = oAuth2User.getAttributes();
        // 转换至Oauth2ThirdAccount
        UserBO thirdAccount = new UserBO();
        // 设置基础用户信息
        thirdAccount.setAvatar(String.valueOf(attributes.get("avatar_url")));
        return thirdAccount;
    }
}
