import { UserResponse } from "@api/models";

export enum UserFormMode {
    CREATE = 'CREATE',
    EDIT = 'EDIT',
}

// EDIT: user = dòng đang sửa (từ danh sách)
export interface UserFormDialogData {
    mode: UserFormMode;
    user?: UserResponse;
}