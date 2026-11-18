import { HttpErrorResponse } from "@angular/common/http";
import { ParsedHttpError } from "./parsed-http-error";
import { parseErrorBody } from "./error-body";
import { ErrorCode } from "@api/models";

// Đọc mã lỗi + fieldErrors một lần để hộp thoại rẽ nhánh theo mã
export function parseHttpError(error: unknown): ParsedHttpError {
    if (!(error instanceof HttpErrorResponse)) {
        return { status: -1, code: null, fieldErrors: [] };
    }
    const body = parseErrorBody(error.error);
    const fieldErrors = body?.code === ErrorCode.VALIDATION_FAILED ? body.data?.fieldErrors ?? [] : [];
    return { status: error.status, code: body?.code ?? null, fieldErrors };
}