package fan.fancy.datasource.core;

import com.zaxxer.hikari.HikariDataSource;
import fan.fancy.datasource.model.DataSourceModel;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理.
 *
 * @author Fan
 */
public class DynamicDataSourceManager {

    private final Map<String, DataSource> datasource = new ConcurrentHashMap<>();

    /**
     * 添加数据源配置, 根据 DataSourceModel 创建 HikariDataSource 实例并注册到 datasource 中.
     *
     * @param model {@link DataSourceModel}
     */
    public void add(DataSourceModel model) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(model.driverClassName());
        dataSource.setJdbcUrl(model.url());
        dataSource.setUsername(model.username());
        dataSource.setPassword(model.password());
        datasource.put(model.code(), dataSource);
    }

    /**
     * 获取所有注册的数据源, 返回一个 Map 供 DynamicRoutingDataSource 使用.
     *
     * @return {@link Map}
     */
    public Map<String, DataSource> getAll() {
        return datasource;
    }
}
