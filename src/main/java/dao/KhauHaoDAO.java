package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.KhauHao;
import com.qlts.entity.KhauHaoInfo;
import com.qlts.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhauHaoDAO {

    /** Lấy danh sách tài sản/thiết bị để cấu hình khấu hao */
    public List<KhauHaoInfo> getDanhSachCauHinh(String keyword) {
        List<KhauHaoInfo> list = new ArrayList<>();
        String like = JdbcUtil.like(keyword);

        // Query Tài sản
        String sqlTs = "SELECT ma_tai_san, ma_ts_code, ten_tai_san, nguyen_gia, thoi_gian_khau_hao, "
                     + "ngay_bat_dau_khau_hao, gia_tri_con_lai, trang_thai "
                     + "FROM tai_san WHERE (ten_tai_san LIKE ? OR ma_ts_code LIKE ?) "
                     + "AND trang_thai != N'Đã thanh lý'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sqlTs)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KhauHaoInfo(
                            rs.getInt("ma_tai_san"), true, rs.getString("ma_ts_code"), rs.getString("ten_tai_san"),
                            rs.getBigDecimal("nguyen_gia"), JdbcUtil.getInt(rs, "thoi_gian_khau_hao"),
                            JdbcUtil.getDate(rs, "ngay_bat_dau_khau_hao"), rs.getBigDecimal("gia_tri_con_lai"), rs.getString("trang_thai")
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }

        // Query Thiết bị
        String sqlTb = "SELECT ma_thiet_bi, ma_tb_code, ten_thiet_bi, nguyen_gia, thoi_gian_khau_hao, "
                     + "ngay_bat_dau_khau_hao, gia_tri_con_lai, trang_thai "
                     + "FROM thiet_bi WHERE (ten_thiet_bi LIKE ? OR ma_tb_code LIKE ?) "
                     + "AND trang_thai != N'Đã thanh lý'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sqlTb)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KhauHaoInfo(
                            rs.getInt("ma_thiet_bi"), false, rs.getString("ma_tb_code"), rs.getString("ten_thiet_bi"),
                            rs.getBigDecimal("nguyen_gia"), JdbcUtil.getInt(rs, "thoi_gian_khau_hao"),
                            JdbcUtil.getDate(rs, "ngay_bat_dau_khau_hao"), rs.getBigDecimal("gia_tri_con_lai"), rs.getString("trang_thai")
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }

        return list;
    }

    /** Cập nhật thông tin cấu hình khấu hao */
    public void updateCauHinh(boolean isTaiSan, int id, BigDecimal nguyenGia, Integer thoiGian, LocalDate ngayBatDau, BigDecimal giaTriConLai) {
        String table = isTaiSan ? "tai_san" : "thiet_bi";
        String idCol = isTaiSan ? "ma_tai_san" : "ma_thiet_bi";
        String sql = "UPDATE " + table + " SET nguyen_gia = ?, thoi_gian_khau_hao = ?, "
                   + "ngay_bat_dau_khau_hao = ?, gia_tri_con_lai = ? WHERE " + idCol + " = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, nguyenGia);
            JdbcUtil.setInt(ps, 2, thoiGian);
            JdbcUtil.setDate(ps, 3, ngayBatDau);
            ps.setBigDecimal(4, giaTriConLai);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    /** Lấy lịch sử khấu hao */
    public List<KhauHao> getLichSu() {
        String sql = "SELECT k.*, COALESCE(ts.ten_tai_san, tb.ten_thiet_bi) AS ten_doi_tuong "
                   + "FROM khau_hao k "
                   + "LEFT JOIN tai_san ts ON k.ma_tai_san = ts.ma_tai_san "
                   + "LEFT JOIN thiet_bi tb ON k.ma_thiet_bi = tb.ma_thiet_bi "
                   + "ORDER BY k.nam DESC, k.thang DESC, k.ma_khau_hao DESC";
        List<KhauHao> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhauHao(
                        rs.getInt("ma_khau_hao"), JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"),
                        rs.getInt("nam"), JdbcUtil.getInt(rs, "thang"), rs.getBigDecimal("gia_tri_dau_ky"),
                        rs.getBigDecimal("ty_le_khau_hao"), rs.getBigDecimal("so_tien_khau_hao"), rs.getBigDecimal("gia_tri_cuoi_ky"),
                        JdbcUtil.getDate(rs, "ngay_tinh"), rs.getString("ghi_chu"), rs.getString("ten_doi_tuong")
                ));
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    /** Kiểm tra xem kỳ này đã được tính cho đối tượng chưa */
    public boolean daTinhKy(int id, boolean isTaiSan, int nam, int thang) {
        String col = isTaiSan ? "ma_tai_san" : "ma_thiet_bi";
        String sql = "SELECT 1 FROM khau_hao WHERE " + col + " = ? AND nam = ? AND thang = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setInt(2, nam); ps.setInt(3, thang);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    /** Lưu lịch sử khấu hao và cập nhật giá trị còn lại */
    public void luuKhauHao(int id, boolean isTaiSan, int nam, int thang, BigDecimal giaTriDau, BigDecimal tyLe, BigDecimal soTien, BigDecimal giaTriCuoi) {
        String sqlInsert = "INSERT INTO khau_hao(ma_tai_san, ma_thiet_bi, nam, thang, gia_tri_dau_ky, ty_le_khau_hao, so_tien_khau_hao, gia_tri_cuoi_ky, ngay_tinh) "
                         + "VALUES (?,?,?,?,?,?,?,?,CAST(GETDATE() AS DATE))";
        String table = isTaiSan ? "tai_san" : "thiet_bi";
        String idCol = isTaiSan ? "ma_tai_san" : "ma_thiet_bi";
        String sqlUpdate = "UPDATE " + table + " SET gia_tri_con_lai = ? WHERE " + idCol + " = ?";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlInsert)) {
                    JdbcUtil.setInt(ps, 1, isTaiSan ? id : null);
                    JdbcUtil.setInt(ps, 2, isTaiSan ? null : id);
                    ps.setInt(3, nam); ps.setInt(4, thang);
                    ps.setBigDecimal(5, giaTriDau); ps.setBigDecimal(6, tyLe);
                    ps.setBigDecimal(7, soTien); ps.setBigDecimal(8, giaTriCuoi);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(sqlUpdate)) {
                    ps.setBigDecimal(1, giaTriCuoi);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }
}