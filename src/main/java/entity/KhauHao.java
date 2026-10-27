package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KhauHao(
        Integer maKhauHao, 
        Integer maTaiSan, 
        Integer maThietBi,
        Integer nam, 
        Integer thang,
        BigDecimal giaTriDauKy, 
        BigDecimal tyLeKhauHao,
        BigDecimal soTienKhauHao, 
        BigDecimal giaTriCuoiKy,
        LocalDate ngayTinh, 
        String ghiChu,
        String tenDoiTuong // Trường hiển thị
) {
}