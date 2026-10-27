package com.qlts.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ThanhLy(
        Integer maThanhLy, 
        String maTlCode,
        Integer maTaiSan, 
        Integer maThietBi,
        LocalDate ngayThanhLy, 
        String lyDo,
        BigDecimal giaTriThuHoi, 
        Integer nguoiDuyet,
        String trangThai, 
        String ghiChu,
        // Hai trường dưới đây dùng để JOIN dữ liệu hiển thị lên bảng
        String tenDoiTuong, 
        String tenNguoiDuyet) {
}