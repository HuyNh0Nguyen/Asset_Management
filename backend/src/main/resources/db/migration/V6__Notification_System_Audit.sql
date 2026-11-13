-- =====================================================================
-- V6 — Thông báo & hệ thống
-- notifications, email_logs, reminder_logs, system_configs, audit_logs, code_sequences
-- Bảng nhật ký / cấu hình: chỉ mang những cột dấu vết có người đọc
-- =====================================================================

-- Thông báo in-app — tạo cùng giao dịch nghiệp vụ
CREATE TABLE notifications (
                             id            UUID          NOT NULL,
                             recipient_id  UUID          NOT NULL,
                             event_type    VARCHAR(40)   NOT NULL,
                             content       TEXT          NOT NULL,   -- câu tiếng Việt dựng lúc tạo
                             link          VARCHAR(512)  NULL,
                             is_read       BOOLEAN       NOT NULL DEFAULT FALSE,
                             created_at    DATETIME(6)   NOT NULL,
                             CONSTRAINT pk_notifications PRIMARY KEY (id),
                              -- Chuông thông báo: chưa đọc của tôi, mới nhất trước; cũng phục vụ FK recipient_id
                             INDEX idx_notifications_recipient (recipient_id, is_read, created_at),
                             CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE RESTRICT,
                              -- Sự kiện có kênh in-app
                             CONSTRAINT ck_notifications_event_type CHECK (event_type IN (
                                                                                          'TASK_ACTIVATED', 'REQUEST_APPROVED', 'REQUEST_REJECTED', 'REQUEST_STOPPED',
                                                                                          'ASSET_HANDED_OVER', 'ASSET_RECOVERED', 'HOLDER_REQUEST', 'TASK_REASSIGNED',
                                                                                          'TASK_UNRESOLVED', 'RECOVERY_DUE_REMINDER', 'RECOVERY_OVERDUE_DIGEST', 'TASK_OVERDUE_REMINDER',
                                                                                          'TASK_OVERDUE_DIGEST', 'APPROVED_REQUEST_CLOSED', 'RECOVERY_EXTENDED', 'HELD_ASSET_MAINTENANCE',
                                                                                          'REQUEST_STAGE_PASSED', 'EXTENSION_REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Nhật ký email — backend chèn PENDING, email-service chốt SENT / FAILED
-- Không lưu nội dung, không tham chiếu ngược đối tượng nghiệp vụ
CREATE TABLE email_logs (
                          id               UUID          NOT NULL,
                          recipient_id     UUID          NOT NULL,
                          recipient_email  VARCHAR(255)  NOT NULL,   -- chụp lúc gửi
                          event_type       VARCHAR(40)   NOT NULL,
                          subject          VARCHAR(255)  NOT NULL,
                          status           VARCHAR(40)   NOT NULL,
                          attempt_count    INT           NOT NULL DEFAULT 0,   -- số lần thử lại đã lên lịch
                          last_error       TEXT          NULL,
                          created_at       DATETIME(6)   NOT NULL,
                          updated_at       DATETIME(6)   NOT NULL,
                          CONSTRAINT pk_email_logs PRIMARY KEY (id),
                          CONSTRAINT fk_email_logs_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE RESTRICT,
                          -- Sự kiện có kênh email
                          CONSTRAINT ck_email_logs_event_type CHECK (event_type IN (
                                                                                    'TASK_ACTIVATED', 'REQUEST_APPROVED', 'REQUEST_REJECTED',
                                                                                    'ASSET_HANDED_OVER', 'ASSET_RECOVERED', 'HOLDER_REQUEST', 'TASK_REASSIGNED',
                                                                                    'TASK_UNRESOLVED', 'RECOVERY_DUE_REMINDER', 'RECOVERY_OVERDUE_DIGEST', 'TASK_OVERDUE_REMINDER',
                                                                                    'ACCOUNT_CREATED', 'PASSWORD_RESET', 'TASK_OVERDUE_DIGEST', 'APPROVED_REQUEST_CLOSED',
                                                                                    'RECOVERY_EXTENDED', 'HELD_ASSET_MAINTENANCE', 'EXTENSION_REJECTED')),
                          CONSTRAINT ck_email_logs_status CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
                          CONSTRAINT ck_email_logs_attempt_count CHECK (attempt_count >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_email_logs_status ON email_logs (status, created_at);

-- Nhật ký nhắc nhở — chống trùng "1 nhắc / đối tượng / người nhận / ngày"
-- target_id đa hình, không FK
CREATE TABLE reminder_logs (
                             id            UUID         NOT NULL,
                             target_type   VARCHAR(40)  NOT NULL,
                             target_id     UUID         NOT NULL,   -- bản tổng hợp: = recipient_id, KHÔNG để NULL
                             recipient_id  UUID         NOT NULL,
                             alert_level   VARCHAR(40)  NULL,
                             sent_date     DATE         NOT NULL,
                             created_at    DATETIME(6)  NOT NULL,
                             CONSTRAINT pk_reminder_logs PRIMARY KEY (id),
                             CONSTRAINT uk_reminder_logs_daily UNIQUE (target_type, target_id, recipient_id, sent_date),
                             CONSTRAINT fk_reminder_logs_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT ck_reminder_logs_target_type CHECK (target_type IN ('ALLOCATION', 'APPROVAL_TASK', 'RECOVERY_DIGEST', 'TASK_DIGEST')),
                             CONSTRAINT ck_reminder_logs_alert_level CHECK (alert_level IN ('NORMAL', 'HIGH', 'HIGHEST')),
                              -- Mức cảnh báo khi và chỉ khi nhắc hạn thu hồi
                             CONSTRAINT ck_reminder_logs_alert_level_target CHECK ((alert_level IS NOT NULL) = (target_type = 'ALLOCATION')),
                              -- Bản tổng hợp trỏ về chính người nhận — giữ khóa chống trùng có tác dụng
                             CONSTRAINT ck_reminder_logs_digest_target CHECK (
                               target_type NOT IN ('RECOVERY_DIGEST', 'TASK_DIGEST') OR target_id = recipient_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Cấu hình hệ thống — 6 khóa cố định, khớp enum khóa cấu hình
CREATE TABLE system_configs (
                              id            UUID          NOT NULL,
                              config_key    VARCHAR(100)  NOT NULL,
                              config_value  VARCHAR(255)  NOT NULL,   -- kiểm kiểu theo khóa ở tầng ứng dụng
                              updated_by    UUID          NOT NULL,
                              updated_at    DATETIME(6)   NOT NULL,
                              CONSTRAINT pk_system_configs PRIMARY KEY (id),
                              CONSTRAINT uk_system_configs_key UNIQUE (config_key),
                              CONSTRAINT fk_system_configs_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT ck_system_configs_key CHECK (config_key IN (
                                                                                     'ALERT_JOB_TIME', 'ALERT_JOB_ENABLED', 'RECOVERY_ALERT_DAYS',
                                                                                     'DEFAULT_STAGE_SLA_DAYS', 'MAX_LOAN_DAYS', 'DASHBOARD_DUE_WINDOW_DAYS'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Audit Log — chỉ ghi thêm, giữ vô thời hạn; target_id đa hình, không FK
CREATE TABLE audit_logs (
                          id           UUID          NOT NULL,
                          actor_id     UUID          NOT NULL,   -- luôn tài khoản thật
                          actor_roles  JSON          NOT NULL,   -- mảng mã vai trò lúc thao tác
                          action       VARCHAR(40)   NOT NULL,
                          target_type  VARCHAR(40)   NOT NULL,
                          target_id    UUID          NOT NULL,
                          target_code  VARCHAR(100)  NOT NULL,   -- mã dễ đọc chụp lúc ghi
                          old_value    JSON          NULL,       -- chỉ trường thay đổi
                          new_value    JSON          NULL,
                          created_at   DATETIME(6)   NOT NULL,
                          CONSTRAINT pk_audit_logs PRIMARY KEY (id),
                          -- Lọc theo người thực hiện; cũng phục vụ FK actor_id
                          INDEX idx_audit_logs_actor (actor_id, created_at),
                          CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE RESTRICT,
                          CONSTRAINT ck_audit_logs_action CHECK (action IN (
                                                                            'CREATE', 'UPDATE', 'DELETE', 'TRANSITION', 'TRANSFER', 'BATCH_SPLIT',
                                                                            'SUBMIT', 'APPROVE', 'REJECT', 'WITHDRAW', 'CLOSE', 'CANCEL', 'REASSIGN', 'CLONE',
                                                                            'IMAGE_ADD', 'IMAGE_REMOVE', 'IMAGE_SET_PRIMARY',
                                                                            'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT', 'PASSWORD_CHANGE', 'PASSWORD_RESET',
                                                                            'DEACTIVATE', 'REACTIVATE')),
                          -- Không có loại "ảnh": thao tác ảnh ghi lên chủ của ảnh
                          CONSTRAINT ck_audit_logs_target_type CHECK (target_type IN (
                                                                                      'ORG_UNIT', 'RANK', 'USER', 'ROLE', 'ASSET_TYPE', 'SUPPLIER', 'ASSET', 'ASSET_BATCH',
                                                                                      'ALLOCATION', 'MAINTENANCE_TICKET', 'REQUEST', 'WORKFLOW', 'APPROVAL_TASK', 'SYSTEM_CONFIG')),
                          CONSTRAINT ck_audit_logs_actor_roles_array CHECK (JSON_TYPE(actor_roles) = 'ARRAY'),
                          CONSTRAINT ck_audit_logs_old_value_object CHECK (old_value IS NULL OR JSON_TYPE(old_value) = 'OBJECT'),
                          CONSTRAINT ck_audit_logs_new_value_object CHECK (new_value IS NULL OR JSON_TYPE(new_value) = 'OBJECT')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Lọc theo mã đối tượng + gộp lịch sử tác động
CREATE INDEX idx_audit_logs_target_code ON audit_logs (target_code, created_at);
CREATE INDEX idx_audit_logs_target ON audit_logs (target_type, target_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

-- Bộ đếm sinh mã — mã đã cấp không bao giờ cấp lại
-- Khóa chuỗi NOT NULL thay vì (loại dãy, asset_type_id, năm) để tránh bẫy NULL trong unique
CREATE TABLE code_sequences (
                              id            UUID          NOT NULL,
                              sequence_key  VARCHAR(100)  NOT NULL,   -- ASSET:<MÃ LOẠI>:<NĂM> · BATCH:<MÃ LOẠI>:<NĂM> · REQUEST:<NĂM>
                              next_value    INT           NOT NULL,
                              updated_at    DATETIME(6)   NOT NULL,
                              CONSTRAINT pk_code_sequences PRIMARY KEY (id),
                              CONSTRAINT uk_code_sequences_key UNIQUE (sequence_key),
                              CONSTRAINT ck_code_sequences_key_format CHECK (
                                CAST(sequence_key AS BINARY) REGEXP '^((ASSET|BATCH):[A-Z0-9]+:[0-9]{4}|REQUEST:[0-9]{4})$'),
                              CONSTRAINT ck_code_sequences_next_value CHECK (next_value > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
