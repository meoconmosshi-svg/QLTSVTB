package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BaoTri(
        Integer maBaoTri, String maBtCode,
        Integer maTaiSan, Integer maThietBi,
        String loaiBaoTri,
        LocalDate ngayBatDau, LocalDate ngayKetThuc,
        String noiDung, BigDecimal chiPhi,
        String donViThucHien, Integer nguoiPhuTrach,
        String ketQua, String trangThai,
        // Trường hiển thị
        String tenDoiTuong, String tenNguoiPhuTrach) {
}