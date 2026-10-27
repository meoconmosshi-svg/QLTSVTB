package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.ThongKeDAO;

import java.math.BigDecimal;
import java.util.Map;

public class ThongKeBUS {
    private final ThongKeDAO dao = new ThongKeDAO();

    private void requireAdmin() {
        if (!Session.isAdmin()) throw new BusinessException("Chỉ ADMIN được xem báo cáo.");
    }

    public BigDecimal tongGiaTriTonKho() { requireAdmin(); return dao.tongGiaTriTonKho(); }
    public Map<String, Integer> taiSanTheoTrangThai() { requireAdmin(); return dao.thongKeTaiSanTheoTrangThai(); }
    public Map<String, Integer> taiSanTheoLoai() { requireAdmin(); return dao.thongKeTaiSanTheoLoai(); }
    public Map<String, Integer> thietBiTheoTrangThai() { requireAdmin(); return dao.thongKeThietBiTheoTrangThai(); }
    public Map<String, Integer> yeuCauTheoTrangThai() { requireAdmin(); return dao.thongKeYeuCau(); }
    public Map<String, Integer> phieuNghiepVu() { requireAdmin(); return dao.thongKePhieuNghiepVu(); }
}