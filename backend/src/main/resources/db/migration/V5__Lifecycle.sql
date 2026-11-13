-- =====================================================================
-- V5 — Vòng đời
-- allocations, maintenance_tickets, asset_movements
-- =====================================================================

-- Bản ghi cấp phát — luôn một người giữ
CREATE TABLE allocations (
                           id                         UUID         NOT NULL,
                           asset_id                   UUID         NULL,
                           batch_id                   UUID         NULL,
                           quantity                   INT          NOT NULL,             -- bất biến sau khi tạo
                           returned_quantity          INT          NOT NULL DEFAULT 0,   -- chỉ có nghĩa với lô
                           holder_id                  UUID         NOT NULL,             -- = người đề xuất phiếu gốc
                           request_line_id            UUID         NOT NULL,
                           due_date                   DATE         NULL,                 -- chép từ phiếu, gia hạn sửa riêng
                           status                     VARCHAR(40)  NOT NULL,
                           holder_request_type        VARCHAR(40)  NULL,                 -- đề nghị gần nhất còn chờ xử lý
                           holder_request_note        TEXT         NULL,
                           holder_requested_at        DATETIME(6)  NULL,
                           holder_requested_due_date  DATE         NULL,                 -- chỉ EXTENSION
                           close_reason               VARCHAR(40)  NULL,
                           closed_by                  UUID         NULL,
                           closed_at                  DATETIME(6)  NULL,
                           created_by                 UUID         NOT NULL,             -- = người bàn giao
                           updated_by                 UUID         NOT NULL,
                           created_at                 DATETIME(6)  NOT NULL,             -- = thời điểm bàn giao
                           updated_at                 DATETIME(6)  NOT NULL,
                           CONSTRAINT pk_allocations PRIMARY KEY (id),
                            -- "Tài sản tôi đang giữ", phạm vi SELF
                           INDEX idx_allocations_holder_status (holder_id, status),
                           INDEX idx_allocations_status_due (status, due_date),
                           CONSTRAINT fk_allocations_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_batch FOREIGN KEY (batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_holder FOREIGN KEY (holder_id) REFERENCES users (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_request_line FOREIGN KEY (request_line_id) REFERENCES request_lines (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_closed_by FOREIGN KEY (closed_by) REFERENCES users (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_allocations_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                           CONSTRAINT ck_allocations_target CHECK ((asset_id IS NULL) <> (batch_id IS NULL)),
                           CONSTRAINT ck_allocations_status CHECK (status IN ('OPEN', 'CLOSED')),
                           CONSTRAINT ck_allocations_holder_request_type CHECK (holder_request_type IN ('RETURN', 'EXTENSION')),
                           CONSTRAINT ck_allocations_close_reason CHECK (close_reason IN (
                                                                                          'RETURNED_BY_HOLDER', 'LOAN_EXPIRED', 'OFFBOARDING', 'NEED_CHANGED',
                                                                                          'UNREPAIRABLE', 'CLOSED_IN_MAINTENANCE', 'OTHER')),
                           CONSTRAINT ck_allocations_quantity CHECK (quantity > 0 AND returned_quantity BETWEEN 0 AND quantity),
                            -- Tài sản cá biệt: 1 chiếc, không trả từng phần
                           CONSTRAINT ck_allocations_serial_quantity CHECK (asset_id IS NULL OR (quantity = 1 AND returned_quantity = 0)),
                            -- CLOSED ⇔ đủ người đóng, thời điểm, lý do
                           CONSTRAINT ck_allocations_close_group CHECK (
                             (status = 'CLOSED') = (closed_by IS NOT NULL)
                               AND (status = 'CLOSED') = (closed_at IS NOT NULL)
                               AND (status = 'CLOSED') = (close_reason IS NOT NULL)),
                            -- Lô chỉ đóng khi đã trả đủ
                           CONSTRAINT ck_allocations_batch_closed CHECK (batch_id IS NULL OR status <> 'CLOSED' OR returned_quantity = quantity),
                            -- Đề nghị của người giữ: loại + mốc cùng có / cùng trống; ghi chú chỉ khi có đề nghị
                           CONSTRAINT ck_allocations_holder_request CHECK (
                             (holder_request_type IS NULL) = (holder_requested_at IS NULL)
                               AND (holder_request_type IS NOT NULL OR holder_request_note IS NULL)),
                            -- Ngày gia hạn mong muốn khi và chỉ khi EXTENSION
                           CONSTRAINT ck_allocations_holder_due_date CHECK (
                             (holder_requested_due_date IS NOT NULL) = (holder_request_type <=> 'EXTENSION'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Phiếu bảo trì — không có allocation_id
CREATE TABLE maintenance_tickets (
                                   id                        UUID         NOT NULL,
                                   asset_id                  UUID         NULL,
                                   batch_id                  UUID         NULL,
                                   quantity                  INT          NOT NULL,
                                   entered_from_status       VARCHAR(40)  NOT NULL,   -- quyết định đường ra
                                   reason                    TEXT         NOT NULL,
                                   maintenance_type          VARCHAR(40)  NOT NULL,
                                   vendor_supplier_id        UUID         NULL,
                                   sent_date                 DATE         NOT NULL,
                                   expected_completion_date  DATE         NOT NULL,
                                   status                    VARCHAR(40)  NOT NULL,
                                   result                    VARCHAR(40)  NULL,       -- chỉ tài sản cá biệt
                                   repaired_quantity         INT          NULL,       -- chỉ lô
                                   completed_date            DATE         NULL,
                                   cost                      BIGINT       NULL,       -- [TC] Kế toán ghi bất kỳ lúc nào
                                   created_by                UUID         NOT NULL,   -- = người gửi bảo trì
                                   updated_by                UUID         NOT NULL,
                                   created_at                DATETIME(6)  NOT NULL,
                                   updated_at                DATETIME(6)  NOT NULL,
                                   CONSTRAINT pk_maintenance_tickets PRIMARY KEY (id),
                                   CONSTRAINT fk_maintenance_tickets_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_maintenance_tickets_batch FOREIGN KEY (batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_maintenance_tickets_vendor FOREIGN KEY (vendor_supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_maintenance_tickets_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_maintenance_tickets_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                                   CONSTRAINT ck_maintenance_tickets_target CHECK ((asset_id IS NULL) <> (batch_id IS NULL)),
                                   CONSTRAINT ck_maintenance_tickets_quantity CHECK (quantity > 0 AND (asset_id IS NULL OR quantity = 1)),
                                   CONSTRAINT ck_maintenance_tickets_entered_from CHECK (entered_from_status IN ('ALLOCATED', 'AVAILABLE', 'RECALLED')),
                                    -- Lô không bảo trì khi đang sử dụng
                                   CONSTRAINT ck_maintenance_tickets_t04_serial CHECK (entered_from_status <> 'ALLOCATED' OR asset_id IS NOT NULL),
                                   CONSTRAINT ck_maintenance_tickets_type CHECK (maintenance_type IN ('WARRANTY', 'OUTSOURCED', 'INTERNAL')),
                                    -- Nội bộ ⇔ không có đơn vị sửa chữa
                                   CONSTRAINT ck_maintenance_tickets_vendor CHECK ((maintenance_type = 'INTERNAL') = (vendor_supplier_id IS NULL)),
                                   CONSTRAINT ck_maintenance_tickets_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
                                   CONSTRAINT ck_maintenance_tickets_result CHECK (result IN ('REPAIRED', 'UNREPAIRABLE')),
                                   CONSTRAINT ck_maintenance_tickets_repaired_quantity CHECK (repaired_quantity BETWEEN 0 AND quantity),
                                   CONSTRAINT ck_maintenance_tickets_completed_date CHECK ((status = 'COMPLETED') = (completed_date IS NOT NULL)),
                                    -- Cá biệt: kết quả khi hoàn tất; lô: số sửa xong khi hoàn tất
                                   CONSTRAINT ck_maintenance_tickets_serial_result CHECK (
                                     asset_id IS NULL OR ((status = 'COMPLETED') = (result IS NOT NULL) AND repaired_quantity IS NULL)),
                                   CONSTRAINT ck_maintenance_tickets_batch_result CHECK (
                                     batch_id IS NULL OR ((status = 'COMPLETED') = (repaired_quantity IS NOT NULL) AND result IS NULL)),
                                   CONSTRAINT ck_maintenance_tickets_cost CHECK (cost >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_maintenance_tickets_status_expected ON maintenance_tickets (status, expected_completion_date);

-- Nhật ký biến động — chỉ ghi thêm (ngoại lệ: tài sản nhập nhầm)
CREATE TABLE asset_movements (
                               id                     UUID         NOT NULL,
                               asset_id               UUID         NULL,
                               batch_id               UUID         NULL,
                               event_type             VARCHAR(40)  NOT NULL,
                               from_status            VARCHAR(40)  NULL,       -- NULL chỉ ở dòng khai sinh
                               to_status              VARCHAR(40)  NOT NULL,
                               quantity               INT          NOT NULL,
                               allocation_id          UUID         NULL,
                               maintenance_ticket_id  UUID         NULL,
                               request_id             UUID         NULL,
                               related_batch_id       UUID         NULL,       -- BATCH_SPLIT
                               from_unit_id           UUID         NULL,
                               to_unit_id             UUID         NULL,
                               note                   TEXT         NULL,
                               created_by             UUID         NOT NULL,   -- người thực hiện biến động
                               created_at             DATETIME(6)  NOT NULL,
                               CONSTRAINT pk_asset_movements PRIMARY KEY (id),
                                -- Lịch sử tác động; cũng phục vụ FK asset_id / batch_id
                               INDEX idx_asset_movements_asset (asset_id, created_at),
                               INDEX idx_asset_movements_batch (batch_id, created_at),
                               CONSTRAINT fk_asset_movements_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_batch FOREIGN KEY (batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_allocation FOREIGN KEY (allocation_id) REFERENCES allocations (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_maintenance FOREIGN KEY (maintenance_ticket_id) REFERENCES maintenance_tickets (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_request FOREIGN KEY (request_id) REFERENCES requests (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_related_batch FOREIGN KEY (related_batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_from_unit FOREIGN KEY (from_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_to_unit FOREIGN KEY (to_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                               CONSTRAINT fk_asset_movements_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                               CONSTRAINT ck_asset_movements_target CHECK ((asset_id IS NULL) <> (batch_id IS NULL)),
                               CONSTRAINT ck_asset_movements_quantity CHECK (quantity > 0 AND (asset_id IS NULL OR quantity = 1)),
                               CONSTRAINT ck_asset_movements_event_type CHECK (event_type IN (
                                                                                              'T01', 'T02', 'T03', 'T04', 'T05', 'T06', 'T07', 'T08', 'T09', 'T10', 'T11', 'T12',
                                                                                              'TRANSFER', 'BATCH_SPLIT', 'INSPECTION')),
                               CONSTRAINT ck_asset_movements_from_status CHECK (from_status IN ('AVAILABLE', 'ALLOCATED', 'IN_MAINTENANCE', 'RECALLED', 'DISPOSED')),
                               CONSTRAINT ck_asset_movements_to_status CHECK (to_status IN ('AVAILABLE', 'ALLOCATED', 'IN_MAINTENANCE', 'RECALLED', 'DISPOSED')),
                                -- Dùng <=> cho from_status để nhánh có NULL ra FALSE, không ra NULL (NULL làm CHECK đạt)
                               CONSTRAINT ck_asset_movements_transition CHECK (
                                 (event_type = 'T01' AND from_status IS NULL                  AND to_status = 'AVAILABLE')
                                   OR (event_type = 'T02' AND from_status <=> 'AVAILABLE'          AND to_status = 'ALLOCATED'
                                   AND allocation_id IS NOT NULL)
                                   OR (event_type = 'T03' AND from_status <=> 'ALLOCATED'          AND to_status = 'RECALLED'
                                   AND allocation_id IS NOT NULL)
                                   OR (event_type = 'T04' AND from_status <=> 'ALLOCATED'          AND to_status = 'IN_MAINTENANCE'
                                   AND maintenance_ticket_id IS NOT NULL AND allocation_id IS NOT NULL AND asset_id IS NOT NULL)
                                   OR (event_type = 'T05' AND from_status <=> 'AVAILABLE'          AND to_status = 'IN_MAINTENANCE'
                                   AND maintenance_ticket_id IS NOT NULL)
                                   OR (event_type = 'T06' AND from_status <=> 'RECALLED'           AND to_status = 'IN_MAINTENANCE'
                                   AND maintenance_ticket_id IS NOT NULL)
                                   OR (event_type = 'T07' AND from_status <=> 'IN_MAINTENANCE'     AND to_status = 'ALLOCATED'
                                   AND maintenance_ticket_id IS NOT NULL AND allocation_id IS NOT NULL AND asset_id IS NOT NULL)
                                   OR (event_type = 'T08' AND from_status <=> 'IN_MAINTENANCE'     AND to_status = 'AVAILABLE'
                                   AND maintenance_ticket_id IS NOT NULL)
                                   OR (event_type = 'T09' AND from_status <=> 'IN_MAINTENANCE'     AND to_status = 'RECALLED'
                                   AND maintenance_ticket_id IS NOT NULL)
                                   OR (event_type = 'T10' AND from_status <=> 'AVAILABLE'          AND to_status = 'RECALLED'
                                   AND note IS NOT NULL)
                                   OR (event_type = 'T11' AND from_status <=> 'RECALLED'           AND to_status = 'AVAILABLE')
                                   OR (event_type = 'T12' AND from_status <=> 'RECALLED'           AND to_status = 'DISPOSED'
                                   AND request_id IS NOT NULL)
                                   OR (event_type = 'TRANSFER' AND from_status <=> to_status       AND to_status IN ('AVAILABLE', 'RECALLED')
                                   AND from_unit_id IS NOT NULL AND to_unit_id IS NOT NULL AND from_unit_id <> to_unit_id)
                                   OR (event_type = 'BATCH_SPLIT' AND batch_id IS NOT NULL AND related_batch_id IS NOT NULL
                                   AND to_status IN ('AVAILABLE', 'RECALLED')
                                   AND (from_status IS NULL OR from_status <=> to_status))
                                   OR (event_type = 'INSPECTION' AND from_status <=> 'RECALLED'    AND to_status = 'RECALLED'
                                   AND note IS NOT NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
