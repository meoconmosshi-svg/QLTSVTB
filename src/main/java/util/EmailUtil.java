package com.qlts.util;

import com.qlts.conn.DBConnection;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Gửi email qua Gmail SMTP.
 * Nếu chưa cấu hình (mail.username trống) → trả về false, caller tự xử lý fallback.
 */
public final class EmailUtil {
    private EmailUtil() {}

    private static Properties mailConfig() {
        Properties p = new Properties();
        // Đọc từ db.properties thông qua reflection đơn giản
        // (DBConnection giữ PROPS private, ta tạo Properties riêng đọc file)
        try (var in = EmailUtil.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        return p;
    }

    public static boolean isConfigured() {
        Properties p = mailConfig();
        String user = p.getProperty("mail.username", "").trim();
        String pass = p.getProperty("mail.password", "").trim();
        return !user.isEmpty() && !pass.isEmpty()
                && !user.equalsIgnoreCase("your_email@gmail.com");
    }

    /** Gửi email HTML đơn giản. Trả về true nếu gửi thành công. */
    public static boolean send(String to, String subject, String htmlBody) {
        if (!isConfigured()) return false;

        Properties p = mailConfig();
        String host = p.getProperty("mail.smtp.host", "smtp.gmail.com");
        String port = p.getProperty("mail.smtp.port", "587");
        final String username = p.getProperty("mail.username");
        final String password = p.getProperty("mail.password");
        String from = p.getProperty("mail.from", username);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "8000");
        props.put("mail.smtp.timeout", "8000");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject, "UTF-8");
            msg.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            return false;
        }
    }

    /** Template email OTP. */
    public static String otpTemplate(String hoTen, String otp) {
        return "<div style='font-family:Segoe UI,Arial;max-width:520px;margin:auto;"
             + "border:1px solid #e0e0e0;border-radius:10px;padding:24px'>"
             + "<h2 style='color:#1565C0;margin-top:0'>Quản Lý Tài Sản & Thiết Bị</h2>"
             + "<p>Xin chào <b>" + (hoTen == null ? "bạn" : hoTen) + "</b>,</p>"
             + "<p>Mã xác thực (OTP) để đặt lại mật khẩu của bạn là:</p>"
             + "<div style='font-size:32px;font-weight:bold;letter-spacing:8px;"
             + "text-align:center;padding:16px;background:#F5F5F5;border-radius:8px;"
             + "color:#D32F2F'>" + otp + "</div>"
             + "<p style='color:#757575;font-size:13px'>Mã có hiệu lực trong <b>5 phút</b>. "
             + "Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>"
             + "</div>";
    }
}