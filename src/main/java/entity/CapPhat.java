package com.qlts.entity;

import java.time.LocalDate;

public record CapPhat(Integer maCapPhat, String maCode, Integer maTaiSan, Integer maThietBi,
                      String tenDoiTuong, Integer maNhanVien, String tenNhanVien,
                      LocalDate ngayCapPhat, LocalDate ngayDuKienTra, LocalDate ngayTraThucTe,
                      String tinhTrangKhiTra, String trangThai, String ghiChu) {}
//kkk
