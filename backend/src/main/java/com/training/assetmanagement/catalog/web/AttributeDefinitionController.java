package com.training.assetmanagement.catalog.web;

import com.training.assetmanagement.catalog.web.dto.response.AttributeDefinitionResponse;
import com.training.assetmanagement.shared.web.ApiPath;
import com.training.assetmanagement.shared.web.ApiTag;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPath.ASSET_TYPES)
@Tag(name = ApiTag.CATALOG)
public class AttributeDefinitionController {

    // Trả trọn danh sách (cấu hình con của MỘT loại, form cần đủ) - không phân trang
    @GetMapping(ApiPath.ASSET_TYPE_ATTRIBUTES)
    public List<AttributeDefinitionResponse> listAttributeDefinitions(@PathVariable UUID assetTypeId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
