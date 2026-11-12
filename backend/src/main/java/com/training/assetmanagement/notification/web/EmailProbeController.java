package com.training.assetmanagement.notification.web;

import com.training.assetmanagement.notification.service.EmailProbeService;
import com.training.assetmanagement.notification.web.dto.request.EmailProbeRequest;
import com.training.assetmanagement.shared.config.AppProfile;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPath.DEV_EMAIL_PROBES)
@Profile({AppProfile.LOCAL, AppProfile.DEV})
@Tag(name = ApiTag.DEV_TOOLS, description = "APIs for development and testing purposes")
@RequiredArgsConstructor
public class EmailProbeController {

    private final EmailProbeService emailProbeService;

    // void -> VoidResponseHandler trả {"code":"SUCCESS", ..., "data": null}
    @PostMapping
    @Operation(operationId = "sendEmailProbe", summary = "Send an email probe to the specified recipient")
    public void send(@Valid @RequestBody EmailProbeRequest request) {
        emailProbeService.sendProbe(request.recipientEmail(), request.recipientName());
    }
}
