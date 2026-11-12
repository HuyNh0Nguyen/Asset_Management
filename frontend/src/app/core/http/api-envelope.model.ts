// Phong bì phản hồi của backend: { code, message, data }
export interface ApiEnvelope<T = unknown> {
    code: string;
    message: string;
    data: T;
}