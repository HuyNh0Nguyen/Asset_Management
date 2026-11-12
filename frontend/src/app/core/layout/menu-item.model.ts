import { Permission } from "@api/models";

// Một mục menu; "permission" để trống = ai đăng nhập cũng thấy
export interface MenuItem {
    label: string;
    icon: string;
    url: string;
    permission?: Permission;
}