package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ThietBi(
        Integer maThietBi, String maCode, String ten, Integer soLuong,
        Integer maLoai, String tenLoai, Integer maNcc,
        String hangSanXuat, String model, String soSerial, String cauHinh,
        LocalDate ngayMua, LocalDate ngayHetBh,
        BigDecimal giaMua, BigDecimal giaTriHienTai,
        String tinhTrang, String trangThai, String viTri, String ghiChu,
        String hinhAnh                                  // ← BẮT BUỘC
) {
    public int soLuongSafe() {
        return soLuong == null ? 1 : soLuong;
    }
}