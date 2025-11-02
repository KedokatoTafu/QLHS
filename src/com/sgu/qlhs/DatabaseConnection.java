package com.sgu.qlhs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/QLHS";
    private static final String USER = "root";
    private static final String PASSWORD = "9851343a";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Đăng ký Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Mở kết nối
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối cơ sở dữ liệu thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối cơ sở dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng kết nối: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}