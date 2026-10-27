package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.dao.NguoiDungDAO;
import com.qlts.dao.NguoiDungDAO.UserFull;
import com.qlts.dao.NhatKyDAO;
import com.qlts.util.PasswordUtil;

import java.util.List;

public class NguoiDungBUS {
    private final NguoiDungDAO dao = new NguoiDungDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public static final String DEFAULT_PASSWORD = "123456";

    public List<UserFull> search(String keyword, Integer maQuyen, Integer trangThai) {
        requireAdmin();
        return dao.search(keyword, maQuyen, trangThai);
    }

    public void them(String username, String hoTen, String email,
                     String soDienThoai, int maQuyen, String matKhau) {
        requireAdmin();
        if (username == null || username.isBlank())
            throw new BusinessException("Tên đăng nhập không được để trống.");
        if (hoTen == null || hoTen.isBlank())
            throw new BusinessException("Họ tên không được để trống.");
        if (matKhau == null || matKhau.length() < 6)
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
        // CHỈ 2 ROLE
        if (maQuyen != Constants.QUYEN_ADMIN && maQuyen != Constants.QUYEN_NHAN_VIEN)
            throw new BusinessException("Quyền chỉ có thể là 1 (Admin) hoặc 3 (Nhân viên).");

        String u = username.trim();
        boolean isEmail = u.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        boolean isPhone = u.matches("^(0|\\+84)[0-9]{9,10}$");
        if (!isEmail && !isPhone && u.length() < 4) {
            throw new BusinessException("Tên đăng nhập phải ≥4 ký tự, hoặc là email/SĐT hợp lệ.");
        }

        String dup = dao.findDuplicate(u, null);
        if (dup != null) {
            throw new BusinessException(tenTruong(dup) + " \"" + u + "\" đã tồn tại.");
        }

        int newId = dao.insert(u, PasswordUtil.sha256(matKhau),
                hoTen.trim(), emptyToNull(email), emptyToNull(soDienThoai), maQuyen);
        if (newId <= 0) throw new BusinessException("Tạo tài khoản thất bại.");

        nhatKy.ghi("THÊM", "TAI_KHOAN", String.valueOf(newId),
                "Tạo tài khoản: " + u + " (" + roleName(maQuyen) + ")");
    }

    public void sua(int maTaiKhoan, String hoTen, String email,
                    String soDienThoai, int maQuyen) {
        requireAdmin();
        if (hoTen == null || hoTen.isBlank())
            throw new BusinessException("Họ tên không được để trống.");
        // CHỈ 2 ROLE
        if (maQuyen != Constants.QUYEN_ADMIN && maQuyen != Constants.QUYEN_NHAN_VIEN)
            throw new BusinessException("Quyền chỉ có thể là 1 (Admin) hoặc 3 (Nhân viên).");

        if (maTaiKhoan == Session.userId()
                && maQuyen != Session.current().maQuyen()
                && Session.current().maQuyen() == Constants.QUYEN_ADMIN
                && dao.countActiveAdmins() <= 1) {
            throw new BusinessException("Không thể đổi quyền của admin cuối cùng.");
        }

        String dupCheckValue = email != null && !email.isBlank() ? email.trim() : null;
        if (dupCheckValue != null) {
            String dup = dao.findDuplicate(dupCheckValue, maTaiKhoan);
            if (dup != null && !dup.equals("ten_dang_nhap")) {
                throw new BusinessException(tenTruong(dup) + " \"" + dupCheckValue + "\" đã bị dùng bởi tài khoản khác.");
            }
        }

        dao.update(maTaiKhoan, hoTen.trim(), emptyToNull(email),
                emptyToNull(soDienThoai), maQuyen);

        nhatKy.ghi("SỬA", "TAI_KHOAN", String.valueOf(maTaiKhoan),
                "Cập nhật: " + hoTen + " (" + roleName(maQuyen) + ")");
    }

    public void xoa(int maTaiKhoan, String tenDangNhap) {
        requireAdmin();
        if (maTaiKhoan == Session.userId())
            throw new BusinessException("Không thể xóa tài khoản đang đăng nhập.");
        if (dao.countActiveAdmins() <= 1 && isAdmin(maTaiKhoan)) {
            throw new BusinessException("Không thể xóa admin cuối cùng.");
        }
        dao.delete(maTaiKhoan);
        nhatKy.ghi("XÓA", "TAI_KHOAN", String.valueOf(maTaiKhoan),
                "Xóa tài khoản: " + tenDangNhap);
    }

    public void khoa(int maTaiKhoan, String lyDo) {
        requireAdmin();
        if (maTaiKhoan == Session.userId())
            throw new BusinessException("Không thể tự khóa chính mình.");
        if (isAdmin(maTaiKhoan) && dao.countActiveAdmins() <= 1)
            throw new BusinessException("Không thể khóa admin cuối cùng.");
        dao.setTrangThai(maTaiKhoan, false, lyDo == null ? "Vi phạm quy định" : lyDo.trim());
        nhatKy.ghi("KHÓA", "TAI_KHOAN", String.valueOf(maTaiKhoan),
                "Khóa tài khoản. Lý do: " + (lyDo == null ? "—" : lyDo));
    }

    public void moKhoa(int maTaiKhoan) {
        requireAdmin();
        dao.setTrangThai(maTaiKhoan, true, null);
        nhatKy.ghi("MỞ KHÓA", "TAI_KHOAN", String.valueOf(maTaiKhoan), "Mở khóa tài khoản");
    }

    public void resetMatKhau(int maTaiKhoan, String tenDangNhap) {
        requireAdmin();
        dao.resetPassword(maTaiKhoan, PasswordUtil.sha256(DEFAULT_PASSWORD));
        nhatKy.ghi("RESET MK", "TAI_KHOAN", String.valueOf(maTaiKhoan),
                "Reset mật khẩu về: " + DEFAULT_PASSWORD);
    }

    // =============== helpers ===============
    private void requireAdmin() {
        if (!Session.isAdmin())
            throw new BusinessException("Chỉ ADMIN được truy cập chức năng này.");
    }
    private boolean isAdmin(int maTaiKhoan) {
        return dao.search(null, Constants.QUYEN_ADMIN, null).stream()
                .anyMatch(u -> u.maTaiKhoan() == maTaiKhoan);
    }
    private String tenTruong(String field) {
        return switch (field) {
            case "ten_dang_nhap" -> "Tên đăng nhập";
            case "email" -> "Email";
            case "so_dien_thoai" -> "Số điện thoại";
            default -> "Trường";
        };
    }
    private String roleName(int maQuyen) {
        return switch (maQuyen) {
            case 1 -> "Quản trị viên";
            case 3 -> "Nhân viên";
            default -> "Không rõ";
        };
    }
    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}