package dialog;

import com.qlts.bus.KiemKeBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.PhongBan;
import com.qlts.entity.TaiSan;
import com.qlts.entity.ThietBi;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.Window;
import java.time.LocalDate;
import java.util.List;

public class KiemKeDialog extends FormDialog {
    private record Item(int id, int soLuong, String label) { 
        @Override public String toString() { return label; } 
    }

    private final KiemKeBUS bus;
    private final JComboBox<String> cboLoaiObj = new JComboBox<>(new String[]{"Tài sản", "Thiết bị"});
    private final JComboBox<Item> cboObj = new JComboBox<>();
    private final JComboBox<PhongBan> cboPhongBan; // 🔥 Thêm Combo Phòng Ban
    
    private final JTextField txtDot = new JTextField("Kiểm kê đột xuất", 20);
    private final JTextField txtNgay = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField txtTinhTrang = new JTextField("Tốt", 20);
    private final JSpinner spnThucTe = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
    private final JLabel lblSoSach = new JLabel("1");
    private final JTextField txtKetQua = new JTextField(20);

    private final List<TaiSan> taiSans;
    private final List<ThietBi> thietBis;

    public KiemKeDialog(Window owner, KiemKeBUS bus, List<TaiSan> ts, List<ThietBi> tb, List<PhongBan> phongBans) {
        super(owner, "Ghi nhận kiểm kê");
        this.bus = bus;
        this.taiSans = ts;
        this.thietBis = tb;
        
        // 🔥 Khởi tạo Combo Phòng Ban
        cboPhongBan = new JComboBox<>(phongBans.toArray(new PhongBan[0]));
        cboPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PhongBan pb) setText(pb.tenPhongBan());
                return this;
            }
        });

        addRow("Loại đối tượng", cboLoaiObj);
        addRow("Chọn đối tượng (*)", cboObj);
        addRow("Phòng ban (*)", cboPhongBan); // 🔥 Thêm vào form
        addRow("Đợt kiểm kê (*)", txtDot);
        addRow("Ngày KK (*)", txtNgay);
        addRow("Tình trạng thực tế", txtTinhTrang);
        addRow("Số lượng sổ sách", lblSoSach);
        addRow("Số lượng thực tế (*)", spnThucTe);
        addRow("Kết quả / Ghi chú", txtKetQua);

        cboLoaiObj.addActionListener(e -> reload());
        cboObj.addActionListener(e -> {
            Item item = (Item) cboObj.getSelectedItem();
            if (item != null) lblSoSach.setText(String.valueOf(item.soLuong()));
        });
        reload();
    }

    private void reload() {
        cboObj.removeAllItems();
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        
        if (cboLoaiObj.getSelectedIndex() == 0) {
            if (taiSans.isEmpty()) {
                m.addElement(new Item(-1, 1, "--- Không có Tài sản nào ---"));
            } else {
                taiSans.forEach(t -> m.addElement(new Item(t.maTaiSan(), t.soLuongSafe(), t.maCode() + " - " + t.ten())));
            }
        } else {
            if (thietBis.isEmpty()) {
                m.addElement(new Item(-1, 1, "--- Không có Thiết bị nào ---"));
            } else {
                thietBis.forEach(t -> m.addElement(new Item(t.maThietBi(), t.soLuongSafe(), t.maCode() + " - " + t.ten())));
            }
        }
        cboObj.setModel(m);
        if (m.getSize() > 0) cboObj.setSelectedIndex(0);
    }

    @Override protected void onSave() {
        Item item = (Item) cboObj.getSelectedItem();
        if (item == null || item.id() == -1) throw new BusinessException("Chưa chọn đối tượng hợp lệ.");
        
        PhongBan pb = (PhongBan) cboPhongBan.getSelectedItem();
        if (pb == null) throw new BusinessException("Vui lòng chọn phòng ban.");
        
        bus.ghiNhan(txtDot.getText(), 
                cboLoaiObj.getSelectedIndex() == 0 ? item.id() : null,
                cboLoaiObj.getSelectedIndex() == 1 ? item.id() : null,
                pb.maPhongBan(), // 🔥 Truyền mã phòng ban vào
                Parse.date(txtNgay.getText(), "Ngày KK"),
                Parse.text(txtTinhTrang.getText()),
                (Integer) spnThucTe.getValue(), item.soLuong(),
                Parse.text(txtKetQua.getText()), null);
    }
}