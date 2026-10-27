package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.BaoTriDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.BaoTri;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaoTriBUS {
    private final BaoTriDAO dao = new BaoTriDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    // 🔥 Admin xem hết, Nhân viên chỉ xem của mình
    public List<BaoTri> search(String keyword) {
        Integer filter = Session.isAdmin() ? null : Session.userId();
        return dao.search(keyword, filter);
    }

    public void taoPhieu(Integer maTaiSan, Integer maThietBi, String loai, LocalDate ngay, String noiDung, String donVi, BigDecimal chiPhi) {
        if (Session.current() == null) throw new BusinessException("Chưa đăng nhập.");
        if ((maTaiSan == null) == (maThietBi == null)) throw new BusinessException("Chọn 1 tài sản hoặc thiết bị.");
        if (noiDung == null || noiDung.isBlank()) throw new BusinessException("Nhập nội dung bảo trì.");
        
        String trangThai = Session.isAdmin() ? "Đang thực hiện" : "Chờ duyệt";
        String code = "BT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        
        BaoTri bt = new BaoTri(null, code, maTaiSan, maThietBi, loai, ngay, null, noiDung, 
                chiPhi == null ? BigDecimal.ZERO : chiPhi, donVi, Session.userId(), null, trangThai, null, null);
        
        dao.insert(bt);
        nhatKy.ghi("TẠO BẢO TRÌ", "BAO_TRI", code, "Tạo phiếu bảo trì");
    }

    public void duyet(BaoTri bt) {
        Session.requireModify();
        if (!"Chờ duyệt".equals(bt.trangThai())) throw new BusinessException("Chỉ duyệt phiếu đang chờ.");
        dao.updateTrangThai(bt.maBaoTri(), "Đang thực hiện");
        nhatKy.ghi("DUYỆT BT", "BAO_TRI", bt.maBtCode(), "Đã duyệt");
    }

    public void hoanThanh(BaoTri bt, LocalDate ngay, String ketQua, BigDecimal chiPhi) {
        Session.requireModify();
        if (ngay == null) throw new BusinessException("Chọn ngày kết thúc.");
        if (chiPhi == null || chiPhi.signum() < 0) throw new BusinessException("Chi phí không hợp lệ.");
        
        dao.hoanThanh(bt.maBaoTri(), ngay, ketQua, chiPhi);
        nhatKy.ghi("HOÀN THÀNH BT", "BAO_TRI", bt.maBtCode(), "Chi phí: " + chiPhi);
    }

    public void xoa(BaoTri bt) {
        if (!Session.isAdmin() && !"Chờ duyệt".equals(bt.trangThai())) {
            throw new BusinessException("Chỉ được xóa phiếu đang chờ duyệt.");
        }
        dao.delete(bt.maBaoTri());
        nhatKy.ghi("XÓA BT", "BAO_TRI", bt.maBtCode(), "Xóa phiếu bảo trì");
    }
}