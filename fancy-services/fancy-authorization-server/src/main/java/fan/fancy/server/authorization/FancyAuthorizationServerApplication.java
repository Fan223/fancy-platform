package fan.fancy.server.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 授权服务器启动类.
 *
 * @author Fan
 */
@SpringBootApplication
public class FancyAuthorizationServerApplication {
    static void main() {
        SpringApplication.run(FancyAuthorizationServerApplication.class);
    }
}
