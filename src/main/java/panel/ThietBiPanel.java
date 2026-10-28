package panel;

import dialog.ThietBiDialog;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.ThietBi;
import com.qlts.util.Parse;

import java.util.List;
import javax.swing.SwingUtilities;

public class ThietBiPanel extends CrudPanel<ThietBi> {
    private final ThietBiBUS bus = new ThietBiBUS();
    private final DanhMucBUS danhMuc = new DanhMucBUS();

    public ThietBiPanel() {
        super(new String[]{
                "Mã", "Tên thiết bị", "Số lượng", "Loại", "Hãng", "Model", "Serial",
                "Ngày mua", "Giá mua", "Tình trạng", "Trạng thái", "Vị trí"}, true);
        refresh();
    }

    @Override protected List<ThietBi> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(ThietBi t) {
        return new Object[]{
                t.maCode(),
                t.ten(),
                t.soLuongSafe(),                                // ← THÊM cột Số lượng
                Parse.nz(t.tenLoai()),
                Parse.nz(t.hangSanXuat()),
                Parse.nz(t.model()),
                Parse.nz(t.soSerial()),
                Parse.fmt(t.ngayMua()),
                Parse.fmt(t.giaMua()),
                Parse.nz(t.tinhTrang()),
                Parse.nz(t.trangThai()),
                Parse.nz(t.viTri())};
    }

    @Override protected void onAdd() { openDialog(null); }
    @Override protected void onEdit(ThietBi t) { openDialog(t); }

    @Override protected void onDelete(ThietBi t) {
        try {
            bus.delete(t);
            refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void openDialog(ThietBi existing) {
        try {
            ThietBiDialog d = new ThietBiDialog(SwingUtilities.getWindowAncestor(this), bus, existing,
                    danhMuc.loaiTaiSan(), danhMuc.nhaCungCap());
            if (d.open()) refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }
}