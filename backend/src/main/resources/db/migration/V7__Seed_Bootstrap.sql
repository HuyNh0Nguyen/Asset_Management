-- =====================================================================
-- V7 — Dữ liệu khởi tạo bắt buộc
-- 1. Đơn vị gốc COMPANY + BRANCH "Trụ sở chính" + DEPARTMENT "Ban quản trị"
-- 2. 6 vai trò + ma trận quyền (34 ô)
-- 3. Admin gốc — must_change_password = TRUE
-- 4. 4 cấp bậc
-- 5. 3 quy trình mặc định (is_default chỉ sinh ở đây)
-- 6. 6 khóa system_configs
--
-- Seed không ghi Audit Log. created_by / updated_by của mọi dòng = Admin gốc.
-- UUID cố định dạng 00000000-0000-7000-8000-00000000GGNN (GG = nhóm, NN = thứ tự)
--   01 đơn vị · 02 cấp bậc · 03 vai trò · 04 người dùng · 05 gán vai trò
--   06 ô quyền · 07 quy trình · 08 bước · 09 quy tắc · 0a cấu hình
-- Dữ liệu demo (db/demo) KHÔNG dùng lại dải này.
-- =====================================================================

-- Phá vòng users ↔ org_units ↔ cột dấu vết
SET FOREIGN_KEY_CHECKS = 0;

SET @now = NOW(6);

-- Định danh dùng chung trong tệp
SET @admin_id        = '00000000-0000-7000-8000-000000000401';
SET @org_company     = '00000000-0000-7000-8000-000000000101';
SET @org_hq          = '00000000-0000-7000-8000-000000000102';
SET @org_admin_dept  = '00000000-0000-7000-8000-000000000103';
SET @rank_staff      = '00000000-0000-7000-8000-000000000201';
SET @role_employee   = '00000000-0000-7000-8000-000000000301';
SET @role_asset_mgr  = '00000000-0000-7000-8000-000000000302';
SET @role_accountant = '00000000-0000-7000-8000-000000000303';
SET @role_admin_ops  = '00000000-0000-7000-8000-000000000304';
SET @role_executive  = '00000000-0000-7000-8000-000000000305';
SET @role_admin      = '00000000-0000-7000-8000-000000000306';

-- ---------------------------------------------------------------------
-- 1. Cây đơn vị — COMPANY chỉ sinh ở đây
-- ---------------------------------------------------------------------
INSERT INTO org_units (id, code, name, unit_type, parent_id, head_user_id, status,
                       created_by, updated_by, created_at, updated_at)
VALUES (@org_company,    'ROOT',   'Công ty',       'COMPANY',    NULL,         NULL, 'ACTIVE', @admin_id, @admin_id, @now, @now),
       (@org_hq,         'HQ',     'Trụ sở chính',  'BRANCH',     @org_company, NULL, 'ACTIVE', @admin_id, @admin_id, @now, @now),
       (@org_admin_dept, 'HQ-ADM', 'Ban quản trị',  'DEPARTMENT', @org_hq,      NULL, 'ACTIVE', @admin_id, @admin_id, @now, @now);

-- ---------------------------------------------------------------------
-- 4. Cấp bậc (chèn trước người dùng vì users.rank_id)
-- ---------------------------------------------------------------------
INSERT INTO ranks (id, code, name, seniority, created_by, updated_by, created_at, updated_at)
VALUES (@rank_staff,                            'STAFF',     'Nhân viên',    1, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000202', 'TEAM_LEAD', 'Trưởng nhóm',  2, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000203', 'HEAD',      'Trưởng phòng', 3, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000204', 'DIRECTOR',  'Giám đốc',     4, @admin_id, @admin_id, @now, @now);

-- ---------------------------------------------------------------------
-- 3. Admin gốc — tài khoản bình thường, bắt buộc đổi mật khẩu lần đầu
--    Mật khẩu mặc định: Admin@123 (BCrypt cost 10)
--    Email: bị ghi đè thành bí danh + của DEMO_MAILBOX khi bật seed demo
-- ---------------------------------------------------------------------
INSERT INTO users (id, username, email, full_name, phone_number, password_hash, org_unit_id, rank_id, status,
                   avatar_image_id, must_change_password, failed_login_count, locked_until, token_valid_from,
                   created_by, updated_by, created_at, updated_at)
VALUES (@admin_id, 'admin', 'huynhnbh.dev@gmail.com', 'Quản trị hệ thống', NULL,
        '$2a$10$dHQJF8u7XutigLBtivgN4.P7mn2EAcAfgyey18lrHpR9/hz/uQg4a',
        @org_admin_dept, @rank_staff, 'ACTIVE',
        NULL, TRUE, 0, NULL, NOW(),
        @admin_id, @admin_id, @now, @now);

-- ---------------------------------------------------------------------
-- 2. Vai trò mặc định — ADMIN là vai trò hệ thống, bị khóa
-- ---------------------------------------------------------------------
INSERT INTO roles (id, code, name, description, created_by, updated_by, created_at, updated_at)
VALUES (@role_employee,   'EMPLOYEE',       'Nhân viên',         'Gán mặc định cho mọi người dùng nghiệp vụ; lập phiếu cho chính mình',  @admin_id, @admin_id, @now, @now),
       (@role_asset_mgr,  'ASSET_MANAGER',  'Quản lý tài sản',   'Nhập, sửa kỹ thuật, vận hành vòng đời và điều chuyển tài sản',          @admin_id, @admin_id, @now, @now),
       (@role_accountant, 'ACCOUNTANT',     'Kế toán',           'Xem và ghi nhận nhóm Tài chính của tài sản',                            @admin_id, @admin_id, @now, @now),
       (@role_admin_ops,  'ADMINISTRATION', 'Hành chính',        'Giám sát tài sản và phiếu đề xuất trong chi nhánh',                     @admin_id, @admin_id, @now, @now),
       (@role_executive,  'EXECUTIVE',      'Lãnh đạo',          'Giám sát tài sản, tài chính và phiếu đề xuất trong chi nhánh',          @admin_id, @admin_id, @now, @now),
       (@role_admin,      'ADMIN',          'Quản trị hệ thống', 'Vai trò hệ thống — quản trị cấu hình và quyền truy cập, không sửa được', @admin_id, @admin_id, @now, @now);

-- Ma trận quyền — "không cấp" = không có dòng
INSERT INTO role_permissions (id, role_id, permission_code, scope, created_by, updated_by, created_at, updated_at)
VALUES
  -- Nhân viên
  ('00000000-0000-7000-8000-000000000601', @role_employee,   'ASSET_VIEW',         'DEPARTMENT', @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000602', @role_employee,   'REQUEST_CREATE',     'SELF',       @admin_id, @admin_id, @now, @now),
  -- Quản lý tài sản — không có ASSET_FINANCE_VIEW
  ('00000000-0000-7000-8000-000000000603', @role_asset_mgr,  'ASSET_VIEW',         'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000604', @role_asset_mgr,  'ASSET_CREATE',       'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000605', @role_asset_mgr,  'ASSET_TECH_EDIT',    'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000606', @role_asset_mgr,  'ASSET_OPERATE',      'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000607', @role_asset_mgr,  'ASSET_TRANSFER',     'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000608', @role_asset_mgr,  'DISPOSAL_PROPOSE',   'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000609', @role_asset_mgr,  'REQUEST_VIEW',       'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000610', @role_asset_mgr,  'DASHBOARD_VIEW',     'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000611', @role_asset_mgr,  'CATALOG_MANAGE',     'ALL',        @admin_id, @admin_id, @now, @now),
  -- Kế toán
  ('00000000-0000-7000-8000-000000000612', @role_accountant, 'ASSET_VIEW',         'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000613', @role_accountant, 'ASSET_FINANCE_VIEW', 'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000614', @role_accountant, 'ASSET_FINANCE_EDIT', 'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000615', @role_accountant, 'REQUEST_VIEW',       'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000616', @role_accountant, 'DASHBOARD_VIEW',     'BRANCH',     @admin_id, @admin_id, @now, @now),
  -- Hành chính
  ('00000000-0000-7000-8000-000000000617', @role_admin_ops,  'ASSET_VIEW',         'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000618', @role_admin_ops,  'REQUEST_VIEW',       'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000619', @role_admin_ops,  'DASHBOARD_VIEW',     'BRANCH',     @admin_id, @admin_id, @now, @now),
  -- Lãnh đạo
  ('00000000-0000-7000-8000-000000000620', @role_executive,  'ASSET_VIEW',         'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000621', @role_executive,  'ASSET_FINANCE_VIEW', 'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000622', @role_executive,  'REQUEST_VIEW',       'BRANCH',     @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000623', @role_executive,  'DASHBOARD_VIEW',     'BRANCH',     @admin_id, @admin_id, @now, @now),
  -- Admin — không xem tài chính, không duyệt nghiệp vụ
  ('00000000-0000-7000-8000-000000000624', @role_admin,      'ASSET_VIEW',         'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000625', @role_admin,      'REQUEST_VIEW',       'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000626', @role_admin,      'DASHBOARD_VIEW',     'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000627', @role_admin,      'CATALOG_MANAGE',     'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000628', @role_admin,      'WORKFLOW_MANAGE',    'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000629', @role_admin,      'TASK_REASSIGN',      'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000630', @role_admin,      'ORG_MANAGE',         'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000631', @role_admin,      'USER_MANAGE',        'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000632', @role_admin,      'ROLE_MANAGE',        'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000633', @role_admin,      'AUDIT_VIEW',         'ALL',        @admin_id, @admin_id, @now, @now),
  ('00000000-0000-7000-8000-000000000634', @role_admin,      'SYSTEM_CONFIG',      'ALL',        @admin_id, @admin_id, @now, @now);

-- Admin gốc chỉ giữ vai trò ADMIN
INSERT INTO user_roles (id, user_id, role_id, created_by, created_at)
VALUES ('00000000-0000-7000-8000-000000000501', @admin_id, @role_admin, @admin_id, @now);

-- ---------------------------------------------------------------------
-- 5. Quy trình mặc định — không điều kiện, ưu tiên thấp nhất (0)
--    ALLOCATION, LOAN: ① Trưởng phòng · DISPOSAL: ① Giám đốc chi nhánh
--    sla_days NULL = dùng DEFAULT_STAGE_SLA_DAYS lúc kích hoạt
-- ---------------------------------------------------------------------
INSERT INTO workflows (id, code, name, description, request_type, priority, is_default, status,
                       min_value, max_value, created_by, updated_by, created_at, updated_at)
VALUES ('00000000-0000-7000-8000-000000000701', 'WF-DEFAULT-ALLOCATION', 'Mặc định — Cấp phát',
        'Áp dụng khi không quy trình nào khác khớp', 'ALLOCATION', 0, TRUE, 'ACTIVE', NULL, NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000702', 'WF-DEFAULT-LOAN', 'Mặc định — Mượn',
        'Áp dụng khi không quy trình nào khác khớp', 'LOAN', 0, TRUE, 'ACTIVE', NULL, NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000703', 'WF-DEFAULT-DISPOSAL', 'Mặc định — Thanh lý',
        'Áp dụng khi không quy trình nào khác khớp', 'DISPOSAL', 0, TRUE, 'ACTIVE', NULL, NULL, @admin_id, @admin_id, @now, @now);

INSERT INTO workflow_stages (id, workflow_id, stage_order, name, completion_policy, sla_days,
                             created_by, updated_by, created_at, updated_at)
VALUES ('00000000-0000-7000-8000-000000000801', '00000000-0000-7000-8000-000000000701', 1, 'Trưởng phòng',       'ALL', NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000802', '00000000-0000-7000-8000-000000000702', 1, 'Trưởng phòng',       'ALL', NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000803', '00000000-0000-7000-8000-000000000703', 1, 'Giám đốc chi nhánh', 'ALL', NULL, @admin_id, @admin_id, @now, @now);

INSERT INTO workflow_approver_rules (id, stage_id, rule_order, rule_type, org_level, role_id, role_scope, user_id,
                                     created_by, updated_by, created_at, updated_at)
VALUES ('00000000-0000-7000-8000-000000000901', '00000000-0000-7000-8000-000000000801', 1, 'ORG_HEAD', 'DEPARTMENT', NULL, NULL, NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000902', '00000000-0000-7000-8000-000000000802', 1, 'ORG_HEAD', 'DEPARTMENT', NULL, NULL, NULL, @admin_id, @admin_id, @now, @now),
       ('00000000-0000-7000-8000-000000000903', '00000000-0000-7000-8000-000000000803', 1, 'ORG_HEAD', 'BRANCH',     NULL, NULL, NULL, @admin_id, @admin_id, @now, @now);

-- ---------------------------------------------------------------------
-- 6. Cấu hình hệ thống — giá trị mặc định
-- ---------------------------------------------------------------------
INSERT INTO system_configs (id, config_key, config_value, updated_by, updated_at)
VALUES ('00000000-0000-7000-8000-000000000a01', 'ALERT_JOB_TIME',            '08:00', @admin_id, @now),
       ('00000000-0000-7000-8000-000000000a02', 'ALERT_JOB_ENABLED',         'true',  @admin_id, @now),
       ('00000000-0000-7000-8000-000000000a03', 'RECOVERY_ALERT_DAYS',       '3',     @admin_id, @now),
       ('00000000-0000-7000-8000-000000000a04', 'DEFAULT_STAGE_SLA_DAYS',    '2',     @admin_id, @now),
       ('00000000-0000-7000-8000-000000000a05', 'MAX_LOAN_DAYS',             '30',    @admin_id, @now),
       ('00000000-0000-7000-8000-000000000a06', 'DASHBOARD_DUE_WINDOW_DAYS', '30',    @admin_id, @now);

SET FOREIGN_KEY_CHECKS = 1;
