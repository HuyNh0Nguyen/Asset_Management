package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.Rank;

import java.util.List;
import java.util.UUID;

public interface RankService {

    // Trọn danh sách (vài dòng) sắp theo seniority - không phân trang
    List<Rank> list();

    Rank create(String code, String name, int seniority);

    // Mã bất biến; số thứ bậc sửa được kể cả khi đang dùng
    Rank update(UUID id, String name, int seniority);

    // RESTRICT từ users / workflow_rank_conditions chính là quy tắc "đang dùng thì không xóa"
    void delete(UUID id);
}
