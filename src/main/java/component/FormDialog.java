package com.qlts.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Khung dialog dạng form dùng chung. Lớp con gọi addRow(...) để thêm trường nhập,
 * cài đặt onSave() (ném BusinessException nếu dữ liệu sai), rồi gọi open().
 */
public abstract class FormDialog extends JDialog {
    private final JPanel form = new JPanel(new GridBagLayout());
    private int row = 0;
    private boolean saved = false;

    protected FormDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        btnSave.addActionListener(e -> {
            try {
                onSave();
                saved = true;
                dispose();
            } catch (RuntimeException ex) {
                Dialogs.error(this, ex);
            }
        });
        btnCancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(btnSave);
        buttons.add(btnCancel);

        getRootPane().setDefaultButton(btnSave);
        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    protected void addRow(String label, JComponent field) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0; l.gridy = row; l.anchor = GridBagConstraints.WEST; l.insets = new Insets(4, 0, 4, 10);
        form.add(new JLabel(label), l);

        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 1; f.gridy = row; f.weightx = 1; f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(4, 0, 4, 0);
        form.add(field, f);
        row++;
    }

    /** Lưu dữ liệu; ném BusinessException/DataAccessException nếu thất bại (dialog sẽ giữ nguyên). */
    protected abstract void onSave();

    /** Hiển thị dialog; trả về true nếu người dùng đã lưu thành công. */
    public boolean open() {
        pack();
        setMinimumSize(new java.awt.Dimension(460, getHeight()));
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return saved;
    }
}
