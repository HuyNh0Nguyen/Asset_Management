import { ApiErrorResponse, ErrorCode } from '@api/models';

const KNOWN_CODES = new Set<string>(Object.values(ErrorCode));

// Đọc phong bì lỗi; thân có thể là chuỗi khi request dùng responseType 'text'
export function parseErrorBody(raw: unknown): ApiErrorResponse | null {
    let body = raw;
    if (typeof raw === 'string') {
        try {
            body = JSON.parse(raw);
        } catch {
            return null;
        }
    }
    if (body !== null && typeof body === 'object' && 'code' in body && KNOWN_CODES.has(String(body.code))) {
        return body as ApiErrorResponse;
    }
    return null;
}