package com.qlts.view;

import panel.QuanLyNguoiDungPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.qlts.bus.AuthBUS;
import com.qlts.bus.CauHinhBUS;
import com.qlts.common.Session;
import com.qlts.common.Theme;
import com.qlts.component.Dialogs;
import com.qlts.dao.TaiKhoanDAO;
import com.qlts.entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class CaiDatPanel extends JPanel {

    private final CauHinhBUS cauHinhBus = new CauHinhBUS();
    private final AuthBUS authBUS = new AuthBUS();

    // ===== Tab Tài khoản =====
    private final JTextField txtTenDangNhap = new JTextField();
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JPasswordField txtOldPwd = new JPasswordField();
    private final JPasswordField txtNewPwd = new JPasswordField();
    private final JPasswordField txtCfPwd = new JPasswordField();

    // ===== Tab Hệ thống =====
    private final JTextField txtCtTen = new JTextField();
    private final JTextField txtCtDiaChi = new JTextField();
    private final JTextField txtCtSdt = new JTextField();
    private final JTextField txtCtEmail = new JTextField();
    private final JTextField txtCtMst = new JTextField();
    private final JSpinner spnBanGhi = new JSpinner(new SpinnerNumberModel(50, 10, 500, 10));

    // ===== Tab Giao diện =====
    private final JComboBox<String> cboTheme = new JComboBox<>(new String[]{"Sáng", "Tối"});

    public CaiDatPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTabs.addTab("👤 Tài khoản", buildTabTaiKhoan());

        // 🔥 THÊM: Chỉ Admin mới thấy 2 tab này
        if (Session.isAdmin()) {
            subTabs.addTab("👥 Người dùng", new QuanLyNguoiDungPanel());   // ← THÊM MỚI
            subTabs.addTab("🏢 Hệ thống", buildTabHeThong());
        }

        subTabs.addTab("🎨 Giao diện", buildTabGiaoDien());

        add(subTabs, BorderLayout.CENTER);

        loadData();
    }

    // ===================== TAB TÀI KHOẢN =====================
    private JPanel buildTabTaiKhoan() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        wrap.add(sectionTitle("Hồ sơ cá nhân"), gc);

        gc.gridy++;
        wrap.add(fieldRow("Tên đăng nhập", txtTenDangNhap), gc);
        gc.gridy++;
        wrap.add(fieldRow("Họ tên", txtHoTen), gc);
        gc.gridy++;
        wrap.add(fieldRow("Email", txtEmail), gc);
        gc.gridy++;
        wrap.add(fieldRow("Số điện thoại", txtSdt), gc);

        gc.gridy++;
        gc.insets = new Insets(10, 4, 10, 4);
        JButton btnLuuProfile = new JButton("💾 Lưu hồ sơ");
        stylePrimary(btnLuuProfile);
        btnLuuProfile.addActionListener(e -> luuProfile());
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p1.setOpaque(false);
        p1.add(btnLuuProfile);
        wrap.add(p1, gc);

        gc.gridy++;
        gc.insets = new Insets(20, 4, 4, 4);
        wrap.add(sectionTitle("Đổi mật khẩu"), gc);

        gc.gridy++;
        gc.insets = new Insets(4, 4, 4, 4);
        wrap.add(fieldRow("Mật khẩu hiện tại", txtOldPwd), gc);
        gc.gridy++;
        wrap.add(fieldRow("Mật khẩu mới", txtNewPwd), gc);
        gc.gridy++;
        wrap.add(fieldRow("Xác nhận mật khẩu", txtCfPwd), gc);

        gc.gridy++;
        gc.insets = new Insets(10, 4, 10, 4);
        JButton btnDoiPwd = new JButton("🔒 Đổi mật khẩu");
        stylePrimary(btnDoiPwd);
        btnDoiPwd.addActionListener(e -> doiMatKhau());
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p2.setOpaque(false);
        p2.add(btnDoiPwd);
        wrap.add(p2, gc);

        gc.gridy++;
        gc.weighty = 1;
        wrap.add(Box.createGlue(), gc);

        txtTenDangNhap.setEditable(false);
        txtTenDangNhap.setBackground(new Color(0xF5F5F5));

        return wrap;
    }

    // ===================== TAB HỆ THỐNG =====================
    private JPanel buildTabHeThong() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        wrap.add(sectionTitle("Thông tin trường"), gc);   // ← từ "Thông tin công ty"

// Dòng 112-116 (khoảng) — đổi label
        gc.gridy++;
        wrap.add(fieldRow("Tên trường", txtCtTen), gc);   // ← từ "Tên công ty"
        gc.gridy++;
        wrap.add(fieldRow("Địa chỉ", txtCtDiaChi), gc);
        gc.gridy++;
        wrap.add(fieldRow("Điện thoại", txtCtSdt), gc);
        gc.gridy++;
        wrap.add(fieldRow("Email", txtCtEmail), gc);
        gc.gridy++;
        wrap.add(fieldRow("Mã số thuế", txtCtMst), gc);

        gc.gridy++;
        gc.insets = new Insets(20, 4, 4, 4);
        wrap.add(sectionTitle("Cấu hình chung"), gc);

        gc.gridy++;
        gc.insets = new Insets(4, 4, 4, 4);
        wrap.add(fieldRow("Số bản ghi / trang", spnBanGhi), gc);

        gc.gridy++;
        gc.insets = new Insets(10, 4, 10, 4);
        JButton btnLuu = new JButton("💾 Lưu cấu hình");
        stylePrimary(btnLuu);
        btnLuu.addActionListener(e -> luuHeThong());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(btnLuu);
        wrap.add(p, gc);

        gc.gridy++;
        gc.weighty = 1;
        wrap.add(Box.createGlue(), gc);

        return wrap;
    }

    // ===================== TAB GIAO DIỆN =====================
    private JPanel buildTabGiaoDien() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        wrap.add(sectionTitle("Chủ đề giao diện"), gc);

        gc.gridy++;
        JPanel pTheme = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pTheme.setOpaque(false);
        pTheme.add(new JLabel("Theme:  "));
        cboTheme.setPreferredSize(new Dimension(180, 30));
        pTheme.add(cboTheme);
        wrap.add(pTheme, gc);

        gc.gridy++;
        gc.insets = new Insets(14, 4, 4, 4);
        JButton btnApDung = new JButton("🎨 Áp dụng theme");
        stylePrimary(btnApDung);
        btnApDung.addActionListener(e -> apDungTheme());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(btnApDung);
        wrap.add(p, gc);

        gc.gridy++;
        gc.insets = new Insets(20, 4, 4, 4);
        JLabel lblHint = new JLabel("<html><i>Lưu ý: Đổi theme cần khởi động lại ứng dụng để có hiệu lực đầy đủ.</i></html>");
        lblHint.setForeground(new Color(0x757575));
        wrap.add(lblHint, gc);

        gc.gridy++;
        gc.weighty = 1;
        wrap.add(Box.createGlue(), gc);

        return wrap;
    }

    // ===================== UI helpers =====================
    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(Theme.primary());
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JPanel fieldRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(150, 30));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (field instanceof JTextField tf) {
            tf.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
        }
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void stylePrimary(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(180, 36));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
        b.setBackground(Theme.primary());
        b.setForeground(Theme.textOn());
    }

    // ===================== Xử lý logic =====================
    private void loadData() {
        TaiKhoan tk = Session.current();
        if (tk == null) {
            return;
        }

        Map<String, String> info = new TaiKhoanDAO().findFullInfo(tk.maTaiKhoan());
        txtTenDangNhap.setText(info.getOrDefault("ten_dang_nhap", ""));
        txtHoTen.setText(info.getOrDefault("ho_ten", ""));
        txtEmail.setText(info.getOrDefault("email", ""));
        txtSdt.setText(info.getOrDefault("so_dien_thoai", ""));

        txtCtTen.setText(cauHinhBus.getCongTyTen());
        txtCtDiaChi.setText(cauHinhBus.getCongTyDiaChi());
        txtCtSdt.setText(cauHinhBus.getCongTyDienThoai());
        txtCtEmail.setText(cauHinhBus.getCongTyEmail());
        txtCtMst.setText(cauHinhBus.getCongTyMaSoThue());
        spnBanGhi.setValue(cauHinhBus.getSoBanGhiTrang());

        cboTheme.setSelectedIndex("dark".equalsIgnoreCase(cauHinhBus.getTheme()) ? 1 : 0);
    }

    private void luuProfile() {
        try {
            authBUS.updateProfile(txtHoTen.getText(), txtEmail.getText(), txtSdt.getText());
            Dialogs.info(this, "Đã lưu hồ sơ cá nhân.");
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void doiMatKhau() {
        try {
            authBUS.changePassword(
                    new String(txtOldPwd.getPassword()),
                    new String(txtNewPwd.getPassword()),
                    new String(txtCfPwd.getPassword()));
            txtOldPwd.setText("");
            txtNewPwd.setText("");
            txtCfPwd.setText("");
            Dialogs.info(this, "Đổi mật khẩu thành công!");
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void luuHeThong() {
        try {
            cauHinhBus.luuHeThong(
                    txtCtTen.getText(), txtCtDiaChi.getText(), txtCtSdt.getText(),
                    txtCtEmail.getText(), txtCtMst.getText(),
                    (Integer) spnBanGhi.getValue());
            Dialogs.info(this, "Đã lưu cấu hình hệ thống.");
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void apDungTheme() {
        try {
            String theme = cboTheme.getSelectedIndex() == 1 ? "dark" : "light";
            cauHinhBus.luuTheme(theme);

            if ("dark".equals(theme)) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            Theme.applyFor(Session.current().maQuyen());

            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
                w.pack();
                w.setLocationRelativeTo(null);
            }
            Dialogs.info(this, "Đã áp dụng theme: " + cboTheme.getSelectedItem());
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }
}
