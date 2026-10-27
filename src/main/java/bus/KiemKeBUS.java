package com.qlts.bus;

import com.qlts.common.Session;
import com.qlts.dao.KiemKeDAO;
import com.qlts.dao.NhatKyDAO;
import com.qlts.entity.KiemKe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KiemKeBUS {
    private final KiemKeDAO dao = new KiemKeDAO();
    private final NhatKyDAO nhatKy = new NhatKyDAO();

    public List<KiemKe> search(String kw) { return dao.search(kw); }

    public void ghiNhan(String dot, Integer maTs, Integer maTb, Integer maPb, LocalDate ngay,
                        String tinhTrang, int thucTe, int soSach, String ketQua, String ghiChu) {
        Session.requireModify();
        String code = "KK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmssSSS"));
        int chenhLech = thucTe - soSach;
        
        KiemKe k = new KiemKe(null, code, dot, maTs, maTb, maPb, ngay, tinhTrang, thucTe, soSach, chenhLech, Session.userId(), ketQua, ghiChu, null, null);
        dao.insert(k);
        nhatKy.ghi("KIỂM KÊ", "KIEM_KE", code, "Ghi nhận kiểm kê đợt " + dot);
    }
}