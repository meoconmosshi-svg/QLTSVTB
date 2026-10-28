package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.BaoTri;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaoTriDAO {
    private static final String SELECT =
            "SELECT b.*, COALESCE(ts.ten_tai_san, tb.ten_thiet_bi) AS ten_doi_tuong, "
          + "       tk.ho_ten AS ten_nguoi_phu_trach "
          + "FROM bao_tri b "
          + "LEFT JOIN tai_san  ts ON b.ma_tai_san  = ts.ma_tai_san "
          + "LEFT JOIN thiet_bi tb ON b.ma_thiet_bi = tb.ma_thiet_bi "
          + "LEFT JOIN tai_khoan tk ON b.nguoi_phu_trach = tk.ma_tai_khoan ";

    // 🔥 Thêm tham số maTaiKhoanFilter để lọc theo nhân viên
    public List<BaoTri> search(String keyword, Integer maTaiKhoanFilter) {
        String like = JdbcUtil.like(keyword);
        StringBuilder sql = new StringBuilder(SELECT)
                .append("WHERE (b.ma_bt_code LIKE ? OR b.noi_dung LIKE ? OR ts.ten_tai_san LIKE ? OR tb.ten_thiet_bi LIKE ?) ");
        
        if (maTaiKhoanFilter != null) {
            sql.append("AND b.nguoi_phu_trach = ? ");
        }
        sql.append("ORDER BY b.ma_bao_tri DESC");

        List<BaoTri> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            for (int k = 1; k <= 4; k++) ps.setString(i++, like);
            if (maTaiKhoanFilter != null) ps.setInt(i, maTaiKhoanFilter);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    public void insert(BaoTri b) {
        String sql = "INSERT INTO bao_tri(ma_bt_code, ma_tai_san, ma_thiet_bi, loai_bao_tri, "
                   + "ngay_bat_dau, noi_dung, chi_phi, don_vi_thuc_hien, nguoi_phu_trach, trang_thai) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.maBtCode());
            JdbcUtil.setInt(ps, 2, b.maTaiSan());
            JdbcUtil.setInt(ps, 3, b.maThietBi());
            ps.setString(4, b.loaiBaoTri());
            JdbcUtil.setDate(ps, 5, b.ngayBatDau());
            ps.setString(6, b.noiDung());
            ps.setBigDecimal(7, b.chiPhi() == null ? java.math.BigDecimal.ZERO : b.chiPhi());
            ps.setString(8, b.donViThucHien());
            JdbcUtil.setInt(ps, 9, b.nguoiPhuTrach());
            ps.setString(10, b.trangThai());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void updateTrangThai(int maBaoTri, String trangThai) {
        String sql = "UPDATE bao_tri SET trang_thai = ? WHERE ma_bao_tri = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maBaoTri);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void hoanThanh(int maBaoTri, java.time.LocalDate ngayKetThuc, String ketQua, java.math.BigDecimal chiPhi) {
        String sql = "UPDATE bao_tri SET ngay_ket_thuc = ?, ket_qua = ?, chi_phi = ?, trang_thai = N'Hoàn thành' WHERE ma_bao_tri = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            JdbcUtil.setDate(ps, 1, ngayKetThuc);
            ps.setString(2, ketQua);
            ps.setBigDecimal(3, chiPhi);
            ps.setInt(4, maBaoTri);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void delete(int maBaoTri) {
        try (Connection c = DBConnection.getConnection(); 
             PreparedStatement ps = c.prepareStatement("DELETE FROM bao_tri WHERE ma_bao_tri = ?")) {
            ps.setInt(1, maBaoTri);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    private BaoTri map(ResultSet rs) throws SQLException {
        return new BaoTri(
                rs.getInt("ma_bao_tri"), rs.getString("ma_bt_code"),
                JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"),
                rs.getString("loai_bao_tri"), JdbcUtil.getDate(rs, "ngay_bat_dau"),
                JdbcUtil.getDate(rs, "ngay_ket_thuc"), rs.getString("noi_dung"),
                rs.getBigDecimal("chi_phi"), rs.getString("don_vi_thuc_hien"),
                JdbcUtil.getInt(rs, "nguoi_phu_trach"), rs.getString("ket_qua"),
                rs.getString("trang_thai"), rs.getString("ten_doi_tuong"), rs.getString("ten_nguoi_phu_trach"));
    }
}