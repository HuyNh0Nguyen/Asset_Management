import { Injectable } from "@angular/core";
import { MatPaginatorIntl } from "@angular/material/paginator";

// Nhãn tiếng Việt cho MatPaginator
@Injectable()
export class ViPaginatorIntl extends MatPaginatorIntl {

    override itemsPerPageLabel = 'Số dòng mỗi trang';
    override nextPageLabel = 'Trang sau';
    override previousPageLabel = 'Trang trước';
    override firstPageLabel = 'Trang đầu';
    override lastPageLabel = 'Trang cuối';

    override getRangeLabel = (page: number, pageSize: number, length: number): string => {
        if (length === 0) {
            return '0 / 0';
        }
        const start = page * pageSize + 1;
        const end = Math.min(length, (page + 1) * pageSize);
        return `${start} - ${end} / ${length}`;
    }
}