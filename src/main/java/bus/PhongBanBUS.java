package com.qlts.bus;

import com.qlts.common.BusinessException;
import com.qlts.common.Session;
import com.qlts.dao.NhatKyDAO;
import com.qlts.dao.PhongBanDAO;
import com.qlts.entity.PhongBan;

import java.util.List;

public class PhongBanBUS {
    private final PhongBanDAO dao = new PhongBanDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<PhongBan> search(String kw) { return dao.search(kw); }

    public void save(PhongBan pb) {
        Session.requireModify();
        if (pb.tenPhongBan() == null || pb.tenPhongBan().isBlank())
            throw new BusinessException("Tên phòng ban không được để trống.");
        
        if (pb.maPhongBan() == 0) {
            dao.insert(pb);
            nhatKy.ghi("THÊM", "PHONG_BAN", pb.maPhongBanCode(), "Thêm phòng ban " + pb.tenPhongBan());
        } else {
            dao.update(pb);
            nhatKy.ghi("SỬA", "PHONG_BAN", pb.maPhongBanCode(), "Sửa phòng ban " + pb.tenPhongBan());
        }
    }

    public void delete(PhongBan pb) {
        Session.requireModify();
        try {
            dao.delete(pb.maPhongBan());
            nhatKy.ghi("XÓA", "PHONG_BAN", pb.maPhongBanCode(), "Xóa phòng ban " + pb.tenPhongBan());
        } catch (Exception e) {
            throw new BusinessException("Không thể xóa phòng ban này vì đang có nhân viên hoặc tài sản trực thuộc.");
        }
    }
}