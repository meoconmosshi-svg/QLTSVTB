package com.qlts.view;

import com.qlts.bus.DanhMucBUS;
import com.qlts.bus.TaiSanBUS;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.TaiSan;
import com.qlts.util.Parse;

import java.util.List;
import javax.swing.SwingUtilities;

public class TaiSanPanel extends CrudPanel<TaiSan> {
    private final TaiSanBUS bus = new TaiSanBUS();
    private final DanhMucBUS danhMuc = new DanhMucBUS();

    public TaiSanPanel() {
        super(new String[]{
                "Mã", "Tên tài sản", "Số lượng", "Loại", "Serial", "Ngày mua",
                "Giá mua", "Giá trị hiện tại", "Tình trạng", "Trạng thái", "Vị trí"}, true);
        refresh();
    }

    @Override protected List<TaiSan> load(String keyword) { return bus.search(keyword); }

    @Override protected Object[] toRow(TaiSan t) {
        return new Object[]{
                t.maCode(),
                t.ten(),
                t.soLuongSafe(),                                // ← THÊM cột Số lượng
                Parse.nz(t.tenLoai()),
                Parse.nz(t.soSerial()),
                Parse.fmt(t.ngayMua()),
                Parse.fmt(t.giaMua()),
                Parse.fmt(t.giaTriHienTai()),
                Parse.nz(t.tinhTrang()),
                Parse.nz(t.trangThai()),
                Parse.nz(t.viTri())};
    }

    @Override protected void onAdd() { openDialog(null); }
    @Override protected void onEdit(TaiSan t) { openDialog(t); }

    @Override protected void onDelete(TaiSan t) {
        try {
            bus.delete(t);
            refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }

    private void openDialog(TaiSan existing) {
        try {
            TaiSanDialog d = new TaiSanDialog(SwingUtilities.getWindowAncestor(this), bus, existing,
                    danhMuc.loaiTaiSan(), danhMuc.nhaCungCap());
            if (d.open()) refresh();
        } catch (RuntimeException ex) {
            Dialogs.error(this, ex);
        }
    }
}