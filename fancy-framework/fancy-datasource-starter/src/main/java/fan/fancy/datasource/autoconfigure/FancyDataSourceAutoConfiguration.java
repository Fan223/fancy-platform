package fan.fancy.datasource.autoconfigure;

import fan.fancy.datasource.aspect.DsAspect;
import fan.fancy.datasource.core.DynamicDataSourceManager;
import fan.fancy.datasource.core.DynamicRoutingDataSource;
import fan.fancy.datasource.provider.DatasourceProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 动态数据源自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class FancyDataSourceAutoConfiguration {

    @Bean
    public DsAspect dsAspect() {
        return new DsAspect();
    }

    /**
     * 动态数据源管理器, 从 DatasourceProvider 加载数据源配置并创建数据源实例.
     *
     * @param objectProvider {@link ObjectProvider}
     * @return {@link DynamicDataSourceManager}
     */
    @Bean
    public DynamicDataSourceManager dynamicDataSourceManager(ObjectProvider<DatasourceProvider> objectProvider) {
        DynamicDataSourceManager manager = new DynamicDataSourceManager();
        DatasourceProvider provider = objectProvider.getIfAvailable();
        if (provider != null) {
            provider.load().forEach(manager::add);
        }
        return manager;
    }

    /**
     * 动态路由数据源, 负责根据当前线程上下文切换数据源, {@code @Primary} 保证优先执行.
     *
     * @param defaultDataSource {@link DataSource}
     * @param manager           {@link DynamicDataSourceManager}
     * @return {@link DataSource}
     */
    @Bean
    @Primary
    public DataSource routingDataSource(DataSource defaultDataSource, DynamicDataSourceManager manager) {
        return new DynamicRoutingDataSource(defaultDataSource, manager);
    }
}
