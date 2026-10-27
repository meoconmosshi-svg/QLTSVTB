package com.qlts.entity;

import java.time.LocalDate;

public record PhongBan(
        int maPhongBan,
        String tenPhongBan,
        String maPhongBanCode,
        String diaDiem,
        Integer maQuanLy,
        LocalDate ngayThanhLap,
        String tenQuanLy // Dùng để hiển thị tên Trưởng phòng
) {
    // Constructor phụ dùng cho Combobox
    public PhongBan(int maPhongBan, String tenPhongBan) {
        this(maPhongBan, tenPhongBan, null, null, null, null, null);
    }

    @Override
    public String toString() { return tenPhongBan; }
}