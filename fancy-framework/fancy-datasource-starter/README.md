# 执行流程

1. `@DS("xxx")`, 执行 "xxx" 数据源
2. `DsAspect` 会拦截到这个注解, 获取到注解的值 "xxx", 并将其放入 `DataSourceContextHolder` 中
3. 执行原方法, 也就是 Mapper 中的方法, 这个方法会执行 SQL
4. `DynamicRoutingDataSource` 会拦截到这个 SQL 执行, 并调用 `determineCurrentLookupKey()` 方法
5. `determineCurrentLookupKey()` 方法会从 `DataSourceContextHolder` 中获取到当前的数据源标识, 也就是 "xxx"
6. `DynamicRoutingDataSource` 会根据这个标识找到对应的数据源, 获取到 `Connection`, 并执行 SQL
7. SQL 执行完成后, `DsAspect` 会将 `DataSourceContextHolder` 中的当前数据源标识清除掉, 以免影响到后续的 SQL 执行