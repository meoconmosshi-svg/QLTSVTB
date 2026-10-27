package com.qlts.component;

import com.qlts.common.Session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Panel danh sách dùng chung: ô tìm kiếm + bảng + các nút Thêm/Sửa/Xóa.
 * - Tooltip khi hover cell
 * - Double-click mở dialog chi tiết
 * - Tự động giãn cột theo nội dung
 */
public abstract class CrudPanel<T> extends JPanel {

    // ===== BẢNG MÀU / FONT DÙNG CHUNG =====
    private static final Font FONT_BASE   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    private static final Color GRID_COLOR    = new Color(0xD0D7DE);
    private static final Color HEADER_BG     = new Color(0xE8EEF4);
    private static final Color HEADER_FG     = new Color(0x1A237E);
    private static final Color HEADER_BORDER = new Color(0xB0B8C0);
    private static final Color ROW_ODD       = Color.WHITE;
    private static final Color ROW_EVEN      = new Color(0xF7F9FB);
    private static final Color ROW_TEXT      = new Color(0x212121);
    private static final Color SELECTION_BG  = new Color(0xBBDEFB);
    private static final Color SELECTION_FG  = new Color(0x0D47A1);

    // Kích thước cột khi auto-fit
    private static final int COL_MIN_WIDTH = 80;
    private static final int COL_MAX_WIDTH = 320;

    protected final DefaultTableModel model;
    protected final JTable table;
    private final JTextField txtSearch = new JTextField(22);
    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final JLabel lblCount = new JLabel(" ");
    private List<T> data = List.of();
    /** Chỉ auto-fit cột 1 lần đầu — tránh mỗi lần refresh lại làm rộng cột. */
    private boolean firstFitDone = false;

    /** @param crud true = hiện nút Thêm/Sửa/Xóa chuẩn (ẩn nếu người dùng chỉ có quyền xem) */
    protected CrudPanel(String[] columns, boolean crud) {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== MODEL ==========
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // ========== TABLE ==========
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(FONT_BASE);
        table.setForeground(ROW_TEXT);

        // Đường kẻ ô
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(GRID_COLOR);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Auto-resize: cho phép các cột co giãn vừa khung
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Màu selection
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(SELECTION_FG);

        // ===== TOOLTIP KHI HOVER CELL =====
        table.setToolTipText("");
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object val = table.getValueAt(row, col);
                    table.setToolTipText(val == null ? null : val.toString());
                } else {
                    table.setToolTipText(null);
                }
            }
        });

        // ===== DOUBLE-CLICK MỞ DIALOG CHI TIẾT =====
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    hienThiChiTiet();
                }
            }
        });

        // ===== RENDERER: tô màu xen kẽ + căn lề =====
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(SELECTION_BG);
                    c.setForeground(SELECTION_FG);
                } else {
                    c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                    c.setForeground(ROW_TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // ========== HEADER ==========
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, HEADER_BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                lbl.setFont(FONT_HEADER);
                lbl.setBackground(HEADER_BG);
                lbl.setForeground(HEADER_FG);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createMatteBorder(
                        0, 0, 1, 1, new Color(0xC5CDD6)));
                lbl.setOpaque(true);
                return lbl;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        // ========== TOOLBAR TRÊN ==========
        JButton btnSearch  = new JButton("🔍 Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        btnSearch.setFont(FONT_BASE);
        btnRefresh.setFont(FONT_BASE);
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); refresh(); });

        txtSearch.setFont(FONT_BASE);
        txtSearch.setPreferredSize(new Dimension(240, 32));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(FONT_BOLD);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.add(lblSearch);
        left.add(txtSearch);
        left.add(btnSearch);
        left.add(btnRefresh);

        // ===== NÚT XEM CHI TIẾT (hiện với mọi role) =====
        JButton btnDetail = new JButton("👁 Chi tiết");
        btnDetail.setFont(FONT_BOLD);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setBackground(new Color(0x6A1B9A));
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.putClientProperty("FlatLaf.style", "arc:8; borderWidth:0; focusWidth:0;");
        btnDetail.addActionListener(e -> hienThiChiTiet());
        actions.add(btnDetail);

        // ===== NÚT CRUD (chỉ hiện nếu có quyền) =====
        if (crud && Session.canModify()) {
            JButton btnAdd    = new JButton("➕ Thêm");
            JButton btnEdit   = new JButton("✎ Sửa");
            JButton btnDelete = new JButton("🗑 Xóa");

            styleCrudButton(btnAdd,    new Color(0x2E7D32));
            styleCrudButton(btnEdit,   new Color(0x1565C0));
            styleCrudButton(btnDelete, new Color(0xC62828));

            btnAdd.addActionListener(e -> onAdd());
            btnEdit.addActionListener(e -> { T t = selected(); if (t != null) onEdit(t); });
            btnDelete.addActionListener(e -> {
                T t = selected();
                if (t != null && Dialogs.confirm(this, "Bạn có chắc muốn xóa dòng đã chọn?")) onDelete(t);
            });
            actions.add(btnAdd);
            actions.add(btnEdit);
            actions.add(btnDelete);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(left, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        // ========== SCROLL PANE ==========
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // ========== ĐẾM BẢN GHI ==========
        lblCount.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCount.setForeground(new Color(0x616161));
        lblCount.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        // ========== GẮN VÀO PANEL ==========
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblCount, BorderLayout.SOUTH);
    }

    // ===================== HELPERS =====================

    /** Style nút CRUD với màu nền và chữ trắng. */
    private void styleCrudButton(JButton b, Color bg) {
        b.setFont(FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.putClientProperty("FlatLaf.style", "arc:8; borderWidth:0; focusWidth:0;");
    }

    /** Tính độ rộng cần thiết của cột dựa trên header + nội dung dài nhất. */
    private int getPreferredColumnWidth(int col) {
        int width = 60;

        // Đo header
        TableColumn tc = table.getColumnModel().getColumn(col);
        Component headerComp = table.getTableHeader().getDefaultRenderer()
                .getTableCellRendererComponent(table, tc.getHeaderValue(), false, false, -1, col);
        width = Math.max(width, headerComp.getPreferredSize().width + 24);

        // Đo 20 dòng đầu để không bị chậm với bảng lớn
        int rows = Math.min(table.getRowCount(), 20);
        for (int r = 0; r < rows; r++) {
            Component comp = table.prepareRenderer(table.getCellRenderer(r, col), r, col);
            width = Math.max(width, comp.getPreferredSize().width + 24);
        }
        return width;
    }

    /** Auto-fit tất cả các cột — gọi 1 lần sau lần refresh đầu tiên. */
    private void autoFitColumns() {
        if (table.getColumnCount() == 0) return;
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            int w = Math.min(COL_MAX_WIDTH, Math.max(COL_MIN_WIDTH, getPreferredColumnWidth(i)));
            col.setPreferredWidth(w);
        }
    }

    /** Mở dialog hiển thị chi tiết dòng đang chọn. */
    private void hienThiChiTiet() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Dialogs.warn(this, "Hãy chọn một dòng để xem chi tiết.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='width:460px;font-family:Segoe UI,Arial;font-size:13px'>");
        sb.append("<h2 style='color:#1565C0;margin:0 0 14px 0;border-bottom:2px solid #1565C0;padding-bottom:6px'>")
          .append("Chi tiết bản ghi #").append(modelRow + 1).append("</h2>");
        sb.append("<table cellpadding='8' cellspacing='0' style='border-collapse:collapse;width:100%'>");

        int colCount = table.getColumnCount();
        for (int c = 0; c < colCount; c++) {
            String colName = table.getColumnName(c);
            Object value = table.getValueAt(viewRow, c);
            String text = (value == null || value.toString().isBlank()) ? "—" : value.toString();

            String bg = (c % 2 == 0) ? "#F7F9FB" : "#FFFFFF";
            sb.append("<tr style='background:").append(bg).append("'>");
            sb.append("<td style='font-weight:bold;color:#1A237E;width:35%;vertical-align:top;")
              .append("border-bottom:1px solid #E0E0E0'>")
              .append(escapeHtml(colName)).append("</td>");
            sb.append("<td style='color:#212121;border-bottom:1px solid #E0E0E0'>")
              .append(escapeHtml(text)).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");

        JEditorPane editor = new JEditorPane("text/html", sb.toString());
        editor.setEditable(false);
        editor.setBackground(new Color(0xFAFBFC));
        editor.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        editor.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(560, 420));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Chi tiết bản ghi",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Escape HTML để tránh lỗi hiển thị ký tự đặc biệt. */
    private String escapeHtml(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // ========== ABSTRACT METHODS ==========
    protected abstract List<T> load(String keyword);
    protected abstract Object[] toRow(T item);

    protected void onAdd() {}
    protected void onEdit(T item) {}
    protected void onDelete(T item) {}

    /** Thêm nút riêng (ví dụ "Cấp phát", "Trả") vào thanh công cụ bên phải. */
    protected void addToolButton(JButton button) {
        button.setFont(FONT_BOLD);
        actions.add(button);
    }

    public void refresh() {
        try {
            data = load(txtSearch.getText());
            model.setRowCount(0);
            for (T item : data) model.addRow(toRow(item));
            lblCount.setText("📋 " + data.size() + " bản ghi");

            // Auto-fit cột 1 lần sau lần refresh đầu tiên
            if (!firstFitDone && model.getRowCount() > 0) {
                firstFitDone = true;
                SwingUtilities.invokeLater(this::autoFitColumns);
            }
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    /** Dòng đang chọn, hoặc null (kèm thông báo) nếu chưa chọn. */
    protected T selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Dialogs.warn(this, "Hãy chọn một dòng trong bảng trước.");
            return null;
        }
        return data.get(table.convertRowIndexToModel(viewRow));
    }
}