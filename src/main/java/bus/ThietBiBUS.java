package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.ThietBiDAO;
import com.qlts.entity.ThietBi;

import java.util.List;

public class ThietBiBUS {
    private final ThietBiDAO dao = new ThietBiDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<ThietBi> search(String keyword) { return dao.search(keyword); }
    public List<ThietBi> findAvailable() { return dao.findAvailable(); }

    public void save(ThietBi t) {
        Session.requireModify();
        if (t.maCode() == null || t.maCode().isBlank()) throw new BusinessException("Mã thiết bị không được để trống.");
        if (t.ten() == null || t.ten().isBlank()) throw new BusinessException("Tên thiết bị không được để trống.");
        if (t.maThietBi() == null) {
            dao.insert(t);
            nhatKy.ghi("THÊM", "THIET_BI", t.maCode(), "Thêm thiết bị " + t.ten());
        } else {
            dao.update(t);
            nhatKy.ghi("SỬA", "THIET_BI", t.maCode(), "Sửa thiết bị " + t.ten());
        }
    }

    public void delete(ThietBi t) {
        Session.requireModify();
        if (Constants.TS_DANG_SU_DUNG.equals(t.trangThai())) {
            throw new BusinessException("Thiết bị đang được sử dụng, hãy ghi nhận trả trước khi xóa.");
        }
        dao.delete(t.maThietBi());
        nhatKy.ghi("XÓA", "THIET_BI", t.maCode(), "Xóa thiết bị " + t.ten());
    }
}
