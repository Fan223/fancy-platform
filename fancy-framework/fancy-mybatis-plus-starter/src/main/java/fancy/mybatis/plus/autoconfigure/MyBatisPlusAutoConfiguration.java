package fancy.mybatis.plus.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import fancy.mybatis.plus.handler.FancyMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置类.
 *
 * @author Fan
 * @since 2025/3/6 9:03
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MyBatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 乐观锁插件.
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防全表更新与删除插件.
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 分页插件. 多数据源可以不配具体类型. 存在多个插件时, 应放到插件执行链的最后面.
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler metaObjectHandler() {
        return new FancyMetaObjectHandler();
    }
}
