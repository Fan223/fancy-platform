package fan.fancy.datasource.autoconfigure;

import fan.fancy.datasource.aspect.DsAspect;
import fan.fancy.datasource.core.DynamicDataSourceManager;
import fan.fancy.datasource.core.DynamicRoutingDataSource;
import fan.fancy.datasource.executor.DynamicSqlExecutor;
import fan.fancy.datasource.executor.JdbcDynamicSqlExecutor;
import fan.fancy.datasource.provider.DataSourceProvider;
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

    @Bean
    public DynamicDataSourceManager dynamicDataSourceManager(ObjectProvider<DataSourceProvider> objectProvider) {
        return new DynamicDataSourceManager(objectProvider);
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
    public DataSource dynamicRoutingDataSource(DataSource defaultDataSource, DynamicDataSourceManager manager) {
        return new DynamicRoutingDataSource(defaultDataSource, manager);
    }

    @Bean
    public DynamicSqlExecutor dynamicSqlExecutor(DynamicDataSourceManager manager) {
        return new JdbcDynamicSqlExecutor(manager);
    }
}
