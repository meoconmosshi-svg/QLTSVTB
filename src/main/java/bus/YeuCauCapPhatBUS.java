package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.YeuCauCapPhatDAO;
import com.qlts.entity.YeuCauCapPhat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class YeuCauCapPhatBUS {
    private final YeuCauCapPhatDAO dao = new YeuCauCapPhatDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    /**
     * Danh sách yêu cầu theo role:
     *  - Admin + Nhân viên: xem TẤT CẢ (để duyệt)
     *  - Khách hàng: chỉ xem yêu cầu của chính mình
     */
    public List<YeuCauCapPhat> search(String keyword, String trangThai) {
        Integer filter = (Session.canModify() || Session.isNhanVien())
                ? null
                : Session.userId();
        return dao.search(keyword, trangThai, filter);
    }

    /** Đếm số yêu cầu chờ duyệt (cho Admin + Nhân viên). */
    public int countChoDuyet() {
        if (!Session.canApprove()) return 0;
        return dao.countChoDuyet();
    }

    /** Nhân viên / Khách hàng gửi yêu cầu. */
    public void guiYeuCau(Integer maLoai, int soLuong, String lyDo,
                          LocalDate ngayMongMuon, String loaiYeuCau, String ghiChu) {
        if (maLoai == null) throw new BusinessException("Vui lòng chọn loại tài sản.");
        if (soLuong < 1)    throw new BusinessException("Số lượng phải từ 1 trở lên.");
        if (lyDo == null || lyDo.isBlank())
            throw new BusinessException("Vui lòng ghi rõ mục đích/lý do.");
        if (ngayMongMuon == null)
            throw new BusinessException("Vui lòng chọn ngày mong muốn nhận.");
        if (ngayMongMuon.isBefore(LocalDate.now()))
            throw new BusinessException("Ngày mong muốn không được ở quá khứ.");

        String code = "YC" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));

        YeuCauCapPhat y = new YeuCauCapPhat(
                null, code, Session.userId(), null,
                maLoai, null, soLuong, lyDo.trim(),
                LocalDate.now(), ngayMongMuon,
                loaiYeuCau == null ? "Mượn" : loaiYeuCau,
                "Chờ duyệt", null, null, null, null,
                ghiChu == null ? null : ghiChu.trim(), null);

        int id = dao.insert(y);
        if (id <= 0) throw new BusinessException("Gửi yêu cầu thất bại.");

        nhatKy.ghi("GỬI YC", "YEU_CAU", code,
                "Gửi yêu cầu " + loaiYeuCau + ": " + code);
    }

    /**
     * Duyệt yêu cầu.
     * - Admin: duyệt mọi yêu cầu (kể cả của chính mình)
     * - Nhân viên: duyệt yêu cầu của KH và NV khác, KHÔNG tự duyệt yêu cầu của mình
     */
    public void duyet(YeuCauCapPhat y) {
        if (!Session.canApprove())
            throw new BusinessException("Chỉ Admin hoặc Nhân viên được duyệt yêu cầu.");
        if (!y.choDuyet())
            throw new BusinessException("Chỉ duyệt được yêu cầu đang chờ.");

        // Nhân viên KHÔNG được tự duyệt yêu cầu của chính mình
        if (Session.isNhanVien() && y.maTaiKhoan() == Session.userId()) {
            throw new BusinessException("Không thể tự duyệt yêu cầu của chính mình.");
        }

        dao.duyet(y.maYeuCau(), Session.userId());
        nhatKy.ghi("DUYỆT YC", "YEU_CAU", y.maYcCode(),
                "Duyệt yêu cầu: " + y.maYcCode()
                + " bởi " + Session.current().tenDangNhap());
    }

    /**
     * Từ chối yêu cầu.
     * - Admin: từ chối mọi yêu cầu
     * - Nhân viên: từ chối yêu cầu của KH và NV khác, KHÔNG tự từ chối yêu cầu của mình
     */
    public void tuChoi(YeuCauCapPhat y, String lyDo) {
        if (!Session.canApprove())
            throw new BusinessException("Chỉ Admin hoặc Nhân viên được từ chối yêu cầu.");
        if (!y.choDuyet())
            throw new BusinessException("Chỉ từ chối được yêu cầu đang chờ.");

        // Nhân viên KHÔNG được tự từ chối yêu cầu của chính mình
        if (Session.isNhanVien() && y.maTaiKhoan() == Session.userId()) {
            throw new BusinessException("Không thể tự từ chối yêu cầu của chính mình.");
        }

        dao.tuChoi(y.maYeuCau(), Session.userId(), lyDo);
        nhatKy.ghi("TỪ CHỐI YC", "YEU_CAU", y.maYcCode(),
                "Từ chối: " + (lyDo == null ? "—" : lyDo));
    }

    /** Người gửi tự hủy yêu cầu (khi còn chờ). */
    public void huy(YeuCauCapPhat y) {
        if (y.maTaiKhoan() != Session.userId())
            throw new BusinessException("Chỉ được hủy yêu cầu của chính mình.");
        dao.delete(y.maYeuCau(), Session.userId());
        nhatKy.ghi("HỦY YC", "YEU_CAU", y.maYcCode(),
                "Hủy yêu cầu: " + y.maYcCode());
    }

    /** Khi Admin tạo phiếu cấp phát thành công → đánh dấu yêu cầu là Đã cấp. */
    public void danhDauDaCap(YeuCauCapPhat y, int maCapPhat) {
        if (!Session.canModify()) return;
        dao.danhDauDaCap(y.maYeuCau(), maCapPhat);
        nhatKy.ghi("CẤP PHÁT YC", "YEU_CAU", y.maYcCode(),
                "Đã cấp phát cho yêu cầu " + y.maYcCode());
    }
}