import { computed, inject, Injectable, signal } from "@angular/core";
import { CreateOrgUnitRequest, MoveCheckResponse, OrgUnitBlockersResponse, OrgUnitHeadResponse, OrgUnitNodeResponse, UpdateOrgUnitRequest } from "@api/models";
import { OrganizationService } from "@api/services";
import { skipErrorToast } from "@core/http/http-context";
import { finalize, Observable } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt để không log trùng
const ignoreError = (): void => undefined;

// Cây và allowedActions luôn lấy từ server; lệnh ghi trả Observable để hộp thoại tự xử lý lỗi
@Injectable()
export class OrgUnitsStore {

    private readonly api = inject(OrganizationService);

    private readonly nodesState = signal<OrgUnitNodeResponse[]>([]);
    private readonly loadingState = signal(false);
    private readonly loadedState = signal(false);

    readonly nodes = this.nodesState.asReadonly();
    readonly loading = this.loadingState.asReadonly();
    readonly loaded = this.loadedState.asReadonly();
    readonly byId = computed(() => new Map(this.nodesState().map((node) => [node.id, node])));

    load(): void {
        this.loadingState.set(true);
        this.api.getOrgUnitTree()
            .pipe(finalize(() => this.loadingState.set(false)))
            .subscribe({
                next: (nodes) => {
                    this.nodesState.set(nodes);
                    this.loadedState.set(true);
                },
                error: ignoreError,
            });
    }

    create(body: CreateOrgUnitRequest): Observable<OrgUnitNodeResponse> {
        return this.api.createOrgUnit({ body }, skipErrorToast());
    }

    update(id: string, body: UpdateOrgUnitRequest): Observable<OrgUnitNodeResponse> {
        return this.api.updateOrgUnit({ id, body }, skipErrorToast());
    }

    // Lỗi để interceptor toast
    headCandidates(id: string): Observable<OrgUnitHeadResponse[]> {
        return this.api.listOrgUnitHeadCandidates({ id });
    }

    checkMove(id: string, parentId: string): Observable<MoveCheckResponse> {
        return this.api.checkOrgUnitMove({ id, parentId });
    }

    move(id: string, parentId: string): Observable<void> {
        return this.api.moveOrgUnit({ id, body: { parentId } }, skipErrorToast());
    }

    checkDeletion(id: string): Observable<OrgUnitBlockersResponse> {
        return this.api.checkOrgUnitDeletion({ id });
    }

    delete(id: string): Observable<void> {
        return this.api.deleteOrgUnit({ id }, skipErrorToast());
    }

    checkDeactivation(id: string): Observable<OrgUnitBlockersResponse> {
        return this.api.checkOrgUnitDeactivation({ id });
    }

    deactivate(id: string): Observable<void> {
        return this.api.deactivateOrgUnit({ id }, skipErrorToast());
    }

    // 422 ORG_UNIT_PARENT_INACTIVE / 409 để interceptor toast
    reactivate(id: string): Observable<void> {
        return this.api.reactivateOrgUnit({ id });
    }
}