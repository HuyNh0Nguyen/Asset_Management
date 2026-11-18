import { RankResponse } from "@api/models";

// rank vắng mặt = thêm mới
export interface RankFormDialogData {
    rank?: RankResponse;
}

// Tên trường trong fieldErrors của 400
export enum RankFormField {
    CODE = 'code',
    NAME = 'name',
    SENIORITY = 'seniority',
}