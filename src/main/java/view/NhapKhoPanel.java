package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.PhieuNhapKhoBUS;
import com.qlts.common.Session;
import com.qlts.component.Dialogs;
import com.qlts.entity.ChiTietPhieuNhap;
import com.qlts.entity.PhieuNhapKho;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class NhapKhoPanel extends JPanel {

    private final PhieuNhapKhoBUS bus = new PhieuNhapKhoBUS();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch = new JTextField(16);
    private final JTextField txtTuNgay = new JTextField(11);
    private final JTextField txtDenNgay = new JTextField(11);
    private final JLabel lblCount = new JLabel(" ");
    private List<PhieuNhapKho> data = List.of();

    public NhapKhoPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{
                "Mã phiếu", "Nhà cung cấp", "Ngày nhập",
                "Người nhập", "Số mặt hàng", "Tổng tiền", "Ghi chú"}, 0) {
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
                    comp.setForeground(new Color(0x212121));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (c == 5 && !sel) setHorizontalAlignment(SwingConstants.RIGHT);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return comp;
            }
        });

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xE8EEF4));
        table.getTableHeader().setForeground(new Color(0x1A237E));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Double-click → xem chi tiết
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    xemChiTiet();
                }
            }
        });

        // ===== TOOLBAR =====
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBackground(new Color(0xFAFBFC));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7DE)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Hàng 1: filter
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        row1.add(new JLabel("🔍 Tìm:"));
        txtSearch.setPreferredSize(new Dimension(180, 30));
        row1.add(txtSearch);

        row1.add(new JLabel("Từ ngày:"));
        txtTuNgay.setPreferredSize(new Dimension(110, 30));
        txtTuNgay.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "yyyy-MM-dd");
        row1.add(txtTuNgay);

        row1.add(new JLabel("Đến ngày:"));
        txtDenNgay.setPreferredSize(new Dimension(110, 30));
        txtDenNgay.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "yyyy-MM-dd");
        row1.add(txtDenNgay);

        JButton btnSearch = new JButton("Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleNeutral(btnSearch); styleNeutral(btnRefresh);
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        txtTuNgay.addActionListener(e -> refresh());
        txtDenNgay.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText(""); txtTuNgay.setText(""); txtDenNgay.setText("");
            refresh();
        });
        row1.add(btnSearch); row1.add(btnRefresh);

        // Hàng 2: actions
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(new JLabel("Thao tác:"));

        JButton btnTao = new JButton("📥 Tạo phiếu nhập");
        JButton btnXem = new JButton("👁 Xem chi tiết");
        styleBtn(btnTao, new Color(0x2E7D32));
        styleBtn(btnXem, new Color(0x6A1B9A));

        btnTao.addActionListener(e -> taoPhieu());
        btnXem.addActionListener(e -> xemChiTiet());

        row2.add(btnTao);
        row2.add(btnXem);

        // 🔥 Admin và Nhân viên đều có thể tạo phiếu nhập
        if (!Session.canManageWarehouse()) btnTao.setEnabled(false);

        toolbar.add(row1);
        toolbar.add(Box.createVerticalStrut(2));
        toolbar.add(row2);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));

        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCount.setForeground(new Color(0x616161));
        lblCount.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblCount, BorderLayout.SOUTH);

        refresh();
    }

    // ===== STYLE =====
    private void styleBtn(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 32));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    private void styleNeutral(JButton b) {
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(90, 30));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    // ===== LOGIC =====
    public void refresh() {
        try {
            LocalDate tu = txtTuNgay.getText().isBlank() ? null
                    : Parse.date(txtTuNgay.getText(), "Từ ngày");
            LocalDate den = txtDenNgay.getText().isBlank() ? null
                    : Parse.date(txtDenNgay.getText(), "Đến ngày");

            data = bus.search(txtSearch.getText(), tu, den);
            model.setRowCount(0);
            for (PhieuNhapKho p : data) {
                model.addRow(new Object[]{
                        p.maPnCode(),
                        Parse.nz(p.tenNcc()),
                        Parse.fmt(p.ngayNhap()),
                        Parse.nz(p.tenNguoiNhap()),
                        p.soMatHang(),
                        Parse.fmt(p.tongTien()) + " VNĐ",
                        Parse.nz(p.ghiChu())
                });
            }
            lblCount.setText("📋 " + data.size() + " phiếu nhập");
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void taoPhieu() {
        NhapKhoDialog d = new NhapKhoDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void xemChiTiet() {
        int r = table.getSelectedRow();
        if (r < 0) { Dialogs.warn(this, "Chọn một phiếu để xem."); return; }
        PhieuNhapKho pn = data.get(table.convertRowIndexToModel(r));
        List<ChiTietPhieuNhap> chiTiet = bus.chiTiet(pn.maPhieuNhap());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width:640px;font-family:Segoe UI;font-size:13px'>");
        sb.append("<h2 style='color:#1565C0;margin:0 0 12px 0'>Chi tiết phiếu ").append(pn.maPnCode()).append("</h2>");
        sb.append("<p style='color:#616161;margin:4px 0'>NCC: <b>").append(n(pn.tenNcc())).append("</b> • ");
        sb.append("Ngày nhập: <b>").append(Parse.fmt(pn.ngayNhap())).append("</b> • ");
        sb.append("Người nhập: <b>").append(n(pn.tenNguoiNhap())).append("</b></p>");

        sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%;margin-top:8px'>");
        sb.append("<tr style='background:#E8EEF4;color:#1A237E;font-weight:bold'>");
        sb.append("<th>Tên tài sản</th><th>Loại</th><th>Serial</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>");

        for (ChiTietPhieuNhap ct : chiTiet) {
            sb.append("<tr>");
            sb.append("<td>").append(esc(ct.tenTaiSan())).append("</td>");
            sb.append("<td>").append(n(ct.tenLoai())).append("</td>");
            sb.append("<td>").append(n(ct.soSerial())).append("</td>");
            sb.append("<td align='center'>").append(ct.soLuong()).append("</td>");
            sb.append("<td align='right'>").append(Parse.fmt(ct.donGia())).append("</td>");
            sb.append("<td align='right'>").append(Parse.fmt(ct.thanhTien())).append("</td>");
            sb.append("</tr>");
        }
        sb.append("<tr style='background:#F7F9FB;font-weight:bold'>");
        sb.append("<td colspan='5' align='right'>TỔNG CỘNG:</td>");
        sb.append("<td align='right' style='color:#C62828'>").append(Parse.fmt(pn.tongTien())).append(" VNĐ</td>");
        sb.append("</tr></table></body></html>");

        JEditorPane editor = new JEditorPane("text/html", sb.toString());
        editor.setEditable(false);
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        editor.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(700, 400));

        JOptionPane.showMessageDialog(this, scroll, "Chi tiết phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
    }

    private String n(String s) { return s == null || s.isBlank() ? "—" : s; }
    private String esc(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}