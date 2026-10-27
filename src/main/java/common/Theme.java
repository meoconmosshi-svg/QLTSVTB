package com.qlts.common;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * Bảng màu theo vai trò:
 *  - Admin    (1): Xanh nước biển
 *  - Nhân viên(3): Đỏ
 */
public final class Theme {
    private Theme() {}

    // ============ ADMIN - XANH NƯỚC BIỂN ============
    public static final Color ADMIN_PRIMARY      = new Color(0x1565C0);
    public static final Color ADMIN_DARK         = new Color(0x0D47A1);
    public static final Color ADMIN_GRADIENT_END = new Color(0x42A5F5);
    public static final Color ADMIN_TEXT_ON      = Color.WHITE;

    // ============ NHÂN VIÊN - ĐỎ ============
    public static final Color EMPLOYEE_PRIMARY      = new Color(0xD32F2F);
    public static final Color EMPLOYEE_DARK         = new Color(0xB71C1C);
    public static final Color EMPLOYEE_GRADIENT_END = new Color(0xEF5350);
    public static final Color EMPLOYEE_TEXT_ON      = Color.WHITE;

    // Trạng thái theme hiện tại
    private static Color currentPrimary      = ADMIN_PRIMARY;
    private static Color currentDark         = ADMIN_DARK;
    private static Color currentGradientEnd  = ADMIN_GRADIENT_END;
    private static Color currentTextOn       = ADMIN_TEXT_ON;

    public static Color primaryFor(int maQuyen) {
        return maQuyen == Constants.QUYEN_ADMIN ? ADMIN_PRIMARY : EMPLOYEE_PRIMARY;
    }
    public static Color darkFor(int maQuyen) {
        return maQuyen == Constants.QUYEN_ADMIN ? ADMIN_DARK : EMPLOYEE_DARK;
    }
    public static Color gradientEndFor(int maQuyen) {
        return maQuyen == Constants.QUYEN_ADMIN ? ADMIN_GRADIENT_END : EMPLOYEE_GRADIENT_END;
    }
    public static Color textOnFor(int maQuyen) {
        return maQuyen == Constants.QUYEN_ADMIN ? ADMIN_TEXT_ON : EMPLOYEE_TEXT_ON;
    }

    // ============ GETTER cho theme đang hoạt động ============
    public static Color primary()     { return currentPrimary; }
    public static Color dark()        { return currentDark; }
    public static Color gradientEnd() { return currentGradientEnd; }
    public static Color textOn()      { return currentTextOn; }

    /** Áp dụng theme cho toàn bộ app (gọi TRƯỚC khi tạo MainFrame). */
    public static void applyFor(int maQuyen) {
        currentPrimary     = primaryFor(maQuyen);
        currentDark        = darkFor(maQuyen);
        currentGradientEnd = gradientEndFor(maQuyen);
        currentTextOn      = textOnFor(maQuyen);

        UIManager.put("Component.accentColor",              currentPrimary);
        UIManager.put("Component.focusColor",               currentPrimary);
        UIManager.put("Component.focusedBorderColor",       currentPrimary);
        UIManager.put("Component.borderColor",              new Color(0xE0E0E0));

        UIManager.put("Button.default.background",          currentPrimary);
        UIManager.put("Button.default.foreground",          currentTextOn);
        UIManager.put("Button.default.hoverBackground",     currentDark);
        UIManager.put("Button.default.pressedBackground",   currentDark);
        UIManager.put("Button.default.borderColor",         currentPrimary);

        UIManager.put("Button.background",                  currentPrimary);
        UIManager.put("Button.foreground",                  currentTextOn);
        UIManager.put("Button.hoverBackground",             currentDark);
        UIManager.put("Button.pressedBackground",           currentDark);

        UIManager.put("TabbedPane.selectedBackground",      currentPrimary);
        UIManager.put("TabbedPane.selectedForeground",      currentTextOn);
        UIManager.put("TabbedPane.underlineColor",          currentPrimary);
        UIManager.put("TabbedPane.hoverColor",              new Color(
                currentPrimary.getRed(), currentPrimary.getGreen(), currentPrimary.getBlue(), 40));
        UIManager.put("TabbedPane.focusColor",              currentPrimary);

        UIManager.put("Table.selectionBackground",          new Color(
                currentPrimary.getRed(), currentPrimary.getGreen(), currentPrimary.getBlue(), 80));
        UIManager.put("Table.selectionForeground",          Color.BLACK);

        UIManager.put("TextField.focusedBorderColor",       currentPrimary);
        UIManager.put("PasswordField.focusedBorderColor",   currentPrimary);
        UIManager.put("ComboBox.focusedBorderColor",        currentPrimary);

        UIManager.put("CheckBox.icon.selectedBackground",   currentPrimary);
        UIManager.put("RadioButton.icon.selectedBackground",currentPrimary);
    }
}