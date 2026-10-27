package com.qlts.view;

import com.qlts.bus.NhanVienBUS;
import com.qlts.common.Constants;
import com.qlts.component.Combos;
import com.qlts.component.FormDialog;
import com.qlts.entity.NhanVien;
import com.qlts.entity.PhongBan;
import com.qlts.util.Parse;

import java.awt.Window;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class NhanVienDialog extends FormDialog {
    private final NhanVienBUS bus;
    private final NhanVien existing;

    private final JTextField txtCode = new JTextField(20);
    private final JTextField txtHoTen = new JTextField(20);
    private final JComboBox<String> cboGioiTinh = new JComboBox<>(Constants.GIOI_TINH);
    private final JTextField txtNgaySinh = new JTextField(20);
    private final JTextField txtCccd = new JTextField(20);
    private final JTextField txtChucVu = new JTextField(20);
    private final JComboBox<PhongBan> cboPhongBan;
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtSdt = new JTextField(20);
    private final JTextField txtNgayVao = new JTextField(20);

    public NhanVienDialog(Window owner, NhanVienBUS bus, NhanVien existing, List<PhongBan> phongBan) {
        super(owner, existing == null ? "Thêm nhân viên" : "Sửa nhân viên");
        this.bus = bus;
        this.existing = existing;
        cboPhongBan = Combos.withEmpty(phongBan);

        addRow("Mã nhân viên (*)", txtCode);
        addRow("Họ tên (*)", txtHoTen);
        addRow("Giới tính", cboGioiTinh);
        addRow("Ngày sinh (yyyy-MM-dd)", txtNgaySinh);
        addRow("CMND/CCCD", txtCccd);
        addRow("Chức vụ", txtChucVu);
        addRow("Phòng ban", cboPhongBan);
        addRow("Email", txtEmail);
        addRow("Số điện thoại", txtSdt);
        addRow("Ngày vào làm (yyyy-MM-dd)", txtNgayVao);

        if (existing != null) fill(existing);
    }

    private void fill(NhanVien n) {
        txtCode.setText(n.maCode());
        txtHoTen.setText(n.hoTen());
        cboGioiTinh.setSelectedItem(n.gioiTinh());
        txtNgaySinh.setText(Parse.fmt(n.ngaySinh()));
        txtCccd.setText(Parse.nz(n.cmndCccd()));
        txtChucVu.setText(Parse.nz(n.chucVu()));
        if (n.maPhongBan() != null) Combos.select(cboPhongBan, x -> x.maPhongBan() == n.maPhongBan());
        txtEmail.setText(Parse.nz(n.email()));
        txtSdt.setText(Parse.nz(n.soDienThoai()));
        txtNgayVao.setText(Parse.fmt(n.ngayVaoLam()));
    }

    @Override protected void onSave() {
        PhongBan pb = (PhongBan) cboPhongBan.getSelectedItem();
        NhanVien n = new NhanVien(
                existing == null ? null : existing.maNhanVien(),
                Parse.require(txtCode.getText(), "Mã nhân viên"),
                Parse.require(txtHoTen.getText(), "Họ tên"),
                (String) cboGioiTinh.getSelectedItem(),
                Parse.date(txtNgaySinh.getText(), "Ngày sinh"),
                Parse.text(txtCccd.getText()),
                Parse.text(txtChucVu.getText()),
                Parse.text(txtEmail.getText()),
                Parse.text(txtSdt.getText()),
                pb == null ? null : pb.maPhongBan(), pb == null ? null : pb.tenPhongBan(),
                Parse.date(txtNgayVao.getText(), "Ngày vào làm"));
        bus.save(n);
    }
}
