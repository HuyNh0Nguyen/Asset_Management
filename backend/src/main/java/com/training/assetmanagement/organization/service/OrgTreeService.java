package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgTree;

public interface OrgTreeService {

    // Nạp cả cây trong một truy vấn, không cache.
    // Lệnh ghi gọi SAU câu khóa đầu tiên và đúng một lần, rồi truyền cây xuống
    OrgTree load();
}
