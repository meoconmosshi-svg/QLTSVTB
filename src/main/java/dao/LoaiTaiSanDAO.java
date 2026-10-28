package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiTaiSanDAO {
    public List<LoaiTaiSan> search(String kw) {
        String like = JdbcUtil.like(kw);
        String sql = "SELECT * FROM loai_tai_san WHERE ten_loai LIKE ? OR ma_loai_code LIKE ? ORDER BY ma_loai";
        List<LoaiTaiSan> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 🔥 Đã thêm tham số thứ 6 (phan_loai)
                    list.add(new LoaiTaiSan(
                            rs.getInt("ma_loai"), 
                            rs.getString("ten_loai"), 
                            rs.getString("ma_loai_code"), 
                            rs.getString("mo_ta"), 
                            rs.getBigDecimal("ty_le_khau_hao"),
                            rs.getString("phan_loai") // Cột này có sẵn trong DB của bạn
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    public void insert(LoaiTaiSan l) {
        String sql = "INSERT INTO loai_tai_san(ma_loai_code, ten_loai, mo_ta, ty_le_khau_hao, phan_loai) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.maCode());
            ps.setString(2, l.tenLoai());
            ps.setString(3, l.moTa());
            ps.setBigDecimal(4, l.tyLeKhauHao());
            ps.setString(5, l.phanLoai() == null ? "TAI_SAN" : l.phanLoai());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void update(LoaiTaiSan l) {
        String sql = "UPDATE loai_tai_san SET ma_loai_code=?, ten_loai=?, mo_ta=?, ty_le_khau_hao=?, phan_loai=? WHERE ma_loai=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, l.maCode());
            ps.setString(2, l.tenLoai());
            ps.setString(3, l.moTa());
            ps.setBigDecimal(4, l.tyLeKhauHao());
            ps.setString(5, l.phanLoai() == null ? "TAI_SAN" : l.phanLoai());
            ps.setInt(6, l.maLoai());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM loai_tai_san WHERE ma_loai = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }
}