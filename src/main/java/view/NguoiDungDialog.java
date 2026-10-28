package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.NguoiDungBUS;
import com.qlts.dao.NguoiDungDAO.UserFull;
import com.qlts.component.Dialogs;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NguoiDungDialog extends JDialog {

    private final NguoiDungBUS bus = new NguoiDungBUS();
    private final UserFull existing;
    private boolean saved = false;

    private final JTextField     txtUsername   = new JTextField(22);
    private final JTextField     txtHoTen      = new JTextField(22);
    private final JTextField     txtEmail      = new JTextField(22);
    private final JTextField     txtSdt        = new JTextField(22);
    // CHỈ 2 ROLE
    private final JComboBox<String> cboQuyen   = new JComboBox<>(new String[]{
            "Quản trị viên", "Nhân viên"});
    private final JPasswordField txtMatKhau    = new JPasswordField(22);

    public NguoiDungDialog(Window owner, UserFull existing) {
        super(owner, existing == null ? "Thêm người dùng" : "Sửa người dùng",
                ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 28, 12, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;

        JLabel title = new JLabel(existing == null ? "Tạo tài khoản mới" : "Sửa tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1565C0));
        form.add(title, gc);

        gc.gridwidth = 1;
        gc.gridy++; form.add(lbl("Tên đăng nhập (*)"), gc);
        gc.gridx = 1; form.add(txtUsername, gc);

        gc.gridx = 0; gc.gridy++; form.add(lbl("Họ tên (*)"), gc);
        gc.gridx = 1; form.add(txtHoTen, gc);

        gc.gridx = 0; gc.gridy++; form.add(lbl("Email"), gc);
        gc.gridx = 1; form.add(txtEmail, gc);

        gc.gridx = 0; gc.gridy++; form.add(lbl("Số điện thoại"), gc);
        gc.gridx = 1; form.add(txtSdt, gc);

        gc.gridx = 0; gc.gridy++; form.add(lbl("Quyền (*)"), gc);
        gc.gridx = 1; form.add(cboQuyen, gc);

        if (existing == null) {
            gc.gridx = 0; gc.gridy++; form.add(lbl("Mật khẩu (*)"), gc);
            gc.gridx = 1; form.add(txtMatKhau, gc);
            txtMatKhau.setText("123456");
        }

        txtUsername.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtHoTen.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtEmail.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtSdt.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtMatKhau.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        cboQuyen.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        if (existing != null) {
            txtUsername.setText(existing.tenDangNhap());
            txtUsername.setEditable(false);
            txtUsername.setBackground(new Color(0xF5F5F5));
            txtHoTen.setText(Parse.nz(existing.hoTen()));
            txtEmail.setText(Parse.nz(existing.email()));
            txtSdt.setText(Parse.nz(existing.soDienThoai()));
            // Quyền: 1 → index 0, 3 → index 1
            cboQuyen.setSelectedIndex(existing.maQuyen() == 1 ? 0 : 1);
        }

        JButton btnSave = new JButton(existing == null ? "TẠO TÀI KHOẢN" : "LƯU");
        JButton btnCancel = new JButton("Hủy");
        stylePrimary(btnSave);
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setPreferredSize(new Dimension(100, 36));

        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSave);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnCancel);
        buttons.add(btnSave);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x424242));
        return l;
    }

    private void stylePrimary(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, 36));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0x1565C0));
        b.setForeground(Color.WHITE);
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    private void doSave() {
        try {
            // CHỈ 2 ROLE: index 0 → 1 (Admin), index 1 → 3 (Nhân viên)
            int maQuyen = cboQuyen.getSelectedIndex() == 0 ? 1 : 3;

            if (existing == null) {
                bus.them(txtUsername.getText(), txtHoTen.getText(),
                        txtEmail.getText(), txtSdt.getText(), maQuyen,
                        new String(txtMatKhau.getPassword()));
                Dialogs.info(this, "Đã tạo tài khoản thành công!");
            } else {
                bus.sua(existing.maTaiKhoan(), txtHoTen.getText(),
                        txtEmail.getText(), txtSdt.getText(), maQuyen);
                Dialogs.info(this, "Đã lưu thay đổi!");
            }
            saved = true;
            dispose();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    public boolean isSaved() { return saved; }
}