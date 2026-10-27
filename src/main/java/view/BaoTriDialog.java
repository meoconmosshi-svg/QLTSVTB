package com.qlts.view;

import com.qlts.bus.BaoTriBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.TaiSan;
import com.qlts.entity.ThietBi;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BaoTriDialog extends FormDialog {
    private record Item(int id, String label) { @Override public String toString() { return label; } }

    private final BaoTriBUS bus;
    private final JComboBox<String> cboLoaiObj = new JComboBox<>(new String[]{"Tài sản", "Thiết bị"});
    private final JComboBox<Item> cboObj = new JComboBox<>();
    
    private final JComboBox<String> cboLoaiBT = new JComboBox<>(new String[]{"Định kỳ", "Đột xuất", "Sửa chữa"});
    private final JTextField txtNgay = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField txtNoiDung = new JTextField(20);
    private final JTextField txtDonVi = new JTextField(20);
    private final JTextField txtChiPhi = new JTextField("0", 20); // 🔥 Thêm ô chi phí
    
    private final List<TaiSan> taiSans;
    private final List<ThietBi> thietBis;

    public BaoTriDialog(Window owner, BaoTriBUS bus, List<TaiSan> ts, List<ThietBi> tb) {
        super(owner, "Tạo phiếu bảo trì");
        this.bus = bus;
        this.taiSans = ts;
        this.thietBis = tb;

        addRow("Loại đối tượng", cboLoaiObj);
        addRow("Chọn đối tượng (*)", cboObj); 
        addRow("Loại bảo trì", cboLoaiBT);
        addRow("Ngày bắt đầu (*)", txtNgay);
        addRow("Nội dung (*)", txtNoiDung);
        addRow("Đơn vị thực hiện", txtDonVi);
        addRow("Chi phí dự kiến", txtChiPhi); // 🔥 Thêm vào form

        cboLoaiObj.addActionListener(e -> reload());
        reload();
    }

    private void reload() {
        cboObj.removeAllItems();
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        
        if (cboLoaiObj.getSelectedIndex() == 0) {
            if (taiSans.isEmpty()) m.addElement(new Item(-1, "--- Không có Tài sản nào ---"));
            else taiSans.forEach(t -> m.addElement(new Item(t.maTaiSan(), t.maCode() + " - " + t.ten())));
        } else {
            if (thietBis.isEmpty()) m.addElement(new Item(-1, "--- Không có Thiết bị nào ---"));
            else thietBis.forEach(t -> m.addElement(new Item(t.maThietBi(), t.maCode() + " - " + t.ten())));
        }
        
        cboObj.setModel(m);
        cboObj.revalidate();
        cboObj.repaint();
    }

    @Override protected void onSave() {
        Item item = (Item) cboObj.getSelectedItem();
        if (item == null || item.id() == -1) throw new BusinessException("Chưa chọn đối tượng hợp lệ.");
        
        bus.taoPhieu(
                cboLoaiObj.getSelectedIndex() == 0 ? item.id() : null,
                cboLoaiObj.getSelectedIndex() == 1 ? item.id() : null,
                (String) cboLoaiBT.getSelectedItem(),
                Parse.date(txtNgay.getText(), "Ngày bắt đầu"),
                Parse.text(txtNoiDung.getText()),
                Parse.text(txtDonVi.getText()),
                Parse.money(txtChiPhi.getText(), "Chi phí") // 🔥 Truyền chi phí vào BUS
        );
    }
}