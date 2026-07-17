-- 用户认证表（用户名/手机/邮箱/第三方 openid + 凭据哈希）
-- 用于 authorization-server 本地身份认证。
-- 关联 IAM 业务用户：user_id = fancy_iam.user.id

CREATE TABLE IF NOT EXISTS user_identity
(
    id
    BIGINT
    NOT
    NULL
    COMMENT
    '雪花ID主键',
    user_id
    BIGINT
    NOT
    NULL
    COMMENT
    '关联 IAM 业务用户 ID',
    identity_type
    TINYINT
    NOT
    NULL
    COMMENT
    '1=USERNAME 2=PHONE 3=EMAIL 4=GITHUB 5=WECHAT 6=QQ',
    identifier
    VARCHAR
(
    128
) NOT NULL COMMENT '身份标识(用户名/手机/邮箱/第三方 openid)',
    credential_hash VARCHAR
(
    255
) NOT NULL COMMENT 'BCrypt 哈希(密码)或第三方 access_token',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY
(
    id
),
    UNIQUE KEY uk_identity_type_identifier
(
    identity_type,
    identifier
),
    KEY idx_user_id
(
    user_id
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证身份表';