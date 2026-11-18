import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal, ViewContainerRef } from "@angular/core";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatDialog } from "@angular/material/dialog";
import { MatFormField, MatLabel, MatPrefix, MatSuffix } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { MatMenu, MatMenuContent, MatMenuItem, MatMenuTrigger } from "@angular/material/menu";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatSlideToggle } from "@angular/material/slide-toggle";
import { OrgUnitAction, OrgUnitNodeResponse, OrgUnitStatus } from "@api/models";
import { ORG_UNIT_ACTION_LABELS, ORG_UNIT_STATUS_LABELS, ORG_UNIT_TYPE_LABELS } from "@core/i18n/labels.vi";
import { OrgUnitBlockersDialog } from "../../components/org-unit-blockers-dialog/org-unit-blockers-dialog";
import { OrgUnitBlockerPurpose, OrgUnitBlockersDialogData } from "../../components/org-unit-blockers-dialog/org-unit-blockers-dialog.model";
import { OrgUnitFormDialog } from "../../components/org-unit-form-dialog/org-unit-form-dialog";
import { OrgUnitFormDialogData, OrgUnitFormMode } from "../../components/org-unit-form-dialog/org-unit-form-dialog.model";
import { OrgUnitMoveDialog } from "../../components/org-unit-move-dialog/org-unit-move-dialog";
import { OrgUnitMoveDialogData } from "../../components/org-unit-move-dialog/org-unit-move-dialog.model";
import { OrgTreeRow } from "../../data/org-tree-row.model";
import { ORG_UNIT_ACTION_ICONS, ORG_UNIT_ACTION_ORDER, ORG_UNIT_TYPE_ICONS } from "../../data/org-unit-ui";
import { OrgUnitsStore } from "../../data/org-units.store";
import { OrgMessage } from "../../organization-messages";
import { ancestorIds, childCountById, matchesOrgUnit, normalizeSearchText } from "@shared/forms/org-unit-picker/org-tree.utils";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { appDialogConfig, DialogWidth } from "@shared/ui/dialog/app-dialog-config";
import { StatusChip } from "@shared/ui/status-chip/status-chip";
import { StatusTone } from "@shared/ui/status-chip/status-tone";
import { ToastService } from "@shared/ui/toast/toast.service";
import { filter, switchMap } from "rxjs";

// Thời gian hoạt ảnh thu gọn khớp với SCSS
const COLLAPSE_ANIMATION_MS = 180;

// Cơ cấu tổ chức: cây dựng từ danh sách phẳng tiền thứ tự + depth của server, mở hết mặc định.
// Menu thao tác chỉ dựng từ allowedActions - FE không tự xét gốc, trạng thái hay loại
@Component({
    selector: 'app-org-unit-tree-page',
    imports: [
        MatButton,
        MatIconButton,
        MatIcon,
        MatFormField,
        MatLabel,
        MatPrefix,
        MatSuffix,
        MatInput,
        MatMenu,
        MatMenuItem,
        MatMenuTrigger,
        MatMenuContent,
        MatSlideToggle,
        MatProgressSpinner,
        StatusChip,
    ],
    templateUrl: './org-unit-tree-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrgUnitTreePage {

    private readonly store = inject(OrgUnitsStore);
    private readonly dialog = inject(MatDialog);
    private readonly toast = inject(ToastService);
    private readonly viewContainerRef = inject(ViewContainerRef);

    protected readonly loading = this.store.loading;
    protected readonly loaded = this.store.loaded;
    protected readonly search = signal('');
    protected readonly hideInactive = signal(false);
    private readonly collapsed = signal<ReadonlySet<string>>(new Set());
    private readonly collapsingIds = signal<ReadonlySet<string>>(new Set());
    private readonly collapseTimers = new Map<string, ReturnType<typeof setTimeout>>();

    protected readonly typeLabels = ORG_UNIT_TYPE_LABELS;
    protected readonly typeIcons = ORG_UNIT_TYPE_ICONS;
    protected readonly statusLabels = ORG_UNIT_STATUS_LABELS;
    protected readonly inactiveStatus = OrgUnitStatus.INACTIVE;
    protected readonly mutedTone = StatusTone.MUTED;
    protected readonly noHead = OrgMessage.NO_HEAD;

    private readonly childCounts = computed(() => childCountById(this.store.nodes()));

    protected readonly rows = computed<OrgTreeRow[]>(() => {
        const visible = this.visibleNodes();
        const query = normalizeSearchText(this.search());
        return query ? this.searchRows(visible, query) : this.expandedRows(visible);
    });

    // Ẩn ngừng hoạt động: bỏ cả cây con (con của đơn vị INACTIVE cũng INACTIVE)
    private readonly visibleNodes = computed(() => {
        const nodes = this.store.nodes();
        if (!this.hideInactive()) {
            return nodes;
        }
        const hidden = new Set<string>();
        return nodes.filter((node) => {
            if (node.status === OrgUnitStatus.INACTIVE || (node.parentId && hidden.has(node.parentId))) {
                hidden.add(node.id);
                return false;
            }
            return true;
        });
    });

    constructor() {
        const destroyRef = inject(DestroyRef);
        destroyRef.onDestroy(() => this.clearCollapseTimers());
        this.store.load();
    }

    protected onSearch(event: Event): void {
        this.search.set((event.target as HTMLInputElement).value);
    }

    protected toggle(id: string): void {
        const isCollapsed = this.collapsed().has(id);

        if (isCollapsed) {
            // Mở rộng: huỷ timer thu gọn cũ nếu có và gỡ khỏi danh sách thu gọn
            const existingTimer = this.collapseTimers.get(id);
            if (existingTimer) {
                clearTimeout(existingTimer);
                this.collapseTimers.delete(id);
            }
            this.collapsed.update((current) => {
                const next = new Set(current);
                next.delete(id);
                return next;
            });
            this.collapsingIds.update((current) => {
                const next = new Set(current);
                next.delete(id);
                return next;
            });
        } else {
            // Thu gọn: tìm tất cả dòng con cháu đang hiển thị để chạy hiệu ứng biến mất trước khi ẩn
            const currentRows = this.rows();
            const index = currentRows.findIndex((r) => r.node.id === id);
            if (index === -1) {
                return;
            }

            const targetDepth = currentRows[index].node.depth;
            const descendantIds = new Set<string>();
            for (let i = index + 1; i < currentRows.length; i++) {
                const row = currentRows[i];
                if (row.node.depth <= targetDepth) {
                    break;
                }
                descendantIds.add(row.node.id);
            }

            this.collapsed.update((current) => new Set(current).add(id));

            if (descendantIds.size > 0) {
                this.collapsingIds.update((current) => new Set([...current, ...descendantIds]));

                const timer = setTimeout(() => {
                    this.collapsingIds.update((current) => {
                        const next = new Set(current);
                        descendantIds.forEach((dId) => next.delete(dId));
                        return next;
                    });
                    this.collapseTimers.delete(id);
                }, COLLAPSE_ANIMATION_MS);

                this.collapseTimers.set(id, timer);
            }
        }
    }

    protected expandAll(): void {
        this.clearCollapseTimers();
        this.collapsingIds.set(new Set());
        this.collapsed.set(new Set());
    }

    protected collapseAll(): void {
        this.clearCollapseTimers();
        const currentRows = this.rows();
        const childIds = new Set(
            currentRows.filter((r) => r.node.depth > 0).map((r) => r.node.id)
        );

        this.collapsed.set(new Set(this.childCounts().keys()));

        if (childIds.size === 0) {
            return;
        }

        this.collapsingIds.set(childIds);
        const timer = setTimeout(() => {
            this.collapsingIds.set(new Set());
            this.collapseTimers.clear();
        }, COLLAPSE_ANIMATION_MS);
        this.collapseTimers.set('__all__', timer);
    }

    private clearCollapseTimers(): void {
        for (const timer of this.collapseTimers.values()) {
            clearTimeout(timer);
        }
        this.collapseTimers.clear();
    }

    protected runAction(action: OrgUnitAction, node: OrgUnitNodeResponse): void {
        switch (action) {
            case OrgUnitAction.ADD_CHILD:
                this.openForm(OrgUnitFormMode.CREATE, node);
                return;
            case OrgUnitAction.EDIT:
                this.openForm(OrgUnitFormMode.EDIT, node);
                return;
            case OrgUnitAction.MOVE:
                this.openMove(node);
                return;
            case OrgUnitAction.DELETE:
                this.openBlockers(node, OrgUnitBlockerPurpose.DELETE);
                return;
            case OrgUnitAction.DEACTIVATE:
                this.openBlockers(node, OrgUnitBlockerPurpose.DEACTIVATE);
                return;
            case OrgUnitAction.REACTIVATE:
                this.reactivate(node);
                return;
        }
    }

    private openForm(mode: OrgUnitFormMode, target: OrgUnitNodeResponse): void {
        this.dialog
            .open<OrgUnitFormDialog, OrgUnitFormDialogData, boolean>(OrgUnitFormDialog,
                appDialogConfig({ mode, target }, DialogWidth.MEDIUM, this.viewContainerRef)
            )
            .afterClosed()
            .subscribe((changed) => this.reloadIf(changed));
    }

    private openMove(unit: OrgUnitNodeResponse): void {
        this.dialog
            .open<OrgUnitMoveDialog, OrgUnitMoveDialogData, boolean>(OrgUnitMoveDialog,
                appDialogConfig({ unit }, DialogWidth.WIDE, this.viewContainerRef)
            )
            .afterClosed()
            .subscribe((changed) => this.reloadIf(changed));
    }

    private openBlockers(unit: OrgUnitNodeResponse, purpose: OrgUnitBlockerPurpose): void {
        this.dialog
            .open<OrgUnitBlockersDialog, OrgUnitBlockersDialogData, boolean>(OrgUnitBlockersDialog,
                appDialogConfig({ unit, purpose }, DialogWidth.WIDE, this.viewContainerRef)
            )
            .afterClosed()
            .subscribe((changed) => this.reloadIf(changed));
    }

    // Lỗi (cha ngừng hoạt động, 409) để interceptor toast, rồi nạp lại cây
    private reactivate(unit: OrgUnitNodeResponse): void {
        confirmAction(this.dialog, {
            title: 'Kích hoạt lại đơn vị',
            message: `Kích hoạt lại "${unit.name}"? ${OrgMessage.REACTIVATE_NOTICE}`,
            confirmLabel: 'Kích hoạt lại',
        }, this.viewContainerRef)
            .pipe(
                filter(Boolean),
                switchMap(() => this.store.reactivate(unit.id)),
            )
            .subscribe({
                next: () => {
                    this.toast.success(OrgMessage.UNIT_REACTIVATED);
                    this.store.load();
                },
                error: () => this.store.load(),
            });
    }

    // Mọi lệnh ghi thành công -> nạp lại: cây và allowedActions luôn lấy từ server
    private reloadIf(changed: boolean | undefined): void {
        if (changed) {
            this.store.load();
        }
    }

    // Tìm kiếm: hiện nút khớp + tổ tiên làm ngữ cảnh, bỏ qua trạng thái thu gọn
    private searchRows(nodes: readonly OrgUnitNodeResponse[], query: string): OrgTreeRow[] {
        const byId = this.store.byId();
        const matched = new Set(nodes.filter((node) => matchesOrgUnit(node, query)).map((node) => node.id));
        const kept = new Set(matched);
        matched.forEach((id) => ancestorIds(byId, id).forEach((ancestorId) => kept.add(ancestorId)));
        return nodes
            .filter((node) => kept.has(node.id))
            .map((node) => this.toRow(node, true, matched.has(node.id), false));
    }

    // Duyệt tiền thứ tự: bỏ con cháu của nút đang thu gọn (giữ lại con cháu đang trong hiệu ứng thu gọn)
    private expandedRows(nodes: readonly OrgUnitNodeResponse[]): OrgTreeRow[] {
        const collapsed = this.collapsed();
        const collapsing = this.collapsingIds();
        const hidden = new Set<string>();
        const rows: OrgTreeRow[] = [];

        for (const node of nodes) {
            const isCollapsing = collapsing.has(node.id);
            const parentIsHidden = !!node.parentId && (hidden.has(node.parentId) || collapsed.has(node.parentId));

            if (parentIsHidden && !isCollapsing) {
                hidden.add(node.id);
                continue;
            }

            if (parentIsHidden && isCollapsing) {
                hidden.add(node.id);
            }

            rows.push(this.toRow(node, !collapsed.has(node.id), false, isCollapsing));
        }
        return rows;
    }

    private toRow(
        node: OrgUnitNodeResponse,
        expanded: boolean,
        match: boolean,
        collapsing = false
    ): OrgTreeRow {
        return {
            node,
            hasChildren: (this.childCounts().get(node.id) ?? 0) > 0,
            expanded,
            match,
            collapsing,
            actions: ORG_UNIT_ACTION_ORDER
                .filter((action) => node.allowedActions.includes(action))
                .map((action) => ({
                    action,
                    label: ORG_UNIT_ACTION_LABELS[action],
                    icon: ORG_UNIT_ACTION_ICONS[action],
                    danger: action === OrgUnitAction.DELETE,
                })),
        };
    }
}
