package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.YeuCauCapPhatBUS;
import com.qlts.common.Session;
import com.qlts.component.Dialogs;
import com.qlts.entity.YeuCauCapPhat;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class YeuCauPanel extends JPanel {

    private final YeuCauCapPhatBUS bus = new YeuCauCapPhatBUS();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{
            "Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối", "Đã cấp"});
    private final JLabel lblCount = new JLabel(" ");
    private List<YeuCauCapPhat> data = List.of();

    private JButton btnDuyet;
    private JButton btnTuChoi;

    public YeuCauPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        model = new DefaultTableModel(new String[]{
                "Mã YC", "Người YC", "Loại YC", "Loại tài sản", "SL",
                "Ngày YC", "Ngày mong muốn", "Trạng thái", "Người duyệt", "Lý do"}, 0) {
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
                    if (c == 7) {
                        String tt = String.valueOf(v);
                        comp.setForeground(switch (tt) {
                            case "Chờ duyệt" -> new Color(0xEF6C00);
                            case "Đã duyệt"  -> new Color(0x2E7D32);
                            case "Từ chối"   -> new Color(0xC62828);
                            case "Đã cấp"    -> new Color(0x1565C0);
                            default -> new Color(0x212121);
                        });
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
        row1.add(new JLabel("🔍 Tìm:"));
        txtSearch.setPreferredSize(new Dimension(220, 30));
        row1.add(txtSearch);
        row1.add(new JLabel("Trạng thái:"));
        cboTrangThai.setPreferredSize(new Dimension(140, 30));
        row1.add(cboTrangThai);

        JButton btnSearch = new JButton("Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleNeutral(btnSearch); styleNeutral(btnRefresh);
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        cboTrangThai.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText(""); cboTrangThai.setSelectedIndex(0); refresh();
        });
        row1.add(btnSearch);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(new JLabel("Thao tác:"));

        JButton btnGui = new JButton("📨 Gửi yêu cầu");
        JButton btnHuy = new JButton("✖ Hủy YC");
        btnDuyet = new JButton("✅ Duyệt");
        btnTuChoi = new JButton("❌ Từ chối");
        JButton btnCapPhat = new JButton("📦 Cấp phát");

        styleBtn(btnGui, new Color(0x1565C0));
        styleBtn(btnHuy, new Color(0x616161));
        styleBtn(btnDuyet, new Color(0x2E7D32));
        styleBtn(btnTuChoi, new Color(0xC62828));
        styleBtn(btnCapPhat, new Color(0x6A1B9A));

        btnGui.addActionListener(e -> guiYeuCau());
        btnHuy.addActionListener(e -> huyYeuCau());
        btnDuyet.addActionListener(e -> duyet());
        btnTuChoi.addActionListener(e -> tuChoi());
        btnCapPhat.addActionListener(e -> capPhat());

        row2.add(btnGui);
        row2.add(btnHuy);

        // CHỈ ADMIN thấy nút duyệt/từ chối/cấp phát
        if (Session.canApprove()) {
            row2.add(btnDuyet);
            row2.add(btnTuChoi);
        }
        if (Session.canModify()) {
            row2.add(btnCapPhat);
        }

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

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            updateButtonState();
        });

        refresh();
    }

    private void updateButtonState() {
        if (btnDuyet == null || btnTuChoi == null) return;
        int r = table.getSelectedRow();
        if (r < 0) {
            btnDuyet.setEnabled(true);
            btnTuChoi.setEnabled(true);
            return;
        }
        YeuCauCapPhat y = data.get(table.convertRowIndexToModel(r));
        boolean enable = y.choDuyet();
        btnDuyet.setEnabled(enable);
        btnTuChoi.setEnabled(enable);
        btnDuyet.setToolTipText(enable ? "Duyệt yêu cầu" : "Chỉ duyệt được yêu cầu đang chờ");
        btnTuChoi.setToolTipText(enable ? "Từ chối yêu cầu" : "Chỉ từ chối được yêu cầu đang chờ");
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

    private void styleNeutral(JButton b) {
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(90, 30));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    public void refresh() {
        try {
            data = bus.search(txtSearch.getText(), (String) cboTrangThai.getSelectedItem());
            model.setRowCount(0);
            for (YeuCauCapPhat y : data) {
                model.addRow(new Object[]{
                        y.maYcCode(), y.tenNguoiYeuCau(), y.loaiYeuCau(),
                        Parse.nz(y.tenLoai()), y.soLuong(),
                        Parse.fmt(y.ngayYeuCau()), Parse.fmt(y.ngayMongMuon()),
                        y.trangThai(), Parse.nz(y.tenNguoiDuyet()),
                        Parse.nz(y.lyDoTuChoi() != null ? y.lyDoTuChoi() : y.lyDo())
                });
            }
            lblCount.setText("📋 " + data.size() + " yêu cầu");
            updateButtonState();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private YeuCauCapPhat selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Dialogs.warn(this, "Hãy chọn một dòng trong bảng."); return null; }
        return data.get(table.convertRowIndexToModel(r));
    }

    private void guiYeuCau() {
        YeuCauDialog d = new YeuCauDialog(SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void huyYeuCau() {
        YeuCauCapPhat y = selected(); if (y == null) return;
        if (!Dialogs.confirm(this, "Hủy yêu cầu \"" + y.maYcCode() + "\"?")) return;
        try {
            bus.huy(y);
            refresh();
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }

    private void duyet() {
        YeuCauCapPhat y = selected(); if (y == null) return;
        if (!y.choDuyet()) {
            Dialogs.warn(this, "Chỉ duyệt được yêu cầu đang chờ.");
            return;
        }
        if (!Dialogs.confirm(this, "Duyệt yêu cầu \"" + y.maYcCode() + "\"?")) return;
        try {
            bus.duyet(y);
            Dialogs.info(this, "Đã duyệt yêu cầu.");
            refresh();
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }

    private void tuChoi() {
        YeuCauCapPhat y = selected(); if (y == null) return;
        if (!y.choDuyet()) {
            Dialogs.warn(this, "Chỉ từ chối được yêu cầu đang chờ.");
            return;
        }
        String lyDo = JOptionPane.showInputDialog(this,
                "Lý do từ chối \"" + y.maYcCode() + "\":",
                "Từ chối yêu cầu", JOptionPane.QUESTION_MESSAGE);
        if (lyDo == null) return;
        try {
            bus.tuChoi(y, lyDo);
            Dialogs.info(this, "Đã từ chối yêu cầu.");
            refresh();
        } catch (RuntimeException ex) { Dialogs.error(this, ex); }
    }

    private void capPhat() {
        YeuCauCapPhat y = selected(); if (y == null) return;
        if (!y.daDuyet()) {
            Dialogs.warn(this, "Chỉ cấp phát cho yêu cầu đã được duyệt.");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Chức năng cấp phát nhanh cho yêu cầu " + y.maYcCode() + "\n"
                + "Hãy vào tab 'Cấp phát' để tạo phiếu và liên kết với yêu cầu này.",
                "Hướng dẫn", JOptionPane.INFORMATION_MESSAGE);
    }
}