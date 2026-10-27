package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.CauHinhDAO;
import com.qlts.dao.NhatKyDAO;

import java.util.HashMap;
import java.util.Map;

public class CauHinhBUS {
    private final CauHinhDAO dao = new CauHinhDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    private static final Map<String, String> CACHE = new HashMap<>();

    public String get(String khoa, String macDinh) {
        return CACHE.computeIfAbsent(khoa, k -> dao.get(k, macDinh));
    }

    public String getCongTyTen()       { return get("cong_ty_ten",        "Công ty"); }
    public String getCongTyDiaChi()    { return get("cong_ty_dia_chi",    ""); }
    public String getCongTyDienThoai() { return get("cong_ty_dien_thoai", ""); }
    public String getCongTyEmail()     { return get("cong_ty_email",      ""); }
    public String getCongTyMaSoThue()  { return get("cong_ty_ma_so_thue", ""); }
    public int    getSoBanGhiTrang()   {
        try { return Integer.parseInt(get("so_ban_ghi_trang", "50")); }
        catch (NumberFormatException e) { return 50; }
    }
    public String getTheme()           { return get("theme", "light"); }

    /** Lưu cấu hình hệ thống (chỉ ADMIN). */
    public void luuHeThong(String ten, String diaChi, String dienThoai,
                           String email, String maSoThue, int soBanGhiTrang) {
        if (!Session.isAdmin()) {
            throw new BusinessException("Chỉ ADMIN được thay đổi cấu hình hệ thống.");
        }
        if (ten == null || ten.isBlank()) {
            throw new BusinessException("Tên công ty không được để trống.");
        }
        if (soBanGhiTrang < 10 || soBanGhiTrang > 500) {
            throw new BusinessException("Số bản ghi/trang phải từ 10 đến 500.");
        }
        Map<String, String> map = new HashMap<>();
        map.put("cong_ty_ten",        ten.trim());
        map.put("cong_ty_dia_chi",    diaChi == null ? "" : diaChi.trim());
        map.put("cong_ty_dien_thoai", dienThoai == null ? "" : dienThoai.trim());
        map.put("cong_ty_email",      email == null ? "" : email.trim());
        map.put("cong_ty_ma_so_thue", maSoThue == null ? "" : maSoThue.trim());
        map.put("so_ban_ghi_trang",   String.valueOf(soBanGhiTrang));

        dao.setBatch(map);
        CACHE.putAll(map);

        nhatKy.ghi("CẬP NHẬT", "CAU_HINH", "HE_THONG", "Cập nhật cấu hình hệ thống");
    }

    /** Lưu theme (mọi user). */
    public void luuTheme(String theme) {
        dao.set("theme", theme);
        CACHE.put("theme", theme);
    }

    /** Xóa cache (gọi khi cần reload). */
    public static void clearCache() { CACHE.clear(); }
}