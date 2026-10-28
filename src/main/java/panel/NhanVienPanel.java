package panel;

import dialog.NhanVienDialog;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.NhanVienBUS;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.NhanVien;
import com.qlts.util.Parse;

import java.util.List;
import javax.swing.SwingUtilities;

public class NhanVienPanel extends CrudPanel<NhanVien> {
    private final NhanVienBUS bus = new NhanVienBUS();
    private final DanhMucBUS danhMuc = new DanhMucBUS();

    public NhanVienPanel() {
        super(new String[]{"Mã NV", "Họ tên", "Giới tính", "Ngày sinh", "Chức vụ", "Phòng ban",
                "Email", "Điện thoại", "Ngày vào làm"}, true);
        refresh();
    }

    @Override protected List<NhanVien> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(NhanVien n) {
        return new Object[]{n.maCode(), n.hoTen(), Parse.nz(n.gioiTinh()), Parse.fmt(n.ngaySinh()),
                Parse.nz(n.chucVu()), Parse.nz(n.tenPhongBan()), Parse.nz(n.email()),
                Parse.nz(n.soDienThoai()), Parse.fmt(n.ngayVaoLam())};
    }

    @Override protected void onAdd() { openDialog(null); }
    @Override protected void onEdit(NhanVien n) { openDialog(n); }

    @Override protected void onDelete(NhanVien n) {
        try {
            bus.delete(n);
            refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void openDialog(NhanVien existing) {
        try {
            NhanVienDialog d = new NhanVienDialog(SwingUtilities.getWindowAncestor(this), bus, existing,
                    danhMuc.phongBan());
            if (d.open()) refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }
}
