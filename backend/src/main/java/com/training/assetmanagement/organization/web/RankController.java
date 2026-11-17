package com.training.assetmanagement.organization.web;

import com.training.assetmanagement.organization.service.RankService;
import com.training.assetmanagement.organization.web.dto.request.CreateRankRequest;
import com.training.assetmanagement.organization.web.dto.request.UpdateRankRequest;
import com.training.assetmanagement.organization.web.dto.response.RankResponse;
import com.training.assetmanagement.organization.web.mapper.OrganizationResponseMapper;
import com.training.assetmanagement.shared.web.ApiAuthority;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Cấp bậc vài dòng, bộ chọn cần đủ - trả trọn danh sách, không phân trang
@RestController
@RequestMapping(ApiPath.RANKS)
@Tag(name = ApiTag.ORGANIZATION)
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    // Mọi người đã đăng nhập
    @GetMapping
    @Operation(operationId = "listRanks", summary = "All ranks ordered by seniority")
    public List<RankResponse> list() {
        return rankService.list().stream().map(OrganizationResponseMapper::toRank).toList();
    }

    @PostMapping
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "createRank", summary = "Create a rank")
    public RankResponse create(@Valid @RequestBody CreateRankRequest request) {
        return OrganizationResponseMapper.toRank(
            rankService.create(request.code(), request.name(), request.seniority())
        );
    }

    @PutMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "updateRank", summary = "Rename a rank or change its seniority")
    public RankResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRankRequest request) {
        return OrganizationResponseMapper.toRank(rankService.update(id, request.name(), request.seniority()));
    }

    @DeleteMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "deleteRank", summary = "Delete an unused rank")
    public void delete(@PathVariable UUID id) {
        rankService.delete(id);
    }
}
