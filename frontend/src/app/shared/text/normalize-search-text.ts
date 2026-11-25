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