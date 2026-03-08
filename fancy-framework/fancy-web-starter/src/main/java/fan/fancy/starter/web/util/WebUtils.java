package fan.fancy.starter.web.util;

import fan.fancy.toolkit.http.Response;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web 工具类.
 *
 * @author Fan
 */
@UtilityClass
public class WebUtils {

    public static File check(String filepath) throws IOException {
        Path safePath = Paths.get(filepath)
                .normalize()
                .toRealPath();
        return safePath.toFile();
    }

    public static void download(File file, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (!file.exists()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Response<String> res = Response.success("文件不存在");
            try (ServletOutputStream out = response.getOutputStream()) {
                out.write(res.message().getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return;
        }

        response.setContentLengthLong(file.length());
        // 获取文件类型
        String contentType = Files.probeContentType(file.toPath());
        if (null == contentType) {
            // 默认二进制流类型
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        response.setContentType(contentType);

        // 处理文件名
        String fileName = file.getName();
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

        // 将文件写入响应流
        try (FileInputStream in = new FileInputStream(file); ServletOutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
            out.flush();
        }
    }
}
