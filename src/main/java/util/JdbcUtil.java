package com.qlts.util;

import com.qlts.common.BusinessException;
import com.qlts.common.DataAccessException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public final class JdbcUtil {
    private JdbcUtil() {}

    public static void setDate(PreparedStatement ps, int i, LocalDate d) throws SQLException {
        if (d == null) ps.setNull(i, Types.DATE); else ps.setDate(i, Date.valueOf(d));
    }

    public static void setInt(PreparedStatement ps, int i, Integer v) throws SQLException {
        if (v == null) ps.setNull(i, Types.INTEGER); else ps.setInt(i, v);
    }

    public static void setMoney(PreparedStatement ps, int i, BigDecimal v) throws SQLException {
        if (v == null) ps.setNull(i, Types.DECIMAL); else ps.setBigDecimal(i, v);
    }

    public static LocalDate getDate(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    public static Integer getInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    public static String like(String keyword) {
        return "%" + (keyword == null ? "" : keyword.trim()) + "%";
    }

    /** Chuyển SQLException thành lỗi dễ hiểu (mã lỗi SQL Server: 547 = FK, 2627/2601 = trùng khóa). */
    public static RuntimeException translate(SQLException e) {
        return switch (e.getErrorCode()) {
            case 547 -> new BusinessException("Không thể thực hiện: dữ liệu đang được sử dụng ở nơi khác.");
            case 2627, 2601 -> new BusinessException("Dữ liệu bị trùng (mã hoặc giá trị duy nhất đã tồn tại).");
            default -> new DataAccessException("Lỗi cơ sở dữ liệu: " + e.getMessage(), e);
        };
    }
}
