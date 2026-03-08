package fan.fancy.server.authorization.controller;

import fan.fancy.toolkit.http.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Fan
 */
@RestController
public class ServerController {

    @GetMapping("/api/getCaptcha")
    public Response<String> getCaptcha() {
        return Response.success("captcha");
    }

    @GetMapping("/test")
    public Response<String> test() {
        return Response.success("Test from ServerController");
    }
}
