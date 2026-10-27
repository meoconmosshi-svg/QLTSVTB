package com.qlts.entity;

import java.time.LocalDateTime;

public record NhatKy(int maNhatKy, String nguoiDung, String hanhDong, String doiTuong,
                     String maDoiTuong, String noiDung, LocalDateTime thoiGian) {}
