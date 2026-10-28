package com.qlts.view;

import com.qlts.bus.LoaiTaiSanBUS;
import com.qlts.component.CrudPanel;
import com.qlts.component.Dialogs;
import com.qlts.entity.LoaiTaiSan;
import com.qlts.util.Parse;

import javax.swing.*;
import java.util.List;

public class LoaiTaiSanPanel extends CrudPanel<LoaiTaiSan> {
    private final LoaiTaiSanBUS bus = new LoaiTaiSanBUS();

    public LoaiTaiSanPanel() {
        super(new String[]{"Mã loại", "Tên loại", "Mô tả", "Tỷ lệ khấu hao (%)"}, true);
        refresh();
    }

    @Override protected List<LoaiTaiSan> load(String kw) { return bus.search(kw); }
    
    @Override protected Object[] toRow(LoaiTaiSan l) {
        return new Object[]{l.maCode(), l.tenLoai(), Parse.nz(l.moTa()), l.tyLeKhauHao()};
    }

    @Override protected void onAdd() { openDialog(null); }
    @Override protected void onEdit(LoaiTaiSan l) { openDialog(l); }

    @Override protected void onDelete(LoaiTaiSan l) {
        if (Dialogs.confirm(this, "Xóa loại tài sản " + l.tenLoai() + "?")) {
            try { bus.delete(l); refresh(); } 
            catch (Exception ex) { Dialogs.error(this, ex); }
        }
    }

    private void openDialog(LoaiTaiSan existing) {
        LoaiTaiSanDialog d = new LoaiTaiSanDialog(SwingUtilities.getWindowAncestor(this), bus, existing);
        if (d.open()) refresh();
    }
}