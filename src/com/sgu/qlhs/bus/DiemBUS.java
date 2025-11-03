package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sgu.qlhs.database.DiemDAO;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import java.util.ArrayList;
import java.util.List;

public class DiemBUS {
    private DiemDAO dao;

    public DiemBUS() {
        dao = new DiemDAO();
    }

    // helper bus for enriching diem rows with student info when querying by MaHS
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();

    public List<DiemDTO> getAllDiem() {
        List<DiemDTO> list = new ArrayList<>();
        // Đọc 10 cột
        List<Object[]> rows = dao.getAllDiem();
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String tenMon = r[2] != null ? r[2].toString() : "";
            String loaiMon = r[3] != null ? r[3].toString() : ""; // Thêm
            int hocKy = (r[4] instanceof Integer) ? (Integer) r[4] : Integer.parseInt(r[4].toString()); // Sửa index
            double mieng = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0; // Sửa index
            double p15 = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0; // Sửa index
            double gk = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0; // Sửa index
            double ck = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0; // Sửa index
            String ketQuaDanhGia = r[9] != null ? r[9].toString() : null; // Thêm

            DiemDTO dto = new DiemDTO();
            dto.setMaHS(maHS);
            dto.setHoTen(hoTen);
            dto.setTenMon(tenMon);
            dto.setLoaiMon(loaiMon); // Thêm
            dto.setHocKy(hocKy);
            dto.setDiemMieng(mieng);
            dto.setDiem15p(p15);
            dto.setDiemGiuaKy(gk);
            dto.setDiemCuoiKy(ck);
            dto.setKetQuaDanhGia(ketQuaDanhGia); // Thêm
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByLopHocKy(int maLop, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        // Đọc 11 cột
        List<Object[]> rows = dao.getDiemByLopHocKy(maLop, hocKy, maNK);
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            // r[2] TenLop ignored here
            int maMon = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            String tenMon = r[4] != null ? r[4].toString() : "";
            String loaiMon = r[5] != null ? r[5].toString() : ""; // Thêm
            double mieng = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0; // Sửa index
            double p15 = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0; // Sửa index
            double gk = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0; // Sửa index
            double ck = r[9] != null ? Double.parseDouble(r[9].toString()) : 0.0; // Sửa index
            String ketQuaDanhGia = r[10] != null ? r[10].toString() : null; // Thêm

            DiemDTO dto = new DiemDTO(maHS, hoTen, maMon, tenMon, mieng, p15, gk, ck);
            dto.setLoaiMon(loaiMon);
            dto.setKetQuaDanhGia(ketQuaDanhGia);
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        // Đọc 10 cột
        List<Object[]> rows = dao.getDiemByMaHS(maHS, hocKy, maNK);
        HocSinhDTO hsInfo = null;
        try {
            hsInfo = hocSinhBUS.getHocSinhByMaHS(maHS);
        } catch (Exception ex) {
            // ignore
        }
        String hoTen = hsInfo != null ? hsInfo.getHoTen() : "";
        String tenLop = hsInfo != null ? hsInfo.getTenLop() : "";
        for (Object[] r : rows) {
            int maDiem = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            int maMon = (r[1] instanceof Integer) ? (Integer) r[1] : Integer.parseInt(r[1].toString());
            String tenMon = r[2] != null ? r[2].toString() : "";
            String loaiMon = r[3] != null ? r[3].toString() : ""; // Thêm
            double mieng = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0; // Sửa index
            double p15 = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0; // Sửa index
            double gk = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0; // Sửa index
            double ck = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0; // Sửa index
            String ghiChu = r[8] != null ? r[8].toString() : ""; // Sửa index
            String ketQuaDanhGia = r[9] != null ? r[9].toString() : null; // Thêm

            DiemDTO d = new DiemDTO();
            d.setMaDiem(maDiem);
            d.setMaHS(maHS);
            d.setHoTen(hoTen);
            d.setTenLop(tenLop);
            d.setHocKy(hocKy);
            d.setMaMon(maMon);
            d.setTenMon(tenMon);
            d.setLoaiMon(loaiMon); // Thêm
            d.setDiemMieng(mieng);
            d.setDiem15p(p15);
            d.setDiemGiuaKy(gk);
            d.setDiemCuoiKy(ck);
            d.setGhiChu(ghiChu);
            d.setKetQuaDanhGia(ketQuaDanhGia); // Thêm
            list.add(d);
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS) {
                return new ArrayList<>();
            }
        }
        return getDiemByMaHS(maHS, hocKy, maNK);
    }

    public String getNhanXet(int maHS, int maNK, int hocKy) {
        return dao.getNhanXet(maHS, maNK, hocKy);
    }

    public String getNhanXet(int maHS, int maNK, int hocKy, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS)
                return null;
        }
        return getNhanXet(maHS, maNK, hocKy);
    }

    public void saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu) {
        dao.upsertNhanXet(maHS, maNK, hocKy, ghiChu);
    }

    public boolean saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu, NguoiDungDTO user) {
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                if (!isTeacherAssigned(user.getId(), maHS, null, hocKy, maNK))
                    return false;
            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu nhận xét: " + ex.getMessage());
                return false;
            }
        }
        try {
            dao.upsertNhanXet(maHS, maNK, hocKy, ghiChu);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu nhận xét: " + ex.getMessage());
            return false;
        }
    }

    // === PHẦN SỬA LỖI (NẠP CHỒNG HÀM) ===

    // HÀM CŨ (cho DiemTinhXepLoaiDialog và DiemTrungBinhTatCaMonDialog)
    // Chuyển từ double -> Double để chấp nhận null
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            double mieng, double p15, double gk, double ck, NguoiDungDTO user) {
        // Gọi hàm mới, truyền null cho ketQuaDanhGia
        return this.saveOrUpdateDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, null, user);
    }

    // HÀM MỚI (cho DiemNhapDialog)
    // Hàm này nhận Double (có thể null) và String (KetQuaDanhGia)
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            Double mieng, Double p15, Double gk, Double ck,
            String ketQuaDanhGia, NguoiDungDTO user) {

        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                if (!isTeacherAssigned(user.getId(), maHS, maMon, hocKy, maNK))
                    return false;
            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu điểm: " + ex.getMessage());
                return false;
            }
        }
        try {
            // Chuyển Double (có thể null) thành double (0.0 nếu null)
            double dMieng = (mieng != null) ? mieng : 0.0;
            double dP15 = (p15 != null) ? p15 : 0.0;
            double dGk = (gk != null) ? gk : 0.0;
            double dCk = (ck != null) ? ck : 0.0;

            // Gọi DAO (đã sửa) với 9 tham số
            dao.upsertDiem(maHS, maMon, hocKy, maNK, dMieng, dP15, dGk, dCk, ketQuaDanhGia);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm: " + ex.getMessage());
            return false;
        }
    }

    // HÀM MỚI (cho BangDiemChiTietDialog)
    // Hàm này nhận cả GhiChu và KetQuaDanhGia
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            Double mieng, Double p15, Double gk, Double ck,
            String ghiChu, String ketQuaDanhGia, NguoiDungDTO user) {
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                if (!isTeacherAssigned(user.getId(), maHS, maMon, hocKy, maNK))
                    return false;
            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu điểm (ghi chú): " + ex.getMessage());
                return false;
            }
        }
        try {
            double dMieng = (mieng != null) ? mieng : 0.0;
            double dP15 = (p15 != null) ? p15 : 0.0;
            double dGk = (gk != null) ? gk : 0.0;
            double dCk = (ck != null) ? ck : 0.0;
            // Gọi DAO (đã sửa) với 10 tham số
            dao.upsertDiemWithNote(maHS, maMon, hocKy, maNK, dMieng, dP15, dGk, dCk, ghiChu, ketQuaDanhGia);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm (with note): " + ex.getMessage());
            return false;
        }
    }
    
    // === KẾT THÚC PHẦN SỬA LỖI ===

    public void deleteDiem(int maHS, int maMon, int hocKy, int maNK) {
        dao.deleteDiem(maHS, maMon, hocKy, maNK);
    }

    public boolean deleteDiem(int maHS, int maMon, int hocKy, int maNK, NguoiDungDTO user) {
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                if (!isTeacherAssigned(user.getId(), maHS, maMon, hocKy, maNK))
                    return false;
            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi xóa điểm: " + ex.getMessage());
                return false;
            }
        }
        try {
            dao.deleteDiem(maHS, maMon, hocKy, maNK);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi xóa điểm: " + ex.getMessage());
            return false;
        }
    }

    private boolean isTeacherAssigned(int maGV, int maHS, Integer maMon, int hocKy, int maNK) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM PhanCongDay pc JOIN HocSinh hs ON hs.MaLop = pc.MaLop "
                + "WHERE pc.MaGV = ? AND pc.MaNK = ? AND pc.HocKy = ? AND hs.MaHS = ?";
        if (maMon != null) {
            sql += " AND pc.MaMon = ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            ps.setInt(idx++, maNK);
            ps.setInt(idx++, hocKy);
            ps.setInt(idx++, maHS);
            if (maMon != null) {
                ps.setInt(idx++, maMon);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        }
        return false;
    }

    public List<DiemDTO> getDiemFiltered(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset) {
        return dao.getDiemFiltered(maLop, maMon, hocKy, maNK, limit, offset);
    }

    public List<DiemDTO> getDiemFilteredForUser(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset, NguoiDungDTO user) {
        List<DiemDTO> all = getDiemFiltered(maLop, maMon, hocKy, maNK, limit, offset);
        if (user == null || !"giao_vien".equalsIgnoreCase(user.getVaiTro()))
            return all;
        java.util.List<DiemDTO> filtered = new java.util.ArrayList<>();
        for (DiemDTO d : all) {
            try {
                if (isTeacherAssignedPublic(user.getId(), d.getMaHS(), d.getMaMon(), d.getHocKy(), maNK)) {
                    filtered.add(d);
                }
            } catch (Exception ex) {
                // on error, be conservative and skip the row
            }
        }
        return filtered;
    }

    public boolean isTeacherAssignedPublic(int maGV, int maHS, Integer maMon, int hocKy, int maNK) {
        try {
            return isTeacherAssigned(maGV, maHS, maMon, hocKy, maNK);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra phân công: " + ex.getMessage());
            return false;
        }
    }
}