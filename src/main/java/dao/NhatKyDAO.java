package com.qlts.dao;

import com.qlts.common.Session;
import com.qlts.conn.DBConnection;
import com.qlts.entity.NhatKy;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NhatKyDAO {

    /** Ghi nhật ký thao tác của người dùng hiện tại. Lỗi ghi log không làm hỏng thao tác chính. */
    public void ghi(String hanhDong, String doiTuong, String maDoiTuong, String noiDung) {
        String sql = "INSERT INTO nhat_ky(ma_tai_khoan, hanh_dong, doi_tuong, ma_doi_tuong, noi_dung) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            JdbcUtil.setInt(ps, 1, Session.userId());
            ps.setString(2, hanhDong);
            ps.setString(3, doiTuong);
            ps.setString(4, maDoiTuong);
            ps.setString(5, noiDung);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Không ghi được nhật ký: " + e.getMessage());
        }
    }

    public List<NhatKy> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = "SELECT TOP 500 n.ma_nhat_ky, t.ten_dang_nhap, n.hanh_dong, n.doi_tuong, n.ma_doi_tuong, "
                + "n.noi_dung, n.thoi_gian FROM nhat_ky n LEFT JOIN tai_khoan t ON n.ma_tai_khoan = t.ma_tai_khoan "
                + "WHERE n.hanh_dong LIKE ? OR n.doi_tuong LIKE ? OR n.noi_dung LIKE ? OR t.ten_dang_nhap LIKE ? "
                + "ORDER BY n.thoi_gian DESC";
        List<NhatKy> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("thoi_gian");
                    list.add(new NhatKy(rs.getInt("ma_nhat_ky"), rs.getString("ten_dang_nhap"),
                            rs.getString("hanh_dong"), rs.getString("doi_tuong"), rs.getString("ma_doi_tuong"),
                            rs.getString("noi_dung"), ts == null ? null : ts.toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }
}
