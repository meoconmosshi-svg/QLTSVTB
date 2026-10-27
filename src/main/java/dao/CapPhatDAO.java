package com.qlts.dao;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.conn.DBConnection;
import com.qlts.entity.CapPhat;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CapPhatDAO {

    public List<CapPhat> search(String keyword) {
        String like = JdbcUtil.like(keyword);
        String sql = "SELECT c.*, nv.ho_ten, ts.ten_tai_san, tb.ten_thiet_bi FROM cap_phat c "
                + "JOIN nhan_vien nv ON c.ma_nhan_vien = nv.ma_nhan_vien "
                + "LEFT JOIN tai_san ts ON c.ma_tai_san = ts.ma_tai_san "
                + "LEFT JOIN thiet_bi tb ON c.ma_thiet_bi = tb.ma_thiet_bi "
                + "WHERE c.ma_cp_code LIKE ? OR nv.ho_ten LIKE ? OR ts.ten_tai_san LIKE ? OR tb.ten_thiet_bi LIKE ? "
                + "ORDER BY c.ma_cap_phat DESC";
        List<CapPhat> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String doiTuong = rs.getString("ten_tai_san") != null
                            ? rs.getString("ten_tai_san") : rs.getString("ten_thiet_bi");
                    list.add(new CapPhat(rs.getInt("ma_cap_phat"), rs.getString("ma_cp_code"),
                            JdbcUtil.getInt(rs, "ma_tai_san"), JdbcUtil.getInt(rs, "ma_thiet_bi"), doiTuong,
                            rs.getInt("ma_nhan_vien"), rs.getString("ho_ten"),
                            JdbcUtil.getDate(rs, "ngay_cap_phat"), JdbcUtil.getDate(rs, "ngay_du_kien_tra"),
                            JdbcUtil.getDate(rs, "ngay_tra_thuc_te"), rs.getString("tinh_trang_khi_tra"),
                            rs.getString("trang_thai"), rs.getString("ghi_chu")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    /** Cấp phát: đánh dấu tài sản/thiết bị "Đang sử dụng" và tạo phiếu - trong cùng một transaction. */
    public void capPhat(CapPhat cp, Integer nguoiCapPhat) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int updated = setAssetStatus(c, cp.maTaiSan(), cp.maThietBi(),
                        Constants.TS_DANG_SU_DUNG, null, Constants.TS_TRONG_KHO);
                if (updated != 1) {
                    throw new BusinessException("Tài sản/thiết bị này không còn ở trạng thái \"Trong kho\".");
                }
                String sql = "INSERT INTO cap_phat(ma_cp_code, ma_tai_san, ma_thiet_bi, ma_nhan_vien, ma_phong_ban, "
                        + "ngay_cap_phat, ngay_du_kien_tra, nguoi_cap_phat, trang_thai, ghi_chu) "
                        + "SELECT ?, ?, ?, nv.ma_nhan_vien, nv.ma_phong_ban, ?, ?, ?, ?, ? "
                        + "FROM nhan_vien nv WHERE nv.ma_nhan_vien = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, cp.maCode());
                    JdbcUtil.setInt(ps, 2, cp.maTaiSan());
                    JdbcUtil.setInt(ps, 3, cp.maThietBi());
                    JdbcUtil.setDate(ps, 4, cp.ngayCapPhat());
                    JdbcUtil.setDate(ps, 5, cp.ngayDuKienTra());
                    JdbcUtil.setInt(ps, 6, nguoiCapPhat);
                    ps.setString(7, Constants.CP_DANG_SU_DUNG);
                    ps.setString(8, cp.ghiChu());
                    ps.setInt(9, cp.maNhanVien());
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Ghi nhận trả: đóng phiếu và đưa tài sản/thiết bị về kho - trong cùng một transaction. */
    public void tra(int maCapPhat, LocalDate ngayTra, String tinhTrang) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer maTs;
                Integer maTb;
                String trangThai;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT ma_tai_san, ma_thiet_bi, trang_thai FROM cap_phat WHERE ma_cap_phat = ?")) {
                    ps.setInt(1, maCapPhat);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new BusinessException("Không tìm thấy phiếu cấp phát.");
                        maTs = JdbcUtil.getInt(rs, "ma_tai_san");
                        maTb = JdbcUtil.getInt(rs, "ma_thiet_bi");
                        trangThai = rs.getString("trang_thai");
                    }
                }
                if (Constants.CP_DA_TRA.equals(trangThai)) {
                    throw new BusinessException("Phiếu này đã được ghi nhận trả.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE cap_phat SET ngay_tra_thuc_te = ?, tinh_trang_khi_tra = ?, trang_thai = ? "
                                + "WHERE ma_cap_phat = ?")) {
                    JdbcUtil.setDate(ps, 1, ngayTra);
                    ps.setString(2, tinhTrang);
                    ps.setString(3, Constants.CP_DA_TRA);
                    ps.setInt(4, maCapPhat);
                    ps.executeUpdate();
                }
                setAssetStatus(c, maTs, maTb, Constants.TS_TRONG_KHO, tinhTrang, null);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /**
     * Cập nhật trạng thái (và tùy chọn tình trạng) của tài sản hoặc thiết bị.
     * Nếu requiredStatus != null thì chỉ cập nhật khi trạng thái hiện tại khớp (chống cấp phát trùng).
     */
    private int setAssetStatus(Connection c, Integer maTs, Integer maTb, String newStatus,
                               String tinhTrang, String requiredStatus) throws SQLException {
        boolean isTs = maTs != null;
        if (!isTs && maTb == null) return 0;
        String table = isTs ? "tai_san" : "thiet_bi";
        String idCol = isTs ? "ma_tai_san" : "ma_thiet_bi";
        String sql = "UPDATE " + table + " SET trang_thai = ?"
                + (tinhTrang != null ? ", tinh_trang = ?" : "")
                + " WHERE " + idCol + " = ?"
                + (requiredStatus != null ? " AND trang_thai = ?" : "");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, newStatus);
            if (tinhTrang != null) ps.setString(i++, tinhTrang);
            ps.setInt(i++, isTs ? maTs : maTb);
            if (requiredStatus != null) ps.setString(i, requiredStatus);
            return ps.executeUpdate();
        }
    }
}
