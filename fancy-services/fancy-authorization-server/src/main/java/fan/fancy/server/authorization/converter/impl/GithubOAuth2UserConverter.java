package fan.fancy.server.authorization.converter.impl;

import fan.fancy.iam.api.pojo.bo.UserBO;
import fan.fancy.iam.api.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.converter.OAuth2UserConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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
    public UserBO convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 获取三方用户信息
        Map<String, Object> attributes = oAuth2User.getAttributes();
        // 转换至系统用户信息
        UserIdentityDO userIdentityDO = new UserIdentityDO();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        userIdentityDO.setType(1);
        userIdentityDO.setIdentity(UserIdentityDO.IdentityType.valueOf(registrationId.toUpperCase()).getCode());
        // 获取ID作为身份标识, 后续登录是判断是否已存在
        userIdentityDO.setIdentifier(String.valueOf(attributes.get("id")));
        userIdentityDO.setCredential(userRequest.getAccessToken().getTokenValue());

        UserBO userBO = new UserBO();
        userBO.setAvatar(String.valueOf(attributes.get("avatar_url")));
        userBO.setNickname(String.valueOf(attributes.get("name")));

        userBO.getUserIdentities().add(userIdentityDO);
        return userBO;
    }
}
