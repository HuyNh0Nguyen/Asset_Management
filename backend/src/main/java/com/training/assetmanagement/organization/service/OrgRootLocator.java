package com.training.assetmanagement.organization.service;

import java.util.UUID;

public interface OrgRootLocator {

    // Id đơn vị gốc COMPANY - nạp một lần trong giao dịch riêng nên không tạo ảnh chụp
    // cho giao dịch đang gọi. Gốc chỉ sinh ở seed, không đổi, không xóa
    UUID rootId();
}
