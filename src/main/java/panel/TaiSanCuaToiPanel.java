package panel;

import com.qlts.bus.CapPhatBUS;
import com.qlts.common.Session;
import com.qlts.component.Dialogs;
import com.qlts.entity.CapPhat;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaiSanCuaToiPanel extends JPanel {

    private final CapPhatBUS capPhatBus = new CapPhatBUS();
    private final DefaultTableModel model;
    private final JTable table;

    public TaiSanCuaToiPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("DANH SÁCH TÀI SẢN ĐANG ĐƯỢC GIAO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0x1565C0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
                "Mã phiếu cấp", "Tài sản / Thiết bị", "Ngày cấp", 
                "Dự kiến trả", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0xE8EEF4));
        table.setSelectionBackground(new Color(0xBBDEFB));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)));
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            // Lấy tất cả phiếu cấp phát, lọc ra những phiếu đang active của user hiện tại
            List<CapPhat> all = capPhatBus.search("");
            model.setRowCount(0);
            
            // Lấy tên nhân viên hiện tại (dựa vào tài khoản đăng nhập)
            // Vì bảng CapPhat join với bảng nhan_vien, ta phải so sánh tên
            String tenNhanVien = Session.current().hoTen();
            
            for (CapPhat c : all) {
                if (c.tenNhanVien().equalsIgnoreCase(tenNhanVien) 
                        && "Đang sử dụng".equals(c.trangThai())) {
                    model.addRow(new Object[]{
                            c.maCode(),
                            c.tenDoiTuong(),
                            Parse.fmt(c.ngayCapPhat()),
                            Parse.fmt(c.ngayDuKienTra()),
                            c.trangThai()
                    });
                }
            }
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }
}