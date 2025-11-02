package com.sgu.qlhs.database;

import com.sgu.qlhs.dto.NguoiDungDTO;
import java.sql.*;

public class NguoiDungDAO {

    private Connection conn;

    public NguoiDungDAO(Connection conn) {
        this.conn = conn;
    }

    public NguoiDungDTO dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT * FROM nguoidung WHERE ten_dang_nhap = ? AND mat_khau = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, tenDangNhap);
        stmt.setString(2, matKhau);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return new NguoiDungDTO(
                rs.getInt("id"),
                rs.getString("ten_dang_nhap"),
                rs.getString("mat_khau"),
                rs.getString("ho_ten"),
                rs.getString("vai_tro")
            );
        }
        return null;
    }
}
