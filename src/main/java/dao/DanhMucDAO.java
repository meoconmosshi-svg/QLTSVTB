package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.entity.NhaCungCap;
import com.qlts.entity.PhongBan;
import com.qlts.util.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Các danh mục dùng cho combobox: loại tài sản, nhà cung cấp, phòng ban. */
public class DanhMucDAO {

    public List<LoaiTaiSan> loaiTaiSan() {
        List<LoaiTaiSan> list = new ArrayList<>();
        query("SELECT ma_loai, ten_loai FROM loai_tai_san ORDER BY ten_loai",
                rs -> list.add(new LoaiTaiSan(rs.getInt(1), rs.getString(2))));
        return list;
    }

    public List<NhaCungCap> nhaCungCap() {
        List<NhaCungCap> list = new ArrayList<>();
        query("SELECT ma_ncc, ten_ncc FROM nha_cung_cap ORDER BY ten_ncc",
                rs -> list.add(new NhaCungCap(rs.getInt(1), rs.getString(2))));
        return list;
    }

    public List<PhongBan> phongBan() {
        List<PhongBan> list = new ArrayList<>();
        query("SELECT ma_phong_ban, ten_phong_ban FROM phong_ban ORDER BY ten_phong_ban",
                rs -> list.add(new PhongBan(rs.getInt(1), rs.getString(2))));
        return list;
    }

    private interface RowHandler { void handle(ResultSet rs) throws SQLException; }

    private void query(String sql, RowHandler handler) {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) handler.handle(rs);
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }
}
