-- =====================================================================
-- V1 — Tổ chức & phân quyền
-- ranks, org_units, users, roles, role_permissions, user_roles
-- Quy ước chung cho V1…V6:
--   * id UUID do ứng dụng sinh (UUIDv7), không DEFAULT
--   * enum = VARCHAR(40) + CHECK, giá trị khớp enum Java
--   * CHECK coi NULL là đạt — so sánh trên cột nullable dùng <=> khi cần chặt
--   * tiền = BIGINT (VND), thời điểm = DATETIME(6), ngày = DATE
-- =====================================================================

-- Cấp bậc — chỉ dùng làm điều kiện chọn quy trình duyệt
CREATE TABLE ranks (
                     id          UUID         NOT NULL,
                     code        VARCHAR(50)  NOT NULL,
                     name        VARCHAR(255) NOT NULL,
                     seniority   INT          NOT NULL,             -- số lớn = bậc cao
                     created_by  UUID         NOT NULL,
                     updated_by  UUID         NOT NULL,
                     created_at  DATETIME(6)  NOT NULL,
                     updated_at  DATETIME(6)  NOT NULL,
                     CONSTRAINT pk_ranks PRIMARY KEY (id),
                     CONSTRAINT uk_ranks_code UNIQUE (code),
                     CONSTRAINT uk_ranks_seniority UNIQUE (seniority)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Đơn vị — cây COMPANY → BRANCH → DEPARTMENT
CREATE TABLE org_units (
                         id            UUID         NOT NULL,
                         code          VARCHAR(50)  NOT NULL,
                         name          VARCHAR(255) NOT NULL,
                         unit_type     VARCHAR(40)  NOT NULL,
                         parent_id     UUID         NULL,
                         head_user_id  UUID         NULL,               -- FK thêm cuối tệp (vòng với users)
                         status        VARCHAR(40)  NOT NULL,
                         created_by    UUID         NOT NULL,
                         updated_by    UUID         NOT NULL,
                         created_at    DATETIME(6)  NOT NULL,
                         updated_at    DATETIME(6)  NOT NULL,
                         CONSTRAINT pk_org_units PRIMARY KEY (id),
                         CONSTRAINT uk_org_units_code UNIQUE (code),
                         CONSTRAINT fk_org_units_parent FOREIGN KEY (parent_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                         CONSTRAINT ck_org_units_unit_type CHECK (unit_type IN ('COMPANY', 'BRANCH', 'DEPARTMENT')),
                         CONSTRAINT ck_org_units_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                          -- Gốc và chỉ gốc không có cha
                         CONSTRAINT ck_org_units_root_parent CHECK ((unit_type = 'COMPANY') = (parent_id IS NULL)),
                         CONSTRAINT ck_org_units_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id),
                          -- Gốc luôn ACTIVE
                         CONSTRAINT ck_org_units_root_active CHECK (unit_type <> 'COMPANY' OR status = 'ACTIVE')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Người dùng — không bao giờ xóa dòng, chỉ INACTIVE
CREATE TABLE users (
                     id                    UUID         NOT NULL,
                     username              VARCHAR(50)  NOT NULL,   -- bất biến
                     email                 VARCHAR(255) NOT NULL,   -- bất biến
                     full_name             VARCHAR(255) NOT NULL,
                     phone_number          VARCHAR(20)  NULL,
                     password_hash         VARCHAR(255) NOT NULL,
                     org_unit_id           UUID         NOT NULL,   -- phải là DEPARTMENT ACTIVE — kiểm ở service
                     rank_id               UUID         NOT NULL,
                     status                VARCHAR(40)  NOT NULL,
                     avatar_image_id       UUID         NULL,       -- FK thêm ở V3 khi có image_files
                     must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
                     failed_login_count    INT          NOT NULL DEFAULT 0,
                     locked_until          DATETIME(6)  NULL,
                     token_valid_from      DATETIME     NOT NULL,   -- độ chính xác giây, so với iat của JWT
                     created_by            UUID         NOT NULL,
                     updated_by            UUID         NOT NULL,
                     created_at            DATETIME(6)  NOT NULL,
                     updated_at            DATETIME(6)  NOT NULL,
                     CONSTRAINT pk_users PRIMARY KEY (id),
                     CONSTRAINT uk_users_username UNIQUE (username),
                     CONSTRAINT uk_users_email UNIQUE (email),
                     CONSTRAINT uk_users_avatar_image UNIQUE (avatar_image_id),
                     CONSTRAINT fk_users_org_unit FOREIGN KEY (org_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                     CONSTRAINT fk_users_rank FOREIGN KEY (rank_id) REFERENCES ranks (id) ON DELETE RESTRICT,
                     CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                     CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                     CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                     CONSTRAINT ck_users_failed_login_count CHECK (failed_login_count >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Vai trò — vai trò hệ thống nhận diện bằng code = 'ADMIN'
CREATE TABLE roles (
                     id           UUID         NOT NULL,
                     code         VARCHAR(50)  NOT NULL,
                     name         VARCHAR(255) NOT NULL,
                     description  TEXT         NULL,
                     created_by   UUID         NOT NULL,
                     updated_by   UUID         NOT NULL,
                     created_at   DATETIME(6)  NOT NULL,
                     updated_at   DATETIME(6)  NOT NULL,
                     CONSTRAINT pk_roles PRIMARY KEY (id),
                     CONSTRAINT uk_roles_code UNIQUE (code),
                     CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                     CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Ô của ma trận quyền: (vai trò, mã quyền) → phạm vi
-- Danh mục 19 quyền là CHECK, không phải bảng
CREATE TABLE role_permissions (
                                id               UUID         NOT NULL,
                                role_id          UUID         NOT NULL,
                                permission_code  VARCHAR(40)  NOT NULL,
                                scope            VARCHAR(40)  NOT NULL,
                                created_by       UUID         NOT NULL,
                                updated_by       UUID         NOT NULL,
                                created_at       DATETIME(6)  NOT NULL,
                                updated_at       DATETIME(6)  NOT NULL,
                                CONSTRAINT pk_role_permissions PRIMARY KEY (id),
                                CONSTRAINT uk_role_permissions_role_permission UNIQUE (role_id, permission_code),
                                CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
                                CONSTRAINT fk_role_permissions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                                CONSTRAINT fk_role_permissions_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                                CONSTRAINT ck_role_permissions_code CHECK (permission_code IN (
                                                                                               'ASSET_VIEW', 'ASSET_CREATE', 'ASSET_TECH_EDIT', 'ASSET_FINANCE_VIEW', 'ASSET_FINANCE_EDIT',
                                                                                               'ASSET_OPERATE', 'ASSET_TRANSFER', 'DISPOSAL_PROPOSE', 'REQUEST_CREATE', 'REQUEST_VIEW',
                                                                                               'CATALOG_MANAGE', 'DASHBOARD_VIEW', 'WORKFLOW_MANAGE', 'TASK_REASSIGN', 'ORG_MANAGE',
                                                                                               'USER_MANAGE', 'ROLE_MANAGE', 'AUDIT_VIEW', 'SYSTEM_CONFIG')),
                                CONSTRAINT ck_role_permissions_scope CHECK (scope IN ('SELF', 'DEPARTMENT', 'BRANCH', 'ALL')),
                                -- Quyền chỉ có một phạm vi hợp lệ — phải khớp phạm vi hợp lệ trong enum Permission
                                CONSTRAINT ck_role_permissions_self_only CHECK (permission_code <> 'REQUEST_CREATE' OR scope = 'SELF'),
                                -- Quyền quản trị toàn công ty: đối tượng không gắn đơn vị hoặc phủ toàn hệ thống
                                CONSTRAINT ck_role_permissions_all_only CHECK (
                                  permission_code NOT IN ('CATALOG_MANAGE', 'WORKFLOW_MANAGE', 'TASK_REASSIGN', 'ORG_MANAGE',
                                                          'USER_MANAGE', 'ROLE_MANAGE', 'AUDIT_VIEW', 'SYSTEM_CONFIG')
                                    OR scope = 'ALL')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Gán vai trò — gỡ vai trò là DELETE nên không có updated_*
CREATE TABLE user_roles (
                          id          UUID         NOT NULL,
                          user_id     UUID         NOT NULL,
                          role_id     UUID         NOT NULL,
                          created_by  UUID         NOT NULL,
                          created_at  DATETIME(6)  NOT NULL,
                          CONSTRAINT pk_user_roles PRIMARY KEY (id),
                          CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
                          CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
                          CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT,
                          CONSTRAINT fk_user_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Khóa ngoại tới users của hai bảng tạo trước users
ALTER TABLE ranks
  ADD CONSTRAINT fk_ranks_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
  ADD CONSTRAINT fk_ranks_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT;

ALTER TABLE org_units
  ADD CONSTRAINT fk_org_units_head_user FOREIGN KEY (head_user_id) REFERENCES users (id) ON DELETE RESTRICT,
  ADD CONSTRAINT fk_org_units_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
  ADD CONSTRAINT fk_org_units_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT;
