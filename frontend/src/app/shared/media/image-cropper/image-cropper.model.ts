// Khung xem trên màn hình và ảnh xuất ra (JPEG 512 x 512)
export enum CropperSize {
    VIEW = 320,         // Cạnh khung hiển thị (px CSS) - khớp max-width ở _media.scss
    OUTPUT = 512,       // Cạnh ảnh JPEG xuất ra
    KEY_STEP = 8,       // Bước di chuyển bằng phím mũi tên
}

export enum CropperZoom {
    MIN = 1,            // Cạnh ngắn vừa khít khung
    MAX = 4,
    STEP = 0.01,
    WHEEL_STEP = 0.1,   // Bước khi cuộn chuột / phím + -
}

// Canvas không đọc được biến CSS
export enum CropperColor {
    BACKGROUND = '#ffffff',             // Nền cho PNG trong suốt
    MASK = 'rgba(15, 23, 42, 0.45)',    // Phủ mờ ngoài vòng tròn
    RING = 'rgba(255, 255, 255, 0.9)',  // Viền vòng tròn
}

export const CROPPER_OUTPUT_QUALITY = 0.9;
export const CROPPER_RING_WIDTH = 2;

export interface CropperPoint {
    x: number;
    y: number;
}