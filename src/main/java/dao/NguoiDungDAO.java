package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.TaiKhoan;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO {

    /** Record nội bộ chứa đầy đủ thông tin user (có cả trạng thái). */
    public record UserFull(int maTaiKhoan, String tenDangNhap, String hoTen,
                           String email, String soDienThoai, int maQuyen,
                           int trangThai, String lyDoKhoa) {
        public String tenQuyen() {
            return switch (maQuyen) {
                case 1 -> "Quản trị viên";             
                case 2 -> "Nhân viên";
                default -> "Không rõ";
            };
        }
        public String trangThaiText() { return trangThai == 1 ? "Hoạt động" : "Đã khóa"; }
    }

    /** Tìm kiếm user theo keyword + filter theo quyền + trạng thái. */
    public List<UserFull> search(String keyword, Integer maQuyenFilter, Integer trangThaiFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT ma_tai_khoan, ten_dang_nhap, ho_ten, email, so_dien_thoai, "
              + "ma_quyen, ISNULL(trang_thai, 1) AS trang_thai, ly_do_khoa "
              + "FROM tai_khoan WHERE 1=1 ");

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (ten_dang_nhap LIKE ? OR ho_ten LIKE ? OR email LIKE ? OR so_dien_thoai LIKE ?) ");
        }
        if (maQuyenFilter != null) sql.append("AND ma_quyen = ? ");
        if (trangThaiFilter != null) sql.append("AND ISNULL(trang_thai,1) = ? ");

        sql.append("ORDER BY ma_quyen, ma_tai_khoan");

        List<UserFull> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                for (int k = 0; k < 4; k++) ps.setString(i++, like);
            }
            if (maQuyenFilter != null) ps.setInt(i++, maQuyenFilter);
            if (trangThaiFilter != null) ps.setInt(i++, trangThaiFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserFull(
                            rs.getInt("ma_tai_khoan"),
                            rs.getString("ten_dang_nhap"),
                            rs.getString("ho_ten"),
                            rs.getString("email"),
                            rs.getString("so_dien_thoai"),
                            rs.getInt("ma_quyen"),
                            rs.getInt("trang_thai"),
                            rs.getString("ly_do_khoa")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    /** Đếm số admin đang hoạt động (để tránh khóa/xóa admin cuối cùng). */
    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM tai_khoan WHERE ma_quyen = 1 AND ISNULL(trang_thai,1) = 1";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Thêm user mới (dùng cho Admin tạo tài khoản). */
    public int insert(String username, String matKhauHash, String hoTen,
                      String email, String soDienThoai, int maQuyen) {
        String sql = "INSERT INTO tai_khoan(ten_dang_nhap, mat_khau, ho_ten, email, "
                   + "so_dien_thoai, ma_quyen, trang_thai) VALUES (?,?,?,?,?,?,1)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, matKhauHash);
            ps.setString(3, hoTen);
            ps.setString(4, email);
            ps.setString(5, soDienThoai);
            ps.setInt(6, maQuyen);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Cập nhật thông tin user (không đổi mật khẩu). */
    public void update(int maTaiKhoan, String hoTen, String email,
                       String soDienThoai, int maQuyen) {
        String sql = "UPDATE tai_khoan SET ho_ten = ?, email = ?, so_dien_thoai = ?, ma_quyen = ? "
                   + "WHERE ma_tai_khoan = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, soDienThoai);
            ps.setInt(4, maQuyen);
            ps.setInt(5, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Xóa user (không cho xóa chính mình — check ở BUS). */
    public void delete(int maTaiKhoan) {
        try (Connection c = DBConnection.getConnection()) {
            // Xóa nhật ký trước
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM nhat_ky WHERE ma_tai_khoan = ?")) {
                ps.setInt(1, maTaiKhoan);
                ps.executeUpdate();
            }
            // Xóa tài khoản
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM tai_khoan WHERE ma_tai_khoan = ?")) {
                ps.setInt(1, maTaiKhoan);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Khóa / mở khóa tài khoản. */
    public void setTrangThai(int maTaiKhoan, boolean hoatDong, String lyDoKhoa) {
        String sql = "UPDATE tai_khoan SET trang_thai = ?, ly_do_khoa = ?, ngay_khoa = ? "
                   + "WHERE ma_tai_khoan = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, hoatDong ? 1 : 0);
            ps.setString(2, hoatDong ? null : lyDoKhoa);
            ps.setTimestamp(3, hoatDong ? null : new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Reset mật khẩu về mặc định. */
    public void resetPassword(int maTaiKhoan, String newHash) {
        String sql = "UPDATE tai_khoan SET mat_khau = ?, otp_code = NULL, otp_expires = NULL "
                   + "WHERE ma_tai_khoan = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Kiểm tra trùng username/email/sđt (bỏ qua 1 id nếu là update). */
    public String findDuplicate(String value, Integer ignoreId) {
        String sql = "SELECT "
                   + "  CASE "
                   + "    WHEN EXISTS (SELECT 1 FROM tai_khoan WHERE ten_dang_nhap = ? AND ma_tai_khoan <> ?) THEN 'ten_dang_nhap' "
                   + "    WHEN EXISTS (SELECT 1 FROM tai_khoan WHERE email = ? AND ma_tai_khoan <> ?) THEN 'email' "
                   + "    WHEN EXISTS (SELECT 1 FROM tai_khoan WHERE so_dien_thoai = ? AND ma_tai_khoan <> ?) THEN 'so_dien_thoai' "
                   + "    ELSE NULL END AS field";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int ignore = (ignoreId == null) ? -1 : ignoreId;
            ps.setString(1, value); ps.setInt(2, ignore);
            ps.setString(3, value); ps.setInt(4, ignore);
            ps.setString(5, value); ps.setInt(6, ignore);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("field");
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return null;
    }
}