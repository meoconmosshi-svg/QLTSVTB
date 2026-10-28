package dialog;

import com.qlts.bus.CapPhatBUS;
import com.qlts.common.Constants;
import com.qlts.component.FormDialog;
import com.qlts.entity.CapPhat;
import com.qlts.util.Parse;

import java.awt.Window;
import java.time.LocalDate;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TraDialog extends FormDialog {
    private final CapPhatBUS bus;
    private final CapPhat capPhat;
    private final JTextField txtNgayTra = new JTextField(LocalDate.now().toString(), 20);
    private final JComboBox<String> cboTinhTrang = new JComboBox<>(Constants.TINH_TRANG);

    public TraDialog(Window owner, CapPhatBUS bus, CapPhat capPhat) {
        super(owner, "Ghi nhận trả - " + capPhat.maCode());
        this.bus = bus;
        this.capPhat = capPhat;
        cboTinhTrang.setSelectedItem("Tốt");

        addRow("Đối tượng", new JLabel(Parse.nz(capPhat.tenDoiTuong())));
        addRow("Nhân viên", new JLabel(capPhat.tenNhanVien()));
        addRow("Ngày trả (*)", txtNgayTra);
        addRow("Tình trạng khi trả", cboTinhTrang);
    }

    @Override protected void onSave() {
        bus.tra(capPhat, Parse.date(txtNgayTra.getText(), "Ngày trả"), (String) cboTinhTrang.getSelectedItem());
    }
}
