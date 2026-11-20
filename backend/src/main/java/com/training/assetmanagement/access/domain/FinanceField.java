package com.training.assetmanagement.access.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// Trường [TC] - khóa camelCase dùng chung cho DTO, tham số sort, khóa Audit
@Getter
@RequiredArgsConstructor
public enum FinanceField {

    REFERENCE_UNIT_PRICE("referenceUnitPrice"),
    DEFAULT_DEPRECIATION_MONTHS("defaultDepreciationMonths"),
    ORIGINAL_COST("originalCost"),
    UNIT_COST("unitCost"),
    DEPRECIATION_START_DATE("depreciationStartDate"),
    DEPRECIATION_MONTHS("depreciationMonths"),
    INVOICE_NUMBER("invoiceNumber"),
    MONTHLY_DEPRECIATION("monthlyDepreciation"),
    ACCUMULATED_DEPRECIATION("accumulatedDepreciation"),
    RESIDUAL_VALUE("residualValue"),
    COST("cost"),
    REQUEST_VALUE("requestValue"),
    DISPOSAL_PROCEEDS("disposalProceeds");

    private final String key;
}
