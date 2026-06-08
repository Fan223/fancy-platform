package fan.fancy.datasource.core;

import fan.fancy.datasource.context.DataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态路由数据源.
 *
 * @author Fan
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    public DynamicRoutingDataSource(DataSource defaultDataSource, DynamicDataSourceManager manager) {
        Map<Object, Object> targetMap = new HashMap<>();
        // 默认数据源(配置文件里配置)
        targetMap.put("default", defaultDataSource);
        // 注册动态数据源
        targetMap.putAll(manager.getAll());

        // 设置默认数据源
        super.setDefaultTargetDataSource(defaultDataSource);
        // 设置所有可选数据源
        super.setTargetDataSources(targetMap);
        // 初始化 AbstractRoutingDataSource
        super.afterPropertiesSet();
    }

    /**
     * 获取当前线程对应的数据源标识, 提供给 Spring 当前应该使用哪个数据源.
     *
     * @return {@link Object}
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.peek();
    }
}
