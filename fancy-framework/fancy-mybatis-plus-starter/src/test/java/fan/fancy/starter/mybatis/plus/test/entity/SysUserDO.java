package fan.fancy.starter.mybatis.plus.test.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import fan.fancy.starter.mybatis.plus.entity.MetaDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试用户实体类.
 *
 * @author Fan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUserDO extends MetaDO {

    private String username;

    private Integer age;
}
