import { AppUrl } from "@core/routing/route.path";
import { MenuItem } from "./menu-item.model";
import { Permission } from "@api/models";

// SessionStore.visibleMenuItems lọc theo mã quyền từ /api/me - không lọc theo tên vai trò
export const MENU_ITEMS: MenuItem[] = [
    { label: 'Cơ cấu tổ chức', icon: 'account_tree', url: AppUrl.ORG_UNITS, permission: Permission.ORG_MANAGE },
    { label: 'Cấp bậc', icon: 'military_tech', url: AppUrl.RANKS, permission: Permission.ORG_MANAGE },
    { label: 'Thử ảnh & email', icon: 'science', url: AppUrl.PROBES },
    { label: 'Thử form thuộc tính', icon: 'dynamic_form', url: AppUrl.ATTRIBUTE_FORM },
];