package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.KhauHaoDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.KhauHao;
import com.qlts.entity.KhauHaoInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class KhauHaoBUS {
    private final KhauHaoDAO dao = new KhauHaoDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<KhauHaoInfo> getDanhSachCauHinh(String keyword) {
        return dao.getDanhSachCauHinh(keyword);
    }

    public List<KhauHao> getLichSu() {
        return dao.getLichSu();
    }

    public void updateCauHinh(KhauHaoInfo info, BigDecimal nguyenGia, Integer thoiGian, LocalDate ngayBatDau, BigDecimal giaTriConLai) {
        Session.requireModify();
        if (nguyenGia == null || nguyenGia.signum() <= 0) throw new BusinessException("Nguyên giá phải > 0.");
        if (thoiGian == null || thoiGian <= 0) throw new BusinessException("Thời gian khấu hao phải > 0 tháng.");
        if (ngayBatDau == null) throw new BusinessException("Chọn ngày bắt đầu khấu hao.");
        if (giaTriConLai == null || giaTriConLai.signum() < 0) throw new BusinessException("Giá trị còn lại không hợp lệ.");

        dao.updateCauHinh(info.isTaiSan(), info.id(), nguyenGia, thoiGian, ngayBatDau, giaTriConLai);
        nhatKy.ghi("CẤU HÌNH KH", "KHAU_HAO", info.maCode(), "Cấu hình khấu hao cho " + info.ten());
    }

    /**
     * Chạy khấu hao cho toàn bộ tài sản/thiết bị trong 1 kỳ (tháng/năm).
     * Phương pháp: Đường thẳng (Nguyên giá / Số tháng).
     */
    public int chayKhauHaoKy(int nam, int thang) {
        Session.requireModify();
        
        List<KhauHaoInfo> ds = dao.getDanhSachCauHinh("");
        int count = 0;

        for (KhauHaoInfo info : ds) {
            // Chỉ tính những máy đã cấu hình (có nguyên giá và thời gian)
            if (info.nguyenGia() == null || info.thoiGianKhauHao() == null || info.ngayBatDau() == null) continue;
            
            // Nếu đã tính kỳ này rồi thì bỏ qua
            if (dao.daTinhKy(info.id(), info.isTaiSan(), nam, thang)) continue;

            // Kiểm tra xem kỳ tính có trước ngày bắt đầu khấu hao không
            LocalDate ngayKy = LocalDate.of(nam, thang, 1);
            if (ngayKy.isBefore(LocalDate.of(info.ngayBatDau().getYear(), info.ngayBatDau().getMonth(), 1))) {
                continue;
            }

            BigDecimal giaTriDau = info.giaTriConLai();
            if (giaTriDau.compareTo(BigDecimal.ZERO) <= 0) continue; // Đã khấu hao hết

            // Tính mức khấu hao 1 tháng
            BigDecimal mucKhauHaoThang = info.nguyenGia().divide(BigDecimal.valueOf(info.thoiGianKhauHao()), 0, RoundingMode.HALF_UP);
            BigDecimal soTienKhauHao = mucKhauHaoThang.min(giaTriDau); // Không trừ quá giá trị đang có
            BigDecimal giaTriCuoi = giaTriDau.subtract(soTienKhauHao);
            
            // Tính tỷ lệ % (dùng để hiển thị)
            BigDecimal tyLe = mucKhauHaoThang.multiply(BigDecimal.valueOf(100)).divide(info.nguyenGia(), 2, RoundingMode.HALF_UP);

            dao.luuKhauHao(info.id(), info.isTaiSan(), nam, thang, giaTriDau, tyLe, soTienKhauHao, giaTriCuoi);
            count++;
        }

        if (count > 0) {
            nhatKy.ghi("CHẠY KHẤU HAO", "KHAU_HAO", thang + "/" + nam, "Đã tính khấu hao cho " + count + " tài sản/thiết bị");
        }
        return count;
    }
}