package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.TaiKhoanDAO;
import com.qlts.entity.TaiKhoan;
import com.qlts.util.EmailUtil;
import com.qlts.util.OtpUtil;
import com.qlts.util.PasswordUtil;

public class AuthBUS {
    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    // ============================================================
    // ĐĂNG NHẬP / ĐĂNG XUẤT
    // ============================================================

    public TaiKhoan login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new BusinessException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        TaiKhoan tk = dao.findByLogin(username.trim(), PasswordUtil.sha256(password));
        if (tk == null) {
            throw new BusinessException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        Session.login(tk);
        nhatKy.ghi("ĐĂNG NHẬP", "TAI_KHOAN", String.valueOf(tk.maTaiKhoan()),
                tk.tenDangNhap() + " đăng nhập hệ thống");
        return tk;
    }

    public void logout() {
        TaiKhoan tk = Session.current();
        if (tk != null) {
            nhatKy.ghi("ĐĂNG XUẤT", "TAI_KHOAN", String.valueOf(tk.maTaiKhoan()),
                    tk.tenDangNhap() + " đăng xuất");
        }
        Session.logout();
    }

    // ============================================================
    // CẬP NHẬT HỒ SƠ CÁ NHÂN
    // ============================================================

    public void updateProfile(String hoTen, String email, String soDienThoai) {
        TaiKhoan tk = Session.current();
        if (tk == null) {
            throw new BusinessException("Chưa đăng nhập.");
        }
        if (hoTen == null || hoTen.isBlank()) {
            throw new BusinessException("Họ tên không được để trống.");
        }
        if (email != null && !email.isBlank()
                && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessException("Email không hợp lệ.");
        }

        String hoTenMoi = hoTen.trim();
        String emailMoi = (email == null || email.isBlank()) ? null : email.trim();
        String sdtMoi   = (soDienThoai == null || soDienThoai.isBlank()) ? null : soDienThoai.trim();

        dao.updateProfile(tk.maTaiKhoan(), hoTenMoi, emailMoi, sdtMoi);

        Session.login(new TaiKhoan(
                tk.maTaiKhoan(),
                tk.tenDangNhap(),
                hoTenMoi,
                emailMoi,
                tk.maQuyen()));

        nhatKy.ghi("CẬP NHẬT", "TAI_KHOAN",
                String.valueOf(tk.maTaiKhoan()), "Cập nhật hồ sơ cá nhân");
    }

    // ============================================================
    // ĐỔI MẬT KHẨU
    // ============================================================

    public void changePassword(String oldPwd, String newPwd, String confirmPwd) {
        TaiKhoan tk = Session.current();
        if (tk == null) {
            throw new BusinessException("Chưa đăng nhập.");
        }
        if (oldPwd == null || oldPwd.isEmpty()) {
            throw new BusinessException("Vui lòng nhập mật khẩu hiện tại.");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!newPwd.equals(confirmPwd)) {
            throw new BusinessException("Xác nhận mật khẩu không khớp.");
        }
        if (newPwd.equals(oldPwd)) {
            throw new BusinessException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }

        boolean ok = dao.changePassword(
                tk.maTaiKhoan(),
                PasswordUtil.sha256(oldPwd),
                PasswordUtil.sha256(newPwd));

        if (!ok) {
            throw new BusinessException("Mật khẩu hiện tại không đúng.");
        }

        nhatKy.ghi("ĐỔI MẬT KHẨU", "TAI_KHOAN",
                String.valueOf(tk.maTaiKhoan()), "Đổi mật khẩu thành công");
    }

    // ============================================================
    // QUÊN MẬT KHẨU — BƯỚC 1: GỬI OTP
    // ============================================================

    public String sendOtp(String username) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("Vui lòng nhập email hoặc số điện thoại.");
        }
        TaiKhoan tk = dao.findByUsername(username.trim());
        if (tk == null) {
            throw new BusinessException("Không tìm thấy tài khoản với email/số điện thoại này.");
        }

        String otp = OtpUtil.generate();
        dao.saveOtp(tk.maTaiKhoan(), otp, 5);

        boolean sent = false;
        String email = tk.email();
        if (email != null && !email.isBlank()) {
            String html = EmailUtil.otpTemplate(tk.hoTen(), otp);
            sent = EmailUtil.send(email, "[QLTS] Mã xác thực đặt lại mật khẩu", html);
        }

        nhatKy.ghi("QUÊN MẬT KHẨU", "TAI_KHOAN", tk.tenDangNhap(),
                "Yêu cầu OTP đặt lại mật khẩu, gửi email: " + sent);

        return sent ? null : otp;
    }

    // ============================================================
    // QUÊN MẬT KHẨU — BƯỚC 2: XÁC THỰC OTP + ĐẶT LẠI MẬT KHẨU
    // ============================================================

    public void resetPassword(String username, String otp,
                              String newPwd, String confirmPwd) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("Vui lòng nhập email hoặc số điện thoại.");
        }
        if (otp == null || otp.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã OTP.");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new BusinessException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        if (!newPwd.equals(confirmPwd)) {
            throw new BusinessException("Xác nhận mật khẩu không khớp.");
        }

        TaiKhoan tk = dao.findByUsername(username.trim());
        if (tk == null) {
            throw new BusinessException("Không tìm thấy tài khoản.");
        }
        if (!dao.verifyOtp(tk.maTaiKhoan(), otp.trim())) {
            throw new BusinessException("Mã OTP không đúng hoặc đã hết hạn.");
        }

        dao.resetPassword(tk.maTaiKhoan(), PasswordUtil.sha256(newPwd));

        nhatKy.ghi("ĐẶT LẠI MẬT KHẨU", "TAI_KHOAN", tk.tenDangNhap(),
                "Đặt lại mật khẩu thành công qua OTP");
    }

    // ❌ ĐÃ XÓA HOÀN TOÀN method register()
}