package dialog;

import com.qlts.bus.LoaiTaiSanBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.Window;
import java.math.BigDecimal;

public class LoaiTaiSanDialog extends FormDialog {
    private final LoaiTaiSanBUS bus;
    private final LoaiTaiSan existing;

    private final JTextField txtCode = new JTextField(20);
    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtMoTa = new JTextField(20);
    private final JTextField txtKhauHao = new JTextField("0", 20);
    // 🔥 Thêm ComboBox chọn phân loại
    private final JComboBox<String> cboPhanLoai = new JComboBox<>(new String[]{"TAI_SAN", "THIET_BI"});

    public LoaiTaiSanDialog(Window owner, LoaiTaiSanBUS bus, LoaiTaiSan existing) {
        super(owner, existing == null ? "Thêm loại" : "Sửa loại");
        this.bus = bus;
        this.existing = existing;

        addRow("Mã loại (*)", txtCode);
        addRow("Tên loại (*)", txtTen);
        addRow("Phân loại", cboPhanLoai);
        addRow("Mô tả", txtMoTa);
        addRow("Tỷ lệ khấu hao (%)", txtKhauHao);

        if (existing != null) {
            txtCode.setText(existing.maCode());
            txtTen.setText(existing.tenLoai());
            txtMoTa.setText(Parse.nz(existing.moTa()));
            txtKhauHao.setText(existing.tyLeKhauHao() == null ? "0" : existing.tyLeKhauHao().toString());
            if (existing.phanLoai() != null) cboPhanLoai.setSelectedItem(existing.phanLoai());
        }
    }

    @Override protected void onSave() {
        BigDecimal tyLe = BigDecimal.ZERO;
        try {
            tyLe = new BigDecimal(txtKhauHao.getText().trim());
        } catch (Exception e) {
            throw new BusinessException("Tỷ lệ khấu hao không hợp lệ.");
        }

        // 🔥 Truyền đủ 6 tham số bao gồm cả cboPhanLoai
        LoaiTaiSan l = new LoaiTaiSan(
                existing == null ? null : existing.maLoai(),
                Parse.require(txtTen.getText(), "Tên loại"),
                Parse.require(txtCode.getText(), "Mã loại"),
                Parse.text(txtMoTa.getText()),
                tyLe,
                (String) cboPhanLoai.getSelectedItem()
        );
        bus.save(l);
    }
}