package com.qlts.view;

import com.qlts.bus.CapPhatBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.NhanVien;
import com.qlts.entity.TaiSan;
import com.qlts.entity.ThietBi;
import com.qlts.entity.YeuCauCapPhat;
import com.qlts.util.Parse;

import java.awt.Window;
import java.time.LocalDate;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class CapPhatDialog extends FormDialog {

    private static final String LOAI_TAI_SAN = "Tài sản";
    private static final String LOAI_THIET_BI = "Thiết bị";

    private record Item(int id, Integer maLoai, String label) {

        @Override
        public String toString() {
            return label;
        }
    }

    private final CapPhatBUS bus;
    private final List<TaiSan> taiSans;
    private final List<ThietBi> thietBis;

    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{LOAI_TAI_SAN, LOAI_THIET_BI});
    private final JComboBox<Item> cboDoiTuong = new JComboBox<>();
    private final JComboBox<NhanVien> cboNhanVien;
    private final JTextField txtNgayCap = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField txtDuKien = new JTextField(20);
    private final JTextField txtGhiChu = new JTextField(20);

    private Integer maCapPhatKetQua = null;

    public CapPhatDialog(Window owner, CapPhatBUS bus, List<TaiSan> taiSans, List<ThietBi> thietBis,
            List<NhanVien> nhanViens, YeuCauCapPhat yeuCau) {
        super(owner, "Cấp phát tài sản / thiết bị");
        this.bus = bus;
        this.taiSans = taiSans;
        this.thietBis = thietBis;
        cboNhanVien = new JComboBox<>(nhanViens.toArray(new NhanVien[0]));

        addRow("Loại đối tượng", cboLoai);
        addRow("Đối tượng (trong kho)", cboDoiTuong);
        addRow("Nhân viên nhận (*)", cboNhanVien);
        addRow("Ngày cấp phát (*)", txtNgayCap);
        addRow("Dự kiến trả (yyyy-MM-dd)", txtDuKien);
        addRow("Ghi chú", txtGhiChu);

        cboLoai.addActionListener(e -> reloadObjects());
        reloadObjects();

        // 🔥 TỰ ĐỘNG ĐIỀN TỪ YÊU CẦU
        if (yeuCau != null) {
            // Chọn nhân viên
            String tenNguoiYeuCau = yeuCau.tenNguoiYeuCau();
            if (tenNguoiYeuCau != null) {
                for (int i = 0; i < cboNhanVien.getItemCount(); i++) {
                    NhanVien nv = cboNhanVien.getItemAt(i);
                    if (nv.hoTen().equalsIgnoreCase(tenNguoiYeuCau)) {
                        cboNhanVien.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // 2. Ghi chú sạch
            txtGhiChu.setText(yeuCau.lyDo() != null ? yeuCau.lyDo() : "");

            // 3. Tự động chọn đối tượng (Tài sản/Thiết bị) dựa vào ma_loai
            if (yeuCau.maLoai() != null) {
                // Kiểm tra xem loại này thuộc Tài sản hay Thiết bị
                // Cách đơn giản: Lặp qua taiSans, nếu thấy item nào có maLoai trùng -> chọn Tài sản. Ngược lại chọn Thiết bị.
                boolean isTaiSan = taiSans.stream().anyMatch(t -> t.maLoai() != null && t.maLoai().equals(yeuCau.maLoai()));
                if (isTaiSan) {
                    cboLoai.setSelectedItem(LOAI_TAI_SAN);
                } else {
                    cboLoai.setSelectedItem(LOAI_THIET_BI);
                }
                reloadObjects(); // load lại combobox đối tượng

                // Tìm và chọn item khớp maLoai đầu tiên
                for (int i = 0; i < cboDoiTuong.getItemCount(); i++) {
                    Item item = cboDoiTuong.getItemAt(i);
                    if (item.maLoai() != null && item.maLoai().equals(yeuCau.maLoai())) {
                        cboDoiTuong.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    

    private void reloadObjects() {
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        if (LOAI_TAI_SAN.equals(cboLoai.getSelectedItem())) {
            taiSans.forEach(t -> m.addElement(new Item(t.maTaiSan(), t.maLoai(), t.maCode() + " - " + t.ten())));
        } else {
            thietBis.forEach(t -> m.addElement(new Item(t.maThietBi(), t.maLoai(), t.maCode() + " - " + t.ten())));
        }
        cboDoiTuong.setModel(m);

    }

    @Override
    protected void onSave() {
        Item item = (Item) cboDoiTuong.getSelectedItem();
        if (item == null) {
            throw new BusinessException("Không có đối tượng nào trong kho để cấp phát.");
        }
        boolean isTs = LOAI_TAI_SAN.equals(cboLoai.getSelectedItem());

        // Lưu và lấy ID trả về
        maCapPhatKetQua = bus.capPhat(isTs ? item.id() : null, isTs ? null : item.id(),
                (NhanVien) cboNhanVien.getSelectedItem(),
                Parse.date(txtNgayCap.getText(), "Ngày cấp phát"),
                Parse.date(txtDuKien.getText(), "Ngày dự kiến trả"),
                Parse.text(txtGhiChu.getText()));
    }

    public Integer getMaCapPhatKetQua() {
        return maCapPhatKetQua;
    }
}
