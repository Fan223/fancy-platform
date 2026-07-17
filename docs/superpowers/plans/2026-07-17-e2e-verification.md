# 用户认证信息与业务信息分离 - 端到端验证报告

> 本次为代码调整与单元测试验证。完整的端到端验证（启动服务 + 注册 + 登录 + 改密 + 用新密码登录）由用户自行执行。

## 已完成的代码改动

### 1. 新增模块 `fancy-auth-api`

- `fancy-platform/fancy-auth-api/pom.xml`
- `fancy-auth-api/.../pojo/enums/IdentityType.java`（USERNAME/PHONE/EMAIL/GITHUB/WECHAT/QQ）
- `fancy-auth-api/.../pojo/dto/AuthBindRequest.java`
- `fancy-auth-api/.../pojo/dto/ChangePasswordRequest.java`
- `fancy-auth-api/.../service/AuthUserApi.java`（Feign 客户端：`POST /auth/users/bind`、
  `PUT /auth/users/{userId}/password`）
- 在 `fancy-platform/pom.xml` 注册模块

### 2. 平台父 pom 调整 `fancy-platform-parent/pom.xml`

- 新增 `spring-cloud.version = 2025.1.0` 属性
- 新增 `spring-cloud-dependencies` BOM import（解决 openfeign 版本未管理问题）
- 已有 `fancy-toolkit 2.0.0` 依赖管理

### 3. `fancy-authorization-server` 改造

- pom 改动：
    - 移除 `fancy-iam-api` 依赖
    - 新增 `fancy-auth-api` 依赖
    - 新增 `spring-boot-starter-validation`（DTO 注解）
    - 新增 `spring-boot-starter-test`（单测）
- 新增 `pojo/entity/UserIdentityDO.java`
- 新增 `mapper/UserIdentityMapper.java`
- 新增 `service/UserIdentityService.java` + `UserIdentityServiceImpl.java`
- **重写** `service/impl/UserDetailsServiceImpl.java`（查本地库 + 不再调远端）
- 新增 `service/AuthUserService.java` + `AuthUserServiceImpl.java`（bind / changePassword）
- 新增 `controller/AuthUserController.java`（`POST /auth/users/bind`、`PUT /auth/users/{userId}/password`）
- 新增 `handler/AuthUserExceptionAdvice.java`（`IllegalStateException` → 409，`IllegalArgumentException` → 400）
- **stub 化** OAuth2 第三方登录链路（spec §8 范围之外）：
    - `FancyOidcUserService`、`FancyOAuth2UserService`、`OAuth2UserConverter`、`OAuth2UserConverterManager`、
      `GiteeOAuth2UserConverter`、`GithubOAuth2UserConverter` 改为占位实现，第三方登录用户创建流程暂不实现
- `config/SecurityConfig.java` 放行 `/auth/users/bind` 路径
- 新增 `resources/sql/user_identity.sql`（建表语句）

### 4. `fancy-iam-api` 清理

- 删除 `pojo/entity/UserIdentityDO.java`
- 删除 `service/UserApiService.java`
- 简化 `pojo/bo/UserBO.java`（移除 `userIdentities` 字段）

### 5. `fancy-iam` 服务改造

- pom 新增 `fancy-auth-api` 依赖
- 删除 IAM 端的 `UserIdentityMapper`、`UserIdentityService`、`UserIdentityServiceImpl`、`UserIdentityController`、
  `UserIdentityDTO`、`UserIdentityVO`
- `UserService` 接口移除 `getByIdentifier`、`createUser`，新增 `createUserWithAuth(UserDTO)`
- `UserServiceImpl` 改造：`createUserWithAuth` 在同一事务中落 IAM 业务用户 + 调 `authUserApi.bind(...)`
- `UserController` 移除 `/auth/*` 端点（`/iam/users/auth/{identifier}`、`/iam/users/auth/creatUser`）；`POST /iam/users` 调用
  `createUserWithAuth`
- `UserVO` 移除 `userIdentities` 字段
- `UserDTO` 新增 `username` 字段
- 修复 `RolePermissionDO` 与 `UserRoleDO` 的 MetaDO import（包名 `fancy.starter.mybatis.plus.entity` 而非
  `fan.fancy.starter.mybatis.plus.entity`）

## 验证结果

### 编译

- `fancy-auth-api`：BUILD SUCCESS
- `fancy-authorization-server`：BUILD SUCCESS
- `fancy-iam`：BUILD SUCCESS

### 单元测试

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
   - fan.fancy.server.authorization.service.impl.AuthUserServiceImplTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
   - fan.fancy.server.authorization.service.impl.UserDetailsServiceImplTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
   - fan.fancy.server.authorization.service.impl.UserIdentityServiceImplTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

8 个单元测试全部通过：

- `UserIdentityServiceImplTest`：2 个
- `UserDetailsServiceImplTest`：2 个
- `AuthUserServiceImplTest`：4 个（bind 成功、bind 重复拒绝、changePassword 旧密码错误拒绝、changePassword 成功更新哈希）

## 端到端验证（待用户执行）

> 端到端需要 user_identity 表结构确认。本报告编写时，fancy_iam 库中已存在 `user_identity` 表，但其字段定义与本计划 Task 1
> 新建的 SQL 不一致（`type`+`identity` 字段 vs `identity_type` 单一字段；`credential` 而非 `credential_hash`；有
`delete_time` 软删除字段）。需要用户决定：
> 1. 用现有表结构，修改 Entity 字段名适配；
> 2. 删表重建为本计划定义的结构。
>
> 表结构确认后，可按以下步骤端到端验证：
> 1. 启动 `fancy-iam` 服务（`cd fancy-space && mvn -pl fancy-services/fancy-iam spring-boot:run`）
> 2. 启动 `fancy-authorization-server`（
     `cd fancy-platform && mvn -pl fancy-services/fancy-authorization-server spring-boot:run`）
> 3. 浏览器打开 `http://localhost:10100/login` 测试用户名密码登录
> 4. 通过 IAM 的 `POST /iam/users` 创建用户（带 username + password），验证 auth 服务的 `user_identity` 表自动插入 BCrypt
     哈希
> 5. 调用 auth 服务的 `PUT /auth/users/{userId}/password` 修改密码
> 6. 用新密码重新登录验证

## spec 验收标准对照

| spec § 验收项                                             | 状态                                                                    |
|--------------------------------------------------------|-----------------------------------------------------------------------|
| `fancy-authorization-server` 的 pom 不依赖 `fancy-iam-api` | 已移除                                                                   |
| `fancy-iam` 的 pom 依赖 `fancy-auth-api`                  | 已新增                                                                   |
| `UserDetailsServiceImpl` 不再调任何外部 HTTP/Feign            | 已重写为查本地 `user_identity`                                               |
| `AuthUserServiceImpl` 的 4 个单测全过                        | 通过                                                                    |
| `user_identity` 表中存储的是 BCrypt 哈希，无明文密码                 | `AuthUserServiceImpl.bind` 调 `passwordEncoder.encode(credential)` 后入库 |
