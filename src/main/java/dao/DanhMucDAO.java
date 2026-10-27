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
        String sql = "SELECT pb.ma_phong_ban, pb.ten_phong_ban, pb.ma_phong_ban_code, "
                   + "pb.dia_diem, pb.ma_quan_ly, pb.ngay_thanh_lap, tk.ho_ten AS ten_quan_ly "
                   + "FROM phong_ban pb LEFT JOIN tai_khoan tk ON pb.ma_quan_ly = tk.ma_tai_khoan "
                   + "ORDER BY pb.ten_phong_ban";
        query(sql, rs -> list.add(new PhongBan(
                rs.getInt("ma_phong_ban"),
                rs.getString("ten_phong_ban"),
                rs.getString("ma_phong_ban_code"),
                rs.getString("dia_diem"),
                JdbcUtil.getInt(rs, "ma_quan_ly"),
                JdbcUtil.getDate(rs, "ngay_thanh_lap"),
                rs.getString("ten_quan_ly")
        )));
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