package com.qlts.entity;

import java.time.LocalDate;

public record DieuChuyen(
        Integer maDieuChuyen, String maDcCode,
        Integer maTaiSan, Integer maThietBi,
        Integer tuPhongBan, Integer denPhongBan,
        Integer tuNhanVien, Integer denNhanVien,
        LocalDate ngayDieuChuyen, String lyDo,
        Integer nguoiThucHien, String trangThai, String ghiChu,
        // Các trường join để hiển thị
        String tenDoiTuong, String tenTuPhongBan, String tenDenPhongBan,
        String tenTuNhanVien, String tenDenNhanVien) {
}