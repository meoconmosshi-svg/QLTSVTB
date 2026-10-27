package com.qlts.entity;
import java.time.LocalDate;

public record KiemKe(
        Integer maKiemKe, String maKkCode, String dotKiemKe,
        Integer maTaiSan, Integer maThietBi, Integer maPhongBan,
        LocalDate ngayKiemKe, String tinhTrangThucTe,
        Integer soLuongThucTe, Integer soLuongSoSach, Integer chenhLech,
        Integer nguoiKiemKe, String ketQua, String ghiChu,
        String tenDoiTuong, String tenPhongBan) {
}