package fan.fancy.starter.mybatis.plus.test;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * 测试启动类.
 *
 * @author Fan
 */
@EnableAutoConfiguration
@MapperScan("fan.fancy.starter.mybatis.plus.mapper")
public class TestApplication {
}
