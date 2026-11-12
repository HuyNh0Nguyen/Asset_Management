package com.training.assetmanagement.auth.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.training.assetmanagement.auth.domain.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Đổi mật khẩu: Chỉ mật khẩu mới + xác nhận, không nhập mật khẩu hiện tại
@Schema(name = "ChangePasswordRequest", description = "Request for changing user password")
public record ChangePasswordRequest(
        @NotBlank(message = "Please provide a new password")
        @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
                message = "Password must be between {min} and {max} characters long")
        @Pattern(regexp = PasswordPolicy.PATTERN,
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
        String newPassword,

        @NotBlank(message = "Please confirm your new password")
        String confirmPassword
) {

    // Kiểm mật khẩu xác nhận; lỗi xuất hiện trong fieldErrors với field = "passwordConfirmed"
    @AssertTrue(message = "Password confirmation does not match the new password")
    @JsonIgnore             // Không coi là trường JSON
    @Schema(hidden = true)  // Không đưa vào openapi.json -> DTO sinh cho FE không có trường thừa
    public boolean isPasswordConfirmed() {
        // Trường trống để @NotBlank báo, tránh hai lỗi cho một trường
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}
