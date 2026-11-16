import { HttpErrorResponse } from "@angular/common/http";
import { resolveErrorMessage, UNKNOWN_ERROR_MESSAGE_VI } from "@core/i18n/error-messages.vi";
import { parseErrorBody } from "./error-body";

// Câu tiếng Việt của một lỗi HTTP - dùng khi màn hình tự hiển thị lỗi (skipErrorToast)
export function describeHttpError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
        return resolveErrorMessage(error.status, parseErrorBody(error.error)?.code);
    }
    return UNKNOWN_ERROR_MESSAGE_VI;
}