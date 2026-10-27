package com.qlts;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.qlts.dao.CauHinhDAO;
import com.qlts.view.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");

        SwingUtilities.invokeLater(() -> {
            // 1. Đọc theme từ DB, mặc định "light"
            String theme = "light";
            try {
                theme = new CauHinhDAO().get("theme", "light");
                System.out.println("[Main] Theme: " + theme);
            } catch (Throwable ex) {
                System.err.println("[Main] Không đọc được theme, dùng Light: " + ex.getMessage());
            }

            // 2. Áp LookAndFeel
            try {
                if ("dark".equalsIgnoreCase(theme)) {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
            } catch (Throwable ex) {
                System.err.println("[Main] Không thể khởi tạo FlatLaf, dùng mặc định.");
                ex.printStackTrace();
            }

            // 3. Cấu hình UI toàn cục
            applyGlobalUIStyle();

            // 4. Mở LoginFrame
            new LoginFrame().setVisible(true);
        });
    }

    private static void applyGlobalUIStyle() {
        Font baseFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 14);

        UIManager.put("defaultFont", baseFont);

        // ===== BẢNG =====
        UIManager.put("Table.font",                  baseFont);
        UIManager.put("Table.rowHeight",             32);
        UIManager.put("Table.showHorizontalLines",   true);
        UIManager.put("Table.showVerticalLines",     true);
        UIManager.put("Table.gridColor",             new Color(0xD0D7DE));
        UIManager.put("Table.intercellSpacing",      new Dimension(1, 1));
        UIManager.put("Table.selectionBackground",   new Color(0xBBDEFB));
        UIManager.put("Table.selectionForeground",   new Color(0x0D47A1));
        UIManager.put("Table.alternateRowColor",     new Color(0xF7F9FB));

        // ===== HEADER BẢNG =====
        UIManager.put("TableHeader.font",             boldFont);
        UIManager.put("TableHeader.background",       new Color(0xE8EEF4));
        UIManager.put("TableHeader.foreground",       new Color(0x1A237E));
        UIManager.put("TableHeader.cellMargins",      new Insets(8, 10, 8, 10));

        // ===== NÚT =====
        UIManager.put("Button.font",                  baseFont);
        UIManager.put("Button.arc",                   8);
        UIManager.put("Button.margin",                new Insets(6, 14, 6, 14));

        // ===== TEXT FIELD =====
        UIManager.put("TextField.font",               baseFont);
        UIManager.put("TextField.margin",             new Insets(6, 10, 6, 10));
        UIManager.put("PasswordField.font",           baseFont);
        UIManager.put("TextComponent.arc",            8);

        // ===== COMBOBOX =====
        UIManager.put("ComboBox.font",                baseFont);
        UIManager.put("ComboBox.padding",             new Insets(4, 8, 4, 8));

        // ===== TAB =====
        UIManager.put("TabbedPane.font",              boldFont);
        UIManager.put("TabbedPane.tabHeight",         38);
        UIManager.put("TabbedPane.tabInsets",         new Insets(8, 16, 8, 16));
        UIManager.put("TabbedPane.showTabSeparators", true);

        // ===== SCROLLBAR =====
        UIManager.put("ScrollBar.thumbArc",           999);
        UIManager.put("ScrollBar.thumbInsets",        new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width",              14);

        // ===== LABEL =====
        UIManager.put("Label.font",                   baseFont);

        // ===== DIALOG =====
        UIManager.put("OptionPane.messageFont",       baseFont);
        UIManager.put("OptionPane.buttonFont",        baseFont);

        // ===== SPINNER (thêm mới) =====
        UIManager.put("Spinner.font",                 baseFont);

        // ===== CHECKBOX / RADIO (thêm mới) =====
        UIManager.put("CheckBox.font",                baseFont);
        UIManager.put("RadioButton.font",             baseFont);

        // ===== TOOLTIP (thêm mới — cho bảng user) =====
        UIManager.put("ToolTip.font",                 new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("ToolTip.background",           new Color(0xFFFDE7));
        UIManager.put("ToolTip.foreground",           new Color(0x212121));
        UIManager.put("ToolTip.border",               BorderFactory.createLineBorder(
                new Color(0xFFC107)));
    }
}