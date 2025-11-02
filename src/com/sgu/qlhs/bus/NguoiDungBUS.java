package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.NguoiDungDAO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.DatabaseConnection; // Thêm dòng này để dùng class kết nối chung
import java.sql.Connection;

public class NguoiDungBUS {

    private Connection conn;
    private NguoiDungDAO dao;

    public NguoiDungBUS() throws Exception {
        // ✅ Dùng class DatabaseConnection thay vì DriverManager trực tiếp
        conn = DatabaseConnection.getConnection();
        dao = new NguoiDungDAO(conn);
    }

    public NguoiDungDTO dangNhap(String username, String password) {
        try {
            return dao.dangNhap(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}








