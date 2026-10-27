package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * DAO chuyên cho thống kê + báo cáo.
 * Chỉ có các câu SELECT - không INSERT/UPDATE/DELETE.
 */
public class ThongKeDAO {

    // ============================================================
    // 1. DOANH THU
    // ============================================================

    /** Tổng doanh thu (chỉ tính phiếu Bán đã xuất thành công). */
    public BigDecimal tongDoanhThu() {
        String sql = "SELECT ISNULL(SUM(so_luong * gia_ban), 0) FROM phieu_xuat_kho "
                   + "WHERE loai_xuat = N'Bán' AND trang_thai = N'Đã bán'";
        return queryMoney(sql);
    }

    /** Doanh thu theo từng THÁNG trong 1 NĂM. Key = tháng (1-12), Value = tổng tiền. */
    public Map<Integer, BigDecimal> doanhThuTheoThang(int nam) {
        Map<Integer, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) map.put(i, BigDecimal.ZERO);

        String sql = "SELECT MONTH(ngay_xuat) AS thang, "
                   + "       ISNULL(SUM(so_luong * gia_ban), 0) AS doanh_thu "
                   + "FROM phieu_xuat_kho "
                   + "WHERE loai_xuat = N'Bán' AND trang_thai = N'Đã bán' "
                   + "  AND YEAR(ngay_xuat) = ? "
                   + "GROUP BY MONTH(ngay_xuat)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("thang"), rs.getBigDecimal("doanh_thu"));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return map;
    }

    /** Doanh thu theo từng NĂM (mọi năm có dữ liệu). */
    public Map<Integer, BigDecimal> doanhThuTheoNam() {
        Map<Integer, BigDecimal> map = new TreeMap<>();
        String sql = "SELECT YEAR(ngay_xuat) AS nam, "
                   + "       ISNULL(SUM(so_luong * gia_ban), 0) AS doanh_thu "
                   + "FROM phieu_xuat_kho "
                   + "WHERE loai_xuat = N'Bán' AND trang_thai = N'Đã bán' "
                   + "GROUP BY YEAR(ngay_xuat) ORDER BY nam";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getInt("nam"), rs.getBigDecimal("doanh_thu"));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return map;
    }

    /** Danh sách năm có dữ liệu (dùng cho combobox). */
    public List<Integer> danhSachNam() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(ngay_xuat) AS nam FROM phieu_xuat_kho "
                   + "WHERE ngay_xuat IS NOT NULL ORDER BY nam DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getInt("nam"));
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        // Đảm bảo năm hiện tại luôn có trong combo
        int now = java.time.Year.now().getValue();
        if (!list.contains(now)) list.add(0, now);
        return list;
    }

    // ============================================================
    // 2. TÀI SẢN
    // ============================================================

    /** Đếm tài sản theo trạng thái. Key = trạng thái, Value = số lượng dòng. */
    public Map<String, Integer> thongKeTaiSanTheoTrangThai() {
        return countByColumn("SELECT trang_thai AS k, COUNT(*) AS c FROM tai_san GROUP BY trang_thai");
    }

    /** Đếm tài sản theo loại. Key = tên loại, Value = tổng số lượng (SUM so_luong). */
    public Map<String, Integer> thongKeTaiSanTheoLoai() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT ISNULL(l.ten_loai, N'Chưa phân loại') AS ten, "
                   + "       ISNULL(SUM(t.so_luong), 0) AS sl "
                   + "FROM tai_san t LEFT JOIN loai_tai_san l ON t.ma_loai = l.ma_loai "
                   + "GROUP BY l.ten_loai ORDER BY sl DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("ten"), rs.getInt("sl"));
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return map;
    }

    /** Đếm thiết bị theo trạng thái. */
    public Map<String, Integer> thongKeThietBiTheoTrangThai() {
        return countByColumn("SELECT trang_thai AS k, COUNT(*) AS c FROM thiet_bi GROUP BY trang_thai");
    }

    /** Tổng giá trị tài sản + thiết bị đang trong kho. */
    public BigDecimal tongGiaTriTonKho() {
        String sql = "SELECT "
                   + " (SELECT ISNULL(SUM(gia_tri_hien_tai * so_luong), 0) FROM tai_san WHERE trang_thai = N'Trong kho') "
                   + " + (SELECT ISNULL(SUM(gia_tri_hien_tai * so_luong), 0) FROM thiet_bi WHERE trang_thai = N'Trong kho') AS tong";
        return queryMoney(sql);
    }

    // ============================================================
    // 3. TOP BÁN CHẠY
    // ============================================================

    /** Record cho top bán chạy. */
    public record TopBanChay(String tenTaiSan, int soLuongBan, BigDecimal doanhThu) {}

    /** Top N tài sản bán chạy nhất. */
    public List<TopBanChay> topBanChay(int topN, int nam) {
        String sql = "SELECT COALESCE(ts.ten_tai_san, tb.ten_thiet_bi, N'(đã xóa)') AS ten, "
                   + "       SUM(p.so_luong) AS sl_ban, "
                   + "       SUM(p.so_luong * ISNULL(p.gia_ban, 0)) AS doanh_thu "
                   + "FROM phieu_xuat_kho p "
                   + "LEFT JOIN tai_san  ts ON p.ma_tai_san = ts.ma_tai_san "
                   + "LEFT JOIN thiet_bi tb ON p.ma_thiet_bi = tb.ma_thiet_bi "
                   + "WHERE p.loai_xuat = N'Bán' AND p.trang_thai = N'Đã bán' "
                   + (nam > 0 ? "  AND YEAR(p.ngay_xuat) = ? " : "")
                   + "GROUP BY COALESCE(ts.ten_tai_san, tb.ten_thiet_bi, N'(đã xóa)') "
                   + "ORDER BY sl_ban DESC, doanh_thu DESC";
        if (topN > 0) sql = sql.replace("SELECT", "SELECT TOP " + topN);

        List<TopBanChay> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (nam > 0) ps.setInt(1, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopBanChay(
                            rs.getString("ten"),
                            rs.getInt("sl_ban"),
                            rs.getBigDecimal("doanh_thu")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    // ============================================================
    // 4. NHÂN SỰ / HOẠT ĐỘNG
    // ============================================================

    /** Đếm số yêu cầu theo trạng thái. */
    public Map<String, Integer> thongKeYeuCau() {
        return countByColumn(
                "SELECT trang_thai AS k, COUNT(*) AS c FROM yeu_cau_cap_phat GROUP BY trang_thai");
    }

    /** Đếm số phiếu xuất theo loại. */
    public Map<String, Integer> thongKePhieuXuatTheoLoai() {
        return countByColumn(
                "SELECT loai_xuat AS k, COUNT(*) AS c FROM phieu_xuat_kho GROUP BY loai_xuat");
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private BigDecimal queryMoney(String sql) {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    private Map<String, Integer> countByColumn(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String k = rs.getString("k");
                if (k == null || k.isBlank()) k = "(không rõ)";
                map.put(k, rs.getInt("c"));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return map;
    }
}