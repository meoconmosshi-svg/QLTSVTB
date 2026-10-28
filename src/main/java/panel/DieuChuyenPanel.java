package panel;

import dialog.DieuChuyenDialog;
import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.DieuChuyenBUS;
import com.qlts.bus.NhanVienBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.bus.ThietBiBUS;
import com.qlts.common.Session;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.DieuChuyen;
import com.qlts.util.Parse;

import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class DieuChuyenPanel extends CrudPanel<DieuChuyen> {
    private final DieuChuyenBUS bus = new DieuChuyenBUS();
    private final TaiSanBUS taiSanBus = new TaiSanBUS();
    private final ThietBiBUS thietBiBus = new ThietBiBUS();
    private final NhanVienBUS nhanVienBus = new NhanVienBUS();
    private final DanhMucBUS danhMucBus = new DanhMucBUS(); // 🔥 Lấy danh mục phòng ban

    public DieuChuyenPanel() {
        super(new String[]{"Mã phiếu", "Tài sản / Thiết bị", "Từ nhân viên", 
                "Đến nhân viên", "Từ phòng ban", "Đến phòng ban", 
                "Ngày chuyển", "Lý do / Hình thức"}, false);
                
        if (Session.canModify()) {
            JButton btnChuyen = new JButton("Tạo phiếu chuyển");
            btnChuyen.addActionListener(e -> dieuChuyen());
            addToolButton(btnChuyen);
        }
        refresh();
    }

    @Override protected List<DieuChuyen> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(DieuChuyen d) {
        return new Object[]{
                d.maDcCode(), 
                Parse.nz(d.tenDoiTuong()), 
                Parse.nz(d.tenTuNhanVien()),
                Parse.nz(d.tenDenNhanVien()), 
                Parse.nz(d.tenTuPhongBan()), 
                Parse.nz(d.tenDenPhongBan()), 
                Parse.fmt(d.ngayDieuChuyen()),
                Parse.nz(d.lyDo()) 
        };
    }

    private void dieuChuyen() {
        try {
            // 🔥 Truyền thêm danh sách Phòng Ban
            DieuChuyenDialog d = new DieuChuyenDialog(
                    SwingUtilities.getWindowAncestor(this), 
                    bus,
                    taiSanBus.findInUse(), 
                    thietBiBus.findInUse(), 
                    nhanVienBus.search(""),
                    danhMucBus.phongBan() 
            );
            if (d.open()) refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }
}