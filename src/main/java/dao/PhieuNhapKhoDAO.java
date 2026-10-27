package com.qlts.dao;

import com.qlts.common.BusinessException;
import com.qlts.conn.DBConnection;
import com.qlts.entity.ChiTietPhieuNhap;
import com.qlts.entity.PhieuNhapKho;
import com.qlts.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapKhoDAO {

    private static final String SELECT =
            "SELECT p.*, n.ten_ncc, t.ho_ten AS ten_nguoi_nhap, "
          + "  (SELECT COUNT(*) FROM chi_tiet_phieu_nhap c WHERE c.ma_phieu_nhap = p.ma_phieu_nhap) AS so_mat_hang "
          + "FROM phieu_nhap_kho p "
          + "LEFT JOIN nha_cung_cap n ON p.ma_ncc = n.ma_ncc "
          + "LEFT JOIN tai_khoan t    ON p.nguoi_nhap = t.ma_tai_khoan ";

    public List<PhieuNhapKho> search(String keyword, LocalDate tuNgay, LocalDate denNgay) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank())
            sql.append("AND (p.ma_pn_code LIKE ? OR n.ten_ncc LIKE ?) ");
        if (tuNgay != null) sql.append("AND p.ngay_nhap >= ? ");
        if (denNgay != null) sql.append("AND p.ngay_nhap <= ? ");
        sql.append("ORDER BY p.ngay_nhap DESC, p.ma_phieu_nhap DESC");

        List<PhieuNhapKho> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (tuNgay != null) JdbcUtil.setDate(ps, i++, tuNgay);
            if (denNgay != null) JdbcUtil.setDate(ps, i++, denNgay);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPn(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    public List<ChiTietPhieuNhap> chiTiet(int maPhieuNhap) {
        String sql = "SELECT c.*, l.ten_loai FROM chi_tiet_phieu_nhap c "
                   + "LEFT JOIN loai_tai_san l ON c.ma_loai = l.ma_loai "
                   + "WHERE c.ma_phieu_nhap = ? ORDER BY c.ma_ct_pn";
        List<ChiTietPhieuNhap> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maPhieuNhap);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCt(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    public int taoPhieuNhap(Integer maNcc, String ghiChu,
                            int nguoiNhap, List<ChiTietPhieuNhap> chiTietList) {
        if (chiTietList == null || chiTietList.isEmpty())
            throw new BusinessException("Phiếu nhập phải có ít nhất 1 mặt hàng.");

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String code = "PN" + System.currentTimeMillis() % 1000000000L;

                BigDecimal tong = chiTietList.stream()
                        .map(x -> x.thanhTien() == null ? BigDecimal.ZERO : x.thanhTien())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int maPn;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO phieu_nhap_kho(ma_pn_code, ma_ncc, "
                      + "ngay_nhap, nguoi_nhap, tong_tien, ghi_chu) "
                      + "VALUES (?,?,CAST(GETDATE() AS DATE),?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code);
                    JdbcUtil.setInt(ps, 2, maNcc);
                    ps.setInt(3, nguoiNhap);
                    ps.setBigDecimal(4, tong);
                    ps.setString(5, ghiChu);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new BusinessException("Không tạo được phiếu nhập.");
                        maPn = keys.getInt(1);
                    }
                }

                for (ChiTietPhieuNhap ct : chiTietList) {
                    Integer maTsExisting = null;
                    int slHienTai = 0;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT TOP 1 ma_tai_san, ISNULL(so_luong, 1) AS so_luong "
                          + "FROM tai_san "
                          + "WHERE ten_tai_san = ? "
                          + "  AND trang_thai = N'Trong kho' "
                          + "  AND ((ma_loai = ?) OR (ma_loai IS NULL AND ? IS NULL)) "
                          + "ORDER BY ma_tai_san")) {
                        ps.setString(1, ct.tenTaiSan());
                        JdbcUtil.setInt(ps, 2, ct.maLoai());
                        JdbcUtil.setInt(ps, 3, ct.maLoai());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                maTsExisting = rs.getInt("ma_tai_san");
                                slHienTai = rs.getInt("so_luong");
                            }
                        }
                    }

                    int maTs;
                    if (maTsExisting != null) {
                        maTs = maTsExisting;
                        int slThem = ct.soLuong() == null ? 1 : ct.soLuong();
                        int slMoi = slHienTai + slThem;

                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE tai_san SET so_luong = ?, "
                              + "gia_mua = ?, gia_tri_hien_tai = ?, "
                              + "tinh_trang = ISNULL(?, tinh_trang) "
                              + "WHERE ma_tai_san = ?")) {
                            ps.setInt(1, slMoi);
                            ps.setBigDecimal(2, ct.donGia());
                            ps.setBigDecimal(3, ct.donGia());
                            ps.setString(4, ct.tinhTrang());
                            ps.setInt(5, maTs);
                            ps.executeUpdate();
                        }
                    } else {
                        String maTsCode = "TS" + System.nanoTime() % 100000000L;

                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO tai_san(ma_ts_code, ten_tai_san, so_luong, ma_loai, ma_ncc, "
                              + "so_serial, ngay_mua, gia_mua, gia_tri_hien_tai, tinh_trang, "
                              + "trang_thai, vi_tri) "
                              + "VALUES (?,?,?,?,?,?,CAST(GETDATE() AS DATE),?,?,?,N'Trong kho',N'Kho nhập')",
                                Statement.RETURN_GENERATED_KEYS)) {
                            ps.setString(1, maTsCode);
                            ps.setString(2, ct.tenTaiSan());
                            ps.setInt(3, ct.soLuong() == null ? 1 : ct.soLuong());
                            JdbcUtil.setInt(ps, 4, ct.maLoai());
                            JdbcUtil.setInt(ps, 5, maNcc);
                            ps.setString(6, ct.soSerial());
                            ps.setBigDecimal(7, ct.donGia());
                            ps.setBigDecimal(8, ct.donGia());
                            ps.setString(9, ct.tinhTrang());
                            ps.executeUpdate();
                            try (ResultSet keys = ps.getGeneratedKeys()) {
                                maTs = keys.next() ? keys.getInt(1) : 0;
                            }
                        }
                    }

                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO chi_tiet_phieu_nhap(ma_phieu_nhap, ma_tai_san, "
                          + "ten_tai_san, ma_loai, so_serial, so_luong, don_gia, thanh_tien, tinh_trang) "
                          + "VALUES (?,?,?,?,?,?,?,?,?)")) {
                        ps.setInt(1, maPn);
                        JdbcUtil.setInt(ps, 2, maTs);
                        ps.setString(3, ct.tenTaiSan());
                        JdbcUtil.setInt(ps, 4, ct.maLoai());
                        ps.setString(5, ct.soSerial());
                        ps.setInt(6, ct.soLuong());
                        JdbcUtil.setMoney(ps, 7, ct.donGia());
                        JdbcUtil.setMoney(ps, 8, ct.thanhTien());
                        ps.setString(9, ct.tinhTrang());
                        ps.executeUpdate();
                    }
                }

                c.commit();
                return maPn;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    private PhieuNhapKho mapPn(ResultSet rs) throws SQLException {
        return new PhieuNhapKho(
                rs.getInt("ma_phieu_nhap"),
                rs.getString("ma_pn_code"),
                JdbcUtil.getInt(rs, "ma_ncc"),
                rs.getString("ten_ncc"),
                JdbcUtil.getDate(rs, "ngay_nhap"),
                rs.getInt("nguoi_nhap"),
                rs.getString("ten_nguoi_nhap"),
                rs.getBigDecimal("tong_tien"),
                rs.getString("ghi_chu"),
                rs.getInt("so_mat_hang"));
    }

    private ChiTietPhieuNhap mapCt(ResultSet rs) throws SQLException {
        return new ChiTietPhieuNhap(
                rs.getInt("ma_ct_pn"),
                rs.getInt("ma_phieu_nhap"),
                JdbcUtil.getInt(rs, "ma_tai_san"),
                rs.getString("ten_tai_san"),
                JdbcUtil.getInt(rs, "ma_loai"),
                rs.getString("ten_loai"),
                rs.getString("so_serial"),
                rs.getInt("so_luong"),
                rs.getBigDecimal("don_gia"),
                rs.getBigDecimal("thanh_tien"),
                rs.getString("tinh_trang"));
    }
}