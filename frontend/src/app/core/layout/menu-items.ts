import { AppUrl } from "@core/routing/route.path";
import { MenuItem } from "./menu-item.model";

// TODO (Phase 2): lọc theo mã quyền từ /api/me - không lọc theo tên vai trò
export const MENU_ITEMS: MenuItem[] = [
    { label: 'Thử ảnh & email', icon: 'science', url: AppUrl.PROBES },
    { label: 'Thử form thuộc tính', icon: 'dynamic_form', url: AppUrl.ATTRIBUTE_FORM },
];