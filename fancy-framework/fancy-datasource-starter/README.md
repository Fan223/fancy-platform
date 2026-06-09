# `@Ds` 注解执行流程

1. `@Ds("xxx")`, 执行 "xxx" 数据源
2. `DsAspect` 会拦截到这个注解, 获取到注解的值 "xxx", 并将其放入 `DataSourceContextHolder` 中
3. 执行原方法, 也就是 Mapper 中的方法, 这个方法会执行 SQL
4. `DynamicRoutingDataSource` 会拦截到这个 SQL 执行, 并调用 `determineTargetDataSource()` 方法获取数据源
5. `determineTargetDataSource()` 方法内部先调用 `determineCurrentLookupKey()` 方法，从 `DataSourceContextHolder`
   中获取到当前的数据源标识, 也就是 "xxx"
6. 根据标识从 `DynamicDataSourceManager` 中获取对应的数据源, 获取不到则使用默认数据源, 然后执行 SQL
7. `DynamicDataSourceManager` 初始化时通过 `DatasourceProvider` 获取到加载的数据源，支持动态刷新
8. SQL 执行完成后, `DsAspect` 会将 `DataSourceContextHolder` 中的当前数据源标识清除掉, 以免影响到后续的 SQL 执行

# 动态 SQL

1. 在前面的基础上，如果需要动态执行 SQL，也就是将 SQL 作为参数传进来直接执行，可以使用 `DynamicSqlExecutor` 来执行 SQL
2. `DynamicSqlExecutor` 会通过 `DynamicDataSourceManager` 获取到当前的数据源, 默认的 `JdbcDynamicSqlExecutor` 会使用
   `JdbcTemplate` 来执行 SQL
