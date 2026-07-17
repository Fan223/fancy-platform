package fan.fancy.api.auth.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求.
 *
 * @author Fan
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码（明文）.
     */
    @NotBlank
    private String oldCredential;

    /**
     * 新密码（明文）.
     */
    @NotBlank
    private String newCredential;
}