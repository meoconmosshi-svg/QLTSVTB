package com.qlts.util;

import com.qlts.common.BusinessException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Chuyển đổi dữ liệu nhập từ form (chuỗi) sang kiểu Java và ngược lại. */
public final class Parse {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private Parse() {}

    public static String require(String s, String field) {
        if (s == null || s.isBlank()) throw new BusinessException(field + " không được để trống.");
        return s.trim();
    }

    public static String text(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public static LocalDate date(String s, String field) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(field + " không hợp lệ (định dạng yyyy-MM-dd, ví dụ 2026-10-10).");
        }
    }

    public static BigDecimal money(String s, String field) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        BigDecimal v;
        try {
            v = new BigDecimal(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new BusinessException(field + " không hợp lệ.");
        }
        if (v.signum() < 0) throw new BusinessException(field + " không được âm.");
        return v;
    }

    public static String fmt(LocalDate d) { return d == null ? "" : d.toString(); }
    public static String fmt(LocalDateTime d) { return d == null ? "" : DATE_TIME.format(d); }
    public static String fmt(BigDecimal v) { return v == null ? "" : new DecimalFormat("#,##0").format(v); }
    public static String nz(String s) { return s == null ? "" : s; }
}
