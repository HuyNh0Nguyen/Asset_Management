import { afterRenderEffect, ChangeDetectionStrategy, Component, computed, ElementRef, input, linkedSignal, viewChild } from "@angular/core";
import { MatIcon } from "@angular/material/icon";
import { MatSlider, MatSliderThumb } from "@angular/material/slider";
import { CROPPER_OUTPUT_QUALITY, CropperColor, CropperPoint, CropperSize, CropperZoom } from "./image-cropper.model";
import { ImageMimeType } from "../avatar-size";

// Bộ cắt vuông tự viết: kéo để di chuyển, thanh trượt để thu phóng
@Component({
    selector: 'app-image-cropper',
    imports: [MatSlider, MatSliderThumb, MatIcon],
    templateUrl: './image-cropper.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageCropper {

    readonly source = input.required<ImageBitmap>();

    protected readonly viewSize = CropperSize.VIEW;
    protected readonly zoomMin = CropperZoom.MIN;
    protected readonly zoomMax = CropperZoom.MAX;
    protected readonly zoomStep = CropperZoom.STEP;

    private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');

    // Tỉ lệ để cạnh ngắn vừa khít khung - mức thu nhỏ nhất
    private readonly coverScale = computed(() => {
        const bitmap = this.source();
        return Math.max(CropperSize.VIEW / bitmap.width, CropperSize.VIEW / bitmap.height);
    });
    // Ảnh mới -> zoom về 1, căn giữa
    protected readonly zoom = linkedSignal<ImageBitmap, number>({
        source: this.source,
        computation: () => CropperZoom.MIN,
    });
    private readonly offset = linkedSignal<ImageBitmap, CropperPoint>({
        source: this.source,
        computation: (bitmap) => this.centered(bitmap, this.coverScale()),
    });
    private readonly scale = computed(() => this.coverScale() * this.zoom());

    private drag: { pointerId: number; start: CropperPoint; origin: CropperPoint } | null = null;

    constructor() {
        // Vẽ lại sau render mỗi khi ảnh / tỉ lệ / vị trí đổi
        afterRenderEffect(() => this.paint(this.canvas().nativeElement, this.source(), this.scale(), this.offset(), true));
    }

    // Xuất JPEG vuông; nền trắng cho ảnh PNG trong suốt
    exportBlob(): Promise<Blob> {
        const output = document.createElement('canvas');
        output.width = CropperSize.OUTPUT;
        output.height = CropperSize.OUTPUT;
        const ratio = CropperSize.OUTPUT / CropperSize.VIEW;
        const offset = this.offset();
        this.paint(output, this.source(), this.scale() * ratio, { x: offset.x * ratio, y: offset.y * ratio }, false);
        return new Promise((resolve, reject) => output.toBlob(
            (blob) => (blob ? resolve(blob) : reject(new Error('Cannot export cropped image'))),
            ImageMimeType.JPEG,
            CROPPER_OUTPUT_QUALITY,
        ));
    }

    // Giữ nguyên điểm ở tâm khung khi thu phóng
    protected onZoomInput(event: Event): void {
        const nextZoom = (event.target as HTMLInputElement).valueAsNumber;
        const before = this.scale();
        const after = this.coverScale() * nextZoom;
        const center = CropperSize.VIEW / 2;
        const offset = this.offset();
        this.zoom.set(nextZoom);
        this.offset.set(this.clamp({
            x: center - (center - offset.x) * (after / before),
            y: center - (center - offset.y) * (after / before),
        }, after));
    }

    protected onPointerDown(event: PointerEvent): void {
        this.canvas().nativeElement.setPointerCapture(event.pointerId);
        this.drag = { pointerId: event.pointerId, start: { x: event.clientX, y: event.clientY }, origin: this.offset() };
    }

    protected onPointerMove(event: PointerEvent): void {
        if (!this.drag || this.drag.pointerId !== event.pointerId) {
            return;
        }
        // Đổi px CSS sang px của canvas (canvas co giãn theo bề rộng màn hình)
        const ratio = CropperSize.VIEW / this.canvas().nativeElement.clientWidth;
        this.offset.set(this.clamp({
            x: this.drag.origin.x + (event.clientX - this.drag.start.x) * ratio,
            y: this.drag.origin.y + (event.clientY - this.drag.start.y) * ratio,
        }, this.scale()));
    }

    protected onPointerUp(event: PointerEvent): void {
        if (this.drag?.pointerId === event.pointerId) {
            this.drag = null;
        }
    }

    // withMask: phủ mờ ngoài vòng tròn - vùng ảnh đại diện thật sự hiển thị
    private paint(canvas: HTMLCanvasElement, bitmap: ImageBitmap, scale: number, offset: CropperPoint,
                    withMask: boolean): void {
        const context = canvas.getContext('2d');
        if (!context) {
            return;
        }
        const size = canvas.width;
        context.fillStyle = CropperColor.BACKGROUND;
        context.fillRect(0, 0, size, size);
        context.imageSmoothingQuality = 'high';
        context.drawImage(bitmap, offset.x, offset.y, bitmap.width * scale, bitmap.height * scale);
        if (withMask) {
            context.fillStyle = CropperColor.MASK;
            context.beginPath();
            context.rect(0, 0, size, size);
            context.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2);
            context.fill('evenodd');
        }
    }

    // Ảnh luôn phủ kín khung
    private clamp(point: CropperPoint, scale: number): CropperPoint {
        const bitmap = this.source();
        const minX = CropperSize.VIEW - bitmap.width * scale;
        const minY = CropperSize.VIEW - bitmap.height * scale;
        return { x: Math.min(0, Math.max(minX, point.x)), y: Math.min(0, Math.max(minY, point.y)) };
    }

    private centered(bitmap: ImageBitmap, scale: number): CropperPoint {
        return {
            x: (CropperSize.VIEW - bitmap.width * scale) / 2,
            y: (CropperSize.VIEW - bitmap.height * scale) / 2,
        };
    }
}