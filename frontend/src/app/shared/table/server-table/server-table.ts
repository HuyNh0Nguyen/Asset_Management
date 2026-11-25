
// Khung tối thiểu: trang chiếu <table mat-table matSort> vào, khung lo phân trang + đang tải + rỗng.

import { ChangeDetectionStrategy, Component, input, output } from "@angular/core";
import { MatPaginator, MatPaginatorIntl, PageEvent } from "@angular/material/paginator";
import { MatProgressBar } from "@angular/material/progress-bar";
import { ViPaginatorIntl } from "./vi-paginator-intl";
import { PAGE_SIZE_OPTIONS } from "./page-query.model";

// TODO: Phase 3 mở rộng: cột tài chính, xuất file
@Component({
    selector: 'app-server-table',
    imports: [MatPaginator, MatProgressBar],
    templateUrl: './server-table.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [{ provide: MatPaginatorIntl, useClass: ViPaginatorIntl }],
})
export class ServerTable {

    readonly total = input.required<number>();
    readonly page = input.required<number>();
    readonly size = input.required<number>();
    readonly loading = input(false);
    readonly emptyText = input('Không có dữ liệu');
    readonly pageChange = output<PageEvent>();

    protected readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
}