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
        List<Object[]> rows = dao.getAllDiem();
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String tenMon = r[2] != null ? r[2].toString() : "";
            int hocKy = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            double mieng = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0;
            double p15 = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double gk = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            double ck = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0;
            DiemDTO dto = new DiemDTO();
            dto.setMaHS(maHS);
            dto.setHoTen(hoTen);
            dto.setTenMon(tenMon);
            dto.setHocKy(hocKy);
            dto.setDiemMieng(mieng);
            dto.setDiem15p(p15);
            dto.setDiemGiuaKy(gk);
            dto.setDiemCuoiKy(ck);
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByLopHocKy(int maLop, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getDiemByLopHocKy(maLop, hocKy, maNK);
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            // r[2] TenLop ignored here
            int maMon = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            String tenMon = r[4] != null ? r[4].toString() : "";
            double mieng = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double p15 = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            double gk = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0;
            double ck = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0;
            list.add(new DiemDTO(maHS, hoTen, maMon, tenMon, mieng, p15, gk, ck));
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getDiemByMaHS(maHS, hocKy, maNK);
        // enrich with student info (maHS, hoTen, tenLop) since DAO returns columns
        // focused on the diem row itself
        HocSinhDTO hsInfo = null;
        try {
            hsInfo = hocSinhBUS.getHocSinhByMaHS(maHS);
        } catch (Exception ex) {
            // ignore - enrichment is best-effort
        }
        String hoTen = hsInfo != null ? hsInfo.getHoTen() : "";
        String tenLop = hsInfo != null ? hsInfo.getTenLop() : "";
        for (Object[] r : rows) {
            int maDiem = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            int maMon = (r[1] instanceof Integer) ? (Integer) r[1] : Integer.parseInt(r[1].toString());
            String tenMon = r[2] != null ? r[2].toString() : "";
            double mieng = r[3] != null ? Double.parseDouble(r[3].toString()) : 0.0;
            double p15 = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0;
            double gk = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double ck = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            DiemDTO d = new DiemDTO();
            d.setMaDiem(maDiem);
            // set student/context information so presentation layer can show MaHS, HoTen,
            // TenLop, HocKy
            d.setMaHS(maHS);
            d.setHoTen(hoTen);
            d.setTenLop(tenLop);
            d.setHocKy(hocKy);
            d.setMaMon(maMon);
            d.setTenMon(tenMon);
            d.setDiemMieng(mieng);
            d.setDiem15p(p15);
            d.setDiemGiuaKy(gk);
            d.setDiemCuoiKy(ck);
            // If DAO returned ghi chu (8th column), set it
            if (r.length > 7 && r[7] != null) {
                d.setGhiChu(r[7].toString());
            } else {
                d.setGhiChu("");
            }
            list.add(d);
        }
        return list;
    }

    /**
     * Read-side permission-aware fetch. If the calling user is a student and
     * requests data for another student, return an empty list.
     */
    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS) {
                // students may not view other students' grades
                return new ArrayList<>();
            }
        }
        return getDiemByMaHS(maHS, hocKy, maNK);
    }

    /**
     * Get teacher comment (nhận xét) for a student in a given niên khóa and học kỳ.
     */
    public String getNhanXet(int maHS, int maNK, int hocKy) {
        return dao.getNhanXet(maHS, maNK, hocKy);
    }

    /**
     * Permission-aware getter for teacher comment. Students may only read their
     * own comments.
     */
    public String getNhanXet(int maHS, int maNK, int hocKy, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS)
                return null;
        }
        return getNhanXet(maHS, maNK, hocKy);
    }

    /**
     * Save or update teacher comment (nhận xét).
     */
    public void saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu) {
        dao.upsertNhanXet(maHS, maNK, hocKy, ghiChu);
    }

    /**
     * Save teacher comment with permission check. Returns true if saved.
     */
    public boolean saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu, NguoiDungDTO user) {
        // if user is teacher, ensure they are assigned to the student's class in this
        // NK/HK
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

    // Thin write facade that delegates to DAO. Keeps Presentation layer unaware of
    // DAO.
    public void saveDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk, double ck) {
        dao.insertDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
    }

    /**
     * Insert or update a diem row. Delegates to DAO.upsertDiem.
     */
    public void saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck) {
        dao.upsertDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
    }

    /**
     * Save or update diem with permission check. Returns true if saved.
     */
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck, NguoiDungDTO user) {
        // If user is teacher, verify assignment
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
            dao.upsertDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Save or update diem and teacher note (ghiChu).
     */
    public void saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck, String ghiChu) {
        dao.upsertDiemWithNote(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, ghiChu);
    }

    /**
     * Save or update diem with note and permission check. Returns true if saved.
     */
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck, String ghiChu, NguoiDungDTO user) {
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
            dao.upsertDiemWithNote(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, ghiChu);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm (with note): " + ex.getMessage());
            return false;
        }
    }

    public void deleteDiem(int maHS, int maMon, int hocKy, int maNK) {
        dao.deleteDiem(maHS, maMon, hocKy, maNK);
    }

    /**
     * Delete diem with permission check. Returns true if deleted.
     */
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

    /**
     * Helper: check whether a teacher (maGV) is assigned to teach the class of maHS
     * for the given niên khóa (maNK) and học kỳ (hocKy).
     * If maMon is not null, also require teacher assigned for that subject.
     */
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

    /**
     * Permission-aware filtered fetch. If user is a teacher, only return diem rows
     * that the teacher is assigned to (by class/subject/NK/HK). This method wraps
     * {@link #getDiemFiltered(Integer,Integer,Integer,Integer,Integer,Integer)} and
     * post-filters the results.
     */
    public List<DiemDTO> getDiemFilteredForUser(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset, NguoiDungDTO user) {
        List<DiemDTO> all = getDiemFiltered(maLop, maMon, hocKy, maNK, limit, offset);
        if (user == null || !"giao_vien".equalsIgnoreCase(user.getVaiTro()))
            return all;
        java.util.List<DiemDTO> filtered = new java.util.ArrayList<>();
        for (DiemDTO d : all) {
            try {
                // only include rows where the teacher is assigned to the student's class /
                // subject
                if (isTeacherAssignedPublic(user.getId(), d.getMaHS(), d.getMaMon(), d.getHocKy(), maNK)) {
                    filtered.add(d);
                }
            } catch (Exception ex) {
                // on error, be conservative and skip the row
            }
        }
        return filtered;
    }

    /**
     * Public wrapper around the internal assignment check. Returns true when the
     * teacher (maGV) is assigned to the student's class (and optionally subject)
     * for the given NK/HK.
     */
    public boolean isTeacherAssignedPublic(int maGV, int maHS, Integer maMon, int hocKy, int maNK) {
        try {
            return isTeacherAssigned(maGV, maHS, maMon, hocKy, maNK);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra phân công: " + ex.getMessage());
            return false;
        }
    }
}
