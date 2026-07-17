# 用户认证信息与业务信息分离 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将用户认证信息（用户名/密码 hash）从 fancy-iam 库迁到 authorization-server 库；消除明文密码网络传输；将 platform → space 的反向依赖改为 iam → auth-api → auth 的单向调用链。

**Architecture:**
- 新建 `fancy-auth-api` 公共模块（仅 Feign 接口 + DTO），作为 `iam` 调用 `authorization-server` 的契约。
- `authorization-server` 新增 `user_identity` 表与本地服务；`UserDetailsServiceImpl` 改为查本库 + BCrypt 校验。
- `fancy-iam` 通过 `AuthUserApi` Feign 客户端调 `authorization-server` 完成"绑定账号"和"修改密码"。
- `fancy-iam-api` 中与认证相关的 `UserApiService`、`UserIdentityDO`、`UserBO.userIdentities` 被删除。

**Tech Stack:** Spring Boot 4.1.0、Spring Authorization Server、Spring Cloud OpenFeign、MyBatis-Plus 3.x、JUnit 5 + Mockito、Java 25。

## Global Constraints

- 所有用户 `id` / `user_id` 统一使用雪花 ID（项目内已有 `MetaDO` 基类生成的 `bigint`），不在本次计划中引入 UUID。
- 密码哈希使用 BCrypt（Spring Security `BCryptPasswordEncoder`），强度默认 10。
- 不引入 Flyway/Liquibase；建表 SQL 放在 `src/main/resources/sql/` 目录供手动执行。
- 不做旧数据迁移（用户已确认）。
- 测试仅使用 JUnit 5 + Mockito，不引入 spring-boot-test/H2，保持轻量。
- `fancy-auth-api` 模块是纯接口模块，**禁止**包含任何业务实现、数据库或 Web 依赖。
- `authorization-server` **不依赖** `fancy-iam-api` 或 `fancy-iam`；`fancy-iam` **不依赖** `fancy-authorization-server`。
- 内部服务间调用复用 `fancy.security.internal-token` 机制（已有 `InternalRequestInterceptor` / `FeignConfig`），新接口 `/auth/users/bind` 需校验该 header。
- 数据库连接复用现有 `fancy_iam` 库（同一 MySQL 实例），通过独立表（`user_identity`）做物理隔离；如未来需要拆库，仅改 datasource URL 即可，代码无需调整。

---

## 文件结构总览

### 新建

```
fancy-platform/
├── fancy-auth-api/
│   ├── pom.xml
│   └── src/main/java/.../
│       ├── pojo/enums/IdentityType.java
│       ├── pojo/dto/AuthBindRequest.java
│       ├── pojo/dto/ChangePasswordRequest.java
│       └── service/AuthUserApi.java
└── fancy-services/fancy-authorization-server/
    ├── src/main/java/.../pojo/entity/UserIdentityDO.java
    ├── src/main/java/.../mapper/UserIdentityMapper.java
    ├── src/main/java/.../service/UserIdentityService.java
    ├── src/main/java/.../service/impl/UserIdentityServiceImpl.java
    ├── src/main/java/.../service/AuthUserService.java
    ├── src/main/java/.../service/impl/AuthUserServiceImpl.java
    ├── src/main/java/.../controller/AuthUserController.java
    ├── src/main/java/.../handler/AuthUserExceptionAdvice.java
    ├── src/main/resources/sql/user_identity.sql
    └── src/test/java/.../
        ├── service/impl/UserDetailsServiceImplTest.java
        ├── service/impl/AuthUserServiceImplTest.java
        └── service/impl/UserIdentityServiceImplTest.java
```

### 修改

```
fancy-platform/
├── pom.xml                                              （新增 fancy-auth-api 模块声明）
├── fancy-services/pom.xml                               （新增 fancy-auth-api 模块声明）
└── fancy-services/fancy-authorization-server/
    ├── pom.xml                                          （移除 iam-api、加 auth-api）
    └── src/main/java/.../service/impl/UserDetailsServiceImpl.java

fancy-space/
├── fancy-space-api/fancy-iam-api/
│   └── src/main/java/.../pojo/bo/UserBO.java           （删除 userIdentities 字段）
│   └── src/main/java/.../pojo/entity/UserIdentityDO.java（删除整个文件）
│   └── src/main/java/.../service/UserApiService.java   （删除整个文件）
└── fancy-services/fancy-iam/
    ├── pom.xml                                          （加 auth-api 依赖）
    ├── src/main/java/.../controller/UserController.java（删除改密接口）
    └── src/main/java/.../service/impl/UserServiceImpl.java（createUser 调 AuthUserApi.bind）
```

### 接口契约

- `AuthUserApi.bind(AuthBindRequest)` → `Response<Void>`
- `AuthUserApi.changePassword(Long userId, ChangePasswordRequest)` → `Response<Void>`

---

## Task 1: 建 user_identity 表 SQL

**Files:**
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/resources/sql/user_identity.sql`

**Interfaces:**
- Produces: `user_identity` 表（数据库对象）。本任务无代码契约。

- [ ] **Step 1: 创建 SQL 文件**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/resources/sql/user_identity.sql`：

```sql
-- 用户认证表（用户名/手机/邮箱/第三方 openid + 凭据哈希）
-- 用于 authorization-server 本地身份认证。
-- 关联 IAM 业务用户：user_id = fancy_iam.user.id

CREATE TABLE IF NOT EXISTS user_identity (
    id              BIGINT       NOT NULL COMMENT '雪花ID主键',
    user_id         BIGINT       NOT NULL COMMENT '关联 IAM 业务用户 ID',
    identity_type   TINYINT      NOT NULL COMMENT '1=USERNAME 2=PHONE 3=EMAIL 4=GITHUB 5=WECHAT 6=QQ',
    identifier      VARCHAR(128) NOT NULL COMMENT '身份标识(用户名/手机/邮箱/第三方 openid)',
    credential_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 哈希(密码)或第三方 access_token',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_identity_type_identifier (identity_type, identifier),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证身份表';
```

- [ ] **Step 2: 在 README 中标注手动执行（可选，不阻塞后续任务）**

若 `fancy-services/fancy-authorization-server/README.md` 不存在则跳过。存在则在 README 顶部加一行：

```
## 初始化
执行 src/main/resources/sql/user_identity.sql 创建 user_identity 表。
```

- [ ] **Step 3: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-services/fancy-authorization-server/src/main/resources/sql/user_identity.sql
git commit -m "feat(auth): add user_identity table init sql"
```

---

## Task 2: 新建 fancy-auth-api 模块（pom + 枚举）

**Files:**
- Create: `fancy-platform/fancy-auth-api/pom.xml`
- Create: `fancy-platform/fancy-auth-api/src/main/java/.../pojo/enums/IdentityType.java`

**Interfaces:**
- Produces: `IdentityType` 枚举（USERNAME/PHONE/EMAIL/GITHUB/WECHAT/QQ）。

- [ ] **Step 1: 创建 pom.xml**

新建 `fancy-platform/fancy-auth-api/pom.xml`：

```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>fan</groupId>
        <artifactId>fancy-platform-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../fancy-platform-parent</relativePath>
    </parent>
    <artifactId>fancy-auth-api</artifactId>
    <packaging>jar</packaging>
    <name>fancy-auth-api</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>fan</groupId>
            <artifactId>fancy-toolkit</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 在 platform 根 pom 注册模块**

修改 `fancy-platform/pom.xml`，在 `<modules>` 块中按字母序加入 `fancy-auth-api`（先 Read 确认现有 modules 列表后插入）。

- [ ] **Step 3: 创建 IdentityType 枚举**

新建 `fancy-platform/fancy-auth-api/src/main/java/fan/fancy/api/auth/pojo/enums/IdentityType.java`：

```java
package fan.fancy.api.auth.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 身份类型.
 *
 * @author Fan
 */
@Getter
@AllArgsConstructor
public enum IdentityType {

    USERNAME(1),
    PHONE(2),
    EMAIL(3),
    GITHUB(4),
    WECHAT(5),
    QQ(6);

    private final int code;
}
```

- [ ] **Step 4: 编译验证**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-auth-api -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-auth-api/ pom.xml
git commit -m "feat(auth-api): create fancy-auth-api module and IdentityType enum"
```

---

## Task 3: fancy-auth-api 新增 DTO

**Files:**
- Create: `fancy-platform/fancy-auth-api/src/main/java/.../pojo/dto/AuthBindRequest.java`
- Create: `fancy-platform/fancy-auth-api/src/main/java/.../pojo/dto/ChangePasswordRequest.java`

**Interfaces:**
- Produces: `AuthBindRequest`（userId/identityType/identifier/credential）、`ChangePasswordRequest`（oldCredential/newCredential）。

- [ ] **Step 1: 创建 AuthBindRequest**

新建 `fancy-platform/fancy-auth-api/src/main/java/fan/fancy/api/auth/pojo/dto/AuthBindRequest.java`：

```java
package fan.fancy.api.auth.pojo.dto;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绑定认证账号请求.
 *
 * @author Fan
 */
@Data
public class AuthBindRequest {

    /**
     * 业务用户ID（雪花ID，由 IAM 生成）.
     */
    @NotNull
    private Long userId;

    /**
     * 身份类型.
     */
    @NotNull
    private IdentityType identityType;

    /**
     * 身份标识（用户名/手机/邮箱/第三方 openid）.
     */
    @NotBlank
    private String identifier;

    /**
     * 明文密码（仅绑定时一次性传输，认证侧立即 BCrypt 哈希入库）.
     */
    @NotBlank
    private String credential;
}
```

- [ ] **Step 2: 创建 ChangePasswordRequest**

新建 `fancy-platform/fancy-auth-api/src/main/java/fan/fancy/api/auth/pojo/dto/ChangePasswordRequest.java`：

```java
package fan.fancy.api.auth.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求.
 *
 * @author Fan
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码（明文）.
     */
    @NotBlank
    private String oldCredential;

    /**
     * 新密码（明文）.
     */
    @NotBlank
    private String newCredential;
}
```

- [ ] **Step 3: 编译验证**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-auth-api -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-auth-api/src/main/java/.../pojo/dto/
git commit -m "feat(auth-api): add AuthBindRequest and ChangePasswordRequest DTOs"
```

> 提示：若 IDE 报 `@NotBlank`/`@NotNull` 找不到，提示 fancy-auth-api 的 pom 还需加 `spring-boot-starter-validation`。当前 pom 暂未加，等本计划 Task 6（authorization-server 引入 auth-api）一起在 server pom 引入 validation 即可（DTO 本身编译不需要 jakarta.validation-api，运行时才需要）。但若 mvn compile 报缺包，则在 fancy-auth-api/pom.xml 加：
> ```xml
> <dependency>
>     <groupId>org.springframework.boot</groupId>
>     <artifactId>spring-boot-starter-validation</artifactId>
> </dependency>
> ```

---

## Task 4: fancy-auth-api 新增 Feign 客户端

**Files:**
- Create: `fancy-platform/fancy-auth-api/src/main/java/.../service/AuthUserApi.java`

**Interfaces:**
- Produces: `AuthUserApi` Feign 接口（`bind`、`changePassword`），路径 `/auth/users`。

- [ ] **Step 1: 创建 Feign 接口**

新建 `fancy-platform/fancy-auth-api/src/main/java/fan/fancy/api/auth/service/AuthUserApi.java`：

```java
package fan.fancy.api.auth.service;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fancy.boot.core.http.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 认证服务用户 API.
 *
 * @author Fan
 */
@FeignClient(name = "fancy-authorization-server", path = "/auth/users")
public interface AuthUserApi {

    /**
     * 绑定认证账号（业务用户创建后调用）.
     */
    @PostMapping("/bind")
    Response<Void> bind(@RequestBody AuthBindRequest request);

    /**
     * 修改密码.
     */
    @PutMapping("/{userId}/password")
    Response<Void> changePassword(@PathVariable("userId") Long userId,
                                  @RequestBody ChangePasswordRequest request);
}
```

- [ ] **Step 2: 确认 `fancy.boot.core.http.Response` 类型存在**

Run: `grep -r "class Response" "D:/Fan/Projects/fancy-platform/fancy-framework" 2>/dev/null | head -5`
Expected: 找到 `fancy.boot.core.http.Response` 的定义文件。

若未找到，Read 一下 `fancy-toolkit` jar 中的类或替换为项目内已有的统一响应类型（查看 `fancy-iam-api` 中 `UserApiService` 用的什么）。当前 IAM 的 `UserApiService.createUser` 返回的是 `fancy.boot.core.http.Response<Integer>`，说明该类型存在。**确认后继续**。

- [ ] **Step 3: 编译验证**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-auth-api -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-auth-api/src/main/java/.../service/AuthUserApi.java
git commit -m "feat(auth-api): add AuthUserApi feign client"
```

---

## Task 5: authorization-server 新增 user_identity 实体与 Mapper

**Files:**
- Modify: `fancy-platform/fancy-services/fancy-authorization-server/pom.xml`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../pojo/entity/UserIdentityDO.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../mapper/UserIdentityMapper.java`

**Interfaces:**
- Produces: `UserIdentityDO`（id/userId/identityType/identifier/credentialHash + 审计字段）、`UserIdentityMapper`（继承 `BaseMapper<UserIdentityDO>`）。

- [ ] **Step 1: 修改 authorization-server pom 依赖**

修改 `fancy-platform/fancy-services/fancy-authorization-server/pom.xml`：

- 移除 `<dependency> ... fancy-iam-api ... </dependency>` 整个块。
- 新增 `<dependency>`：
  ```xml
  <dependency>
      <groupId>fan</groupId>
      <artifactId>fancy-auth-api</artifactId>
      <version>1.0.0</version>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
  </dependency>
  ```

> 注：移除 fancy-iam-api 时记得连同其上的 fancy-mybatis-plus-spring-boot-starter 等**保留**（auth 服务仍需 MyBatis-Plus）。

- [ ] **Step 2: 在 fancy-services 父 pom 注册 auth-server 旁边的模块**

修改 `fancy-platform/fancy-services/pom.xml`：在 `<modules>` 块中新增：

```xml
<module>fancy-authorization-server</module>
```

（已存在则跳过本步。Read 确认当前 modules 列表，避免重复添加。）

- [ ] **Step 3: 创建 UserIdentityDO 实体**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/pojo/entity/UserIdentityDO.java`：

```java
package fan.fancy.server.authorization.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fancy.starter.mybatis.plus.entity.MetaDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户认证身份实体.
 *
 * @author Fan
 */
@TableName("user_identity")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserIdentityDO extends MetaDO {

    /**
     * 业务用户ID.
     */
    private Long userId;

    /**
     * 身份类型.
     */
    private IdentityType identityType;

    /**
     * 身份标识.
     */
    private String identifier;

    /**
     * 凭据哈希（BCrypt）.
     */
    private String credentialHash;
}
```

> `MetaDO` 提供 `id`（雪花主键）与 `createTime/updateTime` 审计字段。

- [ ] **Step 4: 创建 UserIdentityMapper**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/mapper/UserIdentityMapper.java`：

```java
package fan.fancy.server.authorization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fan.fancy.api.auth.pojo.enums.IdentityType;
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
```

- [ ] **Step 5: 编译验证**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server -am compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-services/fancy-authorization-server/pom.xml fancy-services/fancy-authorization-server/src/main/java/.../pojo/entity/UserIdentityDO.java fancy-services/fancy-authorization-server/src/main/java/.../mapper/UserIdentityMapper.java fancy-services/pom.xml
git commit -m "feat(auth): add user_identity entity and mapper; remove iam-api dep"
```

---

## Task 6: authorization-server 改造 UserDetailsServiceImpl + 编写测试

**Files:**
- Modify: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/impl/UserDetailsServiceImpl.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/UserIdentityService.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/impl/UserIdentityServiceImpl.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/.../service/impl/UserDetailsServiceImplTest.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/.../service/impl/UserIdentityServiceImplTest.java`

**Interfaces:**
- Consumes: `UserIdentityMapper.selectByIdentifier(int, String)`（来自 Task 5）。
- Produces: `UserIdentityService.getByIdentifier(IdentityType, String)`、`UserDetailsServiceImpl.loadUserByUsername(String)`。

- [ ] **Step 1: 创建 UserIdentityService 接口**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/service/UserIdentityService.java`：

```java
package fan.fancy.server.authorization.service;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;

/**
 * 用户认证身份服务.
 *
 * @author Fan
 */
public interface UserIdentityService {

    /**
     * 根据身份类型 + identifier 查询.
     */
    UserIdentityDO getByIdentifier(IdentityType identityType, String identifier);

    /**
     * 插入.
     */
    void save(UserIdentityDO userIdentity);
}
```

- [ ] **Step 2: 编写失败的 UserIdentityServiceImpl 单测（TDD）**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/fan/fancy/server/authorization/service/impl/UserIdentityServiceImplTest.java`：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.mapper.UserIdentityMapper;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {

    @Mock
    private UserIdentityMapper userIdentityMapper;

    @InjectMocks
    private UserIdentityServiceImpl userIdentityService;

    @Test
    void getByIdentifier_returnsMapperResult() {
        UserIdentityDO expected = new UserIdentityDO();
        expected.setId(1L);
        when(userIdentityMapper.selectByIdentifier(anyInt(), anyString())).thenReturn(expected);

        UserIdentityDO actual = userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void getByIdentifier_returnsNullWhenNotFound() {
        when(userIdentityMapper.selectByIdentifier(anyInt(), anyString())).thenReturn(null);

        UserIdentityDO actual = userIdentityService.getByIdentifier(IdentityType.USERNAME, "missing");

        assertThat(actual).isNull();
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -Dtest=UserIdentityServiceImplTest -q`
Expected: 编译失败（`UserIdentityServiceImpl` 不存在）。

- [ ] **Step 4: 实现 UserIdentityServiceImpl**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/service/impl/UserIdentityServiceImpl.java`：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.mapper.UserIdentityMapper;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Fan
 */
@Service
@AllArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityMapper userIdentityMapper;

    @Override
    public UserIdentityDO getByIdentifier(IdentityType identityType, String identifier) {
        return userIdentityMapper.selectByIdentifier(identityType.getCode(), identifier);
    }

    @Override
    public void save(UserIdentityDO userIdentity) {
        userIdentityMapper.insert(userIdentity);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -Dtest=UserIdentityServiceImplTest -q`
Expected: Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 6: 编写失败的 UserDetailsServiceImpl 单测（TDD）**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/fan/fancy/server/authorization/service/impl/UserDetailsServiceImplTest.java`：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_throwsWhenIdentityNotFound() {
        when(userIdentityService.getByIdentifier(any(), anyString())).thenReturn(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_returnsUserWithHashedCredential() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setUserId(42L);
        identity.setCredentialHash("$2a$10$hashed");
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(identity);

        UserDetails details = userDetailsService.loadUserByUsername("fan");

        assertThat(details.getUsername()).isEqualTo("fan");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
    }
}
```

- [ ] **Step 7: 重写 UserDetailsServiceImpl**

修改 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/impl/UserDetailsServiceImpl.java`，完整内容替换为：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} 实现类.
 *
 * @author Fan
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserIdentityService userIdentityService;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserIdentityDO identity = userIdentityService.getByIdentifier(IdentityType.USERNAME, username);
        if (identity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(username, identity.getCredentialHash(),
                AuthorityUtils.NO_AUTHORITIES);
    }
}
```

- [ ] **Step 8: 运行全部测试**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -q`
Expected: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 9: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-services/fancy-authorization-server/src/main/java/.../service/ fancy-services/fancy-authorization-server/src/test/
git commit -m "feat(auth): refactor UserDetailsService to query local user_identity"
```

---

## Task 7: authorization-server 新增 AuthUserService 与 AuthUserController

**Files:**
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/AuthUserService.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../service/impl/AuthUserServiceImpl.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../controller/AuthUserController.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../handler/AuthUserExceptionAdvice.java`
- Create: `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/.../service/impl/AuthUserServiceImplTest.java`

**Interfaces:**
- Produces:
  - `AuthUserService.bind(AuthBindRequest)` — 创建认证账号
  - `AuthUserService.changePassword(Long userId, ChangePasswordRequest)` — 修改密码
  - `AuthUserController` — REST 入口（`POST /auth/users/bind`、`PUT /auth/users/{userId}/password`）

- [ ] **Step 1: 编写失败的 AuthUserServiceImpl 单测**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/test/java/fan/fancy/server/authorization/service/impl/AuthUserServiceImplTest.java`：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.UserIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceImplTest {

    @Mock
    private UserIdentityService userIdentityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUserServiceImpl authUserService;

    @Test
    void bind_hashesCredentialAndInserts() {
        when(passwordEncoder.encode("plain")).thenReturn("$2a$10$hashed");
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(null);

        authUserService.bind(new AuthBindRequest() {{
            setUserId(7L);
            setIdentityType(IdentityType.USERNAME);
            setIdentifier("fan");
            setCredential("plain");
        }});

        ArgumentCaptor<UserIdentityDO> captor = ArgumentCaptor.forClass(UserIdentityDO.class);
        verify(userIdentityService).save(captor.capture());
        UserIdentityDO saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(7L);
        assertThat(saved.getIdentityType()).isEqualTo(IdentityType.USERNAME);
        assertThat(saved.getIdentifier()).isEqualTo("fan");
        assertThat(saved.getCredentialHash()).isEqualTo("$2a$10$hashed");
    }

    @Test
    void bind_rejectsDuplicateIdentifier() {
        when(userIdentityService.getByIdentifier(eq(IdentityType.USERNAME), eq("fan"))).thenReturn(new UserIdentityDO());

        try {
            authUserService.bind(new AuthBindRequest() {{
                setUserId(7L);
                setIdentityType(IdentityType.USERNAME);
                setIdentifier("fan");
                setCredential("plain");
            }});
        } catch (IllegalStateException e) {
            // expected
        }

        verify(userIdentityService, never()).save(any());
    }

    @Test
    void changePassword_rejectsWrongOldCredential() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setId(1L);
        identity.setUserId(7L);
        identity.setCredentialHash("$2a$10$old");
        when(userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan")).thenReturn(identity);
        when(passwordEncoder.matches("wrong", "$2a$10$old")).thenReturn(false);

        try {
            authUserService.changePassword(7L, new ChangePasswordRequest() {{
                setOldCredential("wrong");
                setNewCredential("new");
            }});
        } catch (IllegalArgumentException e) {
            // expected
        }

        verify(userIdentityService, never()).save(any());
    }

    @Test
    void changePassword_updatesHashOnSuccess() {
        UserIdentityDO identity = new UserIdentityDO();
        identity.setId(1L);
        identity.setUserId(7L);
        identity.setCredentialHash("$2a$10$old");
        when(userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan")).thenReturn(identity);
        when(passwordEncoder.matches("old", "$2a$10$old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("$2a$10$new");

        authUserService.changePassword(7L, new ChangePasswordRequest() {{
            setOldCredential("old");
            setNewCredential("new");
        }});

        assertThat(identity.getCredentialHash()).isEqualTo("$2a$10$new");
        verify(userIdentityService).save(identity);
    }
}
```

> 注：`changePassword` 实际中需要从当前登录上下文取 `identifier`（用户名），但本测试通过 mock `getByIdentifier` 验证逻辑。**实现层在 `AuthUserServiceImpl.changePassword` 内部需要外部传入 identifier**，或者通过注入 `SecurityContextHolder` 取出。本任务采用显式参数形式，Controller 侧从 `Authentication.getName()` 取。

- [ ] **Step 2: 运行测试确认失败**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -Dtest=AuthUserServiceImplTest -q`
Expected: 编译失败（`AuthUserServiceImpl` 不存在）。

- [ ] **Step 3: 创建 AuthUserService 接口**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/service/AuthUserService.java`：

```java
package fan.fancy.server.authorization.service;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;

/**
 * 认证服务（bind / changePassword）.
 *
 * @author Fan
 */
public interface AuthUserService {

    /**
     * 绑定认证账号.
     */
    void bind(AuthBindRequest request);

    /**
     * 修改密码.
     *
     * @param userId   业务用户ID
     * @param identifier  当前身份标识（用户名）
     * @param request  请求体
     */
    void changePassword(Long userId, String identifier, ChangePasswordRequest request);
}
```

- [ ] **Step 4: 实现 AuthUserServiceImpl**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/service/impl/AuthUserServiceImpl.java`：

```java
package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import fan.fancy.server.authorization.service.AuthUserService;
import fan.fancy.server.authorization.service.UserIdentityService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fan
 */
@Service
@AllArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private final UserIdentityService userIdentityService;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void bind(AuthBindRequest request) {
        if (userIdentityService.getByIdentifier(request.getIdentityType(), request.getIdentifier()) != null) {
            throw new IllegalStateException("identifier already bound: " + request.getIdentifier());
        }
        UserIdentityDO identity = new UserIdentityDO();
        identity.setUserId(request.getUserId());
        identity.setIdentityType(request.getIdentityType());
        identity.setIdentifier(request.getIdentifier());
        identity.setCredentialHash(passwordEncoder.encode(request.getCredential()));
        userIdentityService.save(identity);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String identifier, ChangePasswordRequest request) {
        UserIdentityDO identity = userIdentityService.getByIdentifier(IdentityType.USERNAME, identifier);
        if (identity == null || !identity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("identity not found for userId=" + userId);
        }
        if (!passwordEncoder.matches(request.getOldCredential(), identity.getCredentialHash())) {
            throw new IllegalArgumentException("old credential mismatch");
        }
        identity.setCredentialHash(passwordEncoder.encode(request.getNewCredential()));
        userIdentityService.save(identity);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -Dtest=AuthUserServiceImplTest -q`
Expected: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

- [ ] **Step 6: 创建 AuthUserController**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/controller/AuthUserController.java`：

```java
package fan.fancy.server.authorization.controller;

import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.dto.ChangePasswordRequest;
import fan.fancy.server.authorization.service.AuthUserService;
import fancy.boot.core.http.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证服务用户接口.
 *
 * @author Fan
 */
@RestController
@RequestMapping("/auth/users")
@AllArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;

    /**
     * 绑定认证账号（内部接口，需 X-Internal-Token 校验）.
     */
    @PostMapping("/bind")
    public Response<Void> bind(@RequestBody @Valid AuthBindRequest request) {
        authUserService.bind(request);
        return Response.success();
    }

    /**
     * 修改密码（需登录）.
     */
    @PutMapping("/{userId}/password")
    public Response<Void> changePassword(@PathVariable("userId") Long userId,
                                          @RequestBody @Valid ChangePasswordRequest request,
                                          Authentication authentication) {
        authUserService.changePassword(userId, authentication.getName(), request);
        return Response.success();
    }
}
```

- [ ] **Step 7: 创建异常处理 Advice**

新建 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/fan/fancy/server/authorization/handler/AuthUserExceptionAdvice.java`：

```java
package fan.fancy.server.authorization.handler;

import fancy.boot.core.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Fan
 */
@Slf4j
@RestControllerAdvice(basePackages = "fan.fancy.server.authorization.controller")
public class AuthUserExceptionAdvice {

    @ExceptionHandler(IllegalStateException.class)
    public Response<Void> handleConflict(IllegalStateException ex) {
        log.warn("auth conflict: {}", ex.getMessage());
        return Response.failure("AUTH_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleBadRequest(IllegalArgumentException ex) {
        log.warn("auth bad request: {}", ex.getMessage());
        return Response.failure("AUTH_BAD_REQUEST", ex.getMessage());
    }
}
```

> 若 `fancy.boot.core.http.Response.failure(code, msg)` 签名不同，参考现有 `Response` 用法调整（`failure` 通常有多个重载；如不确定可改成 `Response.failure()` 单参或查源代码）。

- [ ] **Step 8: 编译并跑全部测试**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server test -q`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 9: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-services/fancy-authorization-server/src/main/java/.../service/AuthUserService.java fancy-services/fancy-authorization-server/src/main/java/.../service/impl/AuthUserServiceImpl.java fancy-services/fancy-authorization-server/src/main/java/.../controller/AuthUserController.java fancy-services/fancy-authorization-server/src/main/java/.../handler/AuthUserExceptionAdvice.java fancy-services/fancy-authorization-server/src/test/java/.../service/impl/AuthUserServiceImplTest.java
git commit -m "feat(auth): add AuthUserService and AuthUserController for bind/changePassword"
```

---

## Task 8: authorization-server SecurityConfig 放行新接口

**Files:**
- Modify: `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../config/SecurityConfig.java`

**Interfaces:**
- Produces: `SecurityFilterChain` —— `/auth/users/bind` 走内部 token 校验（不要求 OAuth2 登录态）；`/auth/users/{id}/password` 走 OAuth2 登录态。

- [ ] **Step 1: 修改 SecurityConfig**

修改 `fancy-platform/fancy-services/fancy-authorization-server/src/main/java/.../config/SecurityConfig.java`，在 `securityFilterChain` 的 `authorizeHttpRequests` 中把 `/auth/users/bind` 加入 permitAll（因为它走 `X-Internal-Token` 校验，而非 Spring Security 鉴权）：

找到：

```java
http.authorizeHttpRequests(registry -> registry
        .requestMatchers("/api/**", "/login", "/assets/**", "/favicon.ico", "/error").permitAll()
        .anyRequest().authenticated());
```

替换为：

```java
http.authorizeHttpRequests(registry -> registry
        .requestMatchers("/api/**", "/auth/users/bind", "/login", "/assets/**", "/favicon.ico", "/error").permitAll()
        .anyRequest().authenticated());
```

> 说明：`/auth/users/{userId}/password` 仍走 `authenticated()`，需要 OAuth2 用户 token。

- [ ] **Step 2: 编译**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
cd "D:/Fan/Projects/fancy-platform"
git add fancy-services/fancy-authorization-server/src/main/java/.../config/SecurityConfig.java
git commit -m "feat(auth): permit /auth/users/bind for internal token calls"
```

---

## Task 9: fancy-iam-api 清理认证相关导出

**Files:**
- Delete: `fancy-space/fancy-space-api/fancy-iam-api/src/main/java/.../pojo/entity/UserIdentityDO.java`
- Delete: `fancy-space/fancy-space-api/fancy-iam-api/src/main/java/.../service/UserApiService.java`
- Modify: `fancy-space/fancy-space-api/fancy-iam-api/src/main/java/.../pojo/bo/UserBO.java`

**Interfaces:**
- Produces: `UserBO` 中 `userIdentities` 字段被移除。

- [ ] **Step 1: 删除两个文件**

```bash
cd "D:/Fan/Projects/fancy-space"
rm fancy-space-api/fancy-iam-api/src/main/java/fan/fancy/api/iam/pojo/entity/UserIdentityDO.java
rm fancy-space-api/fancy-iam-api/src/main/java/fan/fancy/api/iam/service/UserApiService.java
```

- [ ] **Step 2: 修改 UserBO 移除 userIdentities 字段**

修改 `fancy-space/fancy-space-api/fancy-iam-api/src/main/java/fan/fancy/api/iam/pojo/bo/UserBO.java`：

- 删除 `import fan.fancy.api.iam.pojo.entity.UserIdentityDO;`
- 删除 `import java.util.ArrayList;`
- 删除 `import java.util.List;`
- 删除字段 `private List<UserIdentityDO> userIdentities = new ArrayList<>();`

最终文件应仅含：`id / avatar / nickname / gender / birthday` 五个字段。

- [ ] **Step 3: 编译验证（仅 iam-api 模块）**

Run: `cd "D:/Fan/Projects/fancy-space" && mvn -pl fancy-space-api/fancy-iam-api -am compile -q 2>&1 | head -40`

Expected: 可能有错误（因为 `fancy-iam` 服务还在引用这些类）。这是预期——Task 10 会修复。

- [ ] **Step 4: 暂存 + 提交**

```bash
cd "D:/Fan/Projects/fancy-space"
git add fancy-space-api/fancy-iam-api/src/main/java/.../pojo/bo/UserBO.java
git rm fancy-space-api/fancy-iam-api/src/main/java/.../pojo/entity/UserIdentityDO.java fancy-space-api/fancy-iam-api/src/main/java/.../service/UserApiService.java
git commit -m "refactor(iam-api): remove authentication-related exports"
```

---

## Task 10: fancy-iam 引入 auth-api 并改造 createUser

**Files:**
- Modify: `fancy-space/fancy-services/fancy-iam/pom.xml`
- Modify: `fancy-space/fancy-services/fancy-iam/src/main/java/.../service/impl/UserServiceImpl.java`
- Modify: `fancy-space/fancy-services/fancy-iam/src/main/java/.../controller/UserController.java`
- Modify: `fancy-space/fancy-services/fancy-iam/src/main/java/.../pojo/dto/UserIdentityDTO.java`（使其不再依赖被删除的 `UserIdentityDO`）
- Modify: `fancy-space/fancy-services/fancy-iam/src/main/java/.../service/impl/UserIdentityServiceImpl.java`（同上）

**Interfaces:**
- Consumes: `AuthUserApi.bind(AuthBindRequest)`（来自 Task 4）。
- Produces: `UserService.createUser(...)` 内部调用 auth 服务的 bind 接口。

- [ ] **Step 1: 引入 fancy-auth-api 依赖**

修改 `fancy-space/fancy-services/fancy-iam/pom.xml`，新增：

```xml
<dependency>
    <groupId>fan</groupId>
    <artifactId>fancy-auth-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 注意：fancy-space 仓库需要能解析到 platform 发布的 fancy-auth-api 1.0.0。如使用本地仓库需先 `mvn install` 到 `~/.m2`；CI 环境下需确认 nexus/仓库配置。本计划不涵盖发布流程。

- [ ] **Step 2: 修改 UserIdentityDTO 内部化**

修改 `fancy-space/fancy-services/fancy-iam/src/main/java/.../pojo/dto/UserIdentityDTO.java`：当前文件已不依赖被删除的 `UserIdentityDO`（已 Read 过该文件，它不引用）。**确认无变化则跳过此步。**

- [ ] **Step 3: 修改 UserIdentityServiceImpl 内部化**

Read `fancy-space/fancy-services/fancy-iam/src/main/java/.../service/impl/UserIdentityServiceImpl.java`，确认其 mapper 引用情况：

如果 mapper 类型还是 `fan.fancy.api.iam.pojo.entity.UserIdentityDO`，需新建一个 IAM 内部的 `UserIdentityDO`（或继续使用 `UserIdentityDTO` 当 DTO/Entity 二合一），让 mapper 改用本地类型。

具体方案：新建 `fancy-space/fancy-services/fancy-iam/src/main/java/.../pojo/entity/UserIdentityDO.java`（仅 IAM 内部，继承 `MetaDO`），字段与原 `fancy.iam.api` 模块中的 `UserIdentityDO` 一致；mapper 与 service 切到本地类型。

```java
package fan.fancy.iam.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import fancy.starter.mybatis.plus.entity.MetaDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("user_identity")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserIdentityDO extends MetaDO {

    private Long userId;
    private Integer type;          // 0:系统内部 1:第三方
    private Integer identity;      // IdentityType code
    private String identifier;
    private String credential;
}
```

> 注：原 `user_identity` 表仍在 IAM 库（暂未迁移，本步骤只是让 IAM 端代码能继续编译）。如果 IAM 服务后续将 `user_identity` 表迁出/废弃，对应 mapper/service 也将删除。本任务只为编译通过。

- [ ] **Step 4: 修改 UserServiceImpl.createUser 调 auth bind**

修改 `fancy-space/fancy-services/fancy-iam/src/main/java/.../service/impl/UserServiceImpl.java`，在 `createUser` 方法**保存业务用户后**追加调用：

```java
import fan.fancy.api.auth.pojo.dto.AuthBindRequest;
import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.api.auth.service.AuthUserApi;
```

在类字段中注入 `AuthUserApi`（构造器或 `@Resource`）。调用示例：

```java
@Override
@Transactional
public UserVO createUser(UserDTO dto) {
    // 1. 创建 IAM 业务用户（已有逻辑）
    UserDO userDO = ...;
    userMapper.insert(userDO);

    // 2. 调 auth 服务绑定认证账号
    AuthBindRequest bindReq = new AuthBindRequest();
    bindReq.setUserId(userDO.getId());
    bindReq.setIdentityType(IdentityType.USERNAME);
    bindReq.setIdentifier(dto.getUsername());
    bindReq.setCredential(dto.getPassword());
    authUserApi.bind(bindReq);

    return ...;
}
```

> **注意**：若 `auth.bind` 抛异常，`@Transactional` 会回滚 IAM 业务用户，符合"接受最终不一致"（详见 spec §6）。**当前实现为强一致**，因为两步在同一事务里——`auth.bind` 网络异常将整体回滚。这超出 spec §6 的"最终一致"叙述，但更安全。**此选择需用户确认**（如不同意可改为：bind 失败不抛业务异常，仅记录日志，由调用方补偿）。

- [ ] **Step 5: 修改 UserController 删除改密接口**

修改 `fancy-space/fancy-services/fancy-iam/src/main/java/.../controller/UserController.java`，删除 `changePassword` 相关的 `@PutMapping` 方法（如果有）。若该 controller 中本来就没有 `changePassword`（仅在 UserIdentityController 中），则跳过。

- [ ] **Step 6: 编译验证**

Run: `cd "D:/Fan/Projects/fancy-space" && mvn -pl fancy-services/fancy-iam -am compile -q`
Expected: BUILD SUCCESS

> 如有报错，按错误信息逐个修复。常见问题：mapper XML 路径、DTO 字段残留引用、缺少 `@EnableFeignClients`。

- [ ] **Step 7: 提交**

```bash
cd "D:/Fan/Projects/fancy-space"
git add fancy-services/fancy-iam/
git commit -m "refactor(iam): call auth-api bind on createUser; remove password change"
```

---

## Task 11: 端到端验证

**Files:** 无代码改动。

- [ ] **Step 1: 启动 IAM 服务**

Run: `cd "D:/Fan/Projects/fancy-space" && mvn -pl fancy-services/fancy-iam spring-boot:run`

确认：
- 启动无错误
- `/actuator/health` 返回 UP

- [ ] **Step 2: 启动 authorization-server**

Run: `cd "D:/Fan/Projects/fancy-platform" && mvn -pl fancy-services/fancy-authorization-server spring-boot:run`

确认：
- 启动无错误
- 登录页 `http://localhost:10100/login` 可访问

- [ ] **Step 3: 手动执行建表 SQL**

在 `fancy_iam` 库上执行 `fancy-platform/fancy-services/fancy-authorization-server/src/main/resources/sql/user_identity.sql`。

- [ ] **Step 4: 调用 IAM 创建用户**

```bash
curl -X POST http://localhost:10099/iam/users \
  -H "Content-Type: application/json" \
  -d '{"username":"fan","password":"plain-pwd","nickname":"fan"}'
```

确认返回中含 `userId`，且 auth 服务的 `user_identity` 表出现对应记录（hash 而非明文）。

- [ ] **Step 5: 浏览器登录 authorization-server**

浏览器打开 `http://localhost:10100/login`，输入 `fan` / `plain-pwd`。

确认：登录成功，跳转到 OAuth2 授权页或 callback。

- [ ] **Step 6: 调改密接口**

```bash
curl -X PUT http://localhost:10100/auth/users/<userId>/password \
  -H "Content-Type: application/json" \
  -H "Cookie: <登录后的 session>" \
  -d '{"oldCredential":"plain-pwd","newCredential":"new-pwd"}'
```

确认返回成功。

- [ ] **Step 7: 用新密码登录**

登出后用 `fan` / `new-pwd` 再次登录。

确认：登录成功。

- [ ] **Step 8: 提交验证报告**

将上述步骤的实际结果记录到 `docs/superpowers/plans/2026-07-17-e2e-verification.md`，提交到 platform 仓库：

```bash
cd "D:/Fan/Projects/fancy-platform"
git add docs/superpowers/plans/2026-07-17-e2e-verification.md
git commit -m "docs: add e2e verification report for user auth separation"
```

---

## 验收标准

完成所有 Task 1–11 后应满足：

1. `fancy-authorization-server` 的 `pom.xml` 不再依赖 `fancy-iam-api`。
2. `fancy-iam` 的 `pom.xml` 依赖 `fancy-auth-api`。
3. `UserDetailsServiceImpl` 不再调任何外部 HTTP/Feign；`mvn test` 全过。
4. `AuthUserServiceImpl` 的 4 个单测全过。
5. 端到端：注册 → 登录成功 → 改密 → 用新密码登录成功。
6. `user_identity` 表中存储的是 BCrypt 哈希，无明文密码。
