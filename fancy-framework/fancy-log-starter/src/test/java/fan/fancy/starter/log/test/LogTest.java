package fan.fancy.starter.log.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 日志测试类.
 *
 * @author Fan
 */
@SpringBootTest(classes = TestApplication.class)
class LogTest {

    @Autowired
    private TestController testController;

    @Test
    void testController() {
        String result = testController.testController("World");
        Assertions.assertEquals("Hello World", result);
    }

    @Test
    void testFancyLog() {
        String result = testController.testFancyLog("World");
        Assertions.assertEquals("Hello World", result);
    }
}
