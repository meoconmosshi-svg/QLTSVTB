package panel;

import dialog.PhongBanDialog;
import com.qlts.bus.NguoiDungBUS;
import com.qlts.bus.PhongBanBUS;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.PhongBan;
import com.qlts.util.Parse;

import javax.swing.*;
import java.util.List;

public class PhongBanPanel extends CrudPanel<PhongBan> {
    private final PhongBanBUS bus = new PhongBanBUS();

    public PhongBanPanel() {
        super(new String[]{"Mã phòng", "Tên phòng ban", "Địa điểm", "Trưởng phòng", "Ngày thành lập"}, true);
        refresh();
    }

    @Override protected List<PhongBan> load(String kw) { return bus.search(kw); }
    @Override protected Object[] toRow(PhongBan pb) {
        return new Object[]{Parse.nz(pb.maPhongBanCode()), pb.tenPhongBan(), Parse.nz(pb.diaDiem()), 
                Parse.nz(pb.tenQuanLy()), Parse.fmt(pb.ngayThanhLap())};
    }

    @Override protected void onAdd() { openDialog(null); }
    @Override protected void onEdit(PhongBan pb) { openDialog(pb); }

    @Override protected void onDelete(PhongBan pb) {
        if (Dialogs.confirm(this, "Xóa phòng ban " + pb.tenPhongBan() + "?")) {
            try {
                bus.delete(pb);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void openDialog(PhongBan existing) {
        try {
            // Lấy danh sách Admin để làm Trưởng phòng
            List<com.qlts.dao.NguoiDungDAO.UserFull> admins = new NguoiDungBUS().search(null, 1, null);
            PhongBanDialog d = new PhongBanDialog(SwingUtilities.getWindowAncestor(this), bus, existing, admins);
            if (d.open()) refresh();
        } catch (Exception ex) { Dialogs.error(this, ex); }
    }
}