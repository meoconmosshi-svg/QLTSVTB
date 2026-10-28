package dialog;

import com.qlts.bus.ThanhLyBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.TaiSan;
import com.qlts.entity.ThietBi;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.Window;
import java.time.LocalDate;
import java.util.List;

public class ThanhLyDialog extends FormDialog {
    private record Item(int id, String label) { @Override public String toString() { return label; } }

    private final ThanhLyBUS bus;
    private final JComboBox<String> cboLoaiObj = new JComboBox<>(new String[]{"Tài sản", "Thiết bị"});
    private final JComboBox<Item> cboObj = new JComboBox<>();
    private final JTextField txtNgay = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField txtLyDo = new JTextField(20);
    private final JTextField txtGiaTri = new JTextField("0", 20);

    private final List<TaiSan> taiSans;
    private final List<ThietBi> thietBis;

    public ThanhLyDialog(Window owner, ThanhLyBUS bus, List<TaiSan> ts, List<ThietBi> tb) {
        super(owner, "Tạo yêu cầu thanh lý");
        this.bus = bus;
        this.taiSans = ts;
        this.thietBis = tb;

        addRow("Loại đối tượng", cboLoaiObj);
        addRow("Chọn thiết bị/TS (*)", cboObj);
        addRow("Ngày thanh lý (*)", txtNgay);
        addRow("Lý do (*)", txtLyDo);
        addRow("Giá trị thu hồi", txtGiaTri);

        cboLoaiObj.addActionListener(e -> reload());
        reload();
    }

    private void reload() {
        cboObj.removeAllItems(); // 🔥 Xóa data cũ để tránh bị lẫn
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        
        if (cboLoaiObj.getSelectedIndex() == 0) {
            if (taiSans.isEmpty()) {
                m.addElement(new Item(-1, "--- Không có Tài sản nào ---"));
            } else {
                taiSans.forEach(t -> m.addElement(new Item(t.maTaiSan(), t.maCode() + " - " + t.ten())));
            }
        } else {
            if (thietBis.isEmpty()) {
                m.addElement(new Item(-1, "--- Không có Thiết bị nào ---"));
            } else {
                thietBis.forEach(t -> m.addElement(new Item(t.maThietBi(), t.maCode() + " - " + t.ten())));
            }
        }
        cboObj.setModel(m);
        cboObj.revalidate();
        cboObj.repaint();
    }

    @Override protected void onSave() {
        Item item = (Item) cboObj.getSelectedItem();
        if (item == null || item.id() == -1) throw new BusinessException("Chưa chọn đối tượng hợp lệ.");
        
        bus.yeuCau(
                cboLoaiObj.getSelectedIndex() == 0 ? item.id() : null,
                cboLoaiObj.getSelectedIndex() == 1 ? item.id() : null,
                Parse.date(txtNgay.getText(), "Ngày"),
                Parse.text(txtLyDo.getText()),
                Parse.money(txtGiaTri.getText(), "Giá trị")
        );
    }
}