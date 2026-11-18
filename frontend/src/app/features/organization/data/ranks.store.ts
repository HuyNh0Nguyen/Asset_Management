import { inject, Injectable, signal } from "@angular/core";
import { CreateRankRequest, RankResponse, UpdateRankRequest } from "@api/models";
import { OrganizationService } from "@api/services";
import { skipErrorToast } from "@core/http/http-context";
import { finalize, Observable } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt để không log trùng
const ignoreError = (): void => undefined;

@Injectable()
export class RanksStore {

    private readonly api = inject(OrganizationService);

    private readonly ranksState = signal<RankResponse[]>([]);
    private readonly loadingState = signal(false);
    private readonly loadedState = signal(false);

    readonly ranks = this.ranksState.asReadonly();
    readonly loading = this.loadingState.asReadonly();
    readonly loaded = this.loadedState.asReadonly();

    // Server đã sắp theo seniority
    load(): void {
        this.loadingState.set(true);
        this.api.listRanks()
            .pipe(finalize(() => this.loadingState.set(false)))
            .subscribe({
                next: (ranks) => {
                    this.ranksState.set(ranks);
                    this.loadedState.set(true);
                },
                error: ignoreError,
            });
    }

    create(body: CreateRankRequest): Observable<RankResponse> {
        return this.api.createRank({ body }, skipErrorToast());
    }

    update(id: string, body: UpdateRankRequest): Observable<RankResponse> {
        return this.api.updateRank({ id, body }, skipErrorToast());
    }

    // 422 RANK_IN_USE để interceptor toast
    delete(id: string): Observable<void> {
        return this.api.deleteRank({ id });
    }
}