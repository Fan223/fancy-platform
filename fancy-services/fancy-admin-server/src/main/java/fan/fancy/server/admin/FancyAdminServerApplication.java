package fan.fancy.server.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 监控服务启动类.
 *
 * @author Fan
 */
@SpringBootApplication
@EnableAdminServer
public class FancyAdminServerApplication {
    static void main() {
        SpringApplication.run(FancyAdminServerApplication.class);
    }
}
