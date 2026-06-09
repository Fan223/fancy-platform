package fan.fancy.datasource.executor;

import fan.fancy.datasource.core.DynamicDataSourceManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link JdbcTemplate} 动态 SQL 执行器, 通过 {@link DynamicDataSourceManager} 获取数据源并执行 SQL 语句.
 *
 * @author Fan
 */
public class JdbcDynamicSqlExecutor implements DynamicSqlExecutor {

    private final DynamicDataSourceManager manager;

    private final Map<String, NamedParameterJdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();

    public JdbcDynamicSqlExecutor(DynamicDataSourceManager manager) {
        this.manager = manager;
    }

    /**
     * 根据数据源唯一标识码获取对应的 {@link NamedParameterJdbcTemplate} 实例, 使用缓存避免重复创建.
     *
     * @param dataSourceCode 数据源标识码
     * @return {@link JdbcTemplate}
     */
    private NamedParameterJdbcTemplate getJdbcTemplate(String dataSourceCode) {
        return jdbcTemplateCache.computeIfAbsent(dataSourceCode, code ->
                new NamedParameterJdbcTemplate(manager.get(code)));
    }

    @Override
    public List<Map<String, Object>> query(String code, String sql, Object... args) {
        return getJdbcTemplate(code).getJdbcTemplate().queryForList(sql, args);
    }

    @Override
    public List<Map<String, Object>> query(String code, String sql, Map<String, Object> params) {
        return getJdbcTemplate(code).queryForList(sql, params);
    }

    @Override
    public int executeUpdate(String code, String sql, Object... args) {
        return getJdbcTemplate(code).getJdbcTemplate().update(sql, args);
    }

    @Override
    public int executeUpdate(String code, String sql, Map<String, Object> params) {
        return getJdbcTemplate(code).update(sql, params);
    }
}
