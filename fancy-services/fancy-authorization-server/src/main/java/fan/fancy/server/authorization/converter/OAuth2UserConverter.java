package fan.fancy.server.authorization.converter;

import fan.fancy.iam.api.bo.UserBO;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * {@link OAuth2User} 转换接口.
 *
 * @author Fan
 */
public interface OAuth2UserConverter {

    UserBO convert(OAuth2User oAuth2User);
}
