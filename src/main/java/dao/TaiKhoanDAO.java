package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.TaiKhoan;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TaiKhoanDAO {

    // ============================================================
    // ĐĂNG NHẬP
    // ============================================================

    /** Trả về tài khoản nếu đúng tên đăng nhập + mật khẩu (đã băm) và đang hoạt động, ngược lại null. */
    public TaiKhoan findByLogin(String username, String passwordHash) {
        String sql = "SELECT ma_tai_khoan, ten_dang_nhap, ho_ten, email, ma_quyen FROM tai_khoan "
                + "WHERE ten_dang_nhap = ? AND mat_khau = ? AND trang_thai = 1";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new TaiKhoan(rs.getInt("ma_tai_khoan"), rs.getString("ten_dang_nhap"),
                        rs.getString("ho_ten"), rs.getString("email"), rs.getInt("ma_quyen"));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    // ============================================================
    // MODULE "CÀI ĐẶT"
    // ============================================================

    /** Cập nhật hồ sơ cá nhân (họ tên, email, SĐT). */
    public void updateProfile(int maTaiKhoan, String hoTen, String email, String soDienThoai) {
        String sql = "UPDATE tai_khoan SET ho_ten = ?, email = ?, so_dien_thoai = ? "
                   + "WHERE ma_tai_khoan = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hoTen);
            ps.setString(2, email);
            ps.setString(3, soDienThoai);
            ps.setInt(4, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Đổi mật khẩu. Trả về true nếu mật khẩu cũ (oldHash) khớp với DB. */
    public boolean changePassword(int maTaiKhoan, String oldHash, String newHash) {
        String sql = "UPDATE tai_khoan SET mat_khau = ? "
                   + "WHERE ma_tai_khoan = ? AND mat_khau = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, maTaiKhoan);
            ps.setString(3, oldHash);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Lấy đầy đủ thông tin user (dùng cho tab Cài đặt → Tài khoản). */
    public Map<String, String> findFullInfo(int maTaiKhoan) {
        String sql = "SELECT ten_dang_nhap, ho_ten, email, so_dien_thoai, ma_quyen "
                   + "FROM tai_khoan WHERE ma_tai_khoan = ?";
        Map<String, String> map = new HashMap<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    map.put("ten_dang_nhap", rs.getString("ten_dang_nhap"));
                    map.put("ho_ten",        rs.getString("ho_ten"));
                    map.put("email",         rs.getString("email") == null ? "" : rs.getString("email"));
                    map.put("so_dien_thoai", rs.getString("so_dien_thoai") == null ? "" : rs.getString("so_dien_thoai"));
                    map.put("ma_quyen",      String.valueOf(rs.getInt("ma_quyen")));
                }
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return map;
    }

    // ============================================================
    // MODULE "ĐĂNG KÝ"
    // ============================================================

    /** Kiểm tra username (email/SĐT) đã tồn tại chưa. */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM tai_khoan WHERE ten_dang_nhap = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Đăng ký tài khoản mới. Trả về mã tài khoản vừa tạo, hoặc -1 nếu lỗi. */
    public int insert(String username, String matKhauHash, String hoTen,
                      String email, String soDienThoai, int maQuyen) {
        String sql = "INSERT INTO tai_khoan (ten_dang_nhap, mat_khau, ho_ten, email, "
                   + "so_dien_thoai, ma_quyen, trang_thai) "
                   + "VALUES (?,?,?,?,?,?,1)";
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

    // ============================================================
    // MODULE "QUÊN MẬT KHẨU"
    // ============================================================

    /** Tìm tài khoản theo username (email/SĐT) — dùng cho quên mật khẩu. */
    public TaiKhoan findByUsername(String username) {
        String sql = "SELECT ma_tai_khoan, ten_dang_nhap, ho_ten, email, ma_quyen "
                   + "FROM tai_khoan WHERE ten_dang_nhap = ? AND trang_thai = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new TaiKhoan(rs.getInt("ma_tai_khoan"), rs.getString("ten_dang_nhap"),
                        rs.getString("ho_ten"), rs.getString("email"), rs.getInt("ma_quyen"));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Lưu OTP + thời hạn (phút) vào DB. */
    public void saveOtp(int maTaiKhoan, String otp, int minutesValid) {
        String sql = "UPDATE tai_khoan SET otp_code = ?, "
                   + "otp_expires = DATEADD(MINUTE, ?, GETDATE()) "
                   + "WHERE ma_tai_khoan = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setInt(2, minutesValid);
            ps.setInt(3, maTaiKhoan);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Xác thực OTP. Trả về true nếu đúng và chưa hết hạn. */
    public boolean verifyOtp(int maTaiKhoan, String otp) {
        String sql = "SELECT 1 FROM tai_khoan WHERE ma_tai_khoan = ? "
                   + "AND otp_code = ? AND otp_expires > GETDATE()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maTaiKhoan);
            ps.setString(2, otp);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Đặt lại mật khẩu mới và xóa OTP. */
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
}