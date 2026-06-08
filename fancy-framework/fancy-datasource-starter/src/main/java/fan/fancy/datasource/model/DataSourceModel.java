package fan.fancy.datasource.model;

/**
 * 数据源模型.
 *
 * @author Fan
 */
public record DataSourceModel(

        // 数据源唯一标识码
        String code,

        String driverClassName,

        String url,

        String username,

        String password
) {
}
