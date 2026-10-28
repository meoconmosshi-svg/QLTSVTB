package panel;

import dialog.NguoiDungDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.NguoiDungBUS;
import com.qlts.common.Session;
import com.qlts.component.Dialogs;
import com.qlts.dao.NguoiDungDAO.UserFull;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyNguoiDungPanel extends JPanel {

    private final NguoiDungBUS bus = new NguoiDungBUS();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch = new JTextField(18);
    // CHỈ 2 ROLE
    private final JComboBox<String> cboQuyen = new JComboBox<>(new String[]{
            "Tất cả quyền", "Quản trị viên", "Nhân viên"});
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Hoạt động", "Đã khóa"});
    private List<UserFull> data = List.of();
    private final JLabel lblCount = new JLabel(" ");

    public QuanLyNguoiDungPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new String[]{
                "Mã", "Tên đăng nhập", "Họ tên", "Email", "SĐT",
                "Quyền", "Trạng thái", "Lý do khóa"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xD0D7DE));
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setSelectionForeground(new Color(0x0D47A1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF7F9FB));
                    if (c == 6 && "Đã khóa".equals(String.valueOf(v))) {
                        comp.setForeground(new Color(0xC62828));
                    } else {
                        comp.setForeground(new Color(0x212121));
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return comp;
            }
        });

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xE8EEF4));
        table.getTableHeader().setForeground(new Color(0x1A237E));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBackground(new Color(0xFAFBFC));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7DE)),
                new EmptyBorder(8, 8, 8, 8)));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSearch = new JLabel("🔍 Tìm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row1.add(lblSearch);

        txtSearch.setPreferredSize(new Dimension(220, 30));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row1.add(txtSearch);

        JLabel lblQ = new JLabel("Quyền:");
        lblQ.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row1.add(lblQ);

        cboQuyen.setPreferredSize(new Dimension(140, 30));
        cboQuyen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row1.add(cboQuyen);

        JLabel lblT = new JLabel("Trạng thái:");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row1.add(lblT);

        cboTrangThai.setPreferredSize(new Dimension(120, 30));
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row1.add(cboTrangThai);

        JButton btnSearch = new JButton("Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleNeutralBtn(btnSearch);
        styleNeutralBtn(btnRefresh);
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        cboQuyen.addActionListener(e -> refresh());
        cboTrangThai.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboQuyen.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            refresh();
        });
        row1.add(btnSearch);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAct = new JLabel("Thao tác:");
        lblAct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        row2.add(lblAct);

        JButton btnThem = new JButton("➕ Thêm");
        JButton btnSua = new JButton("✎ Sửa");
        JButton btnXoa = new JButton("🗑 Xóa");
        JButton btnKhoa = new JButton("🔒 Khóa / Mở");
        JButton btnReset = new JButton("🔑 Reset MK");

        styleBtn(btnThem, new Color(0x2E7D32));
        styleBtn(btnSua, new Color(0x1565C0));
        styleBtn(btnXoa, new Color(0xC62828));
        styleBtn(btnKhoa, new Color(0xEF6C00));
        styleBtn(btnReset, new Color(0x6A1B9A));

        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());
        btnKhoa.addActionListener(e -> khoaMo());
        btnReset.addActionListener(e -> resetMk());

        row2.add(btnThem);
        row2.add(btnSua);
        row2.add(btnXoa);
        row2.add(btnKhoa);
        row2.add(btnReset);

        toolbar.add(row1);
        toolbar.add(Box.createVerticalStrut(2));
        toolbar.add(row2);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));

        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCount.setForeground(new Color(0x616161));
        lblCount.setBorder(new EmptyBorder(4, 4, 0, 0));

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblCount, BorderLayout.SOUTH);

        refresh();
    }

    private void styleBtn(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 32));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    private void styleNeutralBtn(JButton b) {
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(90, 30));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    public void refresh() {
        try {
            // CHỈ 2 ROLE: index 1 → 1 (Admin), index 2 → 3 (NV)
            Integer quyen = switch (cboQuyen.getSelectedIndex()) {
                case 1 -> 1;
                case 2 -> 3;
                default -> null;
            };
            Integer trangThai = switch (cboTrangThai.getSelectedIndex()) {
                case 1 -> 1;
                case 2 -> 0;
                default -> null;
            };
            data = bus.search(txtSearch.getText(), quyen, trangThai);
            model.setRowCount(0);
            for (UserFull u : data) {
                model.addRow(new Object[]{
                        u.maTaiKhoan(), u.tenDangNhap(), u.hoTen(),
                        Parse.nz(u.email()), Parse.nz(u.soDienThoai()),
                        u.tenQuyen(), u.trangThaiText(), Parse.nz(u.lyDoKhoa())
                });
            }
            lblCount.setText("📋 " + data.size() + " người dùng");
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private UserFull selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Dialogs.warn(this, "Hãy chọn một dòng trong bảng."); return null; }
        return data.get(table.convertRowIndexToModel(r));
    }

    private void them() {
        NguoiDungDialog d = new NguoiDungDialog(SwingUtilities.getWindowAncestor(this), null);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void sua() {
        UserFull u = selected(); if (u == null) return;
        NguoiDungDialog d = new NguoiDungDialog(SwingUtilities.getWindowAncestor(this), u);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void xoa() {
        UserFull u = selected(); if (u == null) return;
        if (u.maTaiKhoan() == Session.userId()) {
            Dialogs.warn(this, "Không thể xóa tài khoản đang đăng nhập.");
            return;
        }
        if (!Dialogs.confirm(this, "Xóa tài khoản \"" + u.tenDangNhap() + "\"?")) return;
        try {
            bus.xoa(u.maTaiKhoan(), u.tenDangNhap());
            refresh();
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }

    private void khoaMo() {
        UserFull u = selected(); if (u == null) return;
        try {
            if (u.trangThai() == 1) {
                String lyDo = JOptionPane.showInputDialog(this,
                        "Lý do khóa tài khoản \"" + u.tenDangNhap() + "\":",
                        "Khóa tài khoản", JOptionPane.QUESTION_MESSAGE);
                if (lyDo == null) return;
                bus.khoa(u.maTaiKhoan(), lyDo);
                Dialogs.info(this, "Đã khóa tài khoản.");
            } else {
                if (!Dialogs.confirm(this, "Mở khóa tài khoản \"" + u.tenDangNhap() + "\"?")) return;
                bus.moKhoa(u.maTaiKhoan());
                Dialogs.info(this, "Đã mở khóa tài khoản.");
            }
            refresh();
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }

    private void resetMk() {
        UserFull u = selected(); if (u == null) return;
        if (!Dialogs.confirm(this,
                "Reset mật khẩu của \"" + u.tenDangNhap() + "\" về mặc định ("
                + NguoiDungBUS.DEFAULT_PASSWORD + ")?")) return;
        try {
            bus.resetMatKhau(u.maTaiKhoan(), u.tenDangNhap());
            Dialogs.info(this, "Đã reset mật khẩu về: " + NguoiDungBUS.DEFAULT_PASSWORD);
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }
}