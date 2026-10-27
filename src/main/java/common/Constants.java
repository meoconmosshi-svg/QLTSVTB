package com.qlts.common;

public final class Constants {
    private Constants() {}

    // ============================================================
    // PHÂN QUYỀN — CHỈ CÒN 2 ROLE
    // ============================================================
    public static final int QUYEN_ADMIN      = 1;
    public static final int QUYEN_NHAN_VIEN  = 3;

    // ============================================================
    // TRẠNG THÁI TÀI SẢN / THIẾT BỊ
    // ============================================================
    public static final String TS_TRONG_KHO     = "Trong kho";
    public static final String TS_DANG_SU_DUNG  = "Đang sử dụng";
    public static final String TS_BAO_TRI       = "Bảo trì";
    public static final String TS_THANH_LY      = "Đã thanh lý";
    public static final String[] TRANG_THAI_TAI_SAN =
            {TS_TRONG_KHO, TS_DANG_SU_DUNG, TS_BAO_TRI, TS_THANH_LY};

    public static final String[] TINH_TRANG = {"Mới", "Tốt", "Cũ", "Hỏng"};
    public static final String[] GIOI_TINH  = {"Nam", "Nữ"};

    // ============================================================
    // CẤP PHÁT
    // ============================================================
    public static final String CP_DANG_SU_DUNG = "Đang sử dụng";
    public static final String CP_DA_TRA       = "Đã trả";

    // ============================================================
    // YÊU CẦU CẤP PHÁT
    // ============================================================
    public static final String YC_CHO_DUYET = "Chờ duyệt";
    public static final String YC_DA_DUYET  = "Đã duyệt";
    public static final String YC_TU_CHOI   = "Từ chối";
    public static final String YC_DA_CAP    = "Đã cấp";
    public static final String[] YC_TRANG_THAI =
            {YC_CHO_DUYET, YC_DA_DUYET, YC_TU_CHOI, YC_DA_CAP};

    // ============================================================
    // BẢO TRÌ
    // ============================================================
    public static final String BT_DANG_THUC_HIEN = "Đang thực hiện";
    public static final String BT_HOAN_THANH     = "Hoàn thành";
    public static final String[] BT_TRANG_THAI = {BT_DANG_THUC_HIEN, BT_HOAN_THANH};
    public static final String[] BT_LOAI = {"Định kỳ", "Đột xuất", "Sửa chữa"};

    // ============================================================
    // THANH LÝ
    // ============================================================
    public static final String TL_CHO_DUYET = "Chờ duyệt";
    public static final String TL_DA_DUYET  = "Đã duyệt";
    public static final String TL_TU_CHOI   = "Từ chối";
    public static final String[] TL_TRANG_THAI = {TL_CHO_DUYET, TL_DA_DUYET, TL_TU_CHOI};

    // ============================================================
    // BÁO CÁO SỰ CỐ
    // ============================================================
    public static final String SC_CHO_XU_LY  = "Chờ xử lý";
    public static final String SC_DANG_XU_LY = "Đang xử lý";
    public static final String SC_DA_XU_LY   = "Đã xử lý";
    public static final String[] SC_TRANG_THAI = {SC_CHO_XU_LY, SC_DANG_XU_LY, SC_DA_XU_LY};
    public static final String[] SC_MUC_DO = {"Thấp", "Trung bình", "Cao", "Khẩn cấp"};
}