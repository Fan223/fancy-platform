package fan.fancy.server.authorization.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

/**
 * Web 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class WebUtils {

    public static boolean isBrowser(HttpServletRequest request) {
        return request.getHeader("Accept") != null
                && request.getHeader("Accept").contains(MediaType.TEXT_HTML_VALUE);
    }
}
