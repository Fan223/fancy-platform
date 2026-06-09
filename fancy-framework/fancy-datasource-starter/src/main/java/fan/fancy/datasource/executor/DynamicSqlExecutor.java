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
     * 执行查询语句, 返回结果列表, 每个元素为一行数据的键值对映射, 支持动态传参. 示例:
     * <pre> {@code select * from user where id = ?} </pre>
     *
     * @param code 数据源唯一标识码
     * @param sql  执行的 SQL
     * @param args 动态参数
     * @return {@link List}
     */
    List<Map<String, Object>> query(String code, String sql, Object... args);

    /**
     * 执行查询语句, 返回结果列表, 每个元素为一行数据的键值对映射, 支持命名传参. 示例:
     * <pre> {@code select * from user where id = :id} </pre>
     *
     * @param code   数据源唯一标识码
     * @param sql    执行的 SQL
     * @param params {@link Map}
     * @return {@link List}
     */
    List<Map<String, Object>> query(String code, String sql, Map<String, Object> params);

    /**
     * 返回所有非查询语句的受影响行数, 支持动态参数.
     *
     * @param code 数据源唯一标识码
     * @param sql  执行的 SQL
     * @param args 动态参数
     * @return {@code int}
     */
    int executeUpdate(String code, String sql, Object... args);

    /**
     * 返回所有非查询语句的受影响行数, 支持命名传参.
     *
     * @param code   数据源唯一标识码
     * @param sql    执行的 SQL
     * @param params {@link Map}
     * @return {@code int}
     */
    int executeUpdate(String code, String sql, Map<String, Object> params);
}
