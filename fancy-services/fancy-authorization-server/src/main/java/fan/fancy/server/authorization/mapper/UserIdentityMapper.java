package fan.fancy.server.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户认证身份 Mapper.
 *
 * @author Fan
 */
@Mapper
public interface UserIdentityMapper extends BaseMapper<UserIdentityDO> {

    /**
     * 根据身份类型 + identifier 查询.
     */
    @Select("SELECT * FROM user_identity WHERE identity_type = #{identityType} AND identifier = #{identifier} LIMIT 1")
    UserIdentityDO selectByIdentifier(@Param("identityType") int identityType,
                                      @Param("identifier") String identifier);
}