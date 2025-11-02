package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.ThoiKhoaBieuDAO;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThoiKhoaBieuBUS {
    private final ThoiKhoaBieuDAO dao;

    public ThoiKhoaBieuBUS(Connection conn) {
        this.dao = new ThoiKhoaBieuDAO(conn);
    }

    // ===== Lấy toàn bộ danh sách TKB =====
    public List<ThoiKhoaBieuDTO> getAll() throws SQLException {
        return dao.getAll();
    }

    // ===== Lọc theo lớp và học kỳ =====
    public List<ThoiKhoaBieuDTO> findByLopHocKy(int maLop, String hocKy) throws SQLException {
        return dao.findByLopHocKy(maLop, hocKy);
    }

    // ===== Thêm mới thời khóa biểu =====
    public String addTKB(ThoiKhoaBieuDTO tkb) {
        try {
            // Kiểm tra logic & trùng lặp
            String msg = validateConflict(tkb);
            if (msg != null)
                return msg;

            int id = dao.insert(tkb);
            return (id > 0)
                    ? "✅ Thêm thời khóa biểu thành công!"
                    : "❌ Không thể thêm vào cơ sở dữ liệu!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi thêm thời khóa biểu: " + e.getMessage();
        }
    }

    // ===== Lấy danh sách lớp có trong TKB =====
    public List<String> getDistinctLop() throws SQLException {
        return dao.getDistinctLop();
    }

    // Lấy danh sách tên lớp mà một giáo viên (MaGV) đang dạy
    public List<String> getDistinctLopByGiaoVien(int maGV) throws SQLException {
        return dao.getDistinctLopByGiaoVien(maGV);
    }

    // ===== Lấy mã lớp theo tên lớp =====
    public int getMaLopByTen(String tenLop) throws SQLException {
        return dao.getMaLopByTen(tenLop);
    }

    /**
     * Kiểm tra xung đột thời khóa biểu theo lớp, GV, phòng, tiết, thứ, học kỳ, năm
     * học.
     * Trả về null nếu hợp lệ; trả về thông báo lỗi nếu trùng.
     */
    private String validateConflict(ThoiKhoaBieuDTO tkb) throws SQLException {
        // ===== 1. Kiểm tra dữ liệu cơ bản =====
        if (tkb.getMaLop() <= 0 || tkb.getMaMon() <= 0 || tkb.getMaGV() <= 0 || tkb.getMaPhong() <= 0)
            return "❌ Vui lòng chọn đầy đủ Lớp, Môn học, Giáo viên và Phòng học.";

        if (tkb.getTietBatDau() < 1 || tkb.getTietKetThuc() > 10)
            return "❌ Tiết học phải nằm trong khoảng 1 đến 10.";

        if (tkb.getTietBatDau() > tkb.getTietKetThuc())
            return "❌ Tiết bắt đầu không được lớn hơn tiết kết thúc.";

        // ===== 2. Lấy toàn bộ TKB cùng năm học + học kỳ =====
        List<ThoiKhoaBieuDTO> ds = dao.findByNamHocHocKy(tkb.getNamHoc(), tkb.getHocKy());
        if (ds == null)
            return null;

        // ===== 3. Kiểm tra trùng lớp / giáo viên / phòng =====
        for (ThoiKhoaBieuDTO t : ds) {
            if (tkb.getMaTKB() != 0 && tkb.getMaTKB() == t.getMaTKB())
                continue;
            if (!t.getThuTrongTuan().equalsIgnoreCase(tkb.getThuTrongTuan()))
                continue;

            // Kiểm tra giao tiết (overlap)
            boolean overlap = !(tkb.getTietKetThuc() < t.getTietBatDau()
                    || tkb.getTietBatDau() > t.getTietKetThuc());

            // === Lớp trùng ===
            if (t.getMaLop() == tkb.getMaLop() && overlap) {
                return String.format("❌ Lớp %d đã có tiết %d–%d vào %s (HK:%s, Năm:%s).",
                        t.getMaLop(), t.getTietBatDau(), t.getTietKetThuc(),
                        t.getThuTrongTuan(), t.getHocKy(), t.getNamHoc());
            }

            // === Giáo viên trùng ===
            if (t.getMaGV() == tkb.getMaGV() && overlap) {
                return String.format("❌ Giáo viên %d đã dạy lớp khác vào %s tiết %d–%d (HK:%s, Năm:%s).",
                        t.getMaGV(), t.getThuTrongTuan(), t.getTietBatDau(),
                        t.getTietKetThuc(), t.getHocKy(), t.getNamHoc());
            }

            // === Phòng học trùng ===
            if (t.getMaPhong() == tkb.getMaPhong() && overlap) {
                return String.format("❌ Phòng %d đã có lớp khác vào %s tiết %d–%d (HK:%s, Năm:%s).",
                        t.getMaPhong(), t.getThuTrongTuan(), t.getTietBatDau(),
                        t.getTietKetThuc(), t.getHocKy(), t.getNamHoc());
            }
        }

        // ===== 4. Không có trùng =====
        return null;
    }

    public int deleteTKB(int maTKB) throws SQLException {
        return dao.delete(maTKB);
    }

    public int restoreTKB(int maTKB) throws SQLException {
        return dao.restore(maTKB);
    }

    public List<ThoiKhoaBieuDTO> findDeletedByLopHocKy(int maLop, String hocKy) throws SQLException {
        return dao.findByLopHocKy(maLop, hocKy, false); // false = lấy các bản ghi TrangThai=0
    }

    // ===== Cập nhật thời khóa biểu =====
    public String updateTKB(ThoiKhoaBieuDTO tkb) {
        try {
            if (tkb.getMaTKB() <= 0)
                return "❌ Thiếu MaTKB để cập nhật!";

            // dùng cùng validator với thêm mới; validator đã bỏ qua chính MaTKB khi so sánh
            String msg = validateConflict(tkb);
            if (msg != null)
                return msg;

            int rows = dao.update(tkb);
            return (rows > 0)
                    ? "✅ Cập nhật thời khóa biểu thành công!"
                    : "❌ Không thể cập nhật (không tìm thấy bản ghi hoặc dữ liệu không đổi).";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi cập nhật thời khóa biểu: " + e.getMessage();
        }
    }

    public void exportToCSV(List<ThoiKhoaBieuDTO> list, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.println("MaTKB,MaLop,MaGV,MaMon,MaPhong,HocKy,NamHoc,ThuTrongTuan,TietBatDau,TietKetThuc,TrangThai");
            for (ThoiKhoaBieuDTO tkb : list) {
                writer.printf("%d,%d,%d,%d,%d,%s,%s,%s,%d,%d,%d%n",
                        tkb.getMaTKB(), tkb.getMaLop(), tkb.getMaGV(), tkb.getMaMon(),
                        tkb.getMaPhong(), tkb.getHocKy(), tkb.getNamHoc(),
                        tkb.getThuTrongTuan(), tkb.getTietBatDau(), tkb.getTietKetThuc(), tkb.getTrangThai());
            }
        }
    }

    public List<ThoiKhoaBieuDTO> importFromCSV(File file) throws IOException {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine(); // bỏ dòng tiêu đề
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 11)
                    continue;
                ThoiKhoaBieuDTO tkb = new ThoiKhoaBieuDTO(
                        Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]),
                        p[5], p[6], p[7], Integer.parseInt(p[8]), Integer.parseInt(p[9]),
                        Integer.parseInt(p[10]));
                list.add(tkb);
            }
        }
        return list;
    }

    public int addThoiKhoaBieu(ThoiKhoaBieuDTO dto) throws SQLException {
        return dao.insert(dto); // gọi thẳng xuống DAO
    }

}
