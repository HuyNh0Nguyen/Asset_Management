// Header gửi token - chỉ gắn cho request tới backend
export enum AuthHeader {
    NAME = 'Authorization',
    BEARER_PREFIX = 'Bearer ',
}

// Tiền tố request tới backend; URL ký ảnh MinIO không được mang Authorization
export enum ApiPrefix {
    ROOT = '/api/',
}

// Khóa localStorage - localStorage để mở link email ở tab mới vẫn còn phiên
export enum StorageKey {
    ACCESS_TOKEN = 'asset.accessToken',
}