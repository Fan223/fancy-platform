package fan.fancy.starter.log.test;

import fan.fancy.starter.log.annotation.FancyLog;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 Controller.
 *
 * @author Fan
 */
@RestController
public class TestController {

    public String testController(String name) {
        return "Hello " + name;
    }

    @FancyLog("测试日志注解")
    public String testFancyLog(String name) {
        return "Hello " + name;
    }
}
