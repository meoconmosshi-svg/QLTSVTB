package com.qlts.view;

import com.qlts.bus.KiemKeBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.entity.KiemKe;
import com.qlts.util.Parse;

import javax.swing.*;
import java.util.List;

public class KiemKePanel extends CrudPanel<KiemKe> {
    private final KiemKeBUS bus = new KiemKeBUS();

    public KiemKePanel() {
        super(new String[]{"Mã KK", "Đợt KK", "Đối tượng", "Phòng ban", "Ngày KK", "Sổ sách", "Thực tế", "Lệch", "Tình trạng"}, false);
        if (Session.canModify()) {
            JButton btnTao = new JButton("Ghi nhận KK");
            btnTao.addActionListener(e -> tao());
            addToolButton(btnTao);
        }
        refresh();
    }

    @Override protected List<KiemKe> load(String kw) { return bus.search(kw); }
    @Override protected Object[] toRow(KiemKe k) {
        return new Object[]{k.maKkCode(), k.dotKiemKe(), Parse.nz(k.tenDoiTuong()), Parse.nz(k.tenPhongBan()),
                Parse.fmt(k.ngayKiemKe()), k.soLuongSoSach(), k.soLuongThucTe(), k.chenhLech(), k.tinhTrangThucTe()};
    }

    private void tao() {
        KiemKeDialog d = new KiemKeDialog(SwingUtilities.getWindowAncestor(this), bus, new TaiSanBUS().search(""), new ThietBiBUS().search(""));
        if (d.open()) refresh();
    }
}