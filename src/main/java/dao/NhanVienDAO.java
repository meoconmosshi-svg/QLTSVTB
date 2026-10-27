package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.NhanVien;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    private static final String SELECT =
            "SELECT n.*, p.ten_phong_ban FROM nhan_vien n LEFT JOIN phong_ban p ON n.ma_phong_ban = p.ma_phong_ban ";

    public List<NhanVien> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = SELECT + "WHERE n.ma_nv_code LIKE ? OR n.ho_ten LIKE ? OR n.email LIKE ? "
                + "OR n.so_dien_thoai LIKE ? ORDER BY n.ma_nhan_vien";
        List<NhanVien> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    public void insert(NhanVien n) {
        String sql = "INSERT INTO nhan_vien(ma_nv_code, ho_ten, gioi_tinh, ngay_sinh, cmnd_cccd, chuc_vu, email, "
                + "so_dien_thoai, ma_phong_ban, ngay_vao_lam) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, n);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void update(NhanVien n) {
        String sql = "UPDATE nhan_vien SET ma_nv_code=?, ho_ten=?, gioi_tinh=?, ngay_sinh=?, cmnd_cccd=?, chuc_vu=?, "
                + "email=?, so_dien_thoai=?, ma_phong_ban=?, ngay_vao_lam=? WHERE ma_nhan_vien=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, n);
            ps.setInt(11, n.maNhanVien());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM nhan_vien WHERE ma_nhan_vien = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    private void bind(PreparedStatement ps, NhanVien n) throws SQLException {
        ps.setString(1, n.maCode());
        ps.setString(2, n.hoTen());
        ps.setString(3, n.gioiTinh());
        JdbcUtil.setDate(ps, 4, n.ngaySinh());
        ps.setString(5, n.cmndCccd());
        ps.setString(6, n.chucVu());
        ps.setString(7, n.email());
        ps.setString(8, n.soDienThoai());
        JdbcUtil.setInt(ps, 9, n.maPhongBan());
        JdbcUtil.setDate(ps, 10, n.ngayVaoLam());
        ps.setString(11, "");
    }

    private NhanVien map(ResultSet rs) throws SQLException {
        return new NhanVien(rs.getInt("ma_nhan_vien"), rs.getString("ma_nv_code"), rs.getString("ho_ten"),
                rs.getString("gioi_tinh"), JdbcUtil.getDate(rs, "ngay_sinh"), rs.getString("cmnd_cccd"),
                rs.getString("chuc_vu"), rs.getString("email"), rs.getString("so_dien_thoai"),
                JdbcUtil.getInt(rs, "ma_phong_ban"), rs.getString("ten_phong_ban"),
                JdbcUtil.getDate(rs, "ngay_vao_lam"));
    }
}
