// Kiểu ảnh nhận khi tải lên - máy chủ vẫn kiểm theo chữ ký đầu tệp
export enum ImageMimeType {
    JPEG = 'image/jpeg',
    PNG = 'image/png',
    WEBP = 'image/webp',
}

export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

// Ánh xạ sang class avatar-small / -medium / -large trong _media.scss
export enum AvatarSize {
    SMALL = 'small',
    MEDIUM = 'medium',
    LARGE = 'large',
}