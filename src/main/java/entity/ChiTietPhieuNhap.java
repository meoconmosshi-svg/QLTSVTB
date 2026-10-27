package com.qlts.entity;

import java.math.BigDecimal;

public record ChiTietPhieuNhap(
        Integer maCtPn,
        Integer maPhieuNhap,
        Integer maTaiSan,
        String tenTaiSan,
        Integer maLoai,
        String tenLoai,
        String soSerial,
        Integer soLuong,
        BigDecimal donGia,
        BigDecimal thanhTien,
        String tinhTrang
) {}