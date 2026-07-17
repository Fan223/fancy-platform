# 用户认证信息与业务信息分离

- 日期：2026-07-17
- 状态：待用户审阅
- 范围：`fancy-platform/fancy-authorization-server`、`fancy-platform/fancy-auth-api`（新建）、`fancy-space/fancy-services/fancy-iam`

## 1. 背景与问题

当前 `fancy-authorization-server` 通过 `fancy-iam-api` 中的 Feign 客户端 `UserApiService.getByIdentifier()` 拉取用户认证信息，存在以下问题：

1. **明文密码在网络上传输**：`UserDetailsServiceImpl.loadUserByUsername` 从 IAM 拿到 `credential` 字段（明文密码），再 `passwordEncoder.encode(...)` 跟用户输入的明文密码比较。密码在 HTTP 响应中以明文形式存在。
2. **认证服务反向依赖业务服务**：`fancy-platform` 依赖 `fancy-space` 的 maven artifact，CI/CD 形成跨仓库耦合。
3. **认证信息与业务信息混在同一张表**：`user_identity` 表的字段（用户名、密码、手机、第三方 ID）属于认证概念，本应归认证服务所有，但物理上与 `user`（昵称、头像）一同存放在 IAM 库中。
4. **公共模块暴露认证实体**：`UserIdentityDO`（含 `credential` 字段）放在 `fancy-iam-api` 公共 jar 中。

## 2. 目标

- 认证服务**自治**：不依赖任何业务服务即可完成登录。
- 业务服务**调用认证服务**：注册、修改密码等场景由 IAM 主动调 auth。
- 依赖方向**单向**：`iam → auth-api → authorization-server`，且 `authorization-server` 不依赖任何业务模块。
- 消除明文密码在网络上传输。

## 3. 架构

### 3.1 模块与依赖

```
fancy-platform/
├── fancy-auth-api/                  新建。纯接口模块（Feign 接口 + DTO），无业务实现、无数据库依赖。
│                                    被 authorization-server 和 fancy-iam 同时依赖。
└── fancy-authorization-server/      认证服务。实现 fancy-auth-api 中的接口；不再依赖任何 iam 模块。

fancy-space/
├── fancy-space-api/fancy-iam-api/   移除 UserApiService、UserIdentityDO、UserBO 中与认证相关的字段。
└── fancy-services/fancy-iam/        业务用户服务。依赖 fancy-auth-api（通过 Feign 调 auth）。
```

依赖关系（编译期）：

| 模块 | 依赖 |
|---|---|
| fancy-auth-api | 无（仅 Feign + DTO） |
| fancy-authorization-server | fancy-auth-api（引用 DTO、实现 Feign Controller） |
| fancy-iam | fancy-auth-api（调用 Feign 接口） |
| fancy-iam-api | （移除认证相关导出后）仅供 IAM 内部使用 |

`fancy-authorization-server` **不依赖** `fancy-iam-api` 或 `fancy-iam`。

### 3.2 数据归属

| 表 | 所属库 | 字段 |
|---|---|---|
| `user` | IAM 库 | `id`（雪花 ID，bigint，主键）、`nickname`、`avatar`、`gender`、`birthday`、审计字段 |
| `user_identity` | auth 库 | `id`（雪花 ID）、`user_id`（与 `user.id` 同类型，关联 IAM 主键）、`identity_type`（username/phone/email/github/...）、`identifier`、`credential_hash`（BCrypt）、审计字段 |

`user_id` 由 IAM 生成（雪花 ID），auth 侧只存这个值作为外键关联。两侧 `id` 类型统一为 `bigint`。

### 3.3 数据流

**登录（用户名 + 密码）**：
1. 用户请求 authorization-server 登录页。
2. `UserDetailsServiceImpl.loadUserByUsername(username)` 直接查 auth 库 `user_identity`（按 `identifier + identity_type=USERNAME`）。
3. 拿到 `credential_hash`，与用户输入的明文密码通过 `BCryptPasswordEncoder.matches()` 校验。
4. 校验通过后 Spring Security 颁发 OAuth2 token，token 中携带 `userId`（claim）。

**创建用户（业务注册）**：
1. 调用方（admin 后台）调 IAM `POST /iam/users` 创建业务用户，IAM 内部生成雪花 `userId` 并落库。
2. 调用方拿到返回的 `userId`，调 auth-api `POST /auth/users/bind` 提交 `userId + identifier + credential`。
3. auth 服务落库 `user_identity`。
4. 若第 2 步失败：调用方需自行决定是否回滚 IAM 业务用户（当前阶段为手工补偿；详见 §6）。

**修改密码**：
1. 用户点"修改密码"，前端直接请求 authorization-server `PUT /auth/users/{userId}/password`。
2. auth 服务校验旧密码（从 auth 库查 `credential_hash`，`matches` 旧密码），通过后更新 `credential_hash`。
3. IAM **不参与**该流程。

## 4. 接口定义（fancy-auth-api）

模块 `fan:fancy-auth-api:1.0.0`。

### 4.1 实体 / DTO

```java
// IdentityType 枚举定义在 fancy-auth-api 模块中，auth 服务与 IAM 共享。
public enum IdentityType {
    USERNAME(1), PHONE(2), EMAIL(3),
    GITHUB(4), WECHAT(5), QQ(6);
}

// 绑定请求
@Data
public class AuthBindRequest {
    private Long userId;           // 雪花 ID，由 IAM 生成
    private IdentityType identityType;
    private String identifier;     // 用户名/手机/邮箱/第三方 openid
    private String credential;     // 明文密码（仅绑定时一次性传输，auth 端立即哈希入库）
}

// 修改密码请求
@Data
public class ChangePasswordRequest {
    private String oldCredential;  // 明文
    private String newCredential;  // 明文
}

// 通用响应包装（复用 fancy-toolkit 中的 Response）
```

### 4.2 Feign 客户端

```java
@FeignClient(name = "fancy-authorization-server", path = "/auth/users")
public interface AuthUserApi {

    @PostMapping("/bind")
    Response<Void> bind(@RequestBody AuthBindRequest req);

    @PutMapping("/{userId}/password")
    Response<Void> changePassword(@PathVariable Long userId,
                                   @RequestBody ChangePasswordRequest req);
}
```

### 4.3 auth 服务端 Controller（authorization-server 内部实现）

```java
@RestController
@RequestMapping("/auth/users")
public class AuthUserController {

    @PostMapping("/bind")
    public Response<Void> bind(@RequestBody @Valid AuthBindRequest req) {
        authUserService.bind(req);
        return Response.success();
    }

    @PutMapping("/{userId}/password")
    public Response<Void> changePassword(@PathVariable Long userId,
                                          @RequestBody @Valid ChangePasswordRequest req) {
        // 内部基于 SecurityContext 校验当前登录用户 == userId
        authUserService.changePassword(userId, req);
        return Response.success();
    }
}
```

> 鉴权：`/bind` 内部接口，仅服务间调用，需要内部 token 校验（复用现有 `InternalRequestInterceptor` 或类似机制）。`/password` 走 OAuth2 用户上下文。

## 5. 关键实现要点

### 5.1 authorization-server 改造

- 新增 `user_identity` 表（MyBatis-Plus 实体 + Mapper + Service）。
- `UserDetailsServiceImpl` 改为注入本地 `UserIdentityService`：
  ```java
  UserIdentityDO identity = userIdentityService.getByIdentifier(USERNAME, username);
  if (identity == null) throw new UsernameNotFoundException(username);
  return User.withUsername(username)
             .password(identity.getCredentialHash())
             .authorities(...)  // 从 token claims 取，不在此处查 IAM
             .build();
  ```
- 删除对 `fancy-iam-api` 的依赖与 `UserApiService` 注入。
- `pom.xml`：移除 `<artifactId>fancy-iam-api</artifactId>`，新增 `<artifactId>fancy-auth-api</artifactId>`（仅运行时需要时，auth 服务自身可能不需要——因为它实现接口，调用方依赖 auth-api；不过为了 Controller 引用 DTO 类型，仍需依赖）。

### 5.2 fancy-iam 改造

- `fancy-iam-api` 移除 `UserApiService`、`UserBO.userIdentities`、`UserIdentityDO`（或保留仅 IAM 内部可见）。
- `fancy-iam` 在 `pom.xml` 移除不再需要的导出，**新增** `<artifactId>fancy-auth-api</artifactId>` 依赖。
- `UserService.createUser` 流程：本地落 `user` 表 → 通过 `AuthUserApi` 调 auth 服务的 `bind`。
- `UserController` 修改密码相关接口移除，改成由前端直接调 auth 服务。

### 5.3 雪花 ID

- 使用项目内已封装的雪花 ID 工具类（`MetaDO` 继承的 ID 生成机制保持不变）。
- `user` 表主键、auth 库 `user_identity.user_id` 均使用同一雪花 ID。

### 5.4 错误处理

- `bind` 失败（auth 不可用）：IAM 业务用户已落库 → 抛出业务异常，调用方记录失败并人工/异步重试（详见 §6）。
- `changePassword` 旧密码错误：返回 400 + 业务码。
- `loadUserByUsername` 未找到：抛 `UsernameNotFoundException`，由 Spring Security 统一处理。

## 6. 一致性与失败处理

创建用户是两步调用（IAM 落库 → auth 落库），非分布式事务。当前阶段策略：

- **接受最终不一致**：测试开发阶段，失败时由调用方（admin 后台）捕获异常并提示用户"账号创建失败，请重试"。手动重试时检查 IAM 是否已存在该 `userId`。
- **不引入消息队列 / 本地消息表**：超出当前需求范围；如未来生产化，可加入 Outbox + 重试。

文档**不**包含旧数据迁移方案（按用户确认，旧数据丢弃，重建测试数据）。

## 7. 测试要点

- **单元测试**：
  - `UserDetailsServiceImpl`：传入未知用户名抛 `UsernameNotFoundException`；传入正确用户名 + 正确密码通过；正确用户名 + 错误密码不通过。
  - `AuthUserServiceImpl.bind`：相同 `(identityType, identifier)` 唯一索引冲突时抛业务异常。
  - `AuthUserServiceImpl.changePassword`：旧密码错误拒绝；新密码哈希写入库。
- **集成测试**：
  - 启动 authorization-server + 嵌入式 H2 + IAM 模拟 Feign。
  - 端到端：注册 → 登录 → 颁发 token → 改密 → 用旧密码登录失败 → 用新密码登录成功。
- **契约测试**：
  - `fancy-auth-api` 的 Feign 接口与 auth 服务 Controller 的路径/方法/请求体一致性。

## 8. 范围之外

- 旧数据迁移（用户确认不需要）。
- 第三方登录（Gitee/GitHub）的 token 存储：本设计仅涉及用户名密码路径；现有 `FancyOAuth2UserService` 与 `FederatedIdentityIdTokenCustomizer` 不在本设计改动范围，文件层不删除，但 `user_identity.identity_type` 仍为它们预留枚举位。
- 分布式事务 / Outbox 模式。
- 限流、密码强度策略、密码过期：遵循 Spring Security 默认即可，后续单独设计。

## 9. 实施步骤（概要）

1. 在 `fancy-platform` 新建 `fancy-auth-api` 模块：定义 `IdentityType`、`AuthBindRequest`、`ChangePasswordRequest`、`AuthUserApi`。
2. 在 `fancy-authorization-server`：
   - 新增 `user_identity` 表 + `UserIdentityDO` / `Mapper` / `Service`（仅放本地，不放入 api 模块）。
   - 实现 `AuthUserController`。
   - 改造 `UserDetailsServiceImpl` 查本地库 + BCrypt 校验。
   - 调整 `pom.xml`。
3. 在 `fancy-space/fancy-iam-api`：移除 `UserApiService`、`UserBO` 中 `userIdentities`、`UserIdentityDO`。
4. 在 `fancy-space/fancy-iam`：pom 新增 `fancy-auth-api`；`UserServiceImpl.createUser` 调 `AuthUserApi.bind`；删除改密相关 controller。
5. 单元 + 集成测试。
6. 端到端验证（启动 auth + iam + 前端，注册 → 登录 → 改密 → 登录）。
