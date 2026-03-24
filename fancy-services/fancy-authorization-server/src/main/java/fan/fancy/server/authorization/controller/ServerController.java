package fan.fancy.server.authorization.controller;

import fan.fancy.toolkit.http.Response;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
    public Response<String> test(Authentication authentication, Principal principal) {
        System.out.println(authentication);
        System.out.println(principal);
        return Response.success("Test from ServerController");
    }
}
