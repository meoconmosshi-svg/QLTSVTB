package panel;

import dialog.BaoTriDialog;
import com.qlts.bus.BaoTriBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.BaoTri;
import com.qlts.util.Parse;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;

public class BaoTriPanel extends CrudPanel<BaoTri> {
    private final BaoTriBUS bus = new BaoTriBUS();
    private final TaiSanBUS tsBus = new TaiSanBUS();
    private final ThietBiBUS tbBus = new ThietBiBUS();

    public BaoTriPanel() {
        super(new String[]{"Mã BT", "Tài sản / Thiết bị", "Người YC", "Loại BT", "Ngày BĐ", "Ngày KT", "Nội dung", "Chi phí", "Trạng thái"}, false);
        
        JButton btnTao = new JButton(Session.isAdmin() ? "Tạo phiếu" : "Gửi yêu cầu BT");
        JButton btnXoa = new JButton("Xóa phiếu"); // 🔥 Nút Xóa
        btnTao.addActionListener(e -> tao());
        btnXoa.addActionListener(e -> xoa());
        addToolButton(btnTao);
        addToolButton(btnXoa);

        if (Session.canModify()) {
            JButton btnDuyet = new JButton("Duyệt");
            JButton btnXong = new JButton("Hoàn thành");
            btnDuyet.addActionListener(e -> duyet());
            btnXong.addActionListener(e -> hoanThanh());
            addToolButton(btnDuyet);
            addToolButton(btnXong);
        }
        refresh();
    }

    @Override protected List<BaoTri> load(String kw) { return bus.search(kw); }
    
    @Override protected Object[] toRow(BaoTri b) {
        return new Object[]{b.maBtCode(), Parse.nz(b.tenDoiTuong()), Parse.nz(b.tenNguoiPhuTrach()), 
                b.loaiBaoTri(), Parse.fmt(b.ngayBatDau()), Parse.fmt(b.ngayKetThuc()), 
                Parse.nz(b.noiDung()), Parse.fmt(b.chiPhi()), b.trangThai()};
    }

    private void tao() {
        BaoTriDialog d = new BaoTriDialog(SwingUtilities.getWindowAncestor(this), bus, tsBus.search(""), tbBus.search(""));
        if (d.open()) refresh();
    }

    private void xoa() {
        BaoTri b = selected(); if (b == null) return;
        if (Dialogs.confirm(this, "Xóa phiếu bảo trì " + b.maBtCode() + "?")) {
            try {
                bus.xoa(b);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void duyet() {
        BaoTri b = selected(); if (b == null) return;
        if (!"Chờ duyệt".equals(b.trangThai())) { Dialogs.warn(this, "Chỉ duyệt được phiếu đang chờ."); return; }
        if (Dialogs.confirm(this, "Duyệt phiếu bảo trì này?")) {
            try { bus.duyet(b); refresh(); } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void hoanThanh() {
        BaoTri b = selected(); if (b == null) return;
        if (!"Đang thực hiện".equals(b.trangThai())) { Dialogs.warn(this, "Phiếu phải ở trạng thái 'Đang thực hiện'."); return; }
        
        JTextField txtNgay = new JTextField(LocalDate.now().toString());
        JTextField txtChiPhi = new JTextField(b.chiPhi() != null ? b.chiPhi().toString() : "0");
        JTextField txtKetQua = new JTextField();
        
        JPanel p = new JPanel(new java.awt.GridLayout(3, 2, 5, 5));
        p.add(new JLabel("Ngày hoàn thành:")); p.add(txtNgay);
        p.add(new JLabel("Chi phí thực tế:")); p.add(txtChiPhi);
        p.add(new JLabel("Kết quả:")); p.add(txtKetQua);

        if (JOptionPane.showConfirmDialog(this, p, "Hoàn thành bảo trì", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                bus.hoanThanh(b, Parse.date(txtNgay.getText(), "Ngày"), txtKetQua.getText(), Parse.money(txtChiPhi.getText(), "Chi phí"));
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }
}