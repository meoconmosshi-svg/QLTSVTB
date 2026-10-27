package com.qlts.component;

import com.qlts.common.BusinessException;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class Dialogs {
    private Dialogs() {}

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, Throwable ex) {
        if (ex instanceof BusinessException) {
            warn(parent, ex.getMessage());
        } else {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
