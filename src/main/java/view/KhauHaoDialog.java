package com.qlts.view;

import com.qlts.bus.KhauHaoBUS;
import com.qlts.common.BusinessException;
import com.qlts.component.FormDialog;
import com.qlts.entity.KhauHaoInfo;
import com.qlts.util.Parse;

import javax.swing.*;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;

public class KhauHaoDialog extends FormDialog {
    private final KhauHaoBUS bus;
    private final KhauHaoInfo info;

    private final JTextField txtNguyenGia = new JTextField(20);
    private final JTextField txtThoiGian = new JTextField(20);
    private final JTextField txtNgayBatDau = new JTextField(20);
    private final JTextField txtGiaTriConLai = new JTextField(20);

    public KhauHaoDialog(Window owner, KhauHaoBUS bus, KhauHaoInfo info) {
        super(owner, "Cấu hình khấu hao - " + info.ten());
        this.bus = bus;
        this.info = info;

        addRow("Nguyên giá (*)", txtNguyenGia);
        addRow("Thời gian KH (tháng) (*)", txtThoiGian);
        addRow("Ngày bắt đầu (yyyy-MM-dd) (*)", txtNgayBatDau);
        addRow("Giá trị còn lại (*)", txtGiaTriConLai);

        if (info.nguyenGia() != null) txtNguyenGia.setText(info.nguyenGia().toPlainString());
        if (info.thoiGianKhauHao() != null) txtThoiGian.setText(info.thoiGianKhauHao().toString());
        txtNgayBatDau.setText(info.ngayBatDau() != null ? info.ngayBatDau().toString() : LocalDate.now().toString());
        if (info.giaTriConLai() != null) txtGiaTriConLai.setText(info.giaTriConLai().toPlainString());
    }

    @Override protected void onSave() {
        BigDecimal nguyenGia = Parse.money(txtNguyenGia.getText(), "Nguyên giá");
        Integer thoiGian = Integer.parseInt(txtThoiGian.getText().trim());
        LocalDate ngayBD = Parse.date(txtNgayBatDau.getText(), "Ngày bắt đầu");
        BigDecimal giaTriConLai = Parse.money(txtGiaTriConLai.getText(), "Giá trị còn lại");

        bus.updateCauHinh(info, nguyenGia, thoiGian, ngayBD, giaTriConLai);
    }
}