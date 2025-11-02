package com.sgu.qlhs.database;

import com.sgu.qlhs.dto.NguoiDungDTO;
import java.sql.*;

public class NguoiDungDAO {

    private Connection conn;

    public NguoiDungDAO(Connection conn) {
        this.conn = conn;
    }

    public NguoiDungDTO dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        // New schema: use TaiKhoan + TaiKhoan_GiaoVien / TaiKhoan_HocSinh tables.
        String sql = "SELECT MaTK, TenDangNhap, MatKhau, VaiTro FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, matKhau);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int maTK = rs.getInt("MaTK");
                    String roleRaw = rs.getString("VaiTro");
                    String normalizedRole;
                    // normalize DB role values to app role codes used in UI
                    if (roleRaw == null)
                        roleRaw = "";
                    String r = roleRaw.trim().toLowerCase();
                    if (r.contains("admin") || r.contains("quan_tri")) {
                        normalizedRole = "quan_tri_vien";
                    } else if (r.contains("giao") || r.contains("gv")) {
                        normalizedRole = "giao_vien";
                    } else if (r.contains("hoc") || r.contains("hs")) {
                        normalizedRole = "hoc_sinh";
                    } else {
                        normalizedRole = r.isEmpty() ? "" : r;
                    }

                    String displayName = tenDangNhap;
                    int mappedId = maTK;

                    if ("giao_vien".equals(normalizedRole)) {
                        // lookup MaGV by MaTK
                        String q = "SELECT MaGV FROM TaiKhoan_GiaoVien WHERE MaTK = ?";
                        try (PreparedStatement p2 = conn.prepareStatement(q)) {
                            p2.setInt(1, maTK);
                            try (ResultSet r2 = p2.executeQuery()) {
                                if (r2.next())
                                    mappedId = r2.getInt("MaGV");
                            }
                        }
                        // try fill HoTen from GiaoVien
                        String qname = "SELECT HoTen FROM GiaoVien WHERE MaGV = ?";
                        try (PreparedStatement p3 = conn.prepareStatement(qname)) {
                            p3.setInt(1, mappedId);
                            try (ResultSet r3 = p3.executeQuery()) {
                                if (r3.next())
                                    displayName = r3.getString("HoTen");
                            }
                        }
                    } else if ("hoc_sinh".equals(normalizedRole)) {
                        // lookup MaHS by MaTK
                        String q = "SELECT MaHS FROM TaiKhoan_HocSinh WHERE MaTK = ?";
                        try (PreparedStatement p2 = conn.prepareStatement(q)) {
                            p2.setInt(1, maTK);
                            try (ResultSet r2 = p2.executeQuery()) {
                                if (r2.next())
                                    mappedId = r2.getInt("MaHS");
                            }
                        }
                        // try fill HoTen from HocSinh
                        String qname = "SELECT HoTen FROM HocSinh WHERE MaHS = ?";
                        try (PreparedStatement p3 = conn.prepareStatement(qname)) {
                            p3.setInt(1, mappedId);
                            try (ResultSet r3 = p3.executeQuery()) {
                                if (r3.next())
                                    displayName = r3.getString("HoTen");
                            }
                        }
                    } else if ("quan_tri_vien".equals(normalizedRole)) {
                        // displayName remains TenDangNhap for admin
                    }

                    return new NguoiDungDTO(mappedId, tenDangNhap, matKhau, displayName, normalizedRole);
                }
            }
        }
        return null;
    }
}
