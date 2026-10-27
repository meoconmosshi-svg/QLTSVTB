package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class ThongKeDAO {

    public BigDecimal tongGiaTriTonKho() {
        String sql = "SELECT "
                   + " (SELECT ISNULL(SUM(gia_tri_hien_tai * so_luong), 0) FROM tai_san WHERE trang_thai = N'Trong kho') "
                   + " + (SELECT ISNULL(SUM(gia_tri_hien_tai * so_luong), 0) FROM thiet_bi WHERE trang_thai = N'Trong kho') AS tong";
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public Map<String, Integer> thongKeTaiSanTheoTrangThai() {
        return countByColumn("SELECT trang_thai AS k, COUNT(*) AS c FROM tai_san GROUP BY trang_thai");
    }

    public Map<String, Integer> thongKeTaiSanTheoLoai() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT ISNULL(l.ten_loai, N'Chưa phân loại') AS ten, ISNULL(SUM(t.so_luong), 0) AS sl "
                   + "FROM tai_san t LEFT JOIN loai_tai_san l ON t.ma_loai = l.ma_loai "
                   + "GROUP BY l.ten_loai ORDER BY sl DESC";
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("ten"), rs.getInt("sl"));
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return map;
    }

    public Map<String, Integer> thongKeThietBiTheoTrangThai() {
        return countByColumn("SELECT trang_thai AS k, COUNT(*) AS c FROM thiet_bi GROUP BY trang_thai");
    }

    public Map<String, Integer> thongKeYeuCau() {
        return countByColumn("SELECT trang_thai AS k, COUNT(*) AS c FROM yeu_cau_cap_phat GROUP BY trang_thai");
    }

    public Map<String, Integer> thongKePhieuNghiepVu() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Cấp phát", queryCount("SELECT COUNT(*) FROM cap_phat"));
        map.put("Điều chuyển", queryCount("SELECT COUNT(*) FROM dieu_chuyen"));
        map.put("Bảo trì", queryCount("SELECT COUNT(*) FROM bao_tri"));
        map.put("Thanh lý", queryCount("SELECT COUNT(*) FROM thanh_ly"));
        return map;
    }

    private Map<String, Integer> countByColumn(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String k = rs.getString("k");
                if (k == null || k.isBlank()) k = "(không rõ)";
                map.put(k, rs.getInt("c"));
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return map;
    }

    private int queryCount(String sql) {
        try (Connection c = DBConnection.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }
}