import { ChangeDetectionStrategy, Component, computed, input, linkedSignal } from "@angular/core";
import { AvatarSize } from "../avatar-size";

// Ảnh (URL ký bản thu nhỏ) hoặc chữ cái đầu của tên gọi
@Component({
    selector: 'app-avatar',
    template: `
      <span [class]="avatarClass()">
        @if (url() && !failed()) {
          <img class="avatar-image" [src]="url()" alt="" (error)="failed.set(true)" />
        } @else {
          <span aria-hidden="true">{{ initials() }}</span>
        }
      </span>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Avatar {

    readonly name = input('');
    readonly url = input<string | null | undefined>(null);
    readonly size = input<AvatarSize>(AvatarSize.SMALL);

    // URL ký hết hạn -> ảnh lỗi -> chữ cái; đổi URL thì thử lại
    protected readonly failed = linkedSignal({ source: this.url, computation: () => false });
    protected readonly avatarClass = computed(() => `avatar avatar-${this.size()}`);

    // Tên gọi tiếng Việt là từ cuối: "Nguyễn Văn An" -> "A"
    protected readonly initials = computed(() => {
        const words = this.name().trim().split(/\s+/).filter(Boolean);
        return words.length > 0 ? words[words.length - 1].charAt(0).toUpperCase() : '?';
    })
}