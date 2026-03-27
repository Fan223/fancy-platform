package fan.fancy.server.authorization.converter;

import fan.fancy.iam.api.pojo.bo.UserBO;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * {@link OAuth2User} 转换接口.
 *
 * @author Fan
 */
public interface OAuth2UserConverter {

    UserBO convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User);
}
