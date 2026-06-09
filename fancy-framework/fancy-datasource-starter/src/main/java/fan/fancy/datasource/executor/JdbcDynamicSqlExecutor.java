package fan.fancy.datasource.executor;

import fan.fancy.datasource.core.DynamicDataSourceManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link JdbcTemplate} 动态 SQL 执行器, 通过 DynamicDataSourceManager 获取数据源并执行 SQL 语句.
 *
 * @author Fan
 */
public class JdbcDynamicSqlExecutor implements DynamicSqlExecutor {

    private final DynamicDataSourceManager manager;
    private final Map<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();

    public JdbcDynamicSqlExecutor(DynamicDataSourceManager manager) {
        this.manager = manager;
    }

    /**
     * 根据数据源编码获取对应的 JdbcTemplate 实例, 使用缓存避免重复创建.
     *
     * @param dataSourceCode 数据源标识码
     * @return {@link JdbcTemplate}
     */
    private JdbcTemplate getJdbcTemplate(String dataSourceCode) {
        return jdbcTemplateCache.computeIfAbsent(dataSourceCode, code ->
                new JdbcTemplate(manager.get(code)));
    }

    @Override
    public List<Map<String, Object>> query(String dataSourceCode, String sql) {
        return getJdbcTemplate(dataSourceCode).queryForList(sql);
    }

    @Override
    public int executeUpdate(String dataSourceCode, String sql) {
        return getJdbcTemplate(dataSourceCode).update(sql);
    }
}
