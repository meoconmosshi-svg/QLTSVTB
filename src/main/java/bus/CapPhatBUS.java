package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.dao.CapPhatDAO;
import com.qlts.dao.NhanVienDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.CapPhat;
import com.qlts.entity.NhanVien;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CapPhatBUS {
    private final CapPhatDAO dao = new CapPhatDAO();
    private final NhanVienDAO nhanVienDao = new NhanVienDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<CapPhat> search(String keyword) { return dao.search(keyword); }

    public List<NhanVien> allEmployees() { return nhanVienDao.search(""); }

    /** Cấp phát đúng MỘT trong hai: tài sản hoặc thiết bị. */
    public void capPhat(Integer maTaiSan, Integer maThietBi, NhanVien nv,
                        LocalDate ngayCap, LocalDate ngayDuKienTra, String ghiChu) {
        Session.requireModify();
        if ((maTaiSan == null) == (maThietBi == null)) {
            throw new BusinessException("Hãy chọn một tài sản hoặc một thiết bị để cấp phát.");
        }
        if (nv == null) throw new BusinessException("Hãy chọn nhân viên nhận.");
        if (ngayCap == null) throw new BusinessException("Ngày cấp phát không được để trống.");
        if (ngayDuKienTra != null && ngayDuKienTra.isBefore(ngayCap)) {
            throw new BusinessException("Ngày dự kiến trả không được trước ngày cấp phát.");
        }
        String code = "CP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        CapPhat cp = new CapPhat(null, code, maTaiSan, maThietBi, null, nv.maNhanVien(), nv.hoTen(),
                ngayCap, ngayDuKienTra, null, null, Constants.CP_DANG_SU_DUNG, ghiChu);
        dao.capPhat(cp, Session.userId());
        nhatKy.ghi("CẤP PHÁT", "CAP_PHAT", code, "Cấp phát cho " + nv.hoTen());
    }

    public void tra(CapPhat cp, LocalDate ngayTra, String tinhTrang) {
        Session.requireModify();
        if (ngayTra == null) throw new BusinessException("Ngày trả không được để trống.");
        if (ngayTra.isBefore(cp.ngayCapPhat())) {
            throw new BusinessException("Ngày trả không được trước ngày cấp phát.");
        }
        dao.tra(cp.maCapPhat(), ngayTra, tinhTrang);
        nhatKy.ghi("TRẢ", "CAP_PHAT", cp.maCode(), cp.tenNhanVien() + " trả " + cp.tenDoiTuong());
    }
}
