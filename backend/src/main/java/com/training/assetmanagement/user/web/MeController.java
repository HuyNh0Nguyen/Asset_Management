package com.training.assetmanagement.user.web;

import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.user.service.MeService;
import com.training.assetmanagement.user.web.dto.response.MeResponse;
import com.training.assetmanagement.user.web.mapper.MeResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPath.ME)
@Tag(name = ApiTag.ME)
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;

    @GetMapping
    @Operation(operationId = "getMe", summary = "Get the current authenticated user's information")
    public MeResponse getMe() {
        return MeResponseMapper.toResponse(meService.getMe());
    }
}
