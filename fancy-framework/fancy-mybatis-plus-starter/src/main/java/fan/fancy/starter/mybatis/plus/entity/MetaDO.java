package fan.fancy.starter.mybatis.plus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 元实体类.
 *
 * @author Fan
 */
@Data
public class MetaDO {

    /**
     * 主键 ID, 自定义填充.
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 删除时间, 逻辑删除字段, 未删除为 NULL.
     */
    @TableLogic(value = "NULL", delval = "NOW()")
    private LocalDateTime deleteTime;

    /**
     * 创建时间, 插入时自动填充.
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间, 插入或更新时自动填充.
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
