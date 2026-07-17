package fan.fancy.api.auth.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 身份类型.
 *
 * @author Fan
 */
@Getter
@AllArgsConstructor
public enum IdentityType {

    USERNAME(1),
    PHONE(2),
    EMAIL(3),
    GITHUB(4),
    WECHAT(5),
    QQ(6);

    private final int code;
}