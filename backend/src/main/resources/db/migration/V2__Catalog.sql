-- =====================================================================
-- V2 — Danh mục
-- asset_types, attribute_definitions, suppliers
-- =====================================================================

-- Loại tài sản
CREATE TABLE asset_types (
                           id                           UUID         NOT NULL,
                           code                         VARCHAR(20)  NOT NULL,   -- tiền tố mã tài sản / mã lô, bất biến
                           name                         VARCHAR(255) NOT NULL,
                           description                  TEXT         NULL,
                           tracking_mode                VARCHAR(40)  NOT NULL,   -- khóa khi đã có tài sản / lô — kiểm ở service
                           reference_unit_price         BIGINT       NULL,       -- NULL = chưa mở đề xuất
                           default_depreciation_months  INT          NULL,
                           status                       VARCHAR(40)  NOT NULL,
                           created_by                   UUID         NOT NULL,
                           updated_by                   UUID         NOT NULL,
                           created_at                   DATETIME(6)  NOT NULL,
                           updated_at                   DATETIME(6)  NOT NULL,
                           CONSTRAINT pk_asset_types PRIMARY KEY (id),
                           CONSTRAINT uk_asset_types_code UNIQUE (code),
                           CONSTRAINT fk_asset_types_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                           CONSTRAINT fk_asset_types_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                            -- Chỉ chữ hoa + số; BINARY để REGEXP phân biệt hoa / thường
                           CONSTRAINT ck_asset_types_code_format CHECK (CAST(code AS BINARY) REGEXP '^[A-Z0-9]+$'),
                           CONSTRAINT ck_asset_types_tracking_mode CHECK (tracking_mode IN ('SERIAL', 'QUANTITY')),
                           CONSTRAINT ck_asset_types_reference_unit_price CHECK (reference_unit_price > 0),
                           CONSTRAINT ck_asset_types_default_depreciation_months CHECK (default_depreciation_months > 0),
                           CONSTRAINT ck_asset_types_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Định nghĩa thuộc tính động — không xóa, chỉ INACTIVE
CREATE TABLE attribute_definitions (
                                     id                   UUID         NOT NULL,
                                     asset_type_id        UUID         NOT NULL,
                                     code                 VARCHAR(50)  NOT NULL,   -- khóa trong attributes_json, bất biến
                                     label                VARCHAR(255) NOT NULL,
                                     data_type            VARCHAR(40)  NOT NULL,
                                     is_required          BOOLEAN      NOT NULL DEFAULT FALSE,
                                     measure_unit         VARCHAR(50)  NULL,
                                     options_json         JSON         NULL,       -- [{code, label}]
                                     is_deadline_tracked  BOOLEAN      NOT NULL DEFAULT FALSE,
                                     display_order        INT          NOT NULL DEFAULT 0,
                                     status               VARCHAR(40)  NOT NULL,
                                     created_by           UUID         NOT NULL,
                                     updated_by           UUID         NOT NULL,
                                     created_at           DATETIME(6)  NOT NULL,
                                     updated_at           DATETIME(6)  NOT NULL,
                                     CONSTRAINT pk_attribute_definitions PRIMARY KEY (id),
                                     CONSTRAINT uk_attribute_definitions_type_code UNIQUE (asset_type_id, code),
                                     CONSTRAINT fk_attribute_definitions_asset_type FOREIGN KEY (asset_type_id) REFERENCES asset_types (id) ON DELETE CASCADE,
                                     CONSTRAINT fk_attribute_definitions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                                     CONSTRAINT fk_attribute_definitions_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                                      -- snake_case
                                     CONSTRAINT ck_attribute_definitions_code_format CHECK (CAST(code AS BINARY) REGEXP '^[a-z][a-z0-9_]*$'),
                                     CONSTRAINT ck_attribute_definitions_data_type CHECK (data_type IN ('TEXT', 'NUMBER', 'DATE', 'BOOLEAN', 'SELECT')),
                                      -- Đơn vị đo chỉ cho NUMBER
                                     CONSTRAINT ck_attribute_definitions_measure_unit CHECK (measure_unit IS NULL OR data_type = 'NUMBER'),
                                      -- Có lựa chọn khi và chỉ khi SELECT
                                     CONSTRAINT ck_attribute_definitions_options CHECK ((options_json IS NOT NULL) = (data_type = 'SELECT')),
                                     CONSTRAINT ck_attribute_definitions_options_array CHECK (options_json IS NULL OR JSON_TYPE(options_json) = 'ARRAY'),
                                      -- Theo dõi hạn chỉ cho DATE
                                     CONSTRAINT ck_attribute_definitions_deadline CHECK (is_deadline_tracked = FALSE OR data_type = 'DATE'),
                                     CONSTRAINT ck_attribute_definitions_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Nhà cung cấp
CREATE TABLE suppliers (
                         id            UUID         NOT NULL,
                         code          VARCHAR(50)  NOT NULL,
                         name          VARCHAR(255) NOT NULL,
                         tax_code      VARCHAR(20)  NULL,       -- không duy nhất
                         contact_name  VARCHAR(255) NULL,
                         phone_number  VARCHAR(20)  NULL,
                         email         VARCHAR(255) NULL,
                         address       TEXT         NULL,
                         status        VARCHAR(40)  NOT NULL,
                         created_by    UUID         NOT NULL,
                         updated_by    UUID         NOT NULL,
                         created_at    DATETIME(6)  NOT NULL,
                         updated_at    DATETIME(6)  NOT NULL,
                         CONSTRAINT pk_suppliers PRIMARY KEY (id),
                         CONSTRAINT uk_suppliers_code UNIQUE (code),
                         CONSTRAINT fk_suppliers_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
                         CONSTRAINT fk_suppliers_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE RESTRICT,
                         CONSTRAINT ck_suppliers_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
