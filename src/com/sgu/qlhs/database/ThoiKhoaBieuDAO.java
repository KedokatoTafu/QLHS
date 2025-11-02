package com.sgu.qlhs.database;

import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThoiKhoaBieuDAO {
    private final Connection conn;

    public ThoiKhoaBieuDAO(Connection conn) {
        this.conn = conn;
    }

    // ===== Lấy tất cả TKB (dùng để kiểm tra hoặc thống kê) =====
    public List<ThoiKhoaBieuDTO> getAll() throws SQLException {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
                    SELECT tkb.*, mh.TenMon, gv.HoTen AS TenGV, ph.TenPhong
                    FROM ThoiKhoaBieu tkb
                    JOIN MonHoc mh ON tkb.MaMon = mh.MaMon
                    JOIN GiaoVien gv ON tkb.MaGV = gv.MaGV
                    JOIN PhongHoc ph ON tkb.MaPhong = ph.MaPhong
                    WHERE tkb.TrangThai = 1
                    ORDER BY tkb.MaLop, tkb.ThuTrongTuan, tkb.TietBatDau
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    // ===== Lọc TKB theo lớp và học kỳ =====
    public List<ThoiKhoaBieuDTO> findByLopHocKy(int maLop, String hocKy) throws SQLException {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
                    SELECT tkb.*, mh.TenMon, gv.HoTen AS TenGV, ph.TenPhong
                    FROM ThoiKhoaBieu tkb
                    JOIN MonHoc mh ON tkb.MaMon = mh.MaMon
                    JOIN GiaoVien gv ON tkb.MaGV = gv.MaGV
                    JOIN PhongHoc ph ON tkb.MaPhong = ph.MaPhong
                    WHERE tkb.MaLop = ? AND tkb.HocKy = ? AND tkb.TrangThai = 1
                    ORDER BY tkb.ThuTrongTuan, tkb.TietBatDau
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLop);
            ps.setString(2, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }
        }
        return list;
    }

    // ===== Thêm mới một dòng TKB =====
    public int insert(ThoiKhoaBieuDTO dto) throws SQLException {
        String sql = """
                    INSERT INTO ThoiKhoaBieu
                        (MaLop, MaGV, MaMon, MaPhong, HocKy, NamHoc,
                         ThuTrongTuan, TietBatDau, TietKetThuc, TrangThai)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dto.getMaLop());
            ps.setInt(2, dto.getMaGV());
            ps.setInt(3, dto.getMaMon());
            ps.setInt(4, dto.getMaPhong());
            ps.setString(5, dto.getHocKy());
            ps.setString(6, dto.getNamHoc());
            ps.setString(7, dto.getThuTrongTuan());
            ps.setInt(8, dto.getTietBatDau());
            ps.setInt(9, dto.getTietKetThuc());
            ps.setInt(10, dto.getTrangThai());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        }
        return -1;
    }

    // ===== Lấy danh sách tất cả lớp trong bảng Lop =====
    // (để hiển thị đầy đủ trong combobox, không lỗi ORDER BY)
    public List<String> getDistinctLop() throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = """
                    SELECT TenLop
                    FROM Lop
                    ORDER BY MaLop
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getString("TenLop"));
            }
        }
        return ds;
    }

    // Lấy danh sách tên lớp (distinct) mà giáo viên đang dạy (dựa trên TKB)
    public List<String> getDistinctLopByGiaoVien(int maGV) throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = """
                    SELECT DISTINCT l.TenLop
                    FROM ThoiKhoaBieu tkb
                    JOIN Lop l ON tkb.MaLop = l.MaLop
                    WHERE tkb.MaGV = ? AND tkb.TrangThai = 1
                    ORDER BY l.MaLop
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(rs.getString("TenLop"));
            }
        }
        return ds;
    }

    // ===== Lấy mã lớp theo tên lớp (phục vụ lọc & thêm) =====
    public int getMaLopByTen(String tenLop) throws SQLException {
        String sql = "SELECT MaLop FROM Lop WHERE TenLop = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenLop);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("MaLop");
            }
        }
        return -1; // không tìm thấy
    }

    // ===== (Tuỳ chọn) Lấy danh sách học kỳ có dữ liệu theo lớp =====
    public List<String> getDistinctHocKyByLop(int maLop) throws SQLException {
        List<String> ds = new ArrayList<>();
        String sql = """
                    SELECT DISTINCT HocKy
                    FROM ThoiKhoaBieu
                    WHERE MaLop = ? AND TrangThai = 1
                    ORDER BY HocKy
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(rs.getString("HocKy"));
            }
        }
        return ds;
    }

    // ===== Lấy tất cả TKB theo năm học và học kỳ (phục vụ kiểm tra trùng) =====
    public List<ThoiKhoaBieuDTO> findByNamHocHocKy(String namHoc, String hocKy) throws SQLException {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
                    SELECT tkb.*, mh.TenMon, gv.HoTen AS TenGV, ph.TenPhong
                    FROM ThoiKhoaBieu tkb
                    JOIN MonHoc mh ON tkb.MaMon = mh.MaMon
                    JOIN GiaoVien gv ON tkb.MaGV = gv.MaGV
                    JOIN PhongHoc ph ON tkb.MaPhong = ph.MaPhong
                    WHERE tkb.NamHoc = ? AND tkb.HocKy = ? AND tkb.TrangThai = 1
                    ORDER BY tkb.MaLop, tkb.ThuTrongTuan, tkb.TietBatDau
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namHoc);
            ps.setString(2, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int update(ThoiKhoaBieuDTO dto) throws SQLException {
        String sql = """
                    UPDATE ThoiKhoaBieu
                    SET MaLop = ?, MaGV = ?, MaMon = ?, MaPhong = ?,
                        HocKy = ?, NamHoc = ?, ThuTrongTuan = ?,
                        TietBatDau = ?, TietKetThuc = ?, TrangThai = ?,
                        NgayCapNhat = NOW()
                    WHERE MaTKB = ? AND TrangThai = 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getMaLop());
            ps.setInt(2, dto.getMaGV());
            ps.setInt(3, dto.getMaMon());
            ps.setInt(4, dto.getMaPhong());
            ps.setString(5, dto.getHocKy());
            ps.setString(6, dto.getNamHoc());
            ps.setString(7, dto.getThuTrongTuan());
            ps.setInt(8, dto.getTietBatDau());
            ps.setInt(9, dto.getTietKetThuc());
            ps.setInt(10, dto.getTrangThai());
            ps.setInt(11, dto.getMaTKB());
            return ps.executeUpdate();
        }
    }

    // (tuỳ chọn) Lấy 1 bản ghi theo MaTKB — hữu ích cho màn hình sửa / debug
    public ThoiKhoaBieuDTO findById(int maTKB) throws SQLException {
        String sql = """
                    SELECT tkb.*, mh.TenMon, gv.HoTen AS TenGV, ph.TenPhong
                    FROM ThoiKhoaBieu tkb
                    JOIN MonHoc mh ON tkb.MaMon = mh.MaMon
                    JOIN GiaoVien gv ON tkb.MaGV = gv.MaGV
                    JOIN PhongHoc ph ON tkb.MaPhong = ph.MaPhong
                    WHERE tkb.MaTKB = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTKB);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public int delete(int maTKB) throws SQLException {
        String sql = """
                    UPDATE ThoiKhoaBieu
                    SET TrangThai = 0, NgayCapNhat = NOW()
                    WHERE MaTKB = ? AND TrangThai = 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTKB);
            return ps.executeUpdate(); // trả về 1 nếu xóa thành công, 0 nếu bản ghi không tồn tại hoặc đã ẩn rồi
        }
    }

    public int restore(int maTKB) throws SQLException {
        String sql = """
                    UPDATE ThoiKhoaBieu
                    SET TrangThai = 1, NgayCapNhat = NOW()
                    WHERE MaTKB = ? AND TrangThai = 0
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTKB);
            return ps.executeUpdate(); // trả về 1 nếu khôi phục được, 0 nếu không
        }
    }

    public List<ThoiKhoaBieuDTO> findByLopHocKy(int maLop, String hocKy, boolean active) throws SQLException {
        String sql = """
                    SELECT t.*, m.TenMon, g.HoTen AS TenGV, p.TenPhong
                    FROM ThoiKhoaBieu t
                    JOIN MonHoc m ON t.MaMon = m.MaMon
                    JOIN GiaoVien g ON t.MaGV = g.MaGV
                    JOIN PhongHoc p ON t.MaPhong = p.MaPhong
                    WHERE t.MaLop = ? AND t.HocKy = ? AND t.TrangThai = ?
                    ORDER BY FIELD(t.ThuTrongTuan, 'Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'), t.TietBatDau
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLop);
            ps.setString(2, hocKy);
            ps.setInt(3, active ? 1 : 0);
            ResultSet rs = ps.executeQuery();
            List<ThoiKhoaBieuDTO> list = new java.util.ArrayList<>();
            while (rs.next()) {
                ThoiKhoaBieuDTO dto = new ThoiKhoaBieuDTO();
                dto.setMaTKB(rs.getInt("MaTKB"));
                dto.setMaLop(rs.getInt("MaLop"));
                dto.setMaGV(rs.getInt("MaGV"));
                dto.setMaMon(rs.getInt("MaMon"));
                dto.setMaPhong(rs.getInt("MaPhong"));
                dto.setHocKy(rs.getString("HocKy"));
                dto.setNamHoc(rs.getString("NamHoc"));
                dto.setThuTrongTuan(rs.getString("ThuTrongTuan"));
                dto.setTietBatDau(rs.getInt("TietBatDau"));
                dto.setTietKetThuc(rs.getInt("TietKetThuc"));
                dto.setTenMon(rs.getString("TenMon"));
                dto.setTenGV(rs.getString("TenGV"));
                dto.setTenPhong(rs.getString("TenPhong"));
                dto.setTrangThai(rs.getInt("TrangThai"));
                list.add(dto);
            }
            return list;
        }
    }

    // ===== Map dữ liệu từ ResultSet sang DTO =====
    private ThoiKhoaBieuDTO map(ResultSet rs) throws SQLException {
        ThoiKhoaBieuDTO tkb = new ThoiKhoaBieuDTO();
        tkb.setMaTKB(rs.getInt("MaTKB"));
        tkb.setMaLop(rs.getInt("MaLop"));
        tkb.setMaGV(rs.getInt("MaGV"));
        tkb.setMaMon(rs.getInt("MaMon"));
        tkb.setMaPhong(rs.getInt("MaPhong"));
        tkb.setHocKy(rs.getString("HocKy"));
        tkb.setNamHoc(rs.getString("NamHoc"));
        tkb.setThuTrongTuan(rs.getString("ThuTrongTuan"));
        tkb.setTietBatDau(rs.getInt("TietBatDau"));
        tkb.setTietKetThuc(rs.getInt("TietKetThuc"));
        tkb.setTrangThai(rs.getInt("TrangThai"));
        tkb.setTenMon(rs.getString("TenMon"));
        tkb.setTenGV(rs.getString("TenGV"));
        tkb.setTenPhong(rs.getString("TenPhong"));
        return tkb;
    }
}
