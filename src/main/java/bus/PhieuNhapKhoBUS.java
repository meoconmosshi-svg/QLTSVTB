package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.PhieuNhapKhoDAO;
import com.qlts.entity.ChiTietPhieuNhap;
import com.qlts.entity.PhieuNhapKho;

import java.time.LocalDate;
import java.util.List;

public class PhieuNhapKhoBUS {
    private final PhieuNhapKhoDAO dao = new PhieuNhapKhoDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<PhieuNhapKho> search(String keyword, LocalDate tuNgay, LocalDate denNgay) {
        return dao.search(keyword, tuNgay, denNgay);
    }

    public List<ChiTietPhieuNhap> chiTiet(int maPhieuNhap) {
        return dao.chiTiet(maPhieuNhap);
    }

    public int taoPhieuNhap(Integer maNcc, String ghiChu,
                            List<ChiTietPhieuNhap> chiTietList) {
        if (!Session.canManageWarehouse())
            throw new BusinessException("Chỉ Admin được nhập kho.");
        if (maNcc == null)
            throw new BusinessException("Vui lòng chọn nhà cung cấp.");
        if (chiTietList == null || chiTietList.isEmpty())
            throw new BusinessException("Phiếu nhập phải có ít nhất 1 mặt hàng.");

        for (ChiTietPhieuNhap ct : chiTietList) {
            if (ct.tenTaiSan() == null || ct.tenTaiSan().isBlank())
                throw new BusinessException("Tên tài sản không được để trống.");
            if (ct.soLuong() == null || ct.soLuong() < 1)
                throw new BusinessException("Số lượng phải từ 1 trở lên.");
            if (ct.donGia() != null && ct.donGia().signum() < 0)
                throw new BusinessException("Đơn giá không được âm.");
        }

        int maPn = dao.taoPhieuNhap(maNcc, ghiChu, Session.userId(), chiTietList);

        nhatKy.ghi("NHẬP KHO", "PHIEU_NHAP", String.valueOf(maPn),
                "Nhập kho " + chiTietList.size() + " mặt hàng từ NCC ID " + maNcc);
        return maPn;
    }
}