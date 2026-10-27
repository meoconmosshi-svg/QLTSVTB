package com.qlts.entity;

import java.time.LocalDate;

public record NhanVien(Integer maNhanVien, String maCode, String hoTen, String gioiTinh, LocalDate ngaySinh,
                       String cmndCccd, String chucVu, String email, String soDienThoai, Integer maPhongBan,
                       String tenPhongBan, LocalDate ngayVaoLam) {
    @Override public String toString() { return maCode + " - " + hoTen; }
}
