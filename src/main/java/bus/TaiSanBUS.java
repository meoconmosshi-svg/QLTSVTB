package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.TaiSanDAO;
import com.qlts.entity.TaiSan;

import java.util.List;

public class TaiSanBUS {
    private final TaiSanDAO dao = new TaiSanDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<TaiSan> search(String keyword) { return dao.search(keyword); }
    public List<TaiSan> findAvailable() { return dao.findAvailable(); }
    
    // 🔥 THÊM HÀM NÀY ĐỂ FIX LỖI
    public List<TaiSan> findInUse() { return dao.findInUse(); }

    public void save(TaiSan t) {
        Session.requireModify();
        if (t.maCode() == null || t.maCode().isBlank()) throw new BusinessException("Mã tài sản không được để trống.");
        if (t.ten() == null || t.ten().isBlank()) throw new BusinessException("Tên tài sản không được để trống.");
        if (t.maTaiSan() == null) {
            dao.insert(t);
            nhatKy.ghi("THÊM", "TAI_SAN", t.maCode(), "Thêm tài sản " + t.ten());
        } else {
            dao.update(t);
            nhatKy.ghi("SỬA", "TAI_SAN", t.maCode(), "Sửa tài sản " + t.ten());
        }
    }

    public void delete(TaiSan t) {
        Session.requireModify();
        if (Constants.TS_DANG_SU_DUNG.equals(t.trangThai())) {
            throw new BusinessException("Tài sản đang được sử dụng, hãy ghi nhận trả trước khi xóa.");
        }
        dao.delete(t.maTaiSan());
        nhatKy.ghi("XÓA", "TAI_SAN", t.maCode(), "Xóa tài sản " + t.ten());
    }
}