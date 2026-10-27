package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.AuthBUS;
import com.qlts.common.Session;
import com.qlts.common.Theme;
import com.qlts.entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private JLabel lblUser;

    public MainFrame() {
        super("Quản lý tài sản và thiết bị - Trường Đại học");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        TaiKhoan user = Session.current();
        int maQuyen = user.maQuyen();
        Theme.applyFor(maQuyen);

        String role = maQuyen == 1 ? "QUẢN TRỊ VIÊN" : "NHÂN VIÊN";

        // ========== HEADER ==========
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(Theme.primary());
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        lblUser = new JLabel("Xin chào, " + user.hoTen() + "  •  " + role);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Theme.textOn());

        JLabel lblApp = new JLabel("QUẢN LÝ TÀI SẢN & THIẾT BỊ");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblApp.setForeground(Theme.textOn());

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBackground(Theme.dark());
        btnLogout.setForeground(Theme.textOn());
        btnLogout.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
        btnLogout.addActionListener(e -> logout());

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerLeft.setOpaque(false);
        headerLeft.add(lblApp);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);
        headerRight.add(lblUser);
        headerRight.add(btnLogout);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // ========== TABS THEO ROLE ==========
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        if (Session.isAdmin()) {
            // ====================== ADMIN ======================
            tabs.addTab("🏷 Tài sản",     new TaiSanPanel());
            tabs.addTab("💻 Thiết bị",     new ThietBiPanel());
            tabs.addTab("👥 Nhân viên",    new NhanVienPanel());
            tabs.addTab("📦 Cấp phát",     new CapPhatPanel());
            tabs.addTab("📨 Yêu cầu",      new YeuCauPanel());
            tabs.addTab("📥 Nhập kho",     new NhapKhoPanel());

            // TODO Đợt 5 — bỏ comment khi có panel:
            // tabs.addTab("🔄 Điều chuyển",  new DieuChuyenPanel());
            // tabs.addTab("🔧 Bảo trì",      new BaoTriPanel());
            // tabs.addTab("📋 Kiểm kê",     new KiemKePanel());
            // tabs.addTab("🗑 Thanh lý",    new ThanhLyPanel());
            // tabs.addTab("📉 Khấu hao",    new KhauHaoPanel());

            tabs.addTab("📊 Báo cáo",      new BaoCaoThongKePanel());
            tabs.addTab("📜 Nhật ký",      new NhatKyPanel());
            tabs.addTab("⚙️ Cài đặt",       new CaiDatPanel());

        } else {
            // ====================== NHÂN VIÊN ======================
            // Chỉ xem + gửi yêu cầu (không có nút thêm/sửa/xóa)
            tabs.addTab("🏷 Tài sản",     new TaiSanPanel());
            tabs.addTab("💻 Thiết bị",     new ThietBiPanel());
            tabs.addTab("📨 Yêu cầu",      new YeuCauPanel());
            tabs.addTab("⚙️ Cài đặt",       new CaiDatPanel());
        }

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setMinimumSize(new Dimension(950, 540));
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    public void refreshUserName() {
        TaiKhoan user = Session.current();
        if (user == null) return;
        String role = user.maQuyen() == 1 ? "QUẢN TRỊ VIÊN" : "NHÂN VIÊN";
        lblUser.setText("Xin chào, " + user.hoTen() + "  •  " + role);
    }

    private void logout() {
        new AuthBUS().logout();
        Theme.applyFor(1);
        new LoginFrame().setVisible(true);
        dispose();
    }
}