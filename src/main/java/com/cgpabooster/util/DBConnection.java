package com.cgpabooster.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv("DB_URL");
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found on classpath.", e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null || USERNAME == null || PASSWORD == null) {
            throw new SQLException("Database environment variables are not configured.");
        }

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
