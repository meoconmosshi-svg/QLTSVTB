package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.AuthBUS;
import com.qlts.component.Dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {

    private final AuthBUS bus = new AuthBUS();
    private final JTextField txtUser = new JTextField(22);
    private final JTextField txtOtp = new JTextField(22);
    private final JPasswordField txtNewPwd = new JPasswordField(22);
    private final JPasswordField txtCfPwd = new JPasswordField(22);
    private final JButton btnSendOtp = new JButton("Gửi OTP");
    private final JButton btnReset = new JButton("ĐẶT LẠI MẬT KHẨU");

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Quên mật khẩu", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 32, 12, 32));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;

        JLabel title = new JLabel("Khôi phục mật khẩu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0xD32F2F));
        form.add(title, gc);

        gc.gridwidth = 1; gc.gridy++;
        form.add(new JLabel("Email hoặc SĐT (*)"), gc);
        gc.gridx = 1;
        JPanel p1 = new JPanel(new BorderLayout(6, 0));
        p1.setOpaque(false);
        p1.add(txtUser, BorderLayout.CENTER);
        stylePrimary(btnSendOtp);
        btnSendOtp.setPreferredSize(new Dimension(100, 30));
        p1.add(btnSendOtp, BorderLayout.EAST);
        form.add(p1, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Mã OTP (*)"), gc);
        gc.gridx = 1; form.add(txtOtp, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Mật khẩu mới (*)"), gc);
        gc.gridx = 1; form.add(txtNewPwd, gc);

        gc.gridx = 0; gc.gridy++;
        form.add(new JLabel("Xác nhận mật khẩu (*)"), gc);
        gc.gridx = 1; form.add(txtCfPwd, gc);

        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập email hoặc SĐT đã đăng ký");
        txtOtp.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "6 chữ số");
        txtUser.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtOtp.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtNewPwd.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        txtCfPwd.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        JButton btnCancel = new JButton("Hủy");
        stylePrimary(btnReset);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 34));

        btnSendOtp.addActionListener(e -> doSendOtp());
        btnReset.addActionListener(e -> doReset());
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnReset);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnCancel);
        buttons.add(btnReset);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void stylePrimary(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0xD32F2F));
        b.setForeground(Color.WHITE);
        b.putClientProperty(FlatClientProperties.STYLE, "arc:6; borderWidth:0; focusWidth:0;");
    }

    private void doSendOtp() {
        try {
            String fallbackOtp = bus.sendOtp(txtUser.getText());
            if (fallbackOtp == null) {
                Dialogs.info(this, "Đã gửi mã OTP đến email của bạn.\nVui lòng kiểm tra hộp thư.");
            } else {
                // Chưa cấu hình mail hoặc user không có email → hiển thị OTP (giả lập SMS)
                JTextArea ta = new JTextArea(
                        "Hệ thống chưa cấu hình gửi email, hoặc tài khoản không có email.\n"
                        + "Mã OTP của bạn là (giả lập SMS):\n\n         " + fallbackOtp
                        + "\n\nMã có hiệu lực trong 5 phút.");
                ta.setEditable(false);
                ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                ta.setBackground(new Color(0xFFF8E1));
                ta.setBorder(new EmptyBorder(12, 12, 12, 12));
                JOptionPane.showMessageDialog(this, ta, "Mã xác thực OTP",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void doReset() {
        try {
            bus.resetPassword(txtUser.getText(), txtOtp.getText(),
                    new String(txtNewPwd.getPassword()),
                    new String(txtCfPwd.getPassword()));
            Dialogs.info(this, "Đặt lại mật khẩu thành công!\nHãy đăng nhập bằng mật khẩu mới.");
            dispose();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }
}