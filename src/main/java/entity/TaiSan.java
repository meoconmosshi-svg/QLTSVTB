package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaiSan(
        Integer maTaiSan, String maCode, String ten, Integer soLuong,
        Integer maLoai, String tenLoai, Integer maNcc, String soSerial,
        LocalDate ngayMua, LocalDate ngayHetBh,
        BigDecimal giaMua, BigDecimal giaTriHienTai,
        String tinhTrang, String trangThai, String viTri, String ghiChu,
        String hinhAnh                            // ← THÊM field mới
) {
    public int soLuongSafe() {
        return soLuong == null ? 1 : soLuong;
    }
}