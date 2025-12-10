package com.ispas.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data/ispas.db";
    private static Connection conn;

    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null) {
            try {
                Files.createDirectories(Path.of(DB_DIR));
            } catch (Exception e) {
                // ignore
            }
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            initSchema(conn);
        }
        return conn;
    }

    private static void initSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS plans (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, monthly_fee REAL NOT NULL, rate_per_mb REAL NOT NULL);");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT, phone TEXT, plan_id INTEGER, FOREIGN KEY(plan_id) REFERENCES plans(id));");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS usage_records (id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id INTEGER NOT NULL, device_name TEXT, date_time TEXT NOT NULL, mb_used REAL NOT NULL, FOREIGN KEY(customer_id) REFERENCES customers(id));");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, customer_id INTEGER NOT NULL, title TEXT NOT NULL, description TEXT, status TEXT NOT NULL, created_at TEXT NOT NULL, FOREIGN KEY(customer_id) REFERENCES customers(id));");
        }
    }
}
