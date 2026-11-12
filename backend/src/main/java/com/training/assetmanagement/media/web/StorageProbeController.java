package com.training.assetmanagement.media.web;

import com.training.assetmanagement.media.service.StorageProbeService;
import com.training.assetmanagement.media.web.dto.response.StorageProbeResponse;
import com.training.assetmanagement.shared.config.AppProfile;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ApiPath.DEV_STORAGE_PROBES)
@Profile({AppProfile.LOCAL, AppProfile.DEV})
@Tag(name = ApiTag.DEV_TOOLS, description = "APIs for development and testing purposes")
@RequiredArgsConstructor
public class StorageProbeController {

    private final StorageProbeService storageProbeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "uploadStorageProbe", summary = "Upload a storage probe image and get a signed URL")
    public StorageProbeResponse upload(@RequestPart("file")MultipartFile file) {
        return StorageProbeResponse.from(storageProbeService.upload(file));
    }

    @GetMapping(ApiPath.PROBE_URL)
    @Operation(operationId = "refreshStorageProbeUrl", summary = "Refresh the signed URL for a storage probe image")
    public StorageProbeResponse refreshUrl(@PathVariable UUID probeId) {
        return StorageProbeResponse.from(storageProbeService.refreshUrl(probeId));
    }

}
