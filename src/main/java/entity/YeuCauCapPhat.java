package com.qlts.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record YeuCauCapPhat(
        Integer maYeuCau,
        String maYcCode,
        Integer maTaiKhoan,
        String tenNguoiYeuCau,        // JOIN từ tai_khoan
        Integer maLoai,
        String tenLoai,               // JOIN từ loai_tai_san
        Integer soLuong,
        String lyDo,
        LocalDate ngayYeuCau,
        LocalDate ngayMongMuon,
        String loaiYeuCau,            // 'Mượn' hoặc 'Cấp phát'
        String trangThai,             // 'Chờ duyệt' / 'Đã duyệt' / 'Từ chối' / 'Đã cấp'
        Integer nguoiDuyet,
        String tenNguoiDuyet,
        LocalDateTime ngayDuyet,
        String lyDoTuChoi,
        String ghiChu,
        Integer maCapPhat
) {
    public boolean choDuyet()  { return "Chờ duyệt".equals(trangThai); }
    public boolean daDuyet()   { return "Đã duyệt".equals(trangThai); }
    public boolean tuChoi()    { return "Từ chối".equals(trangThai); }
    public boolean daCap()     { return "Đã cấp".equals(trangThai); }
}