import { ApiEnvelope } from "./api-envelope.model";

const ENVELOPE_KEYS = ['code', 'data', 'message'];

// Nhận diện phong bì bằng đúng ba khóa - DTO nghiệp vụ cũng có trường "code"
export function isApiEnvelope(body: unknown): body is ApiEnvelope {
    if (body === null || typeof body !== 'object' || Array.isArray(body)) {
        return false;
    }
    const keys = Object.keys(body).sort();
    return keys.length === ENVELOPE_KEYS.length && keys.every((key, index) => key === ENVELOPE_KEYS[index]);
}