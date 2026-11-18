
// Chỉ cấu trúc cây trên danh sách phẳng tiền thứ tự từ /api/org-units/tree - không chứa quy tắc nghiệp vụ

import { OrgUnitNodeResponse } from "@api/models";

// Số đơn vị con trực tiếp của mỗi nút
export function childCountById(nodes: readonly OrgUnitNodeResponse[]): Map<string, number> {
    const counts = new Map<string, number>();
    for (const node of nodes) {
        if (node.parentId) {
            counts.set(node.parentId, (counts.get(node.parentId) ?? 0) + 1);
        }
    }
    return counts;
}

// Con cháu của một nút, không gồm chính nó
export function descendantIds(nodes: readonly OrgUnitNodeResponse[], id: string): Set<string> {
    const result = new Set<string>();
    // Tiền thứ tự: cha luôn đứng trước con nên một lượt duyệt là đủ
    for (const node of nodes) {
        if (node.parentId && (node.parentId === id || result.has(node.parentId))) {
            result.add(node.id);
        }
    }
    return result;
}

// Tổ tiên của một nút, từ cha lên gốc
export function ancestorIds(byId: ReadonlyMap<string, OrgUnitNodeResponse>, id: string): string[] {
    const result: string[] = [];
    let parentId = byId.get(id)?.parentId ?? null;
    while (parentId && !result.includes(parentId)) {
        result.push(parentId);
        parentId = byId.get(parentId)?.parentId ?? null;
    }
    return result;
}

// Bỏ dấu tiếng Việt, viết thường - tìm "phong ky thuat" vẫn khớp "Phòng Kỹ thuật"
export function normalizeSearchText(text: string): string {
    return text
        .normalize('NFD')
        .replace(/[̀-ͯ]/g, '')
        .replace(/đ/g, 'd')
        .replace(/Đ/g, 'D')
        .toLowerCase()
        .trim();
}

// normalizedQuery đã qua normalizeSearchText
export function matchesOrgUnit(node: OrgUnitNodeResponse, normalizedQuery: string): boolean {
    return normalizeSearchText(node.name).includes(normalizedQuery)
        || node.code.toLowerCase().includes(normalizedQuery);
}