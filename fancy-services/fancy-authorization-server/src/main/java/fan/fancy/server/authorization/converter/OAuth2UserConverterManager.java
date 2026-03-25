package fan.fancy.server.authorization.converter;

import fan.fancy.iam.api.bo.UserBO;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * 1
 *
 * @author Fan
 */
@AllArgsConstructor
public class OAuth2UserConverterManager {

    /**
     * 构造器 Map 注入, 自动以 Bean 名称为 Key, Bean 实例为 Value 组装成 Map 注入进来.
     */
    private final Map<String, OAuth2UserConverter> userConverterStrategyMap;


    public OAuth2UserConverter getInstance(String loginType) {
        if (ObjectUtils.isEmpty(loginType)) {
            throw new UnsupportedOperationException("登录方式不能为空.");
        }
        OAuth2UserConverter userConverterStrategy = userConverterStrategyMap.get(loginType);
        if (userConverterStrategy == null) {
            throw new UnsupportedOperationException("不支持[" + loginType + "]登录方式获取用户信息转换器");
        }
        return userConverterStrategy;
    }

    public UserBO convert(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // 获取三方登录配置的registrationId, 即配置文件中的 spring.security.oauth2.client.registration, 这里将他当做登录方式
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        UserBO userBO = this.getInstance(registrationId).convert(oAuth2User);
        OAuth2AccessToken accessToken = userRequest.getAccessToken();
//        userBO.set
        return userBO;
    }
}
