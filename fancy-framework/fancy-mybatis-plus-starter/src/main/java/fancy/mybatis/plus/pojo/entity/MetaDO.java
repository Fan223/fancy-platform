package fancy.mybatis.plus.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

/**
 * 元数据实体类.
 *
 * @author Fan
 * @since 2025/3/6 9:06
 */
public class MetaDO {

    /**
     * 主键ID, 自定义填充.
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 删除时间, 逻辑删除字段, 未删除为 NULL.
     */
    @TableLogic(value = "NULL", delval = "NOW()")
    private LocalDateTime deleteTime;

    /**
     * 创建时间, 插入自动填充.
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间, 插入更新自动填充.
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
