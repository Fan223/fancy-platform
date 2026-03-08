package fan.fancy.starter.mybatis.plus.test.bootstrap;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fan.fancy.starter.mybatis.plus.bootstrap.FancyP6SpyEnvironmentPostProcessor;
import fan.fancy.starter.mybatis.plus.test.TestApplication;
import fan.fancy.starter.mybatis.plus.test.mapper.SysUserMapper;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * {@link FancyP6SpyEnvironmentPostProcessor} 测试类.
 *
 * @author Fan
 */
@SpringBootTest(classes = TestApplication.class)
class FancyP6SpyEnvironmentPostProcessorTest {

    @Autowired
    private SysUserMapper sysUserMapper;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger("p6spy");
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        listAppender.stop();
    }

    @Test
    void selectById_withP6Spy_logsFormattedSqlWithExecutionTime() {
        sysUserMapper.selectById(1L);

        List<String> logs = listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        AssertionsForInterfaceTypes.assertThat(logs)
                .anyMatch(log -> log.contains("SELECT"))
                .anyMatch(log -> log.contains("ms"));
    }
}
