package com.qlts.component;

import java.util.List;
import java.util.function.Predicate;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public final class Combos {
    private Combos() {}

    /** Combobox có thêm một dòng trống ở đầu (chọn = null). */
    public static <T> JComboBox<T> withEmpty(List<T> items) {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        model.addElement(null);
        items.forEach(model::addElement);
        return new JComboBox<>(model);
    }

    /** Chọn phần tử đầu tiên thỏa điều kiện (không có thì giữ nguyên). */
    public static <T> void select(JComboBox<T> combo, Predicate<T> match) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            T item = combo.getItemAt(i);
            if (item != null && match.test(item)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
}
