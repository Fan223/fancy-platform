package fan.fancy.starter.mybatis.plus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import fan.fancy.starter.mybatis.plus.entity.MetaDO;
import fan.fancy.toolkit.id.IdUtils;
import fan.fancy.toolkit.lang.LambdaUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 字段自动填充处理器.
 *
 * @author Fan
 */
public class FancyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充主键 ID.
        // 由于 @TableId 走的是 IdentifierGenerator 接口不走自动填充逻辑, 因此使用 setFieldValByName 直接设值.
        String idField = LambdaUtils.getFieldName(MetaDO::getId);
        if (metaObject.getValue(idField) == null) {
            this.setFieldValByName(idField, IdUtils.generateSnowflakeId(), metaObject);
        }
        // 自动填充创建和更新时间.
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, LambdaUtils.getFieldName(MetaDO::getCreateTime), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, LambdaUtils.getFieldName(MetaDO::getUpdateTime), LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间.
        this.strictUpdateFill(metaObject, LambdaUtils.getFieldName(MetaDO::getUpdateTime), LocalDateTime.class, LocalDateTime.now());
    }
}
