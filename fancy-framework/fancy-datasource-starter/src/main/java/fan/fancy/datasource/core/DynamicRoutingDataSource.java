package fan.fancy.datasource.core;

import fan.fancy.datasource.context.DataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态路由数据源.
 *
 * @author Fan
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final DynamicDataSourceManager manager;

    public DynamicRoutingDataSource(DataSource defaultDataSource, DynamicDataSourceManager manager) {
        this.manager = manager;
        // 设置默认数据源
        super.setDefaultTargetDataSource(defaultDataSource);
        // 设置数据源为空 Map 以满足 null 校验, 实际不使用该快照机制, 由 determineTargetDataSource 方法动态查找, 以支持动态刷新
        super.setTargetDataSources(Map.of());
        // 初始化
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

    /**
     * 核心路由方法, 实时委托 {@link DynamicDataSourceManager} 查找, 天然支持动态刷新, 不存在时则走前面设置的默认数据源.
     *
     * @return {@link DataSource}
     */
    @Override
    protected DataSource determineTargetDataSource() {
        Object key = determineCurrentLookupKey();
        if (key != null) {
            DataSource ds = manager.get((String) key);
            if (ds != null) {
                return ds;
            }
        }

        DataSource defaultDs = getResolvedDefaultDataSource();
        if (defaultDs == null) {
            throw new IllegalStateException("No default DataSource configured in DynamicRoutingDataSource");
        }
        return defaultDs;
    }
}
