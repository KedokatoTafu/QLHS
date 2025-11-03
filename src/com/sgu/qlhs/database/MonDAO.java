package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonDAO {

    public List<Object[]> getAllMon() {
        List<Object[]> data = new ArrayList<>();
        // <-- CẬP NHẬT CÂU LỆNH SQL -->
        String sql = "SELECT MaMon, TenMon, SoTiet, GhiChu, LoaiMon FROM MonHoc";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // <-- CẬP NHẬT SỐ LƯỢNG CỘT -->
                Object[] row = new Object[5];
                row[0] = rs.getInt("MaMon");
                row[1] = rs.getString("TenMon");
                row[2] = rs.getInt("SoTiet");
                row[3] = rs.getString("GhiChu");
                row[4] = rs.getString("LoaiMon"); // <-- LẤY DỮ LIỆU CỘT MỚI
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu môn: " + e.getMessage());
        }
        return data;
    }

    public void insertMon(String tenMon, int soTiet, String ghiChu) {
        // Giả định rằng LoaiMon có DEFAULT 'TinhDiem' trong DB
        String sql = "INSERT INTO MonHoc (TenMon, SoTiet, GhiChu) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenMon);
            pstmt.setInt(2, soTiet);
            pstmt.setString(3, ghiChu);
            pstmt.executeUpdate();
            System.out.println("Thêm môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm môn: " + e.getMessage());
        }
    }

    public void updateMon(int maMon, String tenMon, int soTiet, String ghiChu) {
        // Cập nhật không thay đổi LoaiMon, việc này phải làm riêng
        String sql = "UPDATE MonHoc SET TenMon = ?, SoTiet = ?, GhiChu = ? WHERE MaMon = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenMon);
            pstmt.setInt(2, soTiet);
            pstmt.setString(3, ghiChu);
            pstmt.setInt(4, maMon);
            pstmt.executeUpdate();
            System.out.println("Cập nhật môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật môn: " + e.getMessage());
        }
    }

    public void deleteMon(int maMon) {
        String sql = "DELETE FROM MonHoc WHERE MaMon = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maMon);
            pstmt.executeUpdate();
            System.out.println("Xóa môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa môn: " + e.getMessage());
        }
    }
}