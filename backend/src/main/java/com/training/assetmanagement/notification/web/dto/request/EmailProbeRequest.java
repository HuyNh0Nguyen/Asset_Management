package com.training.assetmanagement.notification.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "EmailProbeRequest", description = "Request for sending an email probe")
public record EmailProbeRequest(
    @NotBlank
    @Email
    String recipientEmail,

    @NotBlank
    String recipientName
) {
}
