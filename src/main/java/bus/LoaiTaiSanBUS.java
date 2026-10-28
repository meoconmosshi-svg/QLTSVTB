package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.LoaiTaiSanDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.LoaiTaiSan;

import java.util.List;

public class LoaiTaiSanBUS {
    private final LoaiTaiSanDAO dao = new LoaiTaiSanDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<LoaiTaiSan> search(String kw) { return dao.search(kw); }

    public void save(LoaiTaiSan l) {
        Session.requireModify();
        if (l.tenLoai() == null || l.tenLoai().isBlank()) throw new BusinessException("Tên loại không được để trống.");
        
        if (l.maLoai() == null || l.maLoai() == 0) {
            dao.insert(l);
            nhatKy.ghi("THÊM", "LOAI_TS", l.maCode(), "Thêm loại " + l.tenLoai());
        } else {
            dao.update(l);
            nhatKy.ghi("SỬA", "LOAI_TS", l.maCode(), "Sửa loại " + l.tenLoai());
        }
    }

    public void delete(LoaiTaiSan l) {
        Session.requireModify();
        try {
            dao.delete(l.maLoai());
            nhatKy.ghi("XÓA", "LOAI_TS", l.maCode(), "Xóa loại " + l.tenLoai());
        } catch (Exception e) {
            throw new BusinessException("Không thể xóa vì đang có tài sản thuộc loại này.");
        }
    }
}