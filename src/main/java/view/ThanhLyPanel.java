package com.qlts.view;

import com.qlts.bus.ThanhLyBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.ThanhLy;
import com.qlts.util.Parse;

import javax.swing.*;
import java.util.List;

public class ThanhLyPanel extends CrudPanel<ThanhLy> {
    private final ThanhLyBUS bus = new ThanhLyBUS();

    public ThanhLyPanel() {
        super(new String[]{"Mã TL", "Đối tượng", "Ngày TL", "Lý do", "Giá trị thu hồi", "Người duyệt", "Trạng thái"}, false);
        
        JButton btnTao = new JButton("Yêu cầu TL");
        JButton btnXoa = new JButton("Xóa phiếu");
        btnTao.addActionListener(e -> tao());
        btnXoa.addActionListener(e -> xoa());
        addToolButton(btnTao);
        addToolButton(btnXoa);

        if (Session.canModify()) {
            JButton btnDuyet = new JButton("Duyệt");
            JButton btnTuChoi = new JButton("Từ chối");
            btnDuyet.addActionListener(e -> duyet());
            btnTuChoi.addActionListener(e -> tuChoi());
            addToolButton(btnDuyet);
            addToolButton(btnTuChoi);
        }
        refresh();
    }

    @Override protected List<ThanhLy> load(String kw) { return bus.search(kw); }
    
    @Override protected Object[] toRow(ThanhLy t) {
        return new Object[]{t.maTlCode(), Parse.nz(t.tenDoiTuong()), Parse.fmt(t.ngayThanhLy()),
                Parse.nz(t.lyDo()), Parse.fmt(t.giaTriThuHoi()), Parse.nz(t.tenNguoiDuyet()), t.trangThai()};
    }

    private void tao() {
        // 🔥 CHỈ LẤY NHỮNG TÀI SẢN CHƯA THANH LÝ
        List<com.qlts.entity.TaiSan> ts = new TaiSanBUS().search("").stream()
                .filter(t -> !Constants.TS_THANH_LY.equals(t.trangThai())).toList();
        List<com.qlts.entity.ThietBi> tb = new ThietBiBUS().search("").stream()
                .filter(t -> !Constants.TS_THANH_LY.equals(t.trangThai())).toList();
                
        ThanhLyDialog d = new ThanhLyDialog(SwingUtilities.getWindowAncestor(this), bus, ts, tb);
        if (d.open()) refresh();
    }

    private void xoa() {
        ThanhLy t = selected(); if (t == null) return;
        if (!"Chờ duyệt".equals(t.trangThai()) && !Session.isAdmin()) {
            Dialogs.warn(this, "Chỉ được xóa phiếu đang chờ duyệt.");
            return;
        }
        if (Dialogs.confirm(this, "Xóa phiếu thanh lý " + t.maTlCode() + "?")) {
            try {
                bus.xoa(t);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void duyet() {
        ThanhLy t = selected(); if (t == null) return;
        if (!"Chờ duyệt".equals(t.trangThai())) {
            Dialogs.warn(this, "Chỉ duyệt được phiếu đang chờ."); return;
        }
        if (Dialogs.confirm(this, "Duyệt thanh lý tài sản này?\nTài sản sẽ bị chuyển sang trạng thái 'Đã thanh lý'.")) {
            try {
                bus.duyet(t);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void tuChoi() {
        ThanhLy t = selected(); if (t == null) return;
        if (!"Chờ duyệt".equals(t.trangThai())) {
            Dialogs.warn(this, "Chỉ từ chối được phiếu đang chờ."); return;
        }
        String lyDo = JOptionPane.showInputDialog(this, "Lý do từ chối:", "Từ chối thanh lý", JOptionPane.QUESTION_MESSAGE);
        if (lyDo != null) {
            try {
                bus.tuChoi(t, lyDo);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }
}