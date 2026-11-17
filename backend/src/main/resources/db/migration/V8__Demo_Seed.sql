-- =====================================================================
-- V8 (db/demo) — Dữ liệu demo: cơ cấu tổ chức + người dùng
-- Chỉ nạp khi app.seed-demo=true (DemoSeedFlywayCustomizer)
-- UUID demo: 00000000-0000-7000-8000-00000d00GGNN — không đụng dải của V7
--   01 đơn vị · 04 người dùng · 05 gán vai trò
-- Hai bổ sung: Tổ Hạ tầng HN (phòng ban con), Phòng Dự án HCM + nv.cu (INACTIVE)
-- =====================================================================

SET @now = NOW(6);
SET @admin_id = '00000000-0000-7000-8000-000000000401';

-- Mật khẩu demo chung: User@123 (BCrypt cost 10) — không bắt buộc đổi
SET @demo_pw = '$2a$10$zjeMWFmRY7DOHhwaIYc3UeKfNWoymgAUR3lwZpliWPwPuWB7O3Jj2';

-- Đơn vị của V7
SET @org_company    = '00000000-0000-7000-8000-000000000101';
SET @org_hq         = '00000000-0000-7000-8000-000000000102';
SET @org_hq_adm     = '00000000-0000-7000-8000-000000000103';

-- Đơn vị demo
SET @org_hn_btgd    = '00000000-0000-7000-8000-00000d000101';
SET @org_hn_bgd     = '00000000-0000-7000-8000-00000d000102';
SET @org_hn_kt      = '00000000-0000-7000-8000-00000d000103';
SET @org_hn_kt_ht   = '00000000-0000-7000-8000-00000d000104';
SET @org_hn_ktoan   = '00000000-0000-7000-8000-00000d000105';
SET @org_hcm        = '00000000-0000-7000-8000-00000d000106';
SET @org_hcm_bgd    = '00000000-0000-7000-8000-00000d000107';
SET @org_hcm_kd     = '00000000-0000-7000-8000-00000d000108';
SET @org_hcm_da     = '00000000-0000-7000-8000-00000d000109';

-- Cấp bậc của V7
SET @rank_staff     = '00000000-0000-7000-8000-000000000201';
SET @rank_head      = '00000000-0000-7000-8000-000000000203';
SET @rank_director  = '00000000-0000-7000-8000-000000000204';

-- Vai trò của V7
SET @role_employee   = '00000000-0000-7000-8000-000000000301';
SET @role_asset_mgr  = '00000000-0000-7000-8000-000000000302';
SET @role_accountant = '00000000-0000-7000-8000-000000000303';
SET @role_admin_ops  = '00000000-0000-7000-8000-000000000304';
SET @role_executive  = '00000000-0000-7000-8000-000000000305';

-- Người dùng demo
SET @u_tgd_minh   = '00000000-0000-7000-8000-00000d000401';
SET @u_gd_cuong   = '00000000-0000-7000-8000-00000d000402';
SET @u_tp_binh    = '00000000-0000-7000-8000-00000d000403';
SET @u_nv_an      = '00000000-0000-7000-8000-00000d000404';
SET @u_hc_dung    = '00000000-0000-7000-8000-00000d000405';
SET @u_kt_hien    = '00000000-0000-7000-8000-00000d000406';
SET @u_qlts_giang = '00000000-0000-7000-8000-00000d000407';
SET @u_gd_khanh   = '00000000-0000-7000-8000-00000d000408';
SET @u_nv_hoa     = '00000000-0000-7000-8000-00000d000409';
SET @u_nv_hai     = '00000000-0000-7000-8000-00000d000410';

-- ---------------------------------------------------------------------
-- 1. Đổi tên đơn vị của V7 (mã giữ nguyên)
-- ---------------------------------------------------------------------
UPDATE org_units SET name = 'Công ty ABC',          updated_at = @now WHERE id = @org_company;
UPDATE org_units SET name = 'Chi nhánh Hà Nội',     updated_at = @now WHERE id = @org_hq;
UPDATE org_units SET name = 'Phòng Hành chính HN',  updated_at = @now WHERE id = @org_hq_adm;

-- ---------------------------------------------------------------------
-- 2. Đơn vị demo — chưa gán trưởng (vòng users ↔ org_units)
-- ---------------------------------------------------------------------
INSERT INTO org_units (id, code, name, unit_type, parent_id, head_user_id, status,
                       created_by, updated_by, created_at, updated_at)
VALUES (@org_hn_btgd,  'HN_BTGD',  'Ban Tổng giám đốc',      'DEPARTMENT', @org_hq,      NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hn_bgd,   'HN_BGD',   'Ban giám đốc HN',        'DEPARTMENT', @org_hq,      NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hn_kt,    'HN_KT',    'Phòng Kỹ thuật HN',      'DEPARTMENT', @org_hq,      NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hn_kt_ht, 'HN_KT_HT', 'Tổ Hạ tầng HN',          'DEPARTMENT', @org_hn_kt,   NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hn_ktoan, 'HN_KTOAN', 'Phòng Kế toán HN',       'DEPARTMENT', @org_hq,      NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hcm,      'HCM',      'Chi nhánh Hồ Chí Minh',  'BRANCH',     @org_company, NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hcm_bgd,  'HCM_BGD',  'Ban giám đốc HCM',       'DEPARTMENT', @org_hcm,     NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       (@org_hcm_kd,   'HCM_KD',   'Phòng Kinh doanh HCM',   'DEPARTMENT', @org_hcm,     NULL, 'ACTIVE',   @admin_id, @admin_id, @now, @now),
       -- Đã giải thể, trống thật: chỉ còn nv.hai (INACTIVE)
       (@org_hcm_da,   'HCM_DA',   'Phòng Dự án HCM',        'DEPARTMENT', @org_hcm,     NULL, 'INACTIVE', @admin_id, @admin_id, @now, @now);

-- ---------------------------------------------------------------------
-- 3. Người dùng — email tạm, câu cuối tệp đổi thành bí danh
-- ---------------------------------------------------------------------
INSERT INTO users (id, username, email, full_name, phone_number, password_hash, org_unit_id, rank_id, status,
                   avatar_image_id, must_change_password, failed_login_count, locked_until, token_valid_from,
                   created_by, updated_by, created_at, updated_at)
VALUES (@u_tgd_minh,   'tgd.minh',   'tgd.minh@demo.local',   'Nguyễn Văn Minh',  NULL, @demo_pw, @org_hn_btgd,  @rank_director, 'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_gd_cuong,   'gd.cuong',   'gd.cuong@demo.local',   'Trần Văn Cường',   NULL, @demo_pw, @org_hn_bgd,   @rank_director, 'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_tp_binh,    'tp.binh',    'tp.binh@demo.local',    'Lê Văn Bình',      NULL, @demo_pw, @org_hn_kt,    @rank_head,     'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_nv_an,      'nv.an',      'nv.an@demo.local',      'Phạm Văn An',      NULL, @demo_pw, @org_hn_kt,    @rank_staff,    'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_hc_dung,    'hc.dung',    'hc.dung@demo.local',    'Hoàng Thị Dung',   NULL, @demo_pw, @org_hq_adm,   @rank_staff,    'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_kt_hien,    'kt.hien',    'kt.hien@demo.local',    'Vũ Thị Hiền',      NULL, @demo_pw, @org_hn_ktoan, @rank_staff,    'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_qlts_giang, 'qlts.giang', 'qlts.giang@demo.local', 'Đặng Thị Giang',   NULL, @demo_pw, @org_hq_adm,   @rank_staff,    'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_gd_khanh,   'gd.khanh',   'gd.khanh@demo.local',   'Bùi Văn Khánh',    NULL, @demo_pw, @org_hcm_bgd,  @rank_director, 'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       (@u_nv_hoa,     'nv.hoa',     'nv.hoa@demo.local',     'Đỗ Thị Hoa',       NULL, @demo_pw, @org_hcm_kd,   @rank_staff,    'ACTIVE',   NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now),
       -- Đã nghỉ: minh họa vướng USER khi xóa Phòng Dự án HCM
       (@u_nv_hai,     'nv.hai',     'nv.hai@demo.local',     'Ngô Văn Hải',      NULL, @demo_pw, @org_hcm_da,   @rank_staff,    'INACTIVE', NULL, FALSE, 0, NULL, NOW(), @admin_id, @admin_id, @now, @now);

-- Vai trò: Nhân viên cho mọi người + vai trò riêng
INSERT INTO user_roles (id, user_id, role_id, created_by, created_at)
VALUES ('00000000-0000-7000-8000-00000d000501', @u_tgd_minh,   @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000502', @u_tgd_minh,   @role_executive,  @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000503', @u_gd_cuong,   @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000504', @u_gd_cuong,   @role_executive,  @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000505', @u_tp_binh,    @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000506', @u_nv_an,      @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000507', @u_hc_dung,    @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000508', @u_hc_dung,    @role_admin_ops,  @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000509', @u_kt_hien,    @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000510', @u_kt_hien,    @role_accountant, @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000511', @u_qlts_giang, @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000512', @u_qlts_giang, @role_asset_mgr,  @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000513', @u_gd_khanh,   @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000514', @u_gd_khanh,   @role_executive,  @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000515', @u_nv_hoa,     @role_employee,   @admin_id, @now),
       ('00000000-0000-7000-8000-00000d000516', @u_nv_hai,     @role_employee,   @admin_id, @now);

-- ---------------------------------------------------------------------
-- 4. Gán trưởng sau khi có người dùng — mỗi trưởng ngồi trong cây con của đơn vị mình (BR-05)
-- ---------------------------------------------------------------------
UPDATE org_units SET head_user_id = @u_tgd_minh, updated_at = @now WHERE id = @org_company;
UPDATE org_units SET head_user_id = @u_gd_cuong, updated_at = @now WHERE id = @org_hq;
UPDATE org_units SET head_user_id = @u_tp_binh,  updated_at = @now WHERE id = @org_hn_kt;
UPDATE org_units SET head_user_id = @u_gd_khanh, updated_at = @now WHERE id = @org_hcm;

-- ---------------------------------------------------------------------
-- 5. Email mọi người dùng (kể cả admin) → bí danh + của DEMO_MAILBOX
-- ---------------------------------------------------------------------
UPDATE users SET email = CONCAT('${demoMailUser}', '+', username, '@', '${demoMailDomain}');
