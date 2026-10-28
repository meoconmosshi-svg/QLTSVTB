package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.ThanhLyDAO;
import com.qlts.entity.ThanhLy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ThanhLyBUS {

    private final ThanhLyDAO dao = new ThanhLyDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    /**
     * Admin xem tất cả (để duyệt). Nhân viên CHỈ xem phiếu do mình tạo (để đối
     * chiếu/hủy).
     */
    public List<ThanhLy> search(String kw) {
        Integer filter = Session.isAdmin() ? null : Session.userId();
        return dao.search(kw, filter);
    }

    // Sửa lại hàm yeuCau trong ThanhLyBUS
    public void yeuCau(Integer maTs, Integer maTb, LocalDate ngay, String lyDo, BigDecimal giaTri) {
        if (Session.current() == null) {
            throw new BusinessException("Chưa đăng nhập.");
        }
        if ((maTs == null) == (maTb == null)) {
            throw new BusinessException("Chọn 1 tài sản hoặc thiết bị.");
        }
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessException("Nhập lý do thanh lý.");
        }

        // 🔥 LOGIC CHUYÊN NGHIỆP: Kiểm tra trạng thái tài sản trước khi cho thanh lý
        String trangThaiHienTai = maTs != null
                ? new TaiSanBUS().search("").stream().filter(t -> t.maTaiSan() == maTs).findFirst().get().trangThai()
                : new ThietBiBUS().search("").stream().filter(t -> t.maThietBi() == maTb).findFirst().get().trangThai();

        if ("Đang sử dụng".equals(trangThaiHienTai)) {
            throw new BusinessException("Tài sản đang được nhân viên sử dụng. Phải thu hồi về kho trước khi thanh lý!");
        }
        if ("Bảo trì".equals(trangThaiHienTai)) {
            throw new BusinessException("Tài sản đang trong quá trình bảo trì, không thể thanh lý!");
        }

        String code = "TL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        ThanhLy tl = new ThanhLy(null, code, maTs, maTb, ngay, lyDo, giaTri, null, "Chờ duyệt", null, Session.userId(), null, null, null);
        dao.insert(tl);
        nhatKy.ghi("YÊU CẦU TL", "THANH_LY", code, "Lý do: " + lyDo);
    }

    public void duyet(ThanhLy tl) {
        Session.requireModify(); // Chỉ Admin
        if (!"Chờ duyệt".equals(tl.trangThai())) {
            throw new BusinessException("Phiếu này đã được xử lý.");
        }
        dao.duyet(tl.maThanhLy(), Session.userId());
        nhatKy.ghi("DUYỆT TL", "THANH_LY", tl.maTlCode(), "Đã duyệt thanh lý");
    }

    public void tuChoi(ThanhLy tl, String lyDo) {
        Session.requireModify(); // Chỉ Admin
        if (!"Chờ duyệt".equals(tl.trangThai())) {
            throw new BusinessException("Phiếu này đã được xử lý.");
        }
        dao.tuChoi(tl.maThanhLy(), Session.userId(), lyDo);
        nhatKy.ghi("TỪ CHỐI TL", "THANH_LY", tl.maTlCode(), "Từ chối: " + lyDo);
    }

    public void xoa(ThanhLy tl) {
        // Nhân viên chỉ được xóa phiếu của chính mình (nếu chưa duyệt). Admin xóa được tất cả.
        if (!Session.isAdmin()) {
            if (tl.nguoiTao() == null || tl.nguoiTao() != Session.userId()) {
                throw new BusinessException("Chỉ được xóa đề xuất thanh lý của chính mình.");
            }
            if (!"Chờ duyệt".equals(tl.trangThai())) {
                throw new BusinessException("Chỉ được xóa phiếu đang chờ duyệt.");
            }
        }
        dao.delete(tl.maThanhLy());
        nhatKy.ghi("XÓA TL", "THANH_LY", tl.maTlCode(), "Xóa phiếu thanh lý");
    }
}
