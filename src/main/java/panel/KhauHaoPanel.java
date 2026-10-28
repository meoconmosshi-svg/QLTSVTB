package panel;

import dialog.KhauHaoDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.KhauHaoBUS;
import com.qlts.common.Session;
import com.qlts.common.Theme;
import com.qlts.component.Dialogs;
import com.qlts.entity.KhauHao;
import com.qlts.entity.KhauHaoInfo;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class KhauHaoPanel extends JPanel {

    private final KhauHaoBUS bus = new KhauHaoBUS();
    private final DefaultTableModel modelCauHinh;
    private final DefaultTableModel modelLichSu;
    private final JTable tableCauHinh;
    private final JTable tableLichSu;
    private final JTextField txtSearch = new JTextField(15);
    private List<KhauHaoInfo> dataCauHinh = List.of();

    public KhauHaoPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab 1: Cấu hình khấu hao
        modelCauHinh = new DefaultTableModel(new String[]{
                "Mã", "Tên tài sản / thiết bị", "Nguyên giá", "Thời gian (Tháng)", 
                "Ngày bắt đầu", "Giá trị còn lại", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCauHinh = setupTable(modelCauHinh);
        tabs.addTab("⚙️ Cấu hình khấu hao", buildTabCauHinh());

        // Tab 2: Lịch sử
        modelLichSu = new DefaultTableModel(new String[]{
                "Kỳ", "Tên tài sản / thiết bị", "Giá trị đầu kỳ", 
                "Tỷ lệ (%)", "Khấu hao", "Giá trị cuối kỳ", "Ngày tính"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLichSu = setupTable(modelLichSu);
        tabs.addTab("📜 Lịch sử khấu hao", buildTabLichSu());

        add(tabs, BorderLayout.CENTER);
        refreshAll();
    }

    private JTable setupTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xD0D7DE));
        table.setSelectionBackground(new Color(0xBBDEFB));
        table.setSelectionForeground(new Color(0x0D47A1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xF7F9FB));
                    comp.setForeground(new Color(0x212121));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return comp;
            }
        });

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xE8EEF4));
        table.getTableHeader().setForeground(new Color(0x1A237E));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        return table;
    }

    private JPanel buildTabCauHinh() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        toolbar.setBackground(new Color(0xFAFBFC));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D7DE)),
                new EmptyBorder(8, 8, 8, 8)));

        toolbar.add(new JLabel("🔍 Tìm:"));
        txtSearch.setPreferredSize(new Dimension(180, 30));
        toolbar.add(txtSearch);

        JButton btnSearch = new JButton("Tìm");
        JButton btnRefresh = new JButton("↻ Làm mới");
        styleNeutral(btnSearch); styleNeutral(btnRefresh);
        btnSearch.addActionListener(e -> refreshAll());
        txtSearch.addActionListener(e -> refreshAll());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); refreshAll(); });
        toolbar.add(btnSearch); toolbar.add(btnRefresh);

        if (Session.canModify()) {
            JButton btnCauHinh = new JButton("⚙️ Cấu hình");
            JButton btnChayKH = new JButton("🚀 Chạy khấu hao kỳ này");
            stylePrimary(btnCauHinh, new Color(0x1565C0));
            stylePrimary(btnChayKH, new Color(0xEF6C00));
            btnCauHinh.addActionListener(e -> cauHinh());
            btnChayKH.addActionListener(e -> chayKhauHao());
            toolbar.add(btnCauHinh);
            toolbar.add(btnChayKH);
        }

        JScrollPane scroll = new JScrollPane(tableCauHinh);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));

        wrap.add(toolbar, BorderLayout.NORTH);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildTabLichSu() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scroll = new JScrollPane(tableLichSu);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void stylePrimary(JButton b, Color bg) {
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

    public void refreshAll() {
        try {
            dataCauHinh = bus.getDanhSachCauHinh(txtSearch.getText());
            modelCauHinh.setRowCount(0);
            for (KhauHaoInfo k : dataCauHinh) {
                modelCauHinh.addRow(new Object[]{
                        k.maCode(), k.ten(), 
                        k.nguyenGia() == null ? "---" : Parse.fmt(k.nguyenGia()),
                        k.thoiGianKhauHao() == null ? "---" : k.thoiGianKhauHao(),
                        Parse.fmt(k.ngayBatDau()),
                        k.giaTriConLai() == null ? "---" : Parse.fmt(k.giaTriConLai()),
                        k.trangThai()
                });
            }

            List<KhauHao> lichSu = bus.getLichSu();
            modelLichSu.setRowCount(0);
            for (KhauHao kh : lichSu) {
                modelLichSu.addRow(new Object[]{
                        kh.thang() + "/" + kh.nam(),
                        kh.tenDoiTuong(),
                        Parse.fmt(kh.giaTriDauKy()),
                        kh.tyLeKhauHao() + "%",
                        Parse.fmt(kh.soTienKhauHao()),
                        Parse.fmt(kh.giaTriCuoiKy()),
                        Parse.fmt(kh.ngayTinh())
                });
            }
        } catch (Exception ex) { Dialogs.error(this, ex); }
    }

    private void cauHinh() {
        int r = tableCauHinh.getSelectedRow();
        if (r < 0) { Dialogs.warn(this, "Vui lòng chọn một tài sản/thiết bị để cấu hình."); return; }
        KhauHaoInfo info = dataCauHinh.get(tableCauHinh.convertRowIndexToModel(r));
        
        KhauHaoDialog d = new KhauHaoDialog(SwingUtilities.getWindowAncestor(this), bus, info);
        if (d.open()) refreshAll();
    }

    private void chayKhauHao() {
        LocalDate now = LocalDate.now();
        // Cho phép chọn tháng/năm để chạy (Mặc định tháng hiện tại)
        JComboBox<Integer> cboThang = new JComboBox<>();
        for (int i = 1; i <= 12; i++) cboThang.addItem(i);
        cboThang.setSelectedItem(now.getMonthValue());

        JComboBox<Integer> cboNam = new JComboBox<>();
        for (int i = now.getYear() - 5; i <= now.getYear() + 1; i++) cboNam.addItem(i);
        cboNam.setSelectedItem(now.getYear());

        JPanel p = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        p.add(new JLabel("Chọn Tháng:")); p.add(cboThang);
        p.add(new JLabel("Chọn Năm:")); p.add(cboNam);

        if (JOptionPane.showConfirmDialog(this, p, "Chạy khấu hao", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int thang = (Integer) cboThang.getSelectedItem();
                int nam = (Integer) cboNam.getSelectedItem();
                int count = bus.chayKhauHaoKy(nam, thang);
                
                if (count > 0) {
                    Dialogs.info(this, "Đã chạy khấu hao thành công cho " + count + " tài sản/thiết bị trong kỳ " + thang + "/" + nam);
                    refreshAll();
                } else {
                    Dialogs.warn(this, "Không có tài sản nào cần tính khấu hao trong kỳ này.\n(Kiểm tra lại cấu hình hoặc kỳ trước đã chạy chưa).");
                }
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }
}