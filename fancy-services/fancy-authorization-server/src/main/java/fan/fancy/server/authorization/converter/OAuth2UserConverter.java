package fan.fancy.server.authorization.converter;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * {@link OAuth2User} 转换接口.
 *
 * @author Fan
 */
public interface OAuth2UserConverter {

    /**
     * 转换 OAuth2 用户信息. 第三方登录用户创建流程暂未实现, 返回 null 即可.
     */
    OAuth2User convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User);
}