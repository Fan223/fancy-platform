package fan.fancy.datasource.core;

import com.zaxxer.hikari.HikariDataSource;
import fan.fancy.datasource.model.DataSourceModel;
import fan.fancy.datasource.provider.DataSourceProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 动态数据源管理.
 *
 * @author Fan
 */
public class DynamicDataSourceManager {

    /**
     * 数据源提供者, 通过 {@link ObjectProvider} 获取, 允许在没有 {@link DataSourceProvider} 实现的情况下正常启动应用.
     */
    private final ObjectProvider<DataSourceProvider> provider;

    /**
     * 数据源缓存.
     */
    private final Map<String, DataSource> datasourceMap = new ConcurrentHashMap<>();

    /**
     * 数据源模型缓存.
     */
    private final Map<String, DataSourceModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 数据源提供者实例, 在 {@link #init()} 方法中通过 {@link ObjectProvider} 获取, 可能为 null, 需要在使用前进行 null 检查.
     */
    private DataSourceProvider dataSourceProvider;

    public DynamicDataSourceManager(ObjectProvider<DataSourceProvider> provider) {
        this.provider = provider;
    }

    /**
     * 初始化方法, 在 Spring 容器完成依赖注入后调用, 通过 {@link ObjectProvider} 获取 {@link DataSourceProvider} 实例,
     * 如果存在则加载数据源配置并添加到缓存中.
     */
    @PostConstruct
    public void init() {
        this.dataSourceProvider = provider.getIfAvailable();
        if (this.dataSourceProvider == null) {
            return;
        }
        List<DataSourceModel> models = dataSourceProvider.load();
        models.forEach(this::add);
    }

    /**
     * 添加数据源配置, 根据 DataSourceModel 创建 HikariDataSource 实例并注册到 datasourceMap 中.
     *
     * @param model {@link DataSourceModel}
     */
    public synchronized void add(DataSourceModel model) {
        HikariDataSource ds = createDataSource(model);
        datasourceMap.put(model.code(), ds);
        modelMap.put(model.code(), model);
    }

    /**
     * 根据 DataSourceModel 创建 HikariDataSource 实例.
     *
     * @param model {@link DataSourceModel}
     * @return {@link HikariDataSource}
     */
    private HikariDataSource createDataSource(DataSourceModel model) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(model.driverClassName());
        ds.setJdbcUrl(model.url());
        ds.setUsername(model.username());
        ds.setPassword(model.password());
        return ds;
    }

    /**
     * 刷新数据源配置, 重新加载数据源列表, 对比当前缓存的数据源和最新加载的数据源, 删除已不存在的数据源, 添加或更新数据源.
     */
    public synchronized void refresh() {
        if (dataSourceProvider == null) {
            return;
        }
        // 加载最新的数据源配置
        List<DataSourceModel> latest = dataSourceProvider.load();
        Map<String, DataSourceModel> latestMap = latest.stream()
                .collect(Collectors.toMap(DataSourceModel::code, model -> model));

        // 删除已不存在的数据源
        modelMap.keySet().stream()
                .filter(code -> !latestMap.containsKey(code))
                .toList()
                .forEach(this::remove);

        // 添加或更新数据源
        latest.forEach(model -> {
            DataSourceModel old = modelMap.get(model.code());
            if (old == null) {
                add(model);
                return;
            }
            if (!Objects.equals(old, model)) {
                remove(model.code());
                add(model);
            }
        });
    }

    /**
     * 删除数据源配置, 从 datasourceMap 和 modelMap 中移除对应的数据源实例和模型, 移除实例后调用 close() 方法关闭连接池.
     *
     * @param code 数据源唯一标识码
     */
    public synchronized void remove(String code) {
        DataSource dataSource = datasourceMap.remove(code);
        modelMap.remove(code);
        if (dataSource instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    /**
     * 获取数据源实例, 从 datasourceMap 中根据数据源唯一标识码获取对应的数据源实例, 如果不存在则返回 null.
     *
     * @param code 数据源唯一标识码
     * @return {@link DataSource}
     */
    public DataSource get(String code) {
        return datasourceMap.get(code);
    }
}
