package com.training.assetmanagement.user.web;

import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.user.service.AvatarService;
import com.training.assetmanagement.user.service.MeService;
import com.training.assetmanagement.user.web.dto.response.AvatarResponse;
import com.training.assetmanagement.user.web.dto.response.MeResponse;
import com.training.assetmanagement.user.web.mapper.MeResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPath.ME)
@Tag(name = ApiTag.ME)
@RequiredArgsConstructor
public class MeController {

    private final MeService meService;
    private final AvatarService avatarService;

    @GetMapping
    @Operation(operationId = "getMe", summary = "Get the current authenticated user's information")
    public MeResponse getMe() {
        return MeResponseMapper.toResponse(meService.getMe());
    }

    // Người dùng chỉ tự đổi ảnh - thông tin khác do Admin sửa
    @PutMapping(value = ApiPath.AVATAR, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "changeMyAvatar", summary = "Change my avatar")
    public AvatarResponse changeMyAvatar(@RequestPart("file") MultipartFile file) {
        return new AvatarResponse(avatarService.changeMyAvatar(file));
    }

    @DeleteMapping(ApiPath.AVATAR)
    @Operation(operationId = "removeMyAvatar", summary = "Remove my avatar")
    public void removeMyAvatar() {
        avatarService.removeMyAvatar();
    }
}
