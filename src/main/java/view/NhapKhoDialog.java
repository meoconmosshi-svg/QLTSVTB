package com.qlts.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.PhieuNhapKhoBUS;
import com.qlts.component.Dialogs;
import com.qlts.entity.ChiTietPhieuNhap;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.entity.NhaCungCap;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NhapKhoDialog extends JDialog {

    private final PhieuNhapKhoBUS bus = new PhieuNhapKhoBUS();
    private final DanhMucBUS danhMuc = new DanhMucBUS();
    private boolean saved = false;

    private final JComboBox<NhaCungCap> cboNcc;
    private final JTextField txtGhiChu = new JTextField(30);
    private final ChiTietTableModel model;
    private final JTable table;
    private final JLabel lblTongTien = new JLabel("0 VNĐ");
    private final List<LoaiTaiSan> dsLoai;

    public NhapKhoDialog(Window owner) {
        super(owner, "Tạo phiếu nhập kho", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        dsLoai = danhMuc.loaiTaiSan();
        List<NhaCungCap> dsNcc = danhMuc.nhaCungCap();
        cboNcc = new JComboBox<>(dsNcc.toArray(new NhaCungCap[0]));
        cboNcc.insertItemAt(null, 0); cboNcc.setSelectedIndex(0);
        cboNcc.setPreferredSize(new Dimension(300, 32));
        cboNcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNcc.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);

        // ===== FORM HEADER =====
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(16, 20, 8, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;

        JLabel title = new JLabel(" Phiếu nhập kho");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1565C0));
        header.add(title, gc);

        gc.gridwidth = 1;
        gc.gridy++; gc.gridx = 0;
        header.add(lbl("Nhà cung cấp (*)"), gc);
        gc.gridx = 1; header.add(cboNcc, gc);

        gc.gridy++; gc.gridx = 0;
        header.add(lbl("Ghi chú"), gc);
        gc.gridx = 1; header.add(txtGhiChu, gc);

        // ===== BẢNG CHI TIẾT =====
        model = new ChiTietTableModel();
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setSelectionForeground(new Color(0x0D47A1));
        table.setShowGrid(true);
        table.setGridColor(new Color(0xD0D7DE));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0xE8EEF4));
        table.getTableHeader().setForeground(new Color(0x1A237E));
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        // Editor cho cột "Loại tài sản" (JComboBox)
        JComboBox<LoaiTaiSan> cboLoaiEditor = new JComboBox<>(dsLoai.toArray(new LoaiTaiSan[0]));
        cboLoaiEditor.insertItemAt(null, 0);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cboLoaiEditor));

        // Set độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(220); // Tên TS
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Loại
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Serial
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // SL
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Đơn giá
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Thành tiền

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(820, 220));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));

        // ===== TOOLBAR BẢNG =====
        JPanel tableToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        tableToolbar.setBackground(new Color(0xFAFBFC));
        tableToolbar.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(0xD0D7DE)));

        JButton btnThemDong = new JButton("➕ Thêm dòng");
        JButton btnXoaDong = new JButton("🗑 Xóa dòng");
        JButton btnXoaHet = new JButton("✖ Xóa hết");
        styleSmall(btnThemDong, new Color(0x2E7D32));
        styleSmall(btnXoaDong, new Color(0xC62828));
        styleSmall(btnXoaHet, new Color(0x616161));

        btnThemDong.addActionListener(e -> model.addEmptyRow());
        btnXoaDong.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { Dialogs.warn(this, "Chọn dòng cần xóa."); return; }
            model.removeRow(r);
            capNhatTongTien();
        });
        btnXoaHet.addActionListener(e -> {
            if (Dialogs.confirm(this, "Xóa toàn bộ dòng?")) {
                model.setRowCount(0);
                capNhatTongTien();
            }
        });

        tableToolbar.add(btnThemDong);
        tableToolbar.add(btnXoaDong);
        tableToolbar.add(btnXoaHet);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(Color.WHITE);
        tableWrap.setBorder(new EmptyBorder(0, 20, 8, 20));
        tableWrap.add(tableToolbar, BorderLayout.NORTH);
        tableWrap.add(scroll, BorderLayout.CENTER);

        // ===== FOOTER =====
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(8, 20, 12, 20));

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        totalPanel.setOpaque(false);
        JLabel lblTongLabel = new JLabel("TỔNG TIỀN:");
        lblTongLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongLabel.setForeground(new Color(0x424242));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(0xC62828));
        totalPanel.add(lblTongLabel);
        totalPanel.add(lblTongTien);

        JButton btnLuu = new JButton("💾 Lưu phiếu nhập");
        JButton btnHuy = new JButton("Hủy");
        stylePrimary(btnLuu, new Color(0x2E7D32));
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnHuy.setPreferredSize(new Dimension(90, 38));

        btnLuu.addActionListener(e -> doLuu());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnHuy);
        buttonPanel.add(btnLuu);

        footer.add(totalPanel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);

        // Thêm listener để update tổng khi model thay đổi
        model.addTableModelListener(e -> capNhatTongTien());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Thêm 1 dòng trống ban đầu
        model.addEmptyRow();

        pack();
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
    }

    // ===== HELPERS =====
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x424242));
        return l;
    }

    private void styleSmall(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.putClientProperty(FlatClientProperties.STYLE, "arc:6; borderWidth:0; focusWidth:0;");
    }

    private void stylePrimary(JButton b, Color bg) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(180, 38));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    private void capNhatTongTien() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object v = model.getValueAt(i, 5);
            if (v instanceof BigDecimal bd) total = total.add(bd);
        }
        lblTongTien.setText(Parse.fmt(total) + " VNĐ");
    }

    private void doLuu() {
        try {
            NhaCungCap ncc = (NhaCungCap) cboNcc.getSelectedItem();
            if (ncc == null) throw new RuntimeException("Vui lòng chọn nhà cung cấp.");

            List<ChiTietPhieuNhap> chiTietList = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                ChiTietPhieuNhap ct = model.getChiTiet(i);
                if (ct == null) continue; // dòng trống
                chiTietList.add(ct);
            }
            if (chiTietList.isEmpty())
                throw new RuntimeException("Phiếu nhập phải có ít nhất 1 mặt hàng.");

            int maPn = bus.taoPhieuNhap(ncc.maNcc(), txtGhiChu.getText(), chiTietList);

            saved = true;
            Dialogs.info(this, "Đã lưu phiếu nhập #" + maPn
                    + "\nTổng " + chiTietList.size() + " mặt hàng đã vào kho.");
            dispose();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    public boolean isSaved() { return saved; }

    // ================== CUSTOM TABLE MODEL ==================
    private class ChiTietTableModel extends DefaultTableModel {

        public ChiTietTableModel() {
            super(new String[]{"Tên tài sản (*)", "Loại tài sản", "Serial", "SL (*)",
                    "Đơn giá", "Thành tiền"}, 0);
        }

        @Override public boolean isCellEditable(int r, int c) {
            return c != 5; // cột Thành tiền không cho sửa
        }

        @Override public Class<?> getColumnClass(int col) {
            return switch (col) {
                case 1 -> LoaiTaiSan.class;
                case 3 -> Integer.class;
                case 5 -> BigDecimal.class;
                default -> String.class;
            };
        }

        @Override public void setValueAt(Object value, int row, int col) {
            super.setValueAt(value, row, col);
            // Tự tính lại thành tiền khi SL hoặc đơn giá thay đổi
            if (col == 3 || col == 4) {
                Object slObj = getValueAt(row, 3);
                Object dgObj = getValueAt(row, 4);
                int sl = parseIntSafe(slObj);
                BigDecimal dg = parseMoneySafe(dgObj);
                BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));
                super.setValueAt(tt, row, 5);
            }
        }

        public void addEmptyRow() {
            addRow(new Object[]{null, null, null, 1, BigDecimal.ZERO, BigDecimal.ZERO});
        }

        /** Chuyển 1 dòng thành ChiTietPhieuNhap. Trả null nếu dòng trống. */
        public ChiTietPhieuNhap getChiTiet(int row) {
            String ten = str(getValueAt(row, 0));
            if (ten == null || ten.isBlank()) return null;

            LoaiTaiSan loai = (LoaiTaiSan) getValueAt(row, 1);
            String serial = str(getValueAt(row, 2));
            int sl = parseIntSafe(getValueAt(row, 3));
            BigDecimal dg = parseMoneySafe(getValueAt(row, 4));
            BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));

            return new ChiTietPhieuNhap(
                    null, null, null,
                    ten.trim(),
                    loai == null ? null : loai.maLoai(),
                    loai == null ? null : loai.tenLoai(),
                    serial,
                    sl, dg, tt, "Mới");
        }

        private int parseIntSafe(Object o) {
            if (o == null) return 1;
            if (o instanceof Integer i) return Math.max(1, i);
            try { return Math.max(1, Integer.parseInt(o.toString().trim())); }
            catch (Exception e) { return 1; }
        }

        private BigDecimal parseMoneySafe(Object o) {
            if (o == null) return BigDecimal.ZERO;
            if (o instanceof BigDecimal bd) return bd.signum() < 0 ? BigDecimal.ZERO : bd;
            try {
                String s = o.toString().replace(",", "").replace(".", "").trim();
                if (s.isEmpty()) return BigDecimal.ZERO;
                BigDecimal v = new BigDecimal(s);
                return v.signum() < 0 ? BigDecimal.ZERO : v;
            } catch (Exception e) { return BigDecimal.ZERO; }
        }

        private String str(Object o) { return o == null ? null : o.toString(); }
    }
}