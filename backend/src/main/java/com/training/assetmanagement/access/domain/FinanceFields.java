package com.training.assetmanagement.access.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.training.assetmanagement.access.domain.FinanceField.*;

// Hằng số [TC] duy nhất. Ba chỗ dùng gọi đúng các hàm này:
// Ánh xạ dữ liệu trả về, chặn yêu cầu ghi, che Audit lúc đọc
public final class FinanceFields {

    private record Definition(FinanceAnchor anchor, Set<FinanceField> stored, Set<FinanceField> derived) {}

    private static final Set<FinanceField> NONE = EnumSet.noneOf(FinanceField.class);
    private static final Set<FinanceField> DEPRECIATION_DERIVED =
        EnumSet.of(MONTHLY_DEPRECIATION, ACCUMULATED_DEPRECIATION, RESIDUAL_VALUE);

    private static final Map<FinanceTarget, Definition> DEFINITIONS = Map.of(
        FinanceTarget.ASSET_TYPE, new Definition(FinanceAnchor.ANY_SCOPE,
            EnumSet.of(REFERENCE_UNIT_PRICE, DEFAULT_DEPRECIATION_MONTHS), NONE),
        FinanceTarget.ASSET, new Definition(FinanceAnchor.MANAGING_UNIT,
            EnumSet.of(ORIGINAL_COST, DEPRECIATION_START_DATE, DEPRECIATION_MONTHS, INVOICE_NUMBER), DEPRECIATION_DERIVED),
        FinanceTarget.ASSET_BATCH, new Definition(FinanceAnchor.MANAGING_UNIT,
            EnumSet.of(UNIT_COST, DEPRECIATION_START_DATE, DEPRECIATION_MONTHS, INVOICE_NUMBER), DEPRECIATION_DERIVED),
        FinanceTarget.MAINTENANCE_TICKET, new Definition(FinanceAnchor.MANAGING_UNIT, EnumSet.of(COST), NONE),
        FinanceTarget.REQUEST, new Definition(FinanceAnchor.REQUESTER_UNIT,
            EnumSet.of(REQUEST_VALUE, DISPOSAL_PROCEEDS), NONE),
        FinanceTarget.DISPOSAL_LINE, new Definition(FinanceAnchor.MANAGING_UNIT, EnumSet.of(RESIDUAL_VALUE), NONE)
    );

    private static final Map<FinanceTarget, Set<String>> STORED_KEYS = new EnumMap<>(FinanceTarget.class);
    private static final Map<FinanceTarget, Set<String>> ALL_KEYS = new EnumMap<>(FinanceTarget.class);

    static {
        DEFINITIONS.forEach((target, definition) -> {
            STORED_KEYS.put(target, keys(definition.stored().stream()));
            ALL_KEYS.put(target, keys(Stream.concat(definition.stored().stream(), definition.derived().stream())));
        });
    }

    // Trường lưu + trường suy ra
    public static Set<String> of(FinanceTarget target) {
        return ALL_KEYS.get(target);
    }

    // Chỉ trường lưu - test schema đối chiếu với information_schema.columns
    public static Set<String> storedOf(FinanceTarget target) {
        return STORED_KEYS.get(target);
    }

    public static boolean isFinance(FinanceTarget target, String field) {
        return field != null && ALL_KEYS.get(target).contains(field);
    }

    public static FinanceAnchor anchorOf(FinanceTarget target) {
        return DEFINITIONS.get(target).anchor();
    }

    private static Set<String> keys(Stream<FinanceField> fields) {
        return fields.map(FinanceField::getKey).collect(Collectors.toUnmodifiableSet());
    }

    private FinanceFields() {}
}
