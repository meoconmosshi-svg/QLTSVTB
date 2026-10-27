package com.qlts.bus;

import com.qlts.dao.DanhMucDAO;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.entity.NhaCungCap;
import com.qlts.entity.PhongBan;

import java.util.List;

public class DanhMucBUS {
    private final DanhMucDAO dao = new DanhMucDAO();

    public List<LoaiTaiSan> loaiTaiSan() { return dao.loaiTaiSan(); }
    public List<NhaCungCap> nhaCungCap() { return dao.nhaCungCap(); }
    public List<PhongBan> phongBan() { return dao.phongBan(); }
}
