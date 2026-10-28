package panel;

import dialog.TraDialog;
import dialog.CapPhatDialog;
import com.qlts.bus.CapPhatBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Constants;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.CapPhat;
import com.qlts.util.Parse;

import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class CapPhatPanel extends CrudPanel<CapPhat> {
    private final CapPhatBUS bus = new CapPhatBUS();
    private final TaiSanBUS taiSanBus = new TaiSanBUS();
    private final ThietBiBUS thietBiBus = new ThietBiBUS();

    public CapPhatPanel() {
        super(new String[]{"Mã phiếu", "Tài sản / Thiết bị", "Nhân viên", "Ngày cấp phát",
                "Dự kiến trả", "Ngày trả", "Tình trạng khi trả", "Trạng thái", "Ghi chú"}, false);
                
        if (Session.canModify()) {
            JButton btnCap = new JButton("Cấp phát");
            JButton btnTra = new JButton("Ghi nhận trả");
            btnCap.addActionListener(e -> cap());
            btnTra.addActionListener(e -> tra());
            addToolButton(btnCap);
            addToolButton(btnTra);
        }
        refresh();
    }

    @Override protected List<CapPhat> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(CapPhat c) {
        return new Object[]{
                c.maCode(), 
                Parse.nz(c.tenDoiTuong()), 
                c.tenNhanVien(), 
                Parse.fmt(c.ngayCapPhat()),
                Parse.fmt(c.ngayDuKienTra()), 
                Parse.fmt(c.ngayTraThucTe()), 
                Parse.nz(c.tinhTrangKhiTra()),
                Parse.nz(c.trangThai()),
                Parse.nz(c.ghiChu()) // Thêm cột ghi chú để thấy dòng [YC: ...]
        };
    }

    private void cap() {
        try {
            // 🔥 Sửa dòng này: Thêm chữ null ở cuối vì tạo độc lập không qua Yêu Cầu
            CapPhatDialog d = new CapPhatDialog(
                    SwingUtilities.getWindowAncestor(this), 
                    bus,
                    taiSanBus.findAvailable(), 
                    thietBiBus.findAvailable(), 
                    bus.allEmployees(),
                    null 
            );
            if (d.open()) refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void tra() {
        CapPhat c = selected();
        if (c == null) return;
        if (Constants.CP_DA_TRA.equals(c.trangThai())) {
            Dialogs.warn(this, "Phiếu này đã được ghi nhận trả.");
            return;
        }
        if (new TraDialog(SwingUtilities.getWindowAncestor(this), bus, c).open()) refresh();
    }
}