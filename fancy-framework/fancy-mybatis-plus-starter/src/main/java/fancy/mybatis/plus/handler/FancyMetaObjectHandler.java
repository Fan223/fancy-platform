package fancy.mybatis.plus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import fancy.mybatis.plus.entity.MetaDO;
import fancy.toolkit.function.LambdaUtils;
import fancy.toolkit.id.IdUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 自动填充处理器.
 *
 * @author Fan
 * @since 2025/3/6 9:04
 */
public class FancyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充主键ID.
        this.setFieldValByName(LambdaUtils.getFieldName(MetaDO::getId), IdUtils.getSnowflakeId(), metaObject);
        // 自动填充创建和更新时间.
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, LambdaUtils.getFieldName(MetaDO::getCreateTime), LocalDateTime.class, now);
        this.strictInsertFill(metaObject, LambdaUtils.getFieldName(MetaDO::getUpdateTime), LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间.
        this.strictUpdateFill(metaObject, LambdaUtils.getFieldName(MetaDO::getUpdateTime), LocalDateTime::now, LocalDateTime.class);
    }
}
