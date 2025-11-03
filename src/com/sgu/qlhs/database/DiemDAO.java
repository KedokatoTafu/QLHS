package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiemDAO {

    public List<Object[]> getAllDiem() {
        List<Object[]> data = new ArrayList<>();
        // THÊM: Lấy thêm mh.LoaiMon, d.KetQuaDanhGia
        String sql = "SELECT hs.MaHS, hs.HoTen, mh.TenMon, mh.LoaiMon, d.HocKy, d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy, d.KetQuaDanhGia "
                + "FROM Diem d "
                + "JOIN HocSinh hs ON d.MaHS = hs.MaHS "
                + "JOIN MonHoc mh ON d.MaMon = mh.MaMon";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[10]; // Sửa size
                row[0] = rs.getInt("MaHS");
                row[1] = rs.getString("HoTen");
                row[2] = rs.getString("TenMon");
                row[3] = rs.getString("LoaiMon"); // Thêm
                row[4] = rs.getInt("HocKy");
                row[5] = rs.getDouble("DiemMieng");
                row[6] = rs.getDouble("Diem15p");
                row[7] = rs.getDouble("DiemGiuaKy");
                row[8] = rs.getDouble("DiemCuoiKy");
                row[9] = rs.getString("KetQuaDanhGia"); // Thêm
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu điểm: " + e.getMessage());
        }
        return data;
    }

    // Hàm này giờ không dùng KetQuaDanhGia
    public void insertDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk, double ck) {
        String sql = "INSERT INTO Diem (MaHS, MaMon, HocKy, MaNK, DiemMieng, Diem15p, DiemGiuaKy, DiemCuoiKy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maMon);
            pstmt.setInt(3, hocKy);
            pstmt.setInt(4, maNK);
            pstmt.setDouble(5, mieng);
            pstmt.setDouble(6, p15);
            pstmt.setDouble(7, gk);
            pstmt.setDouble(8, ck);

            pstmt.executeUpdate();
            System.out.println("Thêm điểm thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm điểm: " + e.getMessage());
        }
    }

    /**
     * Insert or update a Diem row identified by (MaHS, MaMon, HocKy, MaNK).
     * This uses MySQL's ON DUPLICATE KEY UPDATE; requires unique constraint uq_diem
     * on those columns.
     * * CẬP NHẬT: Thêm KetQuaDanhGia
     */
    public void upsertDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck, String ketQuaDanhGia) {
        String sql = "INSERT INTO Diem (MaHS, MaMon, HocKy, MaNK, DiemMieng, Diem15p, DiemGiuaKy, DiemCuoiKy, KetQuaDanhGia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE DiemMieng = VALUES(DiemMieng), Diem15p = VALUES(Diem15p), "
                + "DiemGiuaKy = VALUES(DiemGiuaKy), DiemCuoiKy = VALUES(DiemCuoiKy), KetQuaDanhGia = VALUES(KetQuaDanhGia)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maMon);
            pstmt.setInt(3, hocKy);
            pstmt.setInt(4, maNK);
            pstmt.setDouble(5, mieng);
            pstmt.setDouble(6, p15);
            pstmt.setDouble(7, gk);
            pstmt.setDouble(8, ck);
            pstmt.setString(9, ketQuaDanhGia); // Thêm
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi upsert điểm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Upsert with teacher note (GhiChu).
     * CẬP NHẬT: Thêm KetQuaDanhGia
     */
    public void upsertDiemWithNote(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck, String ghiChu, String ketQuaDanhGia) {
        String sql = "INSERT INTO Diem (MaHS, MaMon, HocKy, MaNK, DiemMieng, Diem15p, DiemGiuaKy, DiemCuoiKy, GhiChu, KetQuaDanhGia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE DiemMieng = VALUES(DiemMieng), Diem15p = VALUES(Diem15p), "
                + "DiemGiuaKy = VALUES(DiemGiuaKy), DiemCuoiKy = VALUES(DiemCuoiKy), GhiChu = VALUES(GhiChu), KetQuaDanhGia = VALUES(KetQuaDanhGia)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maMon);
            pstmt.setInt(3, hocKy);
            pstmt.setInt(4, maNK);
            pstmt.setDouble(5, mieng);
            pstmt.setDouble(6, p15);
            pstmt.setDouble(7, gk);
            pstmt.setDouble(8, ck);
            pstmt.setString(9, ghiChu);
            pstmt.setString(10, ketQuaDanhGia); // Thêm
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi upsert điểm (with note): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Object[]> getDiemByLopHocKy(int maLop, int hocKy, int maNK) {
        List<Object[]> data = new ArrayList<>();
        // THÊM: Lấy mh.LoaiMon, d.KetQuaDanhGia
        String sql = "SELECT hs.MaHS, hs.HoTen, l.TenLop, mh.MaMon, mh.TenMon, mh.LoaiMon, d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy, d.KetQuaDanhGia "
                +
                "FROM Diem d " +
                "JOIN HocSinh hs ON d.MaHS = hs.MaHS " +
                "JOIN MonHoc mh ON d.MaMon = mh.MaMon " +
                "JOIN Lop l ON hs.MaLop = l.MaLop " +
                "WHERE l.MaLop = ? AND d.HocKy = ? AND d.MaNK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maLop);
            pstmt.setInt(2, hocKy);
            pstmt.setInt(3, maNK);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[11]; // Sửa size
                    row[0] = rs.getInt("MaHS");
                    row[1] = rs.getString("HoTen");
                    row[2] = rs.getString("TenLop");
                    row[3] = rs.getInt("MaMon");
                    row[4] = rs.getString("TenMon");
                    row[5] = rs.getString("LoaiMon"); // Thêm
                    row[6] = rs.getDouble("DiemMieng");
                    row[7] = rs.getDouble("Diem15p");
                    row[8] = rs.getDouble("DiemGiuaKy");
                    row[9] = rs.getDouble("DiemCuoiKy");
                    row[10] = rs.getString("KetQuaDanhGia"); // Thêm
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn điểm theo lớp: " + e.getMessage());
        }
        return data;
    }

    public List<Object[]> getDiemByMaHS(int maHS, int hocKy, int maNK) {
        List<Object[]> data = new ArrayList<>();
        // THÊM: Lấy mh.LoaiMon, d.KetQuaDanhGia
        String sql = "SELECT d.MaDiem, mh.MaMon, mh.TenMon, mh.LoaiMon, d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy, d.GhiChu, d.KetQuaDanhGia "
                +
                "FROM Diem d JOIN MonHoc mh ON d.MaMon = mh.MaMon " +
                "WHERE d.MaHS = ? AND d.HocKy = ? AND d.MaNK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, hocKy);
            pstmt.setInt(3, maNK);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[10]; // Sửa size
                    row[0] = rs.getInt("MaDiem");
                    row[1] = rs.getInt("MaMon");
                    row[2] = rs.getString("TenMon");
                    row[3] = rs.getString("LoaiMon"); // Thêm
                    row[4] = rs.getDouble("DiemMieng");
                    row[5] = rs.getDouble("Diem15p");
                    row[6] = rs.getDouble("DiemGiuaKy");
                    row[7] = rs.getDouble("DiemCuoiKy");
                    row[8] = rs.getString("GhiChu");
                    row[9] = rs.getString("KetQuaDanhGia"); // Thêm
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn điểm của học sinh: " + e.getMessage());
        }
        return data;
    }

    /**
     * Get the teacher comment (nhận xét) for a student in a given niên khóa and học
     * kỳ.
     */
    public String getNhanXet(int maHS, int maNK, int hocKy) {
        String sql = "SELECT GhiChu FROM DiemNhanXet WHERE MaHS = ? AND MaNK = ? AND HocKy = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maNK);
            pstmt.setInt(3, hocKy);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("GhiChu");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đọc nhận xét: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insert or update a teacher comment (nhận xét) for a student/niên khóa/học kỳ.
     */
    public void upsertNhanXet(int maHS, int maNK, int hocKy, String ghiChu) {
        String sql = "INSERT INTO DiemNhanXet (MaHS, MaNK, HocKy, GhiChu) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE GhiChu = VALUES(GhiChu)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maNK);
            pstmt.setInt(3, hocKy);
            pstmt.setString(4, ghiChu);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi upsert nhận xét: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Flexible server-side filtered query for Diem rows.
     * Any filter parameter can be null to mean "no filter".
     * CẬP NHẬT: Thêm LoaiMon, KetQuaDanhGia
     */
    public List<com.sgu.qlhs.dto.DiemDTO> getDiemFiltered(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset) {
        List<com.sgu.qlhs.dto.DiemDTO> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT d.MaDiem, d.MaHS, hs.HoTen, l.MaLop, l.TenLop, mh.MaMon, mh.TenMon, mh.LoaiMon, d.HocKy, d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy, d.DiemTB, d.XepLoai, d.KetQuaDanhGia, d.GhiChu ");
        sb.append("FROM Diem d ");
        sb.append("JOIN HocSinh hs ON d.MaHS = hs.MaHS ");
        sb.append("LEFT JOIN Lop l ON hs.MaLop = l.MaLop ");
        sb.append("JOIN MonHoc mh ON d.MaMon = mh.MaMon ");
        sb.append("WHERE 1=1 ");

        if (maNK != null) {
            sb.append("AND d.MaNK = ? ");
        }
        if (maLop != null) {
            sb.append("AND l.MaLop = ? ");
        }
        if (maMon != null) {
            sb.append("AND mh.MaMon = ? ");
        }
        if (hocKy != null && hocKy > 0) {
            sb.append("AND d.HocKy = ? ");
        }

        sb.append("ORDER BY l.TenLop, hs.HoTen, mh.TenMon ");

        if (limit != null && limit > 0) {
            sb.append(" LIMIT ? ");
            if (offset != null && offset >= 0) {
                sb.append(" OFFSET ? ");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (maNK != null) {
                pstmt.setInt(idx++, maNK);
            }
            if (maLop != null) {
                pstmt.setInt(idx++, maLop);
            }
            if (maMon != null) {
                pstmt.setInt(idx++, maMon);
            }
            if (hocKy != null && hocKy > 0) {
                pstmt.setInt(idx++, hocKy);
            }
            if (limit != null && limit > 0) {
                pstmt.setInt(idx++, limit);
                if (offset != null && offset >= 0) {
                    pstmt.setInt(idx++, offset);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    com.sgu.qlhs.dto.DiemDTO d = new com.sgu.qlhs.dto.DiemDTO();
                    d.setMaDiem(rs.getInt("MaDiem"));
                    d.setMaHS(rs.getInt("MaHS"));
                    d.setHoTen(rs.getString("HoTen"));
                    d.setMaLop(rs.getInt("MaLop"));
                    d.setTenLop(rs.getString("TenLop"));
                    d.setMaMon(rs.getInt("MaMon"));
                    d.setTenMon(rs.getString("TenMon"));
                    d.setLoaiMon(rs.getString("LoaiMon")); // Thêm
                    d.setHocKy(rs.getInt("HocKy"));
                    d.setDiemMieng(rs.getDouble("DiemMieng"));
                    d.setDiem15p(rs.getDouble("Diem15p"));
                    d.setDiemGiuaKy(rs.getDouble("DiemGiuaKy"));
                    d.setDiemCuoiKy(rs.getDouble("DiemCuoiKy"));
                    d.setDiemTB(rs.getDouble("DiemTB"));
                    d.setXepLoai(rs.getString("XepLoai"));
                    d.setKetQuaDanhGia(rs.getString("KetQuaDanhGia")); // Thêm
                    d.setGhiChu(rs.getString("GhiChu")); // Thêm
                    result.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn điểm (filtered): " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public void deleteDiem(int maHS, int maMon, int hocKy, int maNK) {
        String sql = "DELETE FROM Diem WHERE MaHS = ? AND MaMon = ? AND HocKy = ? AND MaNK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maMon);
            pstmt.setInt(3, hocKy);
            pstmt.setInt(4, maNK);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa điểm: " + e.getMessage());
            e.printStackTrace();
        }
    }
}