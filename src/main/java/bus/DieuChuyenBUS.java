package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.DieuChuyenDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.DieuChuyen;
import com.qlts.entity.NhanVien;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DieuChuyenBUS {
    private final DieuChuyenDAO dao = new DieuChuyenDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<DieuChuyen> search(String keyword) {
        return dao.search(keyword);
    }

    public void dieuChuyen(Integer maTaiSan, Integer maThietBi,
                           Integer tuNhanVien, Integer tuPhongBan,
                           NhanVien denNhanVien,
                           LocalDate ngay, String lyDo, String ghiChu) {
        Session.requireModify();
        if ((maTaiSan == null) == (maThietBi == null))
            throw new BusinessException("Chọn đúng 1 tài sản hoặc thiết bị.");
        if (denNhanVien == null)
            throw new BusinessException("Vui lòng chọn nhân viên tiếp nhận.");
        if (ngay == null)
            throw new BusinessException("Vui lòng chọn ngày điều chuyển.");
        if (lyDo == null || lyDo.isBlank())
            throw new BusinessException("Vui lòng nhập lý do điều chuyển.");

        String code = "DC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        DieuChuyen dc = new DieuChuyen(
                null, code, maTaiSan, maThietBi,
                tuPhongBan, denNhanVien.maPhongBan(),
                tuNhanVien, denNhanVien.maNhanVien(),
                ngay, lyDo.trim(), Session.userId(), "Hoàn thành", ghiChu,
                null, null, null, null, null);

        dao.insert(dc, Session.userId());
        nhatKy.ghi("ĐIỀU CHUYỂN", "DIEU_CHUYEN", code, "Điều chuyển cho " + denNhanVien.hoTen());
    }
}