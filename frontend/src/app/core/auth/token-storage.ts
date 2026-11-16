import { StorageKey } from "./auth-header";

// Mọi lần chạm storage bọc try/catch: trình duyệt có thể chặn (chế độ riêng tư, chính sách công ty)
export function readStoredToken(): string | null {
    try {
        return localStorage.getItem(StorageKey.ACCESS_TOKEN);
    } catch {
        return null;
    }
}

export function writeStoredToken(token: string): void {
    try {
        localStorage.setItem(StorageKey.ACCESS_TOKEN, token);
    } catch {
        // Bị chặn: phiên chỉ sống trong bộ nhớ của tab này
    }
}

export function removedStoredToken(): void {
    try {
        localStorage.removeItem(StorageKey.ACCESS_TOKEN);
    } catch {
        // Bị chặn: không có gì để xóa
    }
}