package com.qlts.view;

import com.qlts.bus.NguoiDungBUS;
import com.qlts.bus.PhongBanBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.dao.NguoiDungDAO.UserFull;
import com.qlts.entity.PhongBan;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.Window;
import java.time.LocalDate;
import java.util.List;

public class PhongBanDialog extends FormDialog {
    private final PhongBanBUS bus;
    private final PhongBan existing;

    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtCode = new JTextField(20);
    private final JTextField txtDiaDiem = new JTextField(20);
    private final JComboBox<UserFull> cboTruongPhong;
    private final JTextField txtNgayLap = new JTextField(LocalDate.now().toString(), 20);

    public PhongBanDialog(Window owner, PhongBanBUS bus, PhongBan existing, List<UserFull> admins) {
        super(owner, existing == null ? "Thêm phòng ban" : "Sửa phòng ban");
        this.bus = bus;
        this.existing = existing;
        
        cboTruongPhong = new JComboBox<>();
        cboTruongPhong.addItem(null); // Cho phép để trống
        for (UserFull u : admins) {
            cboTruongPhong.addItem(u);
        }
        cboTruongPhong.setRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserFull u) setText(u.hoTen());
                else setText("--- Chưa chỉ định ---");
                return this;
            }
        });

        addRow("Mã phòng ban (*)", txtCode);
        addRow("Tên phòng ban (*)", txtTen);
        addRow("Địa điểm", txtDiaDiem);
        addRow("Trưởng phòng", cboTruongPhong);
        addRow("Ngày thành lập", txtNgayLap);

        if (existing != null) {
            txtTen.setText(existing.tenPhongBan());
            txtCode.setText(Parse.nz(existing.maPhongBanCode()));
            txtDiaDiem.setText(Parse.nz(existing.diaDiem()));
            txtNgayLap.setText(Parse.fmt(existing.ngayThanhLap()));
            
            if (existing.maQuanLy() != null) {
                for (int i = 0; i < cboTruongPhong.getItemCount(); i++) {
                    UserFull u = cboTruongPhong.getItemAt(i);
                    if (u != null && u.maTaiKhoan() == existing.maQuanLy()) {
                        cboTruongPhong.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    @Override protected void onSave() {
        UserFull tp = (UserFull) cboTruongPhong.getSelectedItem();
        PhongBan pb = new PhongBan(
                existing == null ? 0 : existing.maPhongBan(),
                Parse.require(txtTen.getText(), "Tên phòng ban"),
                Parse.text(txtCode.getText()),
                Parse.text(txtDiaDiem.getText()),
                tp == null ? null : tp.maTaiKhoan(),
                Parse.date(txtNgayLap.getText(), "Ngày thành lập"),
                tp == null ? null : tp.hoTen()
        );
        bus.save(pb);
    }
}