package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.CauHinh;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CauHinhDAO {

    public List<CauHinh> findAll() {
        List<CauHinh> list = new ArrayList<>();
        String sql = "SELECT khoa, gia_tri, mo_ta FROM cau_hinh ORDER BY khoa";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new CauHinh(rs.getString("khoa"),
                                     rs.getString("gia_tri"),
                                     rs.getString("mo_ta")));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    public Map<String, String> loadAll() {
        Map<String, String> map = new HashMap<>();
        for (CauHinh c : findAll()) map.put(c.khoa(), c.giaTri());
        return map;
    }

    public String get(String khoa, String macDinh) {
        String sql = "SELECT gia_tri FROM cau_hinh WHERE khoa = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, khoa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : macDinh;
            }
        } catch (SQLException e) {
            return macDinh;
        }
    }

    /** Insert nếu chưa có, update nếu đã có (UPSERT). */
    public void set(String khoa, String giaTri) {
        String sql = "MERGE cau_hinh AS target "
                + "USING (SELECT ? AS khoa, ? AS gia_tri) AS src "
                + "ON target.khoa = src.khoa "
                + "WHEN MATCHED THEN UPDATE SET gia_tri = src.gia_tri, ngay_cap_nhat = GETDATE() "
                + "WHEN NOT MATCHED THEN INSERT (khoa, gia_tri) VALUES (src.khoa, src.gia_tri);";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, khoa);
            ps.setString(2, giaTri);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    public void setBatch(Map<String, String> values) {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String sql = "MERGE cau_hinh AS target "
                        + "USING (SELECT ? AS khoa, ? AS gia_tri) AS src "
                        + "ON target.khoa = src.khoa "
                        + "WHEN MATCHED THEN UPDATE SET gia_tri = src.gia_tri, ngay_cap_nhat = GETDATE() "
                        + "WHEN NOT MATCHED THEN INSERT (khoa, gia_tri) VALUES (src.khoa, src.gia_tri);";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for (Map.Entry<String, String> e : values.entrySet()) {
                        ps.setString(1, e.getKey());
                        ps.setString(2, e.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }
}