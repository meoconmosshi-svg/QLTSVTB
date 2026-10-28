package dialog;

import com.formdev.flatlaf.FlatClientProperties;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.YeuCauCapPhatBUS;
import com.qlts.component.Dialogs;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.entity.TaiSan;
import com.qlts.util.Parse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class YeuCauDialog extends JDialog {

    private final YeuCauCapPhatBUS bus = new YeuCauCapPhatBUS();
    private final DanhMucBUS danhMuc = new DanhMucBUS();
    private boolean saved = false;

    // CHỈ 2 LOẠI YÊU CẦU: Mượn / Cấp phát
    private final JComboBox<String> cboLoaiYeuCau = new JComboBox<>(new String[]{"Mượn", "Cấp phát"});
    private final JComboBox<LoaiTaiSan> cboLoai;
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JTextField txtLyDo = new JTextField(28);
    private final JTextField txtNgayMongMuon = new JTextField(LocalDate.now().plusDays(1).toString(), 20);
    private final JTextField txtGhiChu = new JTextField(28);

    public YeuCauDialog(Window owner) {
        this(owner, null, null);
    }

    public YeuCauDialog(Window owner, String loaiYeuCau, TaiSan taiSan) {
        super(owner, "Gửi yêu cầu mượn / cấp phát", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        List<LoaiTaiSan> dsLoai = danhMuc.loaiTaiSan();
        cboLoai = new JComboBox<>(dsLoai.toArray(new LoaiTaiSan[0]));
        cboLoai.insertItemAt(null, 0);
        cboLoai.setSelectedIndex(0);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(22, 28, 12, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;

        JLabel title = new JLabel("Gửi yêu cầu mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1565C0));
        form.add(title, gc);

        gc.gridwidth = 1;

        addRow(form, gc, 1, "Loại yêu cầu (*)", cboLoaiYeuCau);
        addRow(form, gc, 2, "Loại tài sản (*)", cboLoai);
        addRow(form, gc, 3, "Số lượng (*)", spnSoLuong);
        addRow(form, gc, 4, "Mục đích / Lý do (*)", txtLyDo);
        addRow(form, gc, 5, "Ngày mong muốn nhận (*)", txtNgayMongMuon);
        addRow(form, gc, 6, "Ghi chú", txtGhiChu);

        for (Component c : new Component[]{cboLoaiYeuCau, cboLoai, txtLyDo, txtNgayMongMuon, txtGhiChu}) {
            c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            if (c instanceof JComponent jc) {
                jc.putClientProperty(FlatClientProperties.COMPONENT_ROUND_RECT, true);
            }
        }

        if (loaiYeuCau != null) {
            cboLoaiYeuCau.setSelectedItem(loaiYeuCau);
        }

        if (taiSan != null) {
            if (taiSan.maLoai() != null) {
                for (int i = 0; i < cboLoai.getItemCount(); i++) {
                    LoaiTaiSan item = cboLoai.getItemAt(i);
                    if (item != null && item.maLoai() == taiSan.maLoai()) {
                        cboLoai.setSelectedIndex(i);
                        break;
                    }
                }
            }
            spnSoLuong.setValue(1);

            if (txtLyDo.getText().isBlank()) {
                String loaiStr = (loaiYeuCau == null ? "Mượn" : loaiYeuCau);
                txtLyDo.setText(loaiStr + " tài sản: " + taiSan.ten());
            }
        }

        JButton btnGui = new JButton("📨 Gửi yêu cầu");
        JButton btnHuy = new JButton("Hủy");
        stylePrimary(btnGui);
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnHuy.setPreferredSize(new Dimension(100, 36));

        btnGui.addActionListener(e -> doGui());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnGui);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnHuy);
        buttons.add(btnGui);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row; gc.gridx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(0x424242));
        form.add(l, gc);
        gc.gridx = 1;
        form.add(field, gc);
    }

    private void stylePrimary(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 36));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0x1565C0));
        b.setForeground(Color.WHITE);
        b.putClientProperty(FlatClientProperties.STYLE, "arc:8; borderWidth:0; focusWidth:0;");
    }

    private void doGui() {
        try {
            LoaiTaiSan loai = (LoaiTaiSan) cboLoai.getSelectedItem();
            bus.guiYeuCau(
                    loai == null ? null : loai.maLoai(),
                    (Integer) spnSoLuong.getValue(),
                    txtLyDo.getText(),
                    Parse.date(txtNgayMongMuon.getText(), "Ngày mong muốn"),
                    (String) cboLoaiYeuCau.getSelectedItem(),
                    txtGhiChu.getText());
            saved = true;
            Dialogs.info(this, "Đã gửi yêu cầu!\nVui lòng chờ Admin duyệt.");
            dispose();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    public boolean isSaved() { return saved; }
}