package fan.fancy.starter.mybatis.plus.properties;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis-Plus 配置.
 *
 * @author Fan
 */
@Data
@ConfigurationProperties(prefix = "fan.fancy.mybatis-plus")
public class FancyMyBatisPlusProperties {

    private boolean optimisticLocker = true;

    private boolean blockAttack = true;

    private boolean pagination = true;

    private DbType dbType = DbType.MYSQL;
}
