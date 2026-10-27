package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.AuthBUS;
import com.qlts.common.Session;
import com.qlts.common.Theme;
import com.qlts.entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private JLabel lblUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Quản lý trạng thái nút đang được chọn
    private final List<JButton> menuButtons = new ArrayList<>();
    private JButton activeButton;

    public MainFrame() {
        super("Quản lý tài sản và thiết bị - Trường Đại học");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        TaiKhoan user = Session.current();
        int maQuyen = user.maQuyen();
        Theme.applyFor(maQuyen);

        String role = maQuyen == 1 ? "QUẢN TRỊ VIÊN" : "NHÂN VIÊN";

        // ==========================================
        // 1. HEADER (Thanh tiêu đề trên cùng)
        // ==========================================
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
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

        // ==========================================
        // 2. KHỞI TẠO SIDEBAR & CONTENT
        // ==========================================
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(0xF5F7FA));

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0xF8F9FA)); // Nền xám rất nhạt cho Sidebar
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        // Thêm các Module vào Menu
        if (Session.isAdmin()) {
            // ================= ADMIN =================
            addMenuGroup(sidebar, "QUẢN LÝ TÀI SẢN");
            addMenu(sidebar, "Tài sản",      new TaiSanPanel());
            addMenu(sidebar, "Thiết bị",     new ThietBiPanel());
            addMenu(sidebar, "Phòng ban",    new PhongBanPanel());
            addMenu(sidebar, "Nhân viên",    new NhanVienPanel());

            addMenuGroup(sidebar, "NGHIỆP VỤ");
            addMenu(sidebar, "Cấp phát",     new CapPhatPanel());
            addMenu(sidebar, "Điều chuyển",  new DieuChuyenPanel());
            addMenu(sidebar, "Yêu cầu",      new YeuCauPanel());
            addMenu(sidebar, "Nhập kho",     new NhapKhoPanel());
            addMenu(sidebar, "Bảo trì",      new BaoTriPanel());
            addMenu(sidebar, "Kiểm kê",      new KiemKePanel());
            addMenu(sidebar, "Thanh lý",     new ThanhLyPanel());

            addMenuGroup(sidebar, "HỆ THỐNG");
            addMenu(sidebar, "Báo cáo",      new BaoCaoThongKePanel());
            addMenu(sidebar, "Nhật ký",      new NhatKyPanel());
            addMenu(sidebar, "Cài đặt",       new CaiDatPanel());
        } else {
            // ================= NHÂN VIÊN =================
            addMenuGroup(sidebar, "TRA CỨU");
            addMenu(sidebar, "Tài sản",      new TaiSanPanel());
            addMenu(sidebar, "Thiết bị",     new ThietBiPanel());

            addMenuGroup(sidebar, "YÊU CẦU CỦA TÔI");
            addMenu(sidebar, "Yêu cầu cấp",  new YeuCauPanel());
            addMenu(sidebar, "Báo hỏng/BT",  new BaoTriPanel());

            addMenuGroup(sidebar, "TÀI KHOẢN");
            addMenu(sidebar, "Cài đặt",       new CaiDatPanel());
        }

        // Đẩy menu lên trên cùng (khoảng trống dư ra ở dưới)
        sidebar.add(Box.createVerticalGlue());

        // Bọc Sidebar trong JScrollPane để có thể cuộn lên xuống
        JScrollPane scrollSidebar = new JScrollPane(sidebar);
        scrollSidebar.setBorder(null);
        scrollSidebar.setPreferredSize(new Dimension(240, 0)); // Độ rộng của Sidebar
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(16);
        scrollSidebar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xE0E0E0)));

        // ==========================================
        // 3. GẮN VÀO FRAME
        // ==========================================
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(scrollSidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Mở tab đầu tiên mặc định
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).doClick();
        }

        setMinimumSize(new Dimension(1000, 600));
        setSize(1280, 720);
        setLocationRelativeTo(null);
    }

    /**
     * Tạo tiêu đề nhóm chức năng trên Sidebar
     */
    private void addMenuGroup(JPanel sidebar, String groupName) {
        JLabel lblGroup = new JLabel(groupName);
        lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGroup.setForeground(new Color(0x9E9E9E));
        lblGroup.setBorder(new EmptyBorder(15, 10, 5, 10));
        lblGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblGroup);
    }

    /**
     * Thêm 1 nút vào Sidebar và 1 Panel tương ứng vào Content
     */
    private void addMenu(JPanel sidebar, String title, JPanel panel) {
        String cardName = title; 
        contentPanel.add(panel, cardName);

        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        
        // 🔥 Style FlatLaf: Bo góc, không viền, khoảng cách trong đẹp
        btn.putClientProperty(FlatClientProperties.STYLE, 
                "arc:10; borderWidth:0; focusWidth:0; innerFocusWidth:0; margin:0,10,0,10;");
        
        btn.setBackground(Color.WHITE); // Nền trắng nổi bật trên nền xám
        btn.setForeground(new Color(0x333333)); // Chữ xám đậm
        btn.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Sự kiện khi click
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            updateActiveButton(btn);
        });

        // Hiệu ứng hover (Rê chuột)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (activeButton != btn) {
                    btn.setBackground(new Color(0xE3F2FD)); // Màu xanh nhạt
                    btn.setForeground(Theme.primary());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (activeButton != btn) {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(new Color(0x333333));
                }
            }
        });

        menuButtons.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6)); // Khoảng cách giữa các nút
    }

    /**
     * Đổi màu nút đang được chọn (Active)
     */
    private void updateActiveButton(JButton btn) {
        // Reset nút cũ
        if (activeButton != null) {
            activeButton.setBackground(Color.WHITE);
            activeButton.setForeground(new Color(0x333333));
        }
        // Set nút mới (Màu chủ đạo + chữ trắng)
        activeButton = btn;
        activeButton.setBackground(Theme.primary());
        activeButton.setForeground(Color.WHITE);
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