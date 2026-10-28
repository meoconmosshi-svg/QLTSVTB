package panel;

import dialog.KiemKeDialog;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.KiemKeBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.KiemKe;
import com.qlts.util.Parse;

import javax.swing.*;
import java.util.List;

public class KiemKePanel extends CrudPanel<KiemKe> {
    private final KiemKeBUS bus = new KiemKeBUS();

    public KiemKePanel() {
        super(new String[]{"Mã KK", "Đợt KK", "Đối tượng", "Phòng ban", "Ngày KK", "Sổ sách", "Thực tế", "Lệch", "Tình trạng"}, false);
        
        if (Session.canModify()) {
            JButton btnTao = new JButton("Ghi nhận KK");
            JButton btnXoa = new JButton("Xóa phiếu"); // 🔥 Thêm nút Xóa
            btnTao.addActionListener(e -> tao());
            btnXoa.addActionListener(e -> xoa());
            addToolButton(btnTao);
            addToolButton(btnXoa);
        }
        refresh();
    }

    @Override protected List<KiemKe> load(String kw) { return bus.search(kw); }
    @Override protected Object[] toRow(KiemKe k) {
        return new Object[]{k.maKkCode(), k.dotKiemKe(), Parse.nz(k.tenDoiTuong()), Parse.nz(k.tenPhongBan()),
                Parse.fmt(k.ngayKiemKe()), k.soLuongSoSach(), k.soLuongThucTe(), k.chenhLech(), k.tinhTrangThucTe()};
    }

    private void tao() {
        // 🔥 Truyền thêm danh sách phòng ban vào Dialog
        KiemKeDialog d = new KiemKeDialog(
                SwingUtilities.getWindowAncestor(this), 
                bus, 
                new TaiSanBUS().search(""), 
                new ThietBiBUS().search(""),
                new DanhMucBUS().phongBan() 
        );
        if (d.open()) refresh();
    }

    // 🔥 Hàm xử lý nút Xóa
    private void xoa() {
        KiemKe k = selected(); if (k == null) return;
        if (!Session.isAdmin() && k.nguoiKiemKe() != null && k.nguoiKiemKe() != Session.userId()) {
            Dialogs.warn(this, "Chỉ được xóa phiếu do chính mình tạo."); return;
        }
        
        if (Dialogs.confirm(this, "Xóa phiếu kiểm kê " + k.maKkCode() + "?")) {
            try {
                bus.xoa(k);
                refresh();
            } catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }
}