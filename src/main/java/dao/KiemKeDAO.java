package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.KiemKe;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KiemKeDAO {
    private static final String SELECT =
            "SELECT k.*, COALESCE(ts.ten_tai_san, tb.ten_thiet_bi) AS ten_doi_tuong, pb.ten_phong_ban "
          + "FROM kiem_ke k "
          + "LEFT JOIN tai_san ts ON k.ma_tai_san = ts.ma_tai_san "
          + "LEFT JOIN thiet_bi tb ON k.ma_thiet_bi = tb.ma_thiet_bi "
          + "LEFT JOIN phong_ban pb ON k.ma_phong_ban = pb.ma_phong_ban ";

    public List<KiemKe> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = SELECT + "WHERE k.ma_kk_code LIKE ? OR k.dot_kiem_ke LIKE ? ORDER BY k.ma_kiem_ke DESC";
        List<KiemKe> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KiemKe(
                        rs.getInt("ma_kiem_ke"), rs.getString("ma_kk_code"), rs.getString("dot_kiem_ke"),
                        JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"), JdbcUtil.getInt(rs, "ma_phong_ban"),
                        JdbcUtil.getDate(rs, "ngay_kiem_ke"), rs.getString("tinh_trang_thuc_te"),
                        rs.getInt("so_luong_thuc_te"), rs.getInt("so_luong_so_sach"), rs.getInt("chenh_lech"),
                        JdbcUtil.getInt(rs, "nguoi_kiem_ke"), rs.getString("ket_qua"), rs.getString("ghi_chu"),
                        rs.getString("ten_doi_tuong"), rs.getString("ten_phong_ban")
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    public void insert(KiemKe k) {
        String sql = "INSERT INTO kiem_ke(ma_kk_code, dot_kiem_ke, ma_tai_san, ma_thiet_bi, ma_phong_ban, "
                   + "ngay_kiem_ke, tinh_trang_thuc_te, so_luong_thuc_te, so_luong_so_sach, chenh_lech, "
                   + "nguoi_kiem_ke, ket_qua, ghi_chu) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.maKkCode());
            ps.setString(2, k.dotKiemKe());
            JdbcUtil.setInt(ps, 3, k.maTaiSan());
            JdbcUtil.setInt(ps, 4, k.maThietBi());
            JdbcUtil.setInt(ps, 5, k.maPhongBan());
            JdbcUtil.setDate(ps, 6, k.ngayKiemKe());
            ps.setString(7, k.tinhTrangThucTe());
            ps.setInt(8, k.soLuongThucTe());
            ps.setInt(9, k.soLuongSoSach());
            ps.setInt(10, k.chenhLech());
            JdbcUtil.setInt(ps, 11, k.nguoiKiemKe());
            ps.setString(12, k.ketQua());
            ps.setString(13, k.ghiChu());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    // 🔥 THÊM HÀM XÓA NÀY
    public void delete(int maKiemKe) {
        try (Connection c = DBConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement("DELETE FROM kiem_ke WHERE ma_kiem_ke = ?")) {
            ps.setInt(1, maKiemKe);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void capNhatSoLuongTaiSan(int id, boolean isTaiSan, int soLuongMoi) {
        String table = isTaiSan ? "tai_san" : "thiet_bi";
        String idCol = isTaiSan ? "ma_tai_san" : "ma_thiet_bi";
        String sql = "UPDATE " + table + " SET so_luong = ? WHERE " + idCol + " = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, soLuongMoi);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }
}