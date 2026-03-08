package fan.fancy.starter.mybatis.plus.properties;

import lombok.experimental.UtilityClass;

/**
 * P6Spy 配置.
 *
 * @author Fan
 */
@UtilityClass
public class FancyP6SpyProperties {

    public static final String LOG_FORMAT = "分类: %(category)\t耗时: %(executionTime)ms\n\t%(sql)";
}
