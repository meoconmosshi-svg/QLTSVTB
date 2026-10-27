package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KhauHaoInfo(
        Integer id, // ma_tai_san hoặc ma_thiet_bi
        boolean isTaiSan,
        String maCode,
        String ten,
        BigDecimal nguyenGia,
        Integer thoiGianKhauHao,
        LocalDate ngayBatDau,
        BigDecimal giaTriConLai,
        String trangThai // Trong kho / Đang sử dụng / Đã thanh lý
) {
}