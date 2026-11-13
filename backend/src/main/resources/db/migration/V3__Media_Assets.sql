-- =====================================================================
-- V3 — Ảnh & tài sản
-- image_files (+ FK users.avatar_image_id), assets, asset_batches, asset_images
-- =====================================================================

-- Tệp ảnh đã chuẩn hóa trên MinIO — bất biến, không có updated_*
CREATE TABLE image_files (
                           id          UUID          NOT NULL,
                           object_key  VARCHAR(512)  NOT NULL,   -- khóa bản chính; bản thu nhỏ = thumb/<object_key>
                           created_by  UUID          NOT NULL,
                           created_at  DATETIME(6)   NOT NULL,
                           CONSTRAINT pk_image_files PRIMARY KEY (id),
                           CONSTRAINT uk_image_files_object_key UNIQUE (object_key),
                           CONSTRAINT fk_image_files_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Ảnh đại diện: xóa tệp thì người dùng về không có ảnh
ALTER TABLE users
  ADD CONSTRAINT fk_users_avatar_image FOREIGN KEY (avatar_image_id) REFERENCES image_files (id) ON DELETE SET NULL;

-- Tài sản cá biệt (loại SERIAL)
CREATE TABLE assets (
                      id                       UUID          NOT NULL,
                      code                     VARCHAR(50)   NOT NULL,   -- <MÃ LOẠI>-<NĂM>-<5 SỐ>, từ code_sequences
                      asset_type_id            UUID          NOT NULL,   -- loại SERIAL — kiểm ở service
                      name                     VARCHAR(255)  NOT NULL,
                      serial_number            VARCHAR(100)  NOT NULL,
                      manufacturer             VARCHAR(255)  NULL,
                      supplier_id              UUID          NOT NULL,   -- bắt buộc
                      warranty_expiry_date     DATE          NULL,
                      managing_unit_id         UUID          NOT NULL,
                      attributes_json          JSON          NOT NULL,
                      technical_note           TEXT          NULL,
                      status                   VARCHAR(40)   NOT NULL,   -- chỉ đổi bằng CAS
                      original_cost            BIGINT        NULL,       -- [TC]
                      depreciation_start_date  DATE          NULL,       -- [TC]
                      depreciation_months      INT           NULL,       -- [TC]
                      invoice_number           VARCHAR(100)  NULL,       -- [TC]
                      created_by               UUID          NOT NULL,
                      updated_by               UUID          NOT NULL,
                      created_at               DATETIME(6)   NOT NULL,
                      updated_at               DATETIME(6)   NOT NULL,
                      CONSTRAINT pk_assets PRIMARY KEY (id),
                      CONSTRAINT uk_assets_code UNIQUE (code),
                      -- Serial duy nhất trong loại, tính cả tài sản đã thanh lý
                      CONSTRAINT uk_assets_type_serial UNIQUE (asset_type_id, serial_number),
                      CONSTRAINT fk_assets_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE RESTRICT,
                      CONSTRAINT fk_assets_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
                      CONSTRAINT fk_assets_managing_unit FOREIGN KEY (managing_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                      CONSTRAINT fk_assets_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                      CONSTRAINT fk_assets_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                      CONSTRAINT ck_assets_status CHECK (status IN ('AVAILABLE', 'ALLOCATED', 'IN_MAINTENANCE', 'RECALLED', 'DISPOSED')),
                      CONSTRAINT ck_assets_attributes_object CHECK (JSON_TYPE(attributes_json) = 'OBJECT'),
                      CONSTRAINT ck_assets_original_cost CHECK (original_cost >= 0),
                      CONSTRAINT ck_assets_depreciation_months CHECK (depreciation_months > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_assets_status ON assets (status);

-- Lô tài sản (loại QUANTITY) — số đang cấp phát suy ra từ allocations OPEN
CREATE TABLE asset_batches (
                             id                       UUID          NOT NULL,
                             code                     VARCHAR(50)   NOT NULL,   -- <MÃ LOẠI>-L<NĂM>-<SỐ>, từ code_sequences
                             asset_type_id            UUID          NOT NULL,   -- loại QUANTITY — kiểm ở service
                             name                     VARCHAR(255)  NOT NULL,   -- service TRIM trước khi lưu
                             quantity                 INT           NOT NULL,
                             available_quantity       INT           NOT NULL DEFAULT 0,
                             maintenance_quantity     INT           NOT NULL DEFAULT 0,
                             recalled_quantity        INT           NOT NULL DEFAULT 0,
                             disposed_quantity        INT           NOT NULL DEFAULT 0,
                             supplier_id              UUID          NOT NULL,   -- bắt buộc
                             managing_unit_id         UUID          NOT NULL,
                             location                 VARCHAR(255)  NULL,
                             attributes_json          JSON          NOT NULL,
                             technical_note           TEXT          NULL,
                             split_from_batch_id      UUID          NULL,       -- tách từ lô
                             unit_cost                BIGINT        NULL,       -- [TC]
                             depreciation_start_date  DATE          NULL,       -- [TC]
                             depreciation_months      INT           NULL,       -- [TC]
                             invoice_number           VARCHAR(100)  NULL,       -- [TC]
                             created_by               UUID          NOT NULL,
                             updated_by               UUID          NOT NULL,
                             created_at               DATETIME(6)   NOT NULL,
                             updated_at               DATETIME(6)   NOT NULL,
                             CONSTRAINT pk_asset_batches PRIMARY KEY (id),
                             CONSTRAINT uk_asset_batches_code UNIQUE (code),
                              -- Gộp tồn kho theo (loại, tên); cũng phục vụ FK asset_type_id
                             INDEX idx_asset_batches_type_name (asset_type_id, name),
                             CONSTRAINT fk_asset_batches_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_asset_batches_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_asset_batches_managing_unit FOREIGN KEY (managing_unit_id) REFERENCES org_units (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_asset_batches_split_from FOREIGN KEY (split_from_batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_asset_batches_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT fk_asset_batches_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                             CONSTRAINT ck_asset_batches_quantity CHECK (quantity > 0),
                             CONSTRAINT ck_asset_batches_counters_non_negative CHECK (
                               available_quantity >= 0 AND maintenance_quantity >= 0
                                 AND recalled_quantity >= 0 AND disposed_quantity >= 0),
                              -- Số đang cấp phát không âm
                             CONSTRAINT ck_asset_batches_counters_total CHECK (
                               available_quantity + maintenance_quantity + recalled_quantity + disposed_quantity <= quantity),
                             CONSTRAINT ck_asset_batches_not_self_split CHECK (split_from_batch_id IS NULL OR split_from_batch_id <> id),
                             CONSTRAINT ck_asset_batches_attributes_object CHECK (JSON_TYPE(attributes_json) = 'OBJECT'),
                             CONSTRAINT ck_asset_batches_unit_cost CHECK (unit_cost >= 0),
                             CONSTRAINT ck_asset_batches_depreciation_months CHECK (depreciation_months > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Ảnh tài sản — cung loại trừ asset_id / batch_id
CREATE TABLE asset_images (
                            id             UUID         NOT NULL,
                            asset_id       UUID         NULL,
                            batch_id       UUID         NULL,
                            image_file_id  UUID         NOT NULL,
                            is_primary     BOOLEAN      NOT NULL DEFAULT FALSE,   -- "đúng 1 ảnh chính" giữ ở service
                            created_by     UUID         NOT NULL,
                            updated_by     UUID         NOT NULL,
                            created_at     DATETIME(6)  NOT NULL,
                            updated_at     DATETIME(6)  NOT NULL,
                            CONSTRAINT pk_asset_images PRIMARY KEY (id),
                            CONSTRAINT uk_asset_images_image_file UNIQUE (image_file_id),
                            -- RESTRICT: buộc xóa image_files trước khi xóa tài sản
                            CONSTRAINT fk_asset_images_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE RESTRICT,
                            CONSTRAINT fk_asset_images_batch FOREIGN KEY (batch_id) REFERENCES asset_batches (id) ON DELETE RESTRICT,
                            CONSTRAINT fk_asset_images_image_file FOREIGN KEY (image_file_id) REFERENCES image_files (id) ON DELETE CASCADE,
                            CONSTRAINT fk_asset_images_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                            CONSTRAINT fk_asset_images_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                            CONSTRAINT ck_asset_images_target CHECK ((asset_id IS NULL) <> (batch_id IS NULL))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
