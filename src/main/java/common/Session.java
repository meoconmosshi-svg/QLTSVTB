package com.qlts.common;

import com.qlts.entity.TaiKhoan;

public final class Session {
    private static TaiKhoan current;
    private Session() {}

    public static void login(TaiKhoan t) { current = t; }
    public static void logout() { current = null; }
    public static TaiKhoan current() { return current; }

    public static Integer userId() {
        return current == null ? null : current.maTaiKhoan();
    }

    // ============================================================
    // KIỂM TRA ROLE
    // ============================================================
    public static boolean isAdmin() {
        return current != null && current.maQuyen() == Constants.QUYEN_ADMIN;
    }

    public static boolean isNhanVien() {
        return current != null && current.maQuyen() == Constants.QUYEN_NHAN_VIEN;
    }

    // ============================================================
    // PHÂN QUYỀN — CHỈ ADMIN
    // ============================================================

    /** CHỈ ADMIN được thêm/sửa/xóa dữ liệu. Nhân viên chỉ xem + gửi yêu cầu. */
    public static boolean canModify() { return isAdmin(); }
    public static void requireModify() {
        if (!canModify())
            throw new BusinessException("Chỉ ADMIN được thực hiện thao tác này.");
    }

    /** CHỈ ADMIN được duyệt yêu cầu / thanh lý / bảo trì / sự cố. */
    public static boolean canApprove() { return isAdmin(); }
    public static void requireApprove() {
        if (!canApprove())
            throw new BusinessException("Chỉ ADMIN được duyệt yêu cầu.");
    }

    /** CHỈ ADMIN được quản lý kho (nhập, cấp phát, điều chuyển, thanh lý). */
    public static boolean canManageWarehouse() { return isAdmin(); }
    public static void requireManageWarehouse() {
        if (!canManageWarehouse())
            throw new BusinessException("Chỉ ADMIN được quản lý kho.");
    }

    /** CHỈ ADMIN được xem báo cáo & thống kê. */
    public static boolean canViewReports() { return isAdmin(); }
    public static void requireViewReports() {
        if (!canViewReports())
            throw new BusinessException("Chỉ ADMIN được xem báo cáo & thống kê.");
    }
}