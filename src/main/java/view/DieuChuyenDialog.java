package com.qlts.view;

import com.qlts.bus.CapPhatBUS;
import com.qlts.bus.DieuChuyenBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.CapPhat;
import com.qlts.entity.NhanVien;
import com.qlts.entity.PhongBan;
import com.qlts.entity.TaiSan;
import com.qlts.entity.ThietBi;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DieuChuyenDialog extends FormDialog {
    private static final String LOAI_TAI_SAN = "Tài sản";
    private static final String LOAI_THIET_BI = "Thiết bị";

    private record Item(int id, String label) {
        @Override public String toString() { return label; }
    }

    private final DieuChuyenBUS bus;
    private final List<TaiSan> taiSans;
    private final List<ThietBi> thietBis;
    private final List<NhanVien> allNhanViens;
    private final CapPhatBUS capPhatBus = new CapPhatBUS();

    private final JComboBox<String> cboHinhThuc = new JComboBox<>(new String[]{"Điều chuyển", "Bàn giao"});
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{LOAI_TAI_SAN, LOAI_THIET_BI});
    private final JComboBox<Item> cboDoiTuong = new JComboBox<>();
    
    private final JLabel lblTuNhanVien = new JLabel("---");
    private final JLabel lblTuPhongBan = new JLabel("---");
    
    private final JComboBox<PhongBan> cboDenPhongBan;
    private final JComboBox<NhanVien> cboDenNhanVien;
    
    private final JTextField txtNgay = new JTextField(LocalDate.now().toString(), 20);
    private final JTextField txtLyDo = new JTextField(20);
    private final JTextField txtGhiChu = new JTextField(20);

    private CapPhat currentHolder = null;
    private NhanVien nhanVienCu = null;
    
    private boolean isUpdating = false;

    public DieuChuyenDialog(Window owner, DieuChuyenBUS bus, List<TaiSan> taiSans, List<ThietBi> thietBis, 
                            List<NhanVien> nhanViens, List<PhongBan> phongBans) {
        super(owner, "Điều chuyển / Bàn giao tài sản");
        this.bus = bus;
        this.taiSans = taiSans;
        this.thietBis = thietBis;
        this.allNhanViens = nhanViens;
        
        // 1. Khởi tạo Combo Phòng Ban: Thêm null (đại diện cho Tất cả) + các phòng ban
        cboDenPhongBan = new JComboBox<>();
        cboDenPhongBan.addItem(null); 
        for (PhongBan pb : phongBans) {
            cboDenPhongBan.addItem(pb);
        }
        
        // 2. Khởi tạo Combo Nhân Viên: Ban đầu chứa TOÀN BỘ nhân viên
        cboDenNhanVien = new JComboBox<>(allNhanViens.toArray(new NhanVien[0]));

        // 3. Custom Renderer cho Combo Phòng Ban (Xử lý chữ "Tất cả")
        cboDenPhongBan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("---Chọn phòng ban ---");
                } else if (value instanceof PhongBan pb) {
                    setText(pb.tenPhongBan());
                }
                return this;
            }
        });

        // 4. Custom Renderer cho Combo Nhân Viên (Mã - Tên)
        cboDenNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien nv) {
                    setText(nv.maCode() + " - " + nv.hoTen());
                }
                return this;
            }
        });

        addRow("Hình thức (*)", cboHinhThuc);
        addRow("Loại đối tượng", cboLoai);
        addRow("Đối tượng (Đang sử dụng)", cboDoiTuong);
        addRow("Từ nhân viên", lblTuNhanVien);
        addRow("Từ phòng ban", lblTuPhongBan);
        
        addRow("Đến phòng ban (Lọc)", cboDenPhongBan); 
        addRow("Đến nhân viên (*)", cboDenNhanVien); 
        
        addRow("Ngày chuyển (*)", txtNgay);
        addRow("Lý do (*)", txtLyDo);
        addRow("Ghi chú", txtGhiChu);

        // Sự kiện chọn Phòng Ban -> Lọc lại danh sách Nhân Viên
        cboDenPhongBan.addActionListener(e -> {
            if (isUpdating) return;
            isUpdating = true;
            
            PhongBan pb = (PhongBan) cboDenPhongBan.getSelectedItem();
            DefaultComboBoxModel<NhanVien> m = new DefaultComboBoxModel<>();
            
            if (pb == null) {
                // Chọn "Tất cả" -> Load toàn bộ nhân viên
                for (NhanVien nv : allNhanViens) {
                    m.addElement(nv);
                }
            } else {
                // Chọn phòng cụ thể -> Chỉ load nhân viên phòng đó
                for (NhanVien nv : allNhanViens) {
                    if (nv.maPhongBan() != null && nv.maPhongBan().equals(pb.maPhongBan())) {
                        m.addElement(nv);
                    }
                }
            }
            cboDenNhanVien.setModel(m);
            if (m.getSize() > 0) cboDenNhanVien.setSelectedIndex(0);
            
            isUpdating = false;
        });

        // Sự kiện chọn Nhân Viên -> Tự động nhảy ô Phòng Ban tương ứng
        cboDenNhanVien.addActionListener(e -> {
            if (isUpdating) return;
            isUpdating = true;
            
            NhanVien nv = (NhanVien) cboDenNhanVien.getSelectedItem();
            if (nv != null && nv.maPhongBan() != null) {
                for (int i = 0; i < cboDenPhongBan.getItemCount(); i++) {
                    PhongBan pb = cboDenPhongBan.getItemAt(i);
                    if (pb != null && pb.maPhongBan() == nv.maPhongBan()) {
                        cboDenPhongBan.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            isUpdating = false;
        });

        cboLoai.addActionListener(e -> reloadObjects());
        cboDoiTuong.addActionListener(e -> updateCurrentHolder());

        reloadObjects();
        
        // Mặc định chọn "Tất cả phòng ban" khi mở form
        cboDenPhongBan.setSelectedIndex(0); 
    }

    private void reloadObjects() {
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        if (LOAI_TAI_SAN.equals(cboLoai.getSelectedItem())) {
            taiSans.forEach(t -> m.addElement(new Item(t.maTaiSan(), t.maCode() + " - " + t.ten())));
        } else {
            thietBis.forEach(t -> m.addElement(new Item(t.maThietBi(), t.maCode() + " - " + t.ten())));
        }
        cboDoiTuong.setModel(m);
        
        if (m.getSize() > 0) {
            cboDoiTuong.setSelectedIndex(0);
        } else {
            cboDoiTuong.addItem(new Item(-1, "--- Không có " + cboLoai.getSelectedItem() + " nào đang sử dụng ---"));
        }
        updateCurrentHolder();
    }

    private void updateCurrentHolder() {
        Item item = (Item) cboDoiTuong.getSelectedItem();
        if (item == null || item.id() == -1) {
            lblTuNhanVien.setText("---");
            lblTuPhongBan.setText("---");
            currentHolder = null;
            nhanVienCu = null;
            return;
        }
        boolean isTs = LOAI_TAI_SAN.equals(cboLoai.getSelectedItem());
        currentHolder = capPhatBus.getCurrentHolder(isTs ? item.id() : null, isTs ? null : item.id());
        
        if (currentHolder != null) {
            lblTuNhanVien.setText(currentHolder.tenNhanVien());
            nhanVienCu = null;
            for (NhanVien nv : allNhanViens) {
                if (nv.maNhanVien() == currentHolder.maNhanVien()) {
                    nhanVienCu = nv;
                    lblTuPhongBan.setText(Parse.nz(nv.tenPhongBan()));
                    break;
                }
            }
            if (nhanVienCu == null) lblTuPhongBan.setText("Không xác định");
        } else {
            lblTuNhanVien.setText("Chưa xác định");
            lblTuPhongBan.setText("Chưa xác định");
            nhanVienCu = null;
        }
    }

    @Override protected void onSave() {
        Item item = (Item) cboDoiTuong.getSelectedItem();
        if (item == null || item.id() == -1) throw new BusinessException("Không có đối tượng nào để điều chuyển.");
        if (currentHolder == null) throw new BusinessException("Không tìm thấy phiếu cấp phát hiện tại của tài sản này.");

        boolean isTs = LOAI_TAI_SAN.equals(cboLoai.getSelectedItem());
        NhanVien denNv = (NhanVien) cboDenNhanVien.getSelectedItem();
        
        if (denNv == null) throw new BusinessException("Vui lòng chọn nhân viên tiếp nhận.");

        String hinhThuc = (String) cboHinhThuc.getSelectedItem();
        String lyDoFull = "[" + hinhThuc + "] " + Parse.text(txtLyDo.getText());

        Integer tuPhongBan = nhanVienCu != null ? nhanVienCu.maPhongBan() : null;
        
        bus.dieuChuyen(
                isTs ? item.id() : null, 
                isTs ? null : item.id(),
                currentHolder.maNhanVien(), 
                tuPhongBan, 
                denNv,
                Parse.date(txtNgay.getText(), "Ngày chuyển"),
                lyDoFull,
                Parse.text(txtGhiChu.getText())
        );
    }
}