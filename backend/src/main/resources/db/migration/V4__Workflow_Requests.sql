-- =====================================================================
-- V4 — Quy trình duyệt & phiếu đề xuất
-- Cấu hình: workflows, 2 bảng điều kiện, workflow_stages, workflow_approver_rules
-- Phiếu:    requests, request_lines, disposal_lines
-- Thực thi: approval_tasks, task_candidates
-- =====================================================================

-- Quy trình duyệt — CẤU HÌNH
CREATE TABLE workflows (
                         id            UUID          NOT NULL,
                         code          VARCHAR(50)   NOT NULL,
                         name          VARCHAR(255)  NOT NULL,
                         description   TEXT          NULL,
                         request_type  VARCHAR(40)   NOT NULL,
                         priority      INT           NOT NULL,                 -- cao hơn thắng; duy nhất giữa ACTIVE — kiểm ở service
                         is_default    BOOLEAN       NOT NULL DEFAULT FALSE,   -- chỉ sinh từ seed
                         status        VARCHAR(40)   NOT NULL,
                         min_value     BIGINT        NULL,                     -- cận dưới, tính cả bằng
                         max_value     BIGINT        NULL,                     -- cận trên, không tính bằng — [từ, đến)
                         created_by    UUID          NOT NULL,
                         updated_by    UUID          NOT NULL,
                         created_at    DATETIME(6)   NOT NULL,
                         updated_at    DATETIME(6)   NOT NULL,
                         CONSTRAINT pk_workflows PRIMARY KEY (id),
                         CONSTRAINT uk_workflows_code UNIQUE (code),
                         CONSTRAINT fk_workflows_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                         CONSTRAINT fk_workflows_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                         CONSTRAINT ck_workflows_request_type CHECK (request_type IN ('ALLOCATION', 'LOAN', 'DISPOSAL')),
                         CONSTRAINT ck_workflows_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                         CONSTRAINT ck_workflows_min_value CHECK (min_value >= 0),
                         CONSTRAINT ck_workflows_max_value CHECK (max_value >= 0),
                         CONSTRAINT ck_workflows_value_range CHECK (min_value IS NULL OR max_value IS NULL OR min_value < max_value),
                          -- Mặc định: luôn ACTIVE, không ngưỡng
                         CONSTRAINT ck_workflows_default CHECK (
                           is_default = FALSE OR (status = 'ACTIVE' AND min_value IS NULL AND max_value IS NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Điều kiện cấp bậc — không có dòng = không điều kiện
CREATE TABLE workflow_rank_conditions (
                                        id           UUID         NOT NULL,
                                        workflow_id  UUID         NOT NULL,
                                        rank_id      UUID         NOT NULL,
                                        created_by   UUID         NOT NULL,
                                        created_at   DATETIME(6)  NOT NULL,
                                        CONSTRAINT pk_workflow_rank_conditions PRIMARY KEY (id),
                                        CONSTRAINT uk_workflow_rank_conditions UNIQUE (workflow_id, rank_id),
                                        CONSTRAINT fk_workflow_rank_conditions_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE CASCADE,
                                        CONSTRAINT fk_workflow_rank_conditions_rank FOREIGN KEY (rank_id) REFERENCES ranks (id) ON DELETE RESTRICT,
                                        CONSTRAINT fk_workflow_rank_conditions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Điều kiện loại tài sản — khớp khi ≥1 dòng phiếu thuộc tập
CREATE TABLE workflow_asset_type_conditions (
                                              id             UUID         NOT NULL,
                                              workflow_id    UUID         NOT NULL,
                                              asset_type_id  UUID         NOT NULL,
                                              created_by     UUID         NOT NULL,
                                              created_at     DATETIME(6)  NOT NULL,
                                              CONSTRAINT pk_workflow_asset_type_conditions PRIMARY KEY (id),
                                              CONSTRAINT uk_workflow_asset_type_conditions UNIQUE (workflow_id, asset_type_id),
                                              CONSTRAINT fk_workflow_asset_type_conditions_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE CASCADE,
                                              CONSTRAINT fk_workflow_asset_type_conditions_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE RESTRICT,
                                              CONSTRAINT fk_workflow_asset_type_conditions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Bước duyệt — không bảng thực thi nào trỏ tới
CREATE TABLE workflow_stages (
                               id                 UUID          NOT NULL,
                               workflow_id        UUID          NOT NULL,
                               stage_order        INT           NOT NULL,
                               name               VARCHAR(255)  NOT NULL,
                               completion_policy  VARCHAR(40)   NOT NULL,   -- ANY chỉ khi ≥2 quy tắc — kiểm ở service
                               sla_days           INT           NULL,       -- NULL = DEFAULT_STAGE_SLA_DAYS lúc tạo nhiệm vụ
                               created_by         UUID          NOT NULL,
                               updated_by         UUID          NOT NULL,
                               created_at         DATETIME(6)   NOT NULL,
                               updated_at         DATETIME(6)   NOT NULL,
                               CONSTRAINT pk_workflow_stages PRIMARY KEY (id),
                               CONSTRAINT uk_workflow_stages_order UNIQUE (workflow_id, stage_order),
                               CONSTRAINT fk_workflow_stages_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE CASCADE,
                               CONSTRAINT fk_workflow_stages_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_workflow_stages_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                               CONSTRAINT ck_workflow_stages_order CHECK (stage_order > 0),
                               CONSTRAINT ck_workflow_stages_policy CHECK (completion_policy IN ('ALL', 'ANY')),
                               CONSTRAINT ck_workflow_stages_sla_days CHECK (sla_days > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Quy tắc người duyệt — một nhánh của bước
CREATE TABLE workflow_approver_rules (
                                       id          UUID         NOT NULL,
                                       stage_id    UUID         NOT NULL,
                                       rule_order  INT          NOT NULL,   -- thứ tự nhánh, không dùng thứ tự id
                                       rule_type   VARCHAR(40)  NOT NULL,
                                       org_level   VARCHAR(40)  NULL,       -- chỉ ORG_HEAD
                                       role_id     UUID         NULL,       -- chỉ ROLE
                                       role_scope  VARCHAR(40)  NULL,       -- chỉ ROLE
                                       user_id     UUID         NULL,       -- chỉ USER
                                       created_by  UUID         NOT NULL,
                                       updated_by  UUID         NOT NULL,
                                       created_at  DATETIME(6)  NOT NULL,
                                       updated_at  DATETIME(6)  NOT NULL,
                                       CONSTRAINT pk_workflow_approver_rules PRIMARY KEY (id),
                                       CONSTRAINT uk_workflow_approver_rules_order UNIQUE (stage_id, rule_order),
                                       CONSTRAINT fk_workflow_approver_rules_stage FOREIGN KEY (stage_id) REFERENCES workflow_stages (id) ON DELETE CASCADE,
                                       CONSTRAINT fk_workflow_approver_rules_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT,
                                       CONSTRAINT fk_workflow_approver_rules_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
                                       CONSTRAINT fk_workflow_approver_rules_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                                       CONSTRAINT fk_workflow_approver_rules_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                                       CONSTRAINT ck_workflow_approver_rules_order CHECK (rule_order > 0),
                                       CONSTRAINT ck_workflow_approver_rules_type CHECK (rule_type IN ('ORG_HEAD', 'ROLE', 'USER')),
                                       CONSTRAINT ck_workflow_approver_rules_org_level CHECK (org_level IN ('DEPARTMENT', 'BRANCH', 'COMPANY')),
                                       CONSTRAINT ck_workflow_approver_rules_role_scope CHECK (role_scope IN ('SAME_DEPARTMENT', 'SAME_BRANCH', 'COMPANY_WIDE')),
                                        -- Mỗi kiểu quy tắc chỉ mang đúng tham số của nó
                                       CONSTRAINT ck_workflow_approver_rules_shape CHECK (
                                         (rule_type = 'ORG_HEAD' AND org_level IS NOT NULL AND role_id IS NULL AND role_scope IS NULL AND user_id IS NULL)
                                           OR (rule_type = 'ROLE' AND org_level IS NULL AND role_id IS NOT NULL AND role_scope IS NOT NULL AND user_id IS NULL)
                                           OR (rule_type = 'USER' AND org_level IS NULL AND role_id IS NULL AND role_scope IS NULL AND user_id IS NOT NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Phiếu đề xuất — ALLOCATION / LOAN / DISPOSAL, không bao giờ xóa dòng
CREATE TABLE requests (
                        id                        UUID          NOT NULL,
                        code                      VARCHAR(50)   NOT NULL,   -- PDX-<NĂM>-<4 SỐ>, từ code_sequences
                        request_type              VARCHAR(40)   NOT NULL,
                        requester_id              UUID          NOT NULL,
                        reason                    TEXT          NULL,       -- bắt buộc khi gửi
                        desired_receive_date      DATE          NULL,
                        due_date                  DATE          NULL,
                        proposed_disposal_method  VARCHAR(40)   NULL,
                        note                      TEXT          NULL,
                        status                    VARCHAR(40)   NOT NULL,   -- chỉ đổi bằng CAS
                        cloned_from_request_id    UUID          NULL,
                        submitted_at              DATETIME(6)   NULL,
                        request_value             BIGINT        NULL,       -- [TC] chụp lúc gửi
                        workflow_id               UUID          NULL,
                        route_json                JSON          NULL,       -- bản chụp lộ trình, bất biến
                        approved_at               DATETIME(6)   NULL,
                        close_type                VARCHAR(40)   NULL,
                        close_reason              TEXT          NULL,
                        closed_by                 UUID          NULL,
                        closed_at                 DATETIME(6)   NULL,
                        disposal_date             DATE          NULL,
                        actual_disposal_method    VARCHAR(40)   NULL,
                        disposal_record_number    VARCHAR(100)  NULL,
                        disposal_proceeds         BIGINT        NULL,       -- [TC]
                        created_by                UUID          NOT NULL,
                        updated_by                UUID          NOT NULL,
                        created_at                DATETIME(6)   NOT NULL,
                        updated_at                DATETIME(6)   NOT NULL,
                        CONSTRAINT pk_requests PRIMARY KEY (id),
                        CONSTRAINT uk_requests_code UNIQUE (code),
                        CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE RESTRICT,
                        CONSTRAINT fk_requests_cloned_from FOREIGN KEY (cloned_from_request_id) REFERENCES requests (id) ON DELETE RESTRICT,
                        -- Quy trình đã từng được chọn không xóa được
                        CONSTRAINT fk_requests_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE RESTRICT,
                        CONSTRAINT fk_requests_closed_by FOREIGN KEY (closed_by) REFERENCES users (id) ON DELETE RESTRICT,
                        CONSTRAINT fk_requests_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                        CONSTRAINT fk_requests_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                        CONSTRAINT ck_requests_type CHECK (request_type IN ('ALLOCATION', 'LOAN', 'DISPOSAL')),
                        CONSTRAINT ck_requests_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
                        CONSTRAINT ck_requests_proposed_method CHECK (proposed_disposal_method IN ('SELL', 'DESTROY', 'DONATE', 'RETURN_TO_SUPPLIER')),
                        CONSTRAINT ck_requests_actual_method CHECK (actual_disposal_method IN ('SELL', 'DESTROY', 'DONATE', 'RETURN_TO_SUPPLIER')),
                        CONSTRAINT ck_requests_close_type CHECK (close_type IN ('WITHDRAWN', 'CLOSED', 'REQUESTER_DEACTIVATED')),
                        CONSTRAINT ck_requests_request_value CHECK (request_value >= 0),
                        CONSTRAINT ck_requests_disposal_proceeds CHECK (disposal_proceeds >= 0),
                        CONSTRAINT ck_requests_route_object CHECK (route_json IS NULL OR JSON_TYPE(route_json) = 'OBJECT'),
                        CONSTRAINT ck_requests_not_self_clone CHECK (cloned_from_request_id IS NULL OR cloned_from_request_id <> id),
                        CONSTRAINT ck_requests_reason_on_submit CHECK (submitted_at IS NULL OR reason IS NOT NULL),
                        -- (1) Bốn cột chụp lúc gửi cùng có / cùng trống; gắn với trạng thái
                        CONSTRAINT ck_requests_submission_group CHECK (
                          (submitted_at IS NULL) = (request_value IS NULL)
                            AND (submitted_at IS NULL) = (workflow_id IS NULL)
                            AND (submitted_at IS NULL) = (route_json IS NULL)),
                        CONSTRAINT ck_requests_draft_not_submitted CHECK (status <> 'DRAFT' OR submitted_at IS NULL),
                        CONSTRAINT ck_requests_submitted_status CHECK (
                          status NOT IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED') OR submitted_at IS NOT NULL),
                        -- (2) Thời điểm duyệt xong gắn với trạng thái
                        CONSTRAINT ck_requests_approved_at CHECK (
                          (status NOT IN ('APPROVED', 'COMPLETED') OR approved_at IS NOT NULL)
                            AND (approved_at IS NULL OR status IN ('APPROVED', 'COMPLETED', 'CANCELLED'))),
                        -- (3) Nhóm đóng ngoài lộ trình
                        CONSTRAINT ck_requests_close_group CHECK (
                          (close_type IS NULL) = (closed_by IS NULL)
                            AND (close_type IS NULL) = (closed_at IS NULL)),
                        CONSTRAINT ck_requests_close_cancelled CHECK (
                          close_type NOT IN ('WITHDRAWN', 'REQUESTER_DEACTIVATED') OR status = 'CANCELLED'),
                        CONSTRAINT ck_requests_close_closed CHECK (close_type <> 'CLOSED' OR status IN ('COMPLETED', 'CANCELLED')),
                        -- Lý do đóng khi và chỉ khi CLOSED
                        CONSTRAINT ck_requests_close_reason CHECK ((close_reason IS NOT NULL) = (close_type <=> 'CLOSED')),
                        -- (4) Cột riêng theo loại phiếu
                        CONSTRAINT ck_requests_disposal_no_dates CHECK (
                          request_type <> 'DISPOSAL' OR (desired_receive_date IS NULL AND due_date IS NULL)),
                        CONSTRAINT ck_requests_non_disposal_columns CHECK (
                          request_type = 'DISPOSAL'
                            OR (proposed_disposal_method IS NULL AND disposal_date IS NULL AND actual_disposal_method IS NULL
                            AND disposal_record_number IS NULL AND disposal_proceeds IS NULL)),
                        CONSTRAINT ck_requests_disposal_method_on_submit CHECK (
                          request_type <> 'DISPOSAL' OR submitted_at IS NULL OR proposed_disposal_method IS NOT NULL),
                        -- (5) Phiếu LOAN đã gửi phải đủ hai ngày
                        CONSTRAINT ck_requests_loan_dates CHECK (
                          request_type <> 'LOAN' OR submitted_at IS NULL
                            OR (desired_receive_date IS NOT NULL AND due_date IS NOT NULL)),
                        CONSTRAINT ck_requests_due_after_receive CHECK (
                          due_date IS NULL OR desired_receive_date IS NULL OR due_date >= desired_receive_date),
                        CONSTRAINT ck_requests_disposal_date CHECK (disposal_date IS NULL OR disposal_date >= DATE(approved_at))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_requests_status ON requests (status, approved_at);

-- Dòng nhu cầu (loại × số lượng) — chỉ phiếu ALLOCATION / LOAN
CREATE TABLE request_lines (
                             id             UUID         NOT NULL,
                             request_id     UUID         NOT NULL,
                             asset_type_id  UUID         NOT NULL,
                             quantity       INT          NOT NULL,
                             note           TEXT         NULL,
                             created_by     UUID         NOT NULL,
                             updated_by     UUID         NOT NULL,
                             created_at     DATETIME(6)  NOT NULL,
                             updated_at     DATETIME(6)  NOT NULL,
                             CONSTRAINT pk_request_lines PRIMARY KEY (id),
                             CONSTRAINT uk_request_lines_type UNIQUE (request_id, asset_type_id),
                             CONSTRAINT fk_request_lines_request FOREIGN KEY (request_id) REFERENCES requests (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_request_lines_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_request_lines_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_request_lines_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT ck_request_lines_quantity CHECK (quantity > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Dòng thanh lý — chỉ phiếu DISPOSAL, trỏ thẳng tài sản / lô
CREATE TABLE disposal_lines (
                              id              UUID         NOT NULL,
                              request_id      UUID         NOT NULL,
                              asset_id        UUID         NULL,
                              batch_id        UUID         NULL,
                              quantity        INT          NOT NULL,
                              residual_value  BIGINT       NULL,       -- [TC] chụp lúc thực hiện thanh lý
                              created_by      UUID         NOT NULL,
                              updated_by      UUID         NOT NULL,
                              created_at      DATETIME(6)  NOT NULL,
                              updated_at      DATETIME(6)  NOT NULL,
                              CONSTRAINT pk_disposal_lines PRIMARY KEY (id),
                              -- NULL của nhánh còn lại không va chạm — đúng ý
                              CONSTRAINT uk_disposal_lines_asset UNIQUE (request_id, asset_id),
                              CONSTRAINT uk_disposal_lines_batch UNIQUE (request_id, batch_id),
                              CONSTRAINT fk_disposal_lines_request FOREIGN KEY (request_id) REFERENCES requests (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_disposal_lines_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_disposal_lines_batch FOREIGN KEY (batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_disposal_lines_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_disposal_lines_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT ck_disposal_lines_target CHECK ((asset_id IS NULL) <> (batch_id IS NULL)),
                              CONSTRAINT ck_disposal_lines_quantity CHECK (quantity > 0 AND (asset_id IS NULL OR quantity = 1)),
                              CONSTRAINT ck_disposal_lines_residual_value CHECK (residual_value >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Nhiệm vụ duyệt — THỰC THI, định vị bằng (stage_order, rule_index) trong route_json
CREATE TABLE approval_tasks (
                              id                       UUID         NOT NULL,
                              request_id               UUID         NOT NULL,
                              stage_order              INT          NOT NULL,
                              rule_index               INT          NOT NULL,
                              status                   VARCHAR(40)  NOT NULL,
                              due_at                   DATETIME(6)  NOT NULL,   -- lưu, không tính lại khi đổi SLA
                              decided_by               UUID         NULL,
                              decided_at               DATETIME(6)  NULL,
                              comment                  TEXT         NULL,       -- bắt buộc khi từ chối
                              reassigned_from_task_id  UUID         NULL,
                              reassign_reason          TEXT         NULL,
                              created_by               UUID         NOT NULL,   -- người khởi phát
                              updated_by               UUID         NOT NULL,
                              created_at               DATETIME(6)  NOT NULL,   -- = thời điểm kích hoạt
                              updated_at               DATETIME(6)  NOT NULL,
                              CONSTRAINT pk_approval_tasks PRIMARY KEY (id),
                              -- Bốn mắt: NULL của nhiệm vụ chưa quyết định không va chạm — đúng ý
                              CONSTRAINT uk_approval_tasks_request_decider UNIQUE (request_id, decided_by),
                              CONSTRAINT uk_approval_tasks_reassigned_from UNIQUE (reassigned_from_task_id),
                              CONSTRAINT fk_approval_tasks_request FOREIGN KEY (request_id) REFERENCES requests (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_approval_tasks_decided_by FOREIGN KEY (decided_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_approval_tasks_reassigned_from FOREIGN KEY (reassigned_from_task_id) REFERENCES approval_tasks (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_approval_tasks_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT fk_approval_tasks_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                              CONSTRAINT ck_approval_tasks_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'REASSIGNED')),
                              CONSTRAINT ck_approval_tasks_position CHECK (stage_order > 0 AND rule_index >= 0),
                              -- Người / thời điểm quyết định khi và chỉ khi đã quyết định
                              CONSTRAINT ck_approval_tasks_decision CHECK (
                                (decided_by IS NOT NULL) = (status IN ('APPROVED', 'REJECTED'))
                                  AND (decided_at IS NOT NULL) = (status IN ('APPROVED', 'REJECTED'))),
                              CONSTRAINT ck_approval_tasks_reject_comment CHECK (status <> 'REJECTED' OR comment IS NOT NULL),
                              -- Lý do chuyển khi và chỉ khi là nhiệm vụ chuyển
                              CONSTRAINT ck_approval_tasks_reassign CHECK ((reassigned_from_task_id IS NULL) = (reassign_reason IS NULL)),
                              CONSTRAINT ck_approval_tasks_not_self_reassign CHECK (reassigned_from_task_id IS NULL OR reassigned_from_task_id <> id),
                              CONSTRAINT ck_approval_tasks_due_at CHECK (due_at > created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_approval_tasks_stage ON approval_tasks (request_id, stage_order, rule_index);
CREATE INDEX idx_approval_tasks_status_due ON approval_tasks (status, due_at);

-- Ứng viên — quyền duyệt phát sinh từ quan hệ này
CREATE TABLE task_candidates (
                               id                      UUID         NOT NULL,
                               task_id                 UUID         NOT NULL,
                               user_id                 UUID         NOT NULL,
                               escalated_from_unit_id  UUID         NULL,       -- NULL = chọn trực tiếp; có giá trị = đã leo thang
                               created_at              DATETIME(6)  NOT NULL,
                               CONSTRAINT pk_task_candidates PRIMARY KEY (id),
                               CONSTRAINT uk_task_candidates_task_user UNIQUE (task_id, user_id),
                               CONSTRAINT fk_task_candidates_task FOREIGN KEY (task_id) REFERENCES approval_tasks (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_task_candidates_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_task_candidates_escalated_from FOREIGN KEY (escalated_from_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
