package fan.fancy.server.authorization.converter;

import fan.fancy.iam.api.pojo.bo.UserBO;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * 1
 *
 * @author Fan
 */
@Component
@AllArgsConstructor
public class OAuth2UserConverterManager {

    /**
     * 构造器 Map 注入, 自动以 Bean 名称为 Key, Bean 实例为 Value 组装成 Map 注入进来.
     */
    private final Map<String, OAuth2UserConverter> oAuth2UserConverterMap;

    public UserBO convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 获取三方登录配置的registrationId, 即配置文件中的 spring.security.oauth2.client.registration, 这里将他当做登录方式
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return getInstance(registrationId).convert(userRequest, oAuth2User);
    }

    public OAuth2UserConverter getInstance(String loginType) {
        if (ObjectUtils.isEmpty(loginType)) {
            throw new UnsupportedOperationException("登录方式不能为空.");
        }
        OAuth2UserConverter oAuth2UserConverter = oAuth2UserConverterMap.get(loginType);
        if (oAuth2UserConverter == null) {
            throw new UnsupportedOperationException("不支持[" + loginType + "]登录方式获取用户信息转换器");
        }
        return oAuth2UserConverter;
    }
}
