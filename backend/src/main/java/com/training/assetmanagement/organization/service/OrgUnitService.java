package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.*;

import java.util.List;
import java.util.UUID;

public interface OrgUnitService {

    // Danh sách phẳng tiền thứ tự; allowedActions rỗng khi người gọi không có ORG_MANAGE
    List<OrgUnitView> getTree();

    OrgUnitView create(String code, String name, OrgUnitType unitType, UUID parentId);

    // headUserId = null nghĩa là để trống trưởng đơn vị
    OrgUnitView update(UUID id, String name, UUID headUserId);

    // Người dùng ACTIVE trong cây con của đơn vị
    List<OrgUnitHead> headCandidates(UUID id);

    // Cùng hàm kiểm với move(), không khóa; rỗng = chuyển được
    List<BlockingHead> checkMove(UUID id, UUID parentId);

    void move(UUID id, UUID parentId);

    List<OrgUnitBlocker> checkDeletion(UUID id);

    void delete(UUID id);

    // Cửa cho miền user: Đổi / gỡ trưởng trong giao dịch của bên gọi
    void replaceHead(UUID unitId, UUID headUserId);
}
