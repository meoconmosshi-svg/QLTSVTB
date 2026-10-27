package com.qlts.conn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Mở kết nối tới SQL Server theo cấu hình trong db.properties. */
public final class DBConnection {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new ExceptionInInitializerError("Không tìm thấy file db.properties trong resources.");
            }
            PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("QLTS_DB_PASSWORD");
        if (password == null) {
            password = PROPS.getProperty("db.password", "");
        }
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"), PROPS.getProperty("db.user"), password);
    }
}
