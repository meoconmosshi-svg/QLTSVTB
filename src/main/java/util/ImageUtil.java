package com.qlts.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Tiện ích đọc/ghi ảnh tài sản.
 * Ảnh được lưu trong: src/main/resources/images/assets/<maCode>.<ext>
 */
public final class ImageUtil {
    private ImageUtil() {}

    /** Thư mục gốc chứa ảnh (tự tạo nếu chưa có). */
    public static File getImageDir() {
        File dir = new File("src/main/resources/images/assets");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /**
     * Copy ảnh người dùng chọn vào thư mục dự án.
     * @return tên file đã lưu (VD: "TS0001.png") hoặc null nếu lỗi
     */
    public static String saveImage(File source, String maCode) {
        if (source == null || !source.exists()) return null;

        String ext = getExtension(source.getName());
        if (ext == null) ext = "png";

        String fileName = maCode + "." + ext;
        File dest = new File(getImageDir(), fileName);

        try {
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            System.err.println("Lỗi lưu ảnh: " + e.getMessage());
            return null;
        }
    }

    /**
     * Đọc ảnh từ thư mục dự án. Trả null nếu không có.
     */
    public static ImageIcon loadImage(String fileName, int width, int height) {
        if (fileName == null || fileName.isBlank()) return null;

        File f = new File(getImageDir(), fileName);
        if (!f.exists()) return null;

        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) return null;
            return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Đọc ảnh gốc (không scale) — dùng cho panel chi tiết.
     */
    public static ImageIcon loadImageOriginal(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        File f = new File(getImageDir(), fileName);
        if (!f.exists()) return null;
        return new ImageIcon(f.getAbsolutePath());
    }

    /** Xóa ảnh khỏi thư mục. */
    public static boolean deleteImage(String fileName) {
        if (fileName == null || fileName.isBlank()) return false;
        File f = new File(getImageDir(), fileName);
        return f.exists() && f.delete();
    }

    private static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0 && dot < name.length() - 1)
                ? name.substring(dot + 1).toLowerCase()
                : null;
    }
}