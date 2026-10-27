package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhanVienDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.NhanVien;

import java.util.List;

public class NhanVienBUS {
    private final NhanVienDAO dao = new NhanVienDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<NhanVien> search(String keyword) { return dao.search(keyword); }

    public void save(NhanVien n) {
        Session.requireModify();
        if (n.maCode() == null || n.maCode().isBlank()) throw new BusinessException("Mã nhân viên không được để trống.");
        if (n.hoTen() == null || n.hoTen().isBlank()) throw new BusinessException("Họ tên không được để trống.");
        if (n.email() != null && !n.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessException("Email không hợp lệ.");
        }
        if (n.maNhanVien() == null) {
            dao.insert(n);
            nhatKy.ghi("THÊM", "NHAN_VIEN", n.maCode(), "Thêm nhân viên " + n.hoTen());
        } else {
            dao.update(n);
            nhatKy.ghi("SỬA", "NHAN_VIEN", n.maCode(), "Sửa nhân viên " + n.hoTen());
        }
    }

    public void delete(NhanVien n) {
        Session.requireModify();
        dao.delete(n.maNhanVien());
        nhatKy.ghi("XÓA", "NHAN_VIEN", n.maCode(), "Xóa nhân viên " + n.hoTen());
    }
}
