package fan.fancy.datasource.executor;

import java.util.List;
import java.util.Map;

/**
 * 动态 SQL 执行器.
 *
 * @author Fan
 */
public interface DynamicSqlExecutor {

    /**
     * 执行查询语句, 返回结果列表, 每个元素为一行数据的键值对映射.
     *
     * @param code 数据源唯一标识码
     * @param sql  执行的 SQL
     * @return {@link List}
     */
    List<Map<String, Object>> query(String code, String sql);

    /**
     * 返回所有非查询语句的受影响行数.
     *
     * @param code 数据源唯一标识码
     * @param sql  执行的 SQL
     * @return {@code int}
     */
    int executeUpdate(String code, String sql);
}
