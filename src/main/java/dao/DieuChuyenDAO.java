package com.qlts.dao;

import com.qlts.common.BusinessException;
import com.qlts.conn.DBConnection;
import com.qlts.entity.DieuChuyen;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DieuChuyenDAO {

    private static final String SELECT =
            "SELECT d.*, COALESCE(ts.ten_tai_san, tb.ten_thiet_bi) AS ten_doi_tuong, "
          + "  pb1.ten_phong_ban AS ten_tu_pb, pb2.ten_phong_ban AS ten_den_pb, "
          + "  nv1.ho_ten AS ten_tu_nv, nv2.ho_ten AS ten_den_nv "
          + "FROM dieu_chuyen d "
          + "LEFT JOIN tai_san   ts  ON d.ma_tai_san   = ts.ma_tai_san "
          + "LEFT JOIN thiet_bi  tb  ON d.ma_thiet_bi  = tb.ma_thiet_bi "
          + "LEFT JOIN phong_ban pb1 ON d.tu_phong_ban = pb1.ma_phong_ban "
          + "LEFT JOIN phong_ban pb2 ON d.den_phong_ban = pb2.ma_phong_ban "
          + "LEFT JOIN nhan_vien nv1 ON d.tu_nhan_vien = nv1.ma_nhan_vien "
          + "LEFT JOIN nhan_vien nv2 ON d.den_nhan_vien = nv2.ma_nhan_vien ";

    public List<DieuChuyen> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = SELECT + "WHERE d.ma_dc_code LIKE ? OR ts.ten_tai_san LIKE ? OR tb.ten_thiet_bi LIKE ? OR nv1.ho_ten LIKE ? OR nv2.ho_ten LIKE ? "
                + "ORDER BY d.ma_dieu_chuyen DESC";
        List<DieuChuyen> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    public int insert(DieuChuyen dc, int nguoiThucHien) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. Tìm phiếu cấp phát cũ đang active
                int oldCapPhatId = -1;
                String sqlFind = "SELECT TOP 1 ma_cap_phat FROM cap_phat WHERE trang_thai = N'Đang sử dụng' AND ";
                if (dc.maTaiSan() != null) sqlFind += "ma_tai_san = ?";
                else sqlFind += "ma_thiet_bi = ?";

                try (PreparedStatement ps = c.prepareStatement(sqlFind)) {
                    ps.setInt(1, dc.maTaiSan() != null ? dc.maTaiSan() : dc.maThietBi());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) oldCapPhatId = rs.getInt(1);
                    }
                }

                // 2. Đóng phiếu cấp phát cũ
                if (oldCapPhatId != -1) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE cap_phat SET ngay_tra_thuc_te = ?, tinh_trang_khi_tra = N'Chuyển giao', trang_thai = N'Đã trả' WHERE ma_cap_phat = ?")) {
                        JdbcUtil.setDate(ps, 1, dc.ngayDieuChuyen());
                        ps.setInt(2, oldCapPhatId);
                        ps.executeUpdate();
                    }
                }

                // 3. Tạo phiếu cấp phát mới cho người nhận
                String capPhatCode = "CP" + System.currentTimeMillis() % 1000000000L;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO cap_phat(ma_cp_code, ma_tai_san, ma_thiet_bi, ma_nhan_vien, ma_phong_ban, ngay_cap_phat, nguoi_cap_phat, trang_thai, ghi_chu) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?, N'Đang sử dụng', ?)")) {
                    ps.setString(1, capPhatCode);
                    JdbcUtil.setInt(ps, 2, dc.maTaiSan());
                    JdbcUtil.setInt(ps, 3, dc.maThietBi());
                    ps.setInt(4, dc.denNhanVien());
                    JdbcUtil.setInt(ps, 5, dc.denPhongBan());
                    JdbcUtil.setDate(ps, 6, dc.ngayDieuChuyen());
                    ps.setInt(7, nguoiThucHien);
                    ps.setString(8, "Sinh ra từ điều chuyển " + dc.maDcCode());
                    ps.executeUpdate();
                }

                // 4. Ghi log điều chuyển
                int maDc = -1;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dieu_chuyen(ma_dc_code, ma_tai_san, ma_thiet_bi, tu_phong_ban, den_phong_ban, tu_nhan_vien, den_nhan_vien, ngay_dieu_chuyen, ly_do, nguoi_thuc_hien, trang_thai, ghi_chu) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, N'Hoàn thành', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, dc.maDcCode());
                    JdbcUtil.setInt(ps, 2, dc.maTaiSan());
                    JdbcUtil.setInt(ps, 3, dc.maThietBi());
                    JdbcUtil.setInt(ps, 4, dc.tuPhongBan());
                    JdbcUtil.setInt(ps, 5, dc.denPhongBan());
                    JdbcUtil.setInt(ps, 6, dc.tuNhanVien());
                    ps.setInt(7, dc.denNhanVien());
                    JdbcUtil.setDate(ps, 8, dc.ngayDieuChuyen());
                    ps.setString(9, dc.lyDo());
                    ps.setInt(10, nguoiThucHien);
                    ps.setString(11, dc.ghiChu());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) maDc = keys.getInt(1);
                    }
                }

                c.commit();
                return maDc;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    private DieuChuyen map(ResultSet rs) throws SQLException {
        return new DieuChuyen(
                rs.getInt("ma_dieu_chuyen"), rs.getString("ma_dc_code"),
                JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"),
                JdbcUtil.getInt(rs, "tu_phong_ban"), JdbcUtil.getInt(rs, "den_phong_ban"),
                JdbcUtil.getInt(rs, "tu_nhan_vien"), JdbcUtil.getInt(rs, "den_nhan_vien"),
                JdbcUtil.getDate(rs, "ngay_dieu_chuyen"), rs.getString("ly_do"),
                JdbcUtil.getInt(rs, "nguoi_thuc_hien"), rs.getString("trang_thai"), rs.getString("ghi_chu"),
                rs.getString("ten_doi_tuong"), rs.getString("ten_tu_pb"), rs.getString("ten_den_pb"),
                rs.getString("ten_tu_nv"), rs.getString("ten_den_nv")
        );
    }
}