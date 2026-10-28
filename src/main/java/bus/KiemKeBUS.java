package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.KiemKeDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.KiemKe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KiemKeBUS {
    private final KiemKeDAO dao = new KiemKeDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<KiemKe> search(String kw) { return dao.search(kw); }

    public void ghiNhan(String dot, Integer maTs, Integer maTb, Integer maPb, LocalDate ngay,
                        String tinhTrang, int thucTe, int soSach, String ketQua, String ghiChu) {
        Session.requireModify();
        if (maPb == null) throw new BusinessException("Vui lòng chọn phòng ban kiểm kê.");
        
        String code = "KK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        int chenhLech = thucTe - soSach;
        
        KiemKe k = new KiemKe(null, code, dot, maTs, maTb, maPb, ngay, tinhTrang, thucTe, soSach, chenhLech, Session.userId(), ketQua, ghiChu, null, null);
        dao.insert(k);
        
        // Logic chuyên nghiệp: Tự động cập nhật lại số lượng kho nếu có chênh lệch
        if (chenhLech != 0) {
            int id = maTs != null ? maTs : maTb;
            boolean isTaiSan = maTs != null;
            dao.capNhatSoLuongTaiSan(id, isTaiSan, thucTe);
        }
        
        nhatKy.ghi("KIỂM KÊ", "KIEM_KE", code, "Ghi nhận kiểm kê đợt " + dot + " (Lệch: " + chenhLech + ")");
    }

    // 🔥 THÊM HÀM XÓA NÀY
    public void xoa(KiemKe k) {
        // Chỉ Admin hoặc người tạo phiếu mới được xóa
        if (!Session.isAdmin() && k.nguoiKiemKe() != null && k.nguoiKiemKe() != Session.userId()) {
            throw new BusinessException("Chỉ được xóa phiếu kiểm kê do chính mình tạo.");
        }
        dao.delete(k.maKiemKe());
        nhatKy.ghi("XÓA KK", "KIEM_KE", k.maKkCode(), "Xóa phiếu kiểm kê");
    }
}