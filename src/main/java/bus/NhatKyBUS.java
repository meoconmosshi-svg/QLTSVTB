package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.NhatKy;

import java.util.List;

public class NhatKyBUS {
    private final NhatKyDAO dao = new NhatKyDAO();

    public List<NhatKy> search(String keyword) {
        if (!Session.isAdmin()) throw new BusinessException("Chỉ ADMIN được xem nhật ký.");
        return dao.search(keyword);
    }
}
