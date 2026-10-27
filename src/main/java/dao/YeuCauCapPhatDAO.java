package com.qlts.dao;

import com.qlts.common.BusinessException;
import com.qlts.conn.DBConnection;
import com.qlts.entity.YeuCauCapPhat;
import com.qlts.util.JdbcUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class YeuCauCapPhatDAO {

    private static final String SELECT =
            "SELECT y.*, tk.ho_ten AS ten_nguoi_yeu_cau, "
          + "       l.ten_loai, "
          + "       nd.ho_ten AS ten_nguoi_duyet "
          + "FROM yeu_cau_cap_phat y "
          + "LEFT JOIN tai_khoan tk    ON y.ma_tai_khoan = tk.ma_tai_khoan "
          + "LEFT JOIN loai_tai_san l  ON y.ma_loai = l.ma_loai "
          + "LEFT JOIN tai_khoan nd    ON y.nguoi_duyet = nd.ma_tai_khoan ";

    /** Danh sách yêu cầu theo filter. */
    public List<YeuCauCapPhat> search(String keyword, String trangThai, Integer maTaiKhoanFilter) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (y.ma_yc_code LIKE ? OR y.ly_do LIKE ? OR tk.ho_ten LIKE ?) ");
        }
        if (trangThai != null && !trangThai.isBlank() && !"Tất cả".equals(trangThai)) {
            sql.append("AND y.trang_thai = ? ");
        }
        if (maTaiKhoanFilter != null) {
            sql.append("AND y.ma_tai_khoan = ? ");
        }
        sql.append("ORDER BY y.ngay_yeu_cau DESC, y.ma_yeu_cau DESC");

        List<YeuCauCapPhat> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (trangThai != null && !trangThai.isBlank() && !"Tất cả".equals(trangThai)) {
                ps.setString(i++, trangThai);
            }
            if (maTaiKhoanFilter != null) ps.setInt(i++, maTaiKhoanFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
        return list;
    }

    /** Thêm yêu cầu mới. */
    public int insert(YeuCauCapPhat y) {
        String sql = "INSERT INTO yeu_cau_cap_phat(ma_yc_code, ma_tai_khoan, ma_loai, so_luong, "
                   + "ly_do, ngay_mong_muon, loai_yeu_cau, trang_thai, ghi_chu) "
                   + "VALUES (?,?,?,?,?,?,?,N'Chờ duyệt',?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, y.maYcCode());
            ps.setInt(2, y.maTaiKhoan());
            JdbcUtil.setInt(ps, 3, y.maLoai());
            ps.setInt(4, y.soLuong() == null ? 1 : y.soLuong());
            ps.setString(5, y.lyDo());
            JdbcUtil.setDate(ps, 6, y.ngayMongMuon());
            ps.setString(7, y.loaiYeuCau() == null ? "Mượn" : y.loaiYeuCau());
            ps.setString(8, y.ghiChu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Duyệt yêu cầu. */
    public void duyet(int maYeuCau, int nguoiDuyet) {
        String sql = "UPDATE yeu_cau_cap_phat SET trang_thai = N'Đã duyệt', "
                   + "nguoi_duyet = ?, ngay_duyet = GETDATE(), ly_do_tu_choi = NULL "
                   + "WHERE ma_yeu_cau = ? AND trang_thai = N'Chờ duyệt'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nguoiDuyet);
            ps.setInt(2, maYeuCau);
            if (ps.executeUpdate() == 0) {
                throw new BusinessException("Yêu cầu đã được xử lý hoặc không tồn tại.");
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Từ chối yêu cầu. */
    public void tuChoi(int maYeuCau, int nguoiDuyet, String lyDo) {
        String sql = "UPDATE yeu_cau_cap_phat SET trang_thai = N'Từ chối', "
                   + "nguoi_duyet = ?, ngay_duyet = GETDATE(), ly_do_tu_choi = ? "
                   + "WHERE ma_yeu_cau = ? AND trang_thai = N'Chờ duyệt'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nguoiDuyet);
            ps.setString(2, lyDo == null ? "Không đáp ứng điều kiện" : lyDo.trim());
            ps.setInt(3, maYeuCau);
            if (ps.executeUpdate() == 0) {
                throw new BusinessException("Yêu cầu đã được xử lý hoặc không tồn tại.");
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Đánh dấu đã cấp phát (khi tạo phiếu cấp phát thực tế). */
    public void danhDauDaCap(int maYeuCau, int maCapPhat) {
        String sql = "UPDATE yeu_cau_cap_phat SET trang_thai = N'Đã cấp', ma_cap_phat = ? "
                   + "WHERE ma_yeu_cau = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maCapPhat);
            ps.setInt(2, maYeuCau);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Xóa yêu cầu (chỉ khi còn Chờ duyệt). */
    public void delete(int maYeuCau, int maTaiKhoanChuSoHuu) {
        String sql = "DELETE FROM yeu_cau_cap_phat WHERE ma_yeu_cau = ? "
                   + "AND ma_tai_khoan = ? AND trang_thai = N'Chờ duyệt'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            ps.setInt(2, maTaiKhoanChuSoHuu);
            if (ps.executeUpdate() == 0) {
                throw new BusinessException("Chỉ có thể xóa yêu cầu đang chờ duyệt của chính bạn.");
            }
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    /** Đếm số yêu cầu chờ duyệt (dùng cho badge). */
    public int countChoDuyet() {
        String sql = "SELECT COUNT(*) FROM yeu_cau_cap_phat WHERE trang_thai = N'Chờ duyệt'";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw JdbcUtil.translate(e);
        }
    }

    private YeuCauCapPhat map(ResultSet rs) throws SQLException {
        Timestamp ngayDuyet = rs.getTimestamp("ngay_duyet");
        return new YeuCauCapPhat(
                rs.getInt("ma_yeu_cau"),
                rs.getString("ma_yc_code"),
                rs.getInt("ma_tai_khoan"),
                rs.getString("ten_nguoi_yeu_cau"),
                JdbcUtil.getInt(rs, "ma_loai"),
                rs.getString("ten_loai"),
                rs.getInt("so_luong"),
                rs.getString("ly_do"),
                JdbcUtil.getDate(rs, "ngay_yeu_cau"),
                JdbcUtil.getDate(rs, "ngay_mong_muon"),
                rs.getString("loai_yeu_cau"),
                rs.getString("trang_thai"),
                JdbcUtil.getInt(rs, "nguoi_duyet"),
                rs.getString("ten_nguoi_duyet"),
                ngayDuyet == null ? null : ngayDuyet.toLocalDateTime(),
                rs.getString("ly_do_tu_choi"),
                rs.getString("ghi_chu"),
                JdbcUtil.getInt(rs, "ma_cap_phat"));
    }
}