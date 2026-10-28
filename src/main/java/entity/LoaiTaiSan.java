package com.qlts.entity;

import java.math.BigDecimal;

public record LoaiTaiSan(
        Integer maLoai,
        String tenLoai,
        String maCode,
        String moTa,
        BigDecimal tyLeKhauHao,
        String phanLoai // TAI_SAN hoặc THIET_BI
) {
    // 🔥 Constructor phụ (2 tham số) để tương thích với Combobox cũ
    public LoaiTaiSan(int maLoai, String tenLoai) {
        this(maLoai, tenLoai, null, null, BigDecimal.ZERO, "TAI_SAN");
    }

    // Hiển thị tên khi đưa vào JComboBox
    @Override
    public String toString() {
        return tenLoai;
    }
}