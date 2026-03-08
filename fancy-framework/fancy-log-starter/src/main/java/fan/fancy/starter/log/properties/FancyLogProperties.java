package fan.fancy.starter.log.properties;

import fan.fancy.starter.log.annotation.FancyLog;
import fan.fancy.starter.log.aspect.FancyControllerLogAspect;
import fan.fancy.starter.log.aspect.FancyLogAspect;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置.
 *
 * @author Fan
 */
@Data
@ConfigurationProperties(prefix = "fan.fancy.log")
public class FancyLogProperties {

    /**
     * 全局总开关.
     */
    private boolean enabled = true;

    /**
     * {@link FancyLogAspect} 配置.
     */
    private Annotation annotation = new Annotation();

    /**
     * {@link FancyControllerLogAspect} 配置.
     */
    private Controller controller = new Controller();

    /**
     * 是否打印参数.
     */
    private boolean printArgs = true;
    /**
     * 最大参数长度.
     */
    private int maxArgsLength = 2048;

    /**
     * 服务名.
     */
    private String serviceName;
    /**
     * 是否打印返回结果.
     */
    private boolean printResult = true;
    /**
     * 最大返回结果长度.
     */
    private int maxResultLength = 2048;

    @Data
    public static class Annotation {
        /**
         * 是否启用 {@link FancyLog} 切面.
         */
        private boolean enabled = true;
    }

    @Data
    public static class Controller {
        /**
         * 是否启用 Controller 切面.
         */
        private boolean enabled = true;
    }
}
