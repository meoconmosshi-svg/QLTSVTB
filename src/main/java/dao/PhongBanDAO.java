package com.qlts.dao;

import com.qlts.conn.DBConnection;
import com.qlts.entity.PhongBan;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongBanDAO {
    
    public List<PhongBan> search(String kw) {
        String like = JdbcUtil.like(kw);
        String sql = "SELECT pb.*, tk.ho_ten AS ten_quan_ly FROM phong_ban pb "
                   + "LEFT JOIN tai_khoan tk ON pb.ma_quan_ly = tk.ma_tai_khoan "
                   + "WHERE pb.ten_phong_ban LIKE ? OR pb.ma_phong_ban_code LIKE ? "
                   + "ORDER BY pb.ma_phong_ban";
        List<PhongBan> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PhongBan(
                        rs.getInt("ma_phong_ban"), rs.getString("ten_phong_ban"),
                        rs.getString("ma_phong_ban_code"), rs.getString("dia_diem"),
                        JdbcUtil.getInt(rs, "ma_quan_ly"), JdbcUtil.getDate(rs, "ngay_thanh_lap"),
                        rs.getString("ten_quan_ly")
                    ));
                }
            }
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
        return list;
    }

    public void insert(PhongBan pb) {
        String sql = "INSERT INTO phong_ban(ten_phong_ban, ma_phong_ban_code, dia_diem, ma_quan_ly, ngay_thanh_lap) "
                   + "VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pb.tenPhongBan());
            ps.setString(2, pb.maPhongBanCode());
            ps.setString(3, pb.diaDiem());
            JdbcUtil.setInt(ps, 4, pb.maQuanLy());
            JdbcUtil.setDate(ps, 5, pb.ngayThanhLap());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void update(PhongBan pb) {
        String sql = "UPDATE phong_ban SET ten_phong_ban=?, ma_phong_ban_code=?, dia_diem=?, ma_quan_ly=?, ngay_thanh_lap=? WHERE ma_phong_ban=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pb.tenPhongBan());
            ps.setString(2, pb.maPhongBanCode());
            ps.setString(3, pb.diaDiem());
            JdbcUtil.setInt(ps, 4, pb.maQuanLy());
            JdbcUtil.setDate(ps, 5, pb.ngayThanhLap());
            ps.setInt(6, pb.maPhongBan());
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM phong_ban WHERE ma_phong_ban = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.translate(e); }
    }
}