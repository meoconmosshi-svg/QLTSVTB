package com.qlts.dao;

import com.qlts.common.Constants;
import com.qlts.conn.DBConnection;
import com.qlts.entity.ThietBi;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThietBiDAO {

    private static final String SELECT =
            "SELECT t.*, l.ten_loai FROM thiet_bi t "
          + "LEFT JOIN loai_tai_san l ON t.ma_loai = l.ma_loai ";

    /** Tìm kiếm theo mã / tên / serial / hãng SX. */
    public List<ThietBi> search(String keyword) {
        return query(SELECT + "WHERE t.ma_tb_code LIKE ? OR t.ten_thiet_bi LIKE ? OR t.so_serial LIKE ? "
                + "OR t.hang_san_xuat LIKE ? ORDER BY t.ma_thiet_bi DESC", JdbcUtil.like(keyword), 4);
    }

    /** Thiết bị đang trong kho. */
    public List<ThietBi> findAvailable() {
        return query(SELECT + "WHERE t.trang_thai = ? ORDER BY t.ten_thiet_bi",
                Constants.TS_TRONG_KHO, 1);
    }

    // ============================================================
    // CRUD (có so_luong + hinh_anh)
    // ============================================================

    public void insert(ThietBi t) {
        String sql = "INSERT INTO thiet_bi(ma_tb_code, ten_thiet_bi, so_luong, ma_loai, ma_ncc, "
                + "hang_san_xuat, model, so_serial, cau_hinh, ngay_mua, ngay_het_bh, "
                + "gia_mua, gia_tri_hien_tai, tinh_trang, trang_thai, vi_tri, ghi_chu, hinh_anh) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void update(ThietBi t) {
        String sql = "UPDATE thiet_bi SET ma_tb_code=?, ten_thiet_bi=?, so_luong=?, ma_loai=?, ma_ncc=?, "
                + "hang_san_xuat=?, model=?, so_serial=?, cau_hinh=?, ngay_mua=?, ngay_het_bh=?, "
                + "gia_mua=?, gia_tri_hien_tai=?, tinh_trang=?, trang_thai=?, vi_tri=?, ghi_chu=?, "
                + "hinh_anh=? WHERE ma_thiet_bi=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);                          // Set 18 cột đầu
            ps.setInt(19, t.maThietBi());         // Cột 19 = WHERE
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM thiet_bi WHERE ma_thiet_bi = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void bind(PreparedStatement ps, ThietBi t) throws SQLException {
        ps.setString(1, t.maCode());
        ps.setString(2, t.ten());
        ps.setInt(3, t.soLuongSafe());
        JdbcUtil.setInt(ps, 4, t.maLoai());
        JdbcUtil.setInt(ps, 5, t.maNcc());
        ps.setString(6, t.hangSanXuat());
        ps.setString(7, t.model());
        ps.setString(8, t.soSerial());
        ps.setString(9, t.cauHinh());
        JdbcUtil.setDate(ps, 10, t.ngayMua());
        JdbcUtil.setDate(ps, 11, t.ngayHetBh());
        JdbcUtil.setMoney(ps, 12, t.giaMua());
        JdbcUtil.setMoney(ps, 13, t.giaTriHienTai());
        ps.setString(14, t.tinhTrang());
        ps.setString(15, t.trangThai());
        ps.setString(16, t.viTri());
        ps.setString(17, t.ghiChu());
        ps.setString(18, t.hinhAnh());            // ← THÊM cột ảnh
    }

    private List<ThietBi> query(String sql, String param, int paramCount) {
        List<ThietBi> list = new ArrayList<>();
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

    private ThietBi map(ResultSet rs) throws SQLException {
        return new ThietBi(
                rs.getInt("ma_thiet_bi"),
                rs.getString("ma_tb_code"),
                rs.getString("ten_thiet_bi"),
                rs.getInt("so_luong"),
                JdbcUtil.getInt(rs, "ma_loai"),
                rs.getString("ten_loai"),
                JdbcUtil.getInt(rs, "ma_ncc"),
                rs.getString("hang_san_xuat"),
                rs.getString("model"),
                rs.getString("so_serial"),
                rs.getString("cau_hinh"),
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