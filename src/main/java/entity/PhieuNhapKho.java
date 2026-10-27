package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PhieuNhapKho(
        Integer maPhieuNhap,
        String maPnCode,
        Integer maNcc,
        String tenNcc,
        LocalDate ngayNhap,
        Integer nguoiNhap,
        String tenNguoiNhap,
        BigDecimal tongTien,
        String ghiChu,
        int soMatHang) {
}