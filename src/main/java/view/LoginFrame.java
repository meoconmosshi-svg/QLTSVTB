package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.AuthBUS;
import com.qlts.common.Theme;
import com.qlts.component.Dialogs;
import com.qlts.entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private record RoleOption(int maQuyen, String label) {
        @Override public String toString() { return label; }
    }

    private final AuthBUS authBus = new AuthBUS();
    private final JTextField     txtUser = new JTextField(20);
    private final JPasswordField txtPass = new JPasswordField(20);
    private final JComboBox<RoleOption> cboRole = new JComboBox<>(new RoleOption[]{
            new RoleOption(1, "Quản trị viên"),
            new RoleOption(3, "Nhân viên")
    });

    private final JPanel leftPanel = new GradientPanel();

    public LoginFrame() {
        super("Đăng nhập - Quản lý tài sản");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        setContentPane(root);

        cboRole.addActionListener(e -> {
            int maQuyen = currentSelectedRole();
            Theme.applyFor(maQuyen);
            leftPanel.repaint();
        });

        Theme.applyFor(currentSelectedRole());
        pack();
        setLocationRelativeTo(null);
    }

    private int currentSelectedRole() {
        RoleOption r = (RoleOption) cboRole.getSelectedItem();
        return r == null ? 1 : r.maQuyen();
    }

    private JPanel buildLeftPanel() {
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel lblTitle = new JLabel("<html>QUẢN LÝ<br>TÀI SẢN &<br>THIẾT BỊ</html>");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Hệ thống quản lý tài sản trường đại học");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(0xE3F2FD));

        JLabel lblVer = new JLabel("Phiên bản 2.0 - Desktop Edition");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(new Color(0xBBDEFB));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(lblTitle, gc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 0, 0);
        leftPanel.add(lblSub, gc);

        gc.gridy = 2; gc.weighty = 1; gc.anchor = GridBagConstraints.SOUTHWEST;
        gc.insets = new Insets(240, 0, 0, 0);
        leftPanel.add(lblVer, gc);

        return leftPanel;
    }

    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, Theme.dark(),
                    getWidth(), getHeight(), Theme.gradientEnd());
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillOval(-80, -80, 300, 300);
            g2.fillOval(getWidth() - 180, getHeight() - 220, 320, 320);
            g2.dispose();
        }
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1; gc.gridx = 0; gc.gridy = 0;
        gc.insets = new Insets(0, 0, 4, 0);

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(lblTitle, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 20, 0);
        JLabel lblSub = new JLabel("Vui lòng chọn vai trò và đăng nhập");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(0x757575));
        p.add(lblSub, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 4, 0);
        p.add(fieldLabel("Vai trò đăng nhập"), gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 14, 0);
        cboRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboRole.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        p.add(cboRole, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 4, 0);
        p.add(fieldLabel("Tên đăng nhập"), gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 14, 0);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập");
        txtUser.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        p.add(txtUser, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 4, 0);
        p.add(fieldLabel("Mật khẩu"), gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 24, 0);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");
        txtPass.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        p.add(txtPass, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 10, 0);
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        styleButton(btnLogin, true);
        btnLogin.addActionListener(this::doLogin);
        getRootPane().setDefaultButton(btnLogin);
        p.add(btnLogin, gc);

        gc.gridy++; gc.insets = new Insets(0, 0, 10, 0);
        JButton btnExit = new JButton("Thoát");
        styleButton(btnExit, false);
        btnExit.addActionListener(e -> System.exit(0));
        p.add(btnExit, gc);

        // Chỉ còn link "Quên mật khẩu" — KHÔNG còn "Đăng ký"
        gc.gridy++; gc.insets = new Insets(6, 0, 0, 0);
        JPanel pLinks = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pLinks.setBackground(Color.WHITE);
        JButton btnForgot = linkButton("Quên mật khẩu?", new Color(0xD32F2F));
        btnForgot.addActionListener(e -> moQuenMatKhau());
        pLinks.add(btnForgot);
        p.add(pLinks, gc);

        return p;
    }

    private JButton linkButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(color);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(0, 0, 0, 0));
        return b;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x424242));
        return l;
    }

    private void styleButton(JButton b, boolean primary) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 40));
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:8; borderWidth:0; focusWidth:0;");
        if (primary) {
            b.putClientProperty("FlatLaf.style", "background:$Button.default.background; foreground:#FFFFFF");
        } else {
            b.setBackground(new Color(0xEEEEEE));
            b.setForeground(new Color(0x616161));
        }
    }

    private void moQuenMatKhau() {
        try {
            ForgotPasswordDialog d = new ForgotPasswordDialog(SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void doLogin(ActionEvent e) {
        try {
            TaiKhoan tk = authBus.login(txtUser.getText(), new String(txtPass.getPassword()));

            int selected = currentSelectedRole();
            if (tk.maQuyen() != selected) {
                Dialogs.warn(this, "Vai trò đã chọn không khớp với tài khoản.\n"
                        + "Vui lòng chọn đúng vai trò.");
                return;
            }

            Theme.applyFor(tk.maQuyen());
            new MainFrame().setVisible(true);
            dispose();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
            txtPass.setText("");
        }
    }
}