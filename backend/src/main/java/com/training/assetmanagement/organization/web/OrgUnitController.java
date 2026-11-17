package com.training.assetmanagement.organization.web;

import com.training.assetmanagement.organization.service.OrgUnitService;
import com.training.assetmanagement.organization.service.OrgUnitStatusService;
import com.training.assetmanagement.organization.web.dto.request.CreateOrgUnitRequest;
import com.training.assetmanagement.organization.web.dto.request.MoveOrgUnitRequest;
import com.training.assetmanagement.organization.web.dto.request.UpdateOrgUnitRequest;
import com.training.assetmanagement.organization.web.dto.response.MoveCheckResponse;
import com.training.assetmanagement.organization.web.dto.response.OrgUnitBlockersResponse;
import com.training.assetmanagement.organization.web.dto.response.OrgUnitHeadResponse;
import com.training.assetmanagement.organization.web.dto.response.OrgUnitNodeResponse;
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

// Cây trả trọn danh sách (vài chục nút, bộ chọn cần đủ) - không phân trang
@RestController
@RequestMapping(ApiPath.ORG_UNITS)
@Tag(name = ApiTag.ORGANIZATION)
@RequiredArgsConstructor
public class OrgUnitController {

    private final OrgUnitService orgUnitService;
    private final OrgUnitStatusService orgUnitStatusService;

    // Mọi người đã đăng nhập - dữ liệu tham chiếu cho bộ chọn
    @GetMapping(ApiPath.TREE)
    @Operation(operationId = "getOrgUnitTree", summary = "Flat pre-ordered org unit tree")
    public List<OrgUnitNodeResponse> getTree() {
        return orgUnitService.getTree().stream().map(OrganizationResponseMapper::toNode).toList();
    }

    @PostMapping
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "createOrgUnit", summary = "Create a branch or department")
    public OrgUnitNodeResponse create(@Valid @RequestBody CreateOrgUnitRequest request) {
        return OrganizationResponseMapper.toNode(
            orgUnitService.create(request.code(), request.name(), request.unitType(), request.parentId())
        );
    }

    @PutMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "updateOrgUnit", summary = "Rename org unit or change its head")
    public OrgUnitNodeResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateOrgUnitRequest request) {
        return OrganizationResponseMapper.toNode(orgUnitService.update(id, request.name(), request.headUserId()));
    }

    // Hộp thoại vô hiệu hóa người dùng (USER_MANAGE) dùng lại để chọn người thay
    @GetMapping(ApiPath.HEAD_CANDIDATES)
    @PreAuthorize(ApiAuthority.ORG_OR_USER_MANAGE)
    @Operation(operationId = "listOrgUnitHeadCandidates", summary = "Active users in the unit subtree")
    public List<OrgUnitHeadResponse> listHeadCandidates(@PathVariable UUID id) {
        return orgUnitService.headCandidates(id).stream().map(OrganizationResponseMapper::toHead).toList();
    }

    @GetMapping(ApiPath.MOVE_CHECK)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "checkOrgUnitMove", summary = "Check whether a department can move under a new parent")
    public MoveCheckResponse checkMove(@PathVariable UUID id, @RequestParam UUID parentId) {
        return OrganizationResponseMapper.toMoveCheck(orgUnitService.checkMove(id, parentId));
    }

    @PostMapping(ApiPath.MOVE)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "moveOrgUnit", summary = "Move a department under a new parent")
    public void move(@PathVariable UUID id, @Valid @RequestBody MoveOrgUnitRequest request) {
        orgUnitService.move(id, request.parentId());
    }

    @GetMapping(ApiPath.DELETION_CHECK)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "checkOrgUnitDeletion", summary = "List what blocks deleting the org unit")
    public OrgUnitBlockersResponse checkDeletion(@PathVariable UUID id) {
        return OrganizationResponseMapper.toBlockers(orgUnitService.checkDeletion(id));
    }

    @DeleteMapping(ApiPath.BY_ID)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "deleteOrgUnit", summary = "Delete an unused org unit")
    public void delete(@PathVariable UUID id) {
        orgUnitService.delete(id);
    }

    @GetMapping(ApiPath.DEACTIVATION_CHECK)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "checkOrgUnitDeactivation", summary = "List what blocks deactivating the org unit")
    public OrgUnitBlockersResponse checkDeactivation(@PathVariable UUID id) {
        return OrganizationResponseMapper.toBlockers(orgUnitStatusService.checkDeactivation(id));
    }

    @PostMapping(ApiPath.DEACTIVATE)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "deactivateOrgUnit", summary = "Deactivate an empty org unit")
    public void deactivate(@PathVariable UUID id) {
        orgUnitStatusService.deactivate(id);
    }

    @PostMapping(ApiPath.REACTIVATE)
    @PreAuthorize(ApiAuthority.ORG_MANAGE)
    @Operation(operationId = "reactivateOrgUnit", summary = "Reactivate an org unit whose parent is active")
    public void reactivate(@PathVariable UUID id) {
        orgUnitStatusService.reactivate(id);
    }
}
