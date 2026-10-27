package com.qlts.dao;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.conn.DBConnection;
import com.qlts.entity.ThanhLy;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThanhLyDAO {
    private static final String SELECT = 
            "SELECT t.*, COALESCE(ts.ten_tai_san, tb.ten_thiet_bi) AS ten_doi_tuong, tk.ho_ten AS ten_nguoi_duyet "
          + "FROM thanh_ly t "
          + "LEFT JOIN tai_san ts ON t.ma_tai_san = ts.ma_tai_san "
          + "LEFT JOIN thiet_bi tb ON t.ma_thiet_bi = tb.ma_thiet_bi "
          + "LEFT JOIN tai_khoan tk ON t.nguoi_duyet = tk.ma_tai_khoan ";

    public List<ThanhLy> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = SELECT + "WHERE t.ma_tl_code LIKE ? OR t.ly_do LIKE ? ORDER BY t.ma_thanh_ly DESC";
        List<ThanhLy> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ThanhLy(
                        rs.getInt("ma_thanh_ly"), rs.getString("ma_tl_code"),
                        JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"),
                        JdbcUtil.getDate(rs, "ngay_thanh_ly"), rs.getString("ly_do"),
                        rs.getBigDecimal("gia_tri_thu_hoi"), JdbcUtil.getInt(rs, "nguoi_duyet"),
                        rs.getString("trang_thai"), rs.getString("ghi_chu"),
                        rs.getString("ten_doi_tuong"), rs.getString("ten_nguoi_duyet")
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    public void insert(ThanhLy t) {
        String sql = "INSERT INTO thanh_ly(ma_tl_code, ma_tai_san, ma_thiet_bi, ngay_thanh_ly, ly_do, gia_tri_thu_hoi, trang_thai) "
                   + "VALUES (?,?,?,?,?,?,N'Chờ duyệt')";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.maTlCode());
            JdbcUtil.setInt(ps, 2, t.maTaiSan());
            JdbcUtil.setInt(ps, 3, t.maThietBi());
            JdbcUtil.setDate(ps, 4, t.ngayThanhLy());
            ps.setString(5, t.lyDo());
            ps.setBigDecimal(6, t.giaTriThuHoi());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void duyet(int maThanhLy, int nguoiDuyet) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer maTs = null, maTb = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT ma_tai_san, ma_thiet_bi FROM thanh_ly WHERE ma_thanh_ly = ?")) {
                    ps.setInt(1, maThanhLy);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maTs = JdbcUtil.getInt(rs, "ma_tai_san");
                            maTb = JdbcUtil.getInt(rs, "ma_thiet_bi");
                        }
                    }
                }
                
                try (PreparedStatement ps = c.prepareStatement("UPDATE thanh_ly SET trang_thai = N'Đã duyệt', nguoi_duyet = ? WHERE ma_thanh_ly = ?")) {
                    ps.setInt(1, nguoiDuyet);
                    ps.setInt(2, maThanhLy);
                    ps.executeUpdate();
                }

                if (maTs != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE tai_san SET trang_thai = ? WHERE ma_tai_san = ?")) {
                        ps.setString(1, Constants.TS_THANH_LY);
                        ps.setInt(2, maTs);
                        ps.executeUpdate();
                    }
                } else if (maTb != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE thiet_bi SET trang_thai = ? WHERE ma_thiet_bi = ?")) {
                        ps.setString(1, Constants.TS_THANH_LY);
                        ps.setInt(2, maTb);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void tuChoi(int maThanhLy, int nguoiDuyet, String lyDoTuChoi) {
        String sql = "UPDATE thanh_ly SET trang_thai = N'Từ chối', nguoi_duyet = ?, ghi_chu = ? WHERE ma_thanh_ly = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nguoiDuyet);
            ps.setString(2, lyDoTuChoi);
            ps.setInt(3, maThanhLy);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void delete(int maThanhLy) {
        String sql = "DELETE FROM thanh_ly WHERE ma_thanh_ly = ? AND trang_thai = N'Chờ duyệt'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maThanhLy);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }
}