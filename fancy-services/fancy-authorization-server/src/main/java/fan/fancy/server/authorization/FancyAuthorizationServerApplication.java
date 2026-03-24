package fan.fancy.server.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 授权服务器启动类.
 *
 * @author Fan
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "fan.fancy.iam.api")
public class FancyAuthorizationServerApplication {
    static void main() {
        SpringApplication.run(FancyAuthorizationServerApplication.class);
    }
}
