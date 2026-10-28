package panel;

import com.qlts.bus.NhatKyBUS;
import com.qlts.component.CrudPanel;
import com.qlts.entity.NhatKy;
import com.qlts.util.Parse;

import java.util.List;

public class NhatKyPanel extends CrudPanel<NhatKy> {
    private final NhatKyBUS bus = new NhatKyBUS();

    public NhatKyPanel() {
        super(new String[]{"Thời gian", "Người dùng", "Hành động", "Đối tượng", "Mã", "Nội dung"}, false);
        refresh();
    }

    @Override protected List<NhatKy> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(NhatKy n) {
        return new Object[]{Parse.fmt(n.thoiGian()), Parse.nz(n.nguoiDung()), n.hanhDong(),
                Parse.nz(n.doiTuong()), Parse.nz(n.maDoiTuong()), Parse.nz(n.noiDung())};
    }
}
