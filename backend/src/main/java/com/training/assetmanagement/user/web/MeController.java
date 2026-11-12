package com.training.assetmanagement.user.web;

import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.web.dto.response.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPath.ME)
@Tag(name = ApiTag.ME)
public class MeController {

    @GetMapping
    @Operation(operationId = "getMe", summary = "Get the current authenticated user's information")
    public MeResponse getMe() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
