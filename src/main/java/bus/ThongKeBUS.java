package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.ThongKeDAO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ThongKeBUS {
    private final ThongKeDAO dao = new ThongKeDAO();

    /** Chỉ Admin mới xem được thống kê. */
    private void requireAdmin() {
        if (!Session.isAdmin())
            throw new BusinessException("Chỉ ADMIN được xem báo cáo & thống kê.");
    }

    public BigDecimal tongDoanhThu() {
        requireAdmin();
        return dao.tongDoanhThu();
    }

    public Map<Integer, BigDecimal> doanhThuTheoThang(int nam) {
        requireAdmin();
        return dao.doanhThuTheoThang(nam);
    }

    public Map<Integer, BigDecimal> doanhThuTheoNam() {
        requireAdmin();
        return dao.doanhThuTheoNam();
    }

    public List<Integer> danhSachNam() {
        requireAdmin();
        return dao.danhSachNam();
    }

    public Map<String, Integer> taiSanTheoTrangThai() {
        requireAdmin();
        return dao.thongKeTaiSanTheoTrangThai();
    }

    public Map<String, Integer> taiSanTheoLoai() {
        requireAdmin();
        return dao.thongKeTaiSanTheoLoai();
    }

    public Map<String, Integer> thietBiTheoTrangThai() {
        requireAdmin();
        return dao.thongKeThietBiTheoTrangThai();
    }

    public BigDecimal tongGiaTriTonKho() {
        requireAdmin();
        return dao.tongGiaTriTonKho();
    }

    public List<ThongKeDAO.TopBanChay> topBanChay(int topN, int nam) {
        requireAdmin();
        return dao.topBanChay(topN, nam);
    }

    public Map<String, Integer> yeuCauTheoTrangThai() {
        requireAdmin();
        return dao.thongKeYeuCau();
    }

    public Map<String, Integer> phieuXuatTheoLoai() {
        requireAdmin();
        return dao.thongKePhieuXuatTheoLoai();
    }
}