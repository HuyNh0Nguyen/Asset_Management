// Khung xem trên màn hình và ảnh xuất ra (JPEG 512 x 512)
export enum CropperSize {
    VIEW = 320,
    OUTPUT = 512,
}

export enum CropperZoom {
    MIN = 1,
    MAX = 4,
    STEP = 0.01,
}

// Canvas không đọc được biến CSS
export enum CropperColor {
    BACKGROUND = '#ffffff',
    MASK = 'rgba(15, 23, 42, 0.45)',
}

export const CROPPER_OUTPUT_QUALITY = 0.9;

export interface CropperPoint {
    x: number;
    y: number;
}