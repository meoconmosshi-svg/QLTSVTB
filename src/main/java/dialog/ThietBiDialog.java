package dialog;

import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Constants;
import com.qlts.component.Combos;
import com.qlts.component.FormDialog;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.entity.NhaCungCap;
import com.qlts.entity.ThietBi;
import com.qlts.util.ImageUtil;
import com.qlts.util.Parse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ThietBiDialog extends FormDialog {

    // ===== Các field cho ảnh =====
    private final JLabel lblPreview = new JLabel();
    private final JButton btnChonAnh = new JButton("📷 Chọn ảnh...");
    private final JButton btnXoaAnh = new JButton("🗑 Xóa ảnh");
    private File selectedImageFile = null;
    private String currentImageName = null;

    // ===== Field nghiệp vụ =====
    private final ThietBiBUS bus;
    private final ThietBi existing;

    private final JTextField txtCode = new JTextField(20);
    private final JTextField txtTen = new JTextField(20);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    private final JComboBox<LoaiTaiSan> cboLoai;
    private final JComboBox<NhaCungCap> cboNcc;
    private final JTextField txtHang = new JTextField(20);
    private final JTextField txtModel = new JTextField(20);
    private final JTextField txtSerial = new JTextField(20);
    private final JTextField txtCauHinh = new JTextField(20);
    private final JTextField txtNgayMua = new JTextField(20);
    private final JTextField txtHetBh = new JTextField(20);
    private final JTextField txtGiaMua = new JTextField(20);
    private final JTextField txtGiaTri = new JTextField(20);
    private final JComboBox<String> cboTinhTrang = new JComboBox<>(Constants.TINH_TRANG);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(Constants.TRANG_THAI_TAI_SAN);
    private final JTextField txtViTri = new JTextField(20);
    private final JTextField txtGhiChu = new JTextField(20);

    public ThietBiDialog(Window owner, ThietBiBUS bus, ThietBi existing,
                         List<LoaiTaiSan> loai, List<NhaCungCap> ncc) {
        super(owner, existing == null ? "Thêm thiết bị" : "Sửa thiết bị");
        this.bus = bus;
        this.existing = existing;
        cboLoai = Combos.withEmpty(loai);
        cboNcc = Combos.withEmpty(ncc);

        addRow("Mã thiết bị (*)", txtCode);
        addRow("Tên thiết bị (*)", txtTen);
        addRow("Số lượng (*)", spnSoLuong);
        addRow("Loại", cboLoai);
        addRow("Nhà cung cấp", cboNcc);
        addRow("Hãng sản xuất", txtHang);
        addRow("Model", txtModel);
        addRow("Số serial", txtSerial);
        addRow("Cấu hình", txtCauHinh);
        addRow("Ngày mua (yyyy-MM-dd)", txtNgayMua);
        addRow("Hết bảo hành (yyyy-MM-dd)", txtHetBh);
        addRow("Giá mua", txtGiaMua);
        addRow("Giá trị hiện tại", txtGiaTri);
        addRow("Tình trạng", cboTinhTrang);
        addRow("Trạng thái", cboTrangThai);
        addRow("Vị trí", txtViTri);
        addRow("Ghi chú", txtGhiChu);
        addRow("Ảnh thiết bị", buildImagePanel());    // ← THÊM

        if (existing != null) fill(existing);
        else cboTinhTrang.setSelectedItem("Mới");
    }

    // ============================================================
    // PANEL CHỌN ẢNH
    // ============================================================
    private JPanel buildImagePanel() {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setOpaque(false);

        // Preview ảnh
        lblPreview.setPreferredSize(new Dimension(180, 180));
        lblPreview.setHorizontalAlignment(JLabel.CENTER);
        lblPreview.setBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE), 1));
        lblPreview.setBackground(Color.WHITE);
        lblPreview.setOpaque(true);
        lblPreview.setText("(Chưa có ảnh)");
        lblPreview.setForeground(new Color(0x9E9E9E));
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btnChonAnh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnXoaAnh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnChonAnh.setFocusPainted(false);
        btnXoaAnh.setFocusPainted(false);
        btnChonAnh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoaAnh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonAnh.addActionListener(e -> chonAnh());
        btnXoaAnh.addActionListener(e -> xoaAnh());
        btns.add(btnChonAnh);
        btns.add(btnXoaAnh);

        p.add(lblPreview, BorderLayout.WEST);
        p.add(btns, BorderLayout.CENTER);
        return p;
    }

    private void chonAnh() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh thiết bị");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Ảnh (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fc.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            lblPreview.setIcon(new ImageIcon(scaled));
            lblPreview.setText("");
        }
    }

    private void xoaAnh() {
        selectedImageFile = null;
        currentImageName = null;
        lblPreview.setIcon(null);
        lblPreview.setText("(Chưa có ảnh)");
    }

    // ============================================================
    // FILL DATA
    // ============================================================
    private void fill(ThietBi t) {
        txtCode.setText(t.maCode());
        txtTen.setText(t.ten());
        spnSoLuong.setValue(t.soLuongSafe());
        if (t.maLoai() != null) Combos.select(cboLoai, x -> x.maLoai() == t.maLoai());
        if (t.maNcc() != null) Combos.select(cboNcc, x -> x.maNcc() == t.maNcc());
        txtHang.setText(Parse.nz(t.hangSanXuat()));
        txtModel.setText(Parse.nz(t.model()));
        txtSerial.setText(Parse.nz(t.soSerial()));
        txtCauHinh.setText(Parse.nz(t.cauHinh()));
        txtNgayMua.setText(Parse.fmt(t.ngayMua()));
        txtHetBh.setText(Parse.fmt(t.ngayHetBh()));
        txtGiaMua.setText(t.giaMua() == null ? "" : t.giaMua().toPlainString());
        txtGiaTri.setText(t.giaTriHienTai() == null ? "" : t.giaTriHienTai().toPlainString());
        cboTinhTrang.setSelectedItem(t.tinhTrang());
        cboTrangThai.setSelectedItem(t.trangThai());
        txtViTri.setText(Parse.nz(t.viTri()));
        txtGhiChu.setText(Parse.nz(t.ghiChu()));

        // 🔥 Load ảnh hiện tại
        currentImageName = t.hinhAnh();
        if (currentImageName != null && !currentImageName.isBlank()) {
            ImageIcon icon = ImageUtil.loadImage(currentImageName, 180, 180);
            if (icon != null) {
                lblPreview.setIcon(icon);
                lblPreview.setText("");
            } else {
                lblPreview.setIcon(null);
                lblPreview.setText("(Không tìm thấy ảnh)");
            }
        }
    }

    // ============================================================
    // SAVE
    // ============================================================
    @Override
    protected void onSave() {
        LoaiTaiSan loai = (LoaiTaiSan) cboLoai.getSelectedItem();
        NhaCungCap ncc = (NhaCungCap) cboNcc.getSelectedItem();

        String maCode = Parse.require(txtCode.getText(), "Mã thiết bị");

        // Xử lý ảnh
        String imageName = currentImageName;
        if (selectedImageFile != null) {
            imageName = ImageUtil.saveImage(selectedImageFile, maCode);
        } else if (currentImageName == null) {
            imageName = null;
        }

        ThietBi t = new ThietBi(
                existing == null ? null : existing.maThietBi(),
                maCode,
                Parse.require(txtTen.getText(), "Tên thiết bị"),
                (Integer) spnSoLuong.getValue(),
                loai == null ? null : loai.maLoai(), loai == null ? null : loai.tenLoai(),
                ncc == null ? null : ncc.maNcc(),
                Parse.text(txtHang.getText()), Parse.text(txtModel.getText()),
                Parse.text(txtSerial.getText()), Parse.text(txtCauHinh.getText()),
                Parse.date(txtNgayMua.getText(), "Ngày mua"),
                Parse.date(txtHetBh.getText(), "Ngày hết bảo hành"),
                Parse.money(txtGiaMua.getText(), "Giá mua"),
                Parse.money(txtGiaTri.getText(), "Giá trị hiện tại"),
                (String) cboTinhTrang.getSelectedItem(),
                (String) cboTrangThai.getSelectedItem(),
                Parse.text(txtViTri.getText()),
                Parse.text(txtGhiChu.getText()),
                imageName);     // ← THÊM field hinhAnh
        bus.save(t);
    }
}