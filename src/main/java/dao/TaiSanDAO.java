package com.qlts.dao;

import com.qlts.common.Constants;
import com.qlts.conn.DBConnection;
import com.qlts.entity.TaiSan;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaiSanDAO {

    private static final String SELECT =
            "SELECT t.*, l.ten_loai FROM tai_san t "
          + "LEFT JOIN loai_tai_san l ON t.ma_loai = l.ma_loai ";

    /** Tìm kiếm theo mã / tên / serial. */
    public List<TaiSan> search(String keyword) {
        return query(SELECT + "WHERE t.ma_ts_code LIKE ? OR t.ten_tai_san LIKE ? OR t.so_serial LIKE ? "
                + "ORDER BY t.ma_tai_san DESC", JdbcUtil.like(keyword), 3);
    }

    /** Tài sản đang trong kho, có thể cấp phát. */
    public List<TaiSan> findAvailable() {
        return query(SELECT + "WHERE t.trang_thai = ? ORDER BY t.ten_tai_san",
                Constants.TS_TRONG_KHO, 1);
    }

    // ============================================================
    // CRUD (có so_luong + hinh_anh)
    // ============================================================

    public void insert(TaiSan t) {
        String sql = "INSERT INTO tai_san(ma_ts_code, ten_tai_san, so_luong, ma_loai, ma_ncc, "
                + "so_serial, ngay_mua, ngay_het_bh, gia_mua, gia_tri_hien_tai, "
                + "tinh_trang, trang_thai, vi_tri, ghi_chu, hinh_anh) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void update(TaiSan t) {
        String sql = "UPDATE tai_san SET ma_ts_code=?, ten_tai_san=?, so_luong=?, ma_loai=?, ma_ncc=?, "
                + "so_serial=?, ngay_mua=?, ngay_het_bh=?, gia_mua=?, gia_tri_hien_tai=?, "
                + "tinh_trang=?, trang_thai=?, vi_tri=?, ghi_chu=?, hinh_anh=? "
                + "WHERE ma_tai_san=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);                          // Set 15 cột đầu
            ps.setInt(16, t.maTaiSan());          // Cột 16 = WHERE
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }
    
        /** Tài sản đang được sử dụng (có thể điều chuyển/bàn giao). */
    public List<TaiSan> findInUse() {
        return query(SELECT + "WHERE t.trang_thai = ? ORDER BY t.ten_tai_san",
                Constants.TS_DANG_SU_DUNG, 1);
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM tai_san WHERE ma_tai_san = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void bind(PreparedStatement ps, TaiSan t) throws SQLException {
        ps.setString(1, t.maCode());
        ps.setString(2, t.ten());
        ps.setInt(3, t.soLuongSafe());
        JdbcUtil.setInt(ps, 4, t.maLoai());
        JdbcUtil.setInt(ps, 5, t.maNcc());
        ps.setString(6, t.soSerial());
        JdbcUtil.setDate(ps, 7, t.ngayMua());
        JdbcUtil.setDate(ps, 8, t.ngayHetBh());
        JdbcUtil.setMoney(ps, 9, t.giaMua());
        JdbcUtil.setMoney(ps, 10, t.giaTriHienTai());
        ps.setString(11, t.tinhTrang());
        ps.setString(12, t.trangThai());
        ps.setString(13, t.viTri());
        ps.setString(14, t.ghiChu());
        ps.setString(15, t.hinhAnh());            // ← THÊM cột ảnh
    }

    private List<TaiSan> query(String sql, String param, int paramCount) {
        List<TaiSan> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= paramCount; i++) ps.setString(i, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    private TaiSan map(ResultSet rs) throws SQLException {
        return new TaiSan(
                rs.getInt("ma_tai_san"),
                rs.getString("ma_ts_code"),
                rs.getString("ten_tai_san"),
                rs.getInt("so_luong"),
                JdbcUtil.getInt(rs, "ma_loai"),
                rs.getString("ten_loai"),
                JdbcUtil.getInt(rs, "ma_ncc"),
                rs.getString("so_serial"),
                JdbcUtil.getDate(rs, "ngay_mua"),
                JdbcUtil.getDate(rs, "ngay_het_bh"),
                rs.getBigDecimal("gia_mua"),
                rs.getBigDecimal("gia_tri_hien_tai"),
                rs.getString("tinh_trang"),
                rs.getString("trang_thai"),
                rs.getString("vi_tri"),
                rs.getString("ghi_chu"),
                rs.getString("hinh_anh"));        // ← THÊM cột ảnh
    }
}