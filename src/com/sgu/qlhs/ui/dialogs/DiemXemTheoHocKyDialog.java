package com.sgu.qlhs.ui.dialogs;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog để xem điểm trung bình theo học kỳ và cả năm
 */
public class DiemXemTheoHocKyDialog extends JDialog {
    private final JComboBox<String> cboLoaiXem = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2", "Cả năm" });
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboNamHoc = new JComboBox<>();
    // parallel list mapping combo index -> MaNK
    private java.util.List<Integer> nienKhoaIds = new java.util.ArrayList<>();
    private DefaultTableModel model;
    private JTable table;
    // Note: DiemHocKyDAO / DiemHocKyBUS removed; dialog will query DB directly
    // using views/columns
    // BUS helpers
    private LopBUS lopBUS;
    private HocSinhBUS hocSinhBUS;
    private DiemBUS diemBUS;
    // lưu danh sách lớp để map index -> MaLop / TenLop
    private List<LopDTO> lopList = new ArrayList<>();

    public DiemXemTheoHocKyDialog(Window owner) {
        super(owner, "Xem điểm theo học kỳ / Cả năm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
        lopBUS = new LopBUS();
        hocSinhBUS = new HocSinhBUS();
        diemBUS = new DiemBUS();
        build();
        loadLopData();
        loadNamHocOptions();
        pack();
    }

    /**
     * Load năm học (Niên khóa) options from DB into cboNamHoc and nienKhoaIds.
     */
    private void loadNamHocOptions() {
        cboNamHoc.removeAllItems();
        nienKhoaIds.clear();
        // order ascending so combo shows older years first (2022-2023, 2023-2024,
        // 2024-2025)
        String sql = "SELECT MaNK, NamBatDau, NamKetThuc FROM NienKhoa ORDER BY NamBatDau ASC, MaNK ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int maNK = rs.getInt("MaNK");
                int nb = rs.getInt("NamBatDau");
                int nk = rs.getInt("NamKetThuc");
                String label = nb + "-" + nk;
                cboNamHoc.addItem(label);
                nienKhoaIds.add(maNK);
            }
        } catch (SQLException ex) {
            System.err.println("Không thể load Niên khóa: " + ex.getMessage());
        }

        // select current MaNK if present
        int current = com.sgu.qlhs.bus.NienKhoaBUS.current();
        int idx = nienKhoaIds.indexOf(current);
        if (idx >= 0) {
            cboNamHoc.setSelectedIndex(idx);
        } else if (cboNamHoc.getItemCount() > 0) {
            cboNamHoc.setSelectedIndex(0);
        }
    }

    // --- DB-backed helpers (replace previous DiemHocKyBUS methods) ---
    private List<Object[]> queryDiemTrungBinhHocKy(int hocKy, int maNK) {
        List<Object[]> result = new ArrayList<>();
        // Check logged-in user: if student, show only that student's average
        com.sgu.qlhs.dto.NguoiDungDTO nd = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                nd = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
        }
        if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
            int maHS = nd.getId();
            List<DiemDTO> rows = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, nd);
            double sum = 0;
            int cnt = 0;
            String name = "";
            for (DiemDTO d : rows) {
                name = d.getHoTen() != null ? d.getHoTen() : name;
                sum += d.getDiemTB();
                cnt++;
            }
            double avg = cnt > 0 ? round1(sum / cnt) : 0.0;
            Object[] row = new Object[4];
            row[0] = maHS;
            row[1] = name;
            row[2] = avg;
            row[3] = xepLoai(avg);
            result.add(row);
            return result;
        }

        // default: aggregate for all students
        List<DiemDTO> rows = diemBUS.getDiemFiltered(null, null, hocKy, maNK, null, null);
        java.util.Map<Integer, java.util.List<Double>> m = new java.util.HashMap<>();
        java.util.Map<Integer, String> names = new java.util.HashMap<>();
        for (DiemDTO d : rows) {
            names.put(d.getMaHS(), d.getHoTen());
            m.computeIfAbsent(d.getMaHS(), k -> new java.util.ArrayList<>()).add(d.getDiemTB());
        }
        java.util.List<Integer> keys = new java.util.ArrayList<>(m.keySet());
        java.util.Collections.sort(keys, (a, b) -> names.get(a).compareToIgnoreCase(names.get(b)));
        for (Integer maHS : keys) {
            java.util.List<Double> vals = m.get(maHS);
            double sum = 0;
            for (Double v : vals)
                sum += v;
            double avg = vals.size() > 0 ? round1(sum / vals.size()) : 0.0;
            Object[] row = new Object[4];
            row[0] = maHS;
            row[1] = names.get(maHS);
            row[2] = avg;
            row[3] = xepLoai(avg);
            result.add(row);
        }
        return result;
    }

    private List<Object[]> queryDiemChiTietHocKy(int maLop, int hocKy, int maNK) {
        List<Object[]> result = new ArrayList<>();
        // Respect student view: if logged-in user is student, fetch only their rows
        com.sgu.qlhs.dto.NguoiDungDTO nd = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                nd = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
        }
        List<DiemDTO> rows;
        if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
            rows = diemBUS.getDiemByMaHS(nd.getId(), hocKy, maNK, nd);
        } else {
            rows = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);
        }
        for (DiemDTO d : rows) {
            Object[] row = new Object[8];
            row[0] = d.getMaHS();
            row[1] = d.getHoTen();
            row[2] = d.getTenMon();
            row[3] = d.getDiemMieng();
            row[4] = d.getDiem15p();
            row[5] = d.getDiemGiuaKy();
            row[6] = d.getDiemCuoiKy();
            row[7] = d.getDiemTB();
            result.add(row);
        }
        return result;
    }

    private List<Object[]> queryDiemTrungBinhCaNam(int maNK) {
        List<Object[]> result = new ArrayList<>();

        // get year weights
        double w1 = 0.4, w2 = 0.6; // fallback
        String sqlWs = "SELECT wHK1, wHK2 FROM TrongSoNam WHERE MaNK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement psw = conn.prepareStatement(sqlWs)) {
            psw.setInt(1, maNK);
            try (ResultSet rsw = psw.executeQuery()) {
                if (rsw.next()) {
                    w1 = rsw.getDouble("wHK1");
                    w2 = rsw.getDouble("wHK2");
                }
            }
        } catch (SQLException e) {
            System.err.println("Không lấy được trọng số năm, dùng mặc định 0.4/0.6: " + e.getMessage());
        }

        // Check logged-in user: if student, fetch only that student's rows
        com.sgu.qlhs.dto.NguoiDungDTO nd = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                nd = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
        }

        List<DiemDTO> hk1rows;
        List<DiemDTO> hk2rows;
        if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
            hk1rows = diemBUS.getDiemByMaHS(nd.getId(), 1, maNK, nd);
            hk2rows = diemBUS.getDiemByMaHS(nd.getId(), 2, maNK, nd);
        } else {
            // Fetch HK1 and HK2 averages per student using DiemBUS
            hk1rows = diemBUS.getDiemFiltered(null, null, 1, maNK, null, null);
            hk2rows = diemBUS.getDiemFiltered(null, null, 2, maNK, null, null);
        }

        java.util.Map<Integer, java.util.List<Double>> map1 = new java.util.HashMap<>();
        java.util.Map<Integer, java.util.List<Double>> map2 = new java.util.HashMap<>();
        java.util.Map<Integer, String> names = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> lopOf = new java.util.HashMap<>();

        for (DiemDTO d : hk1rows) {
            names.put(d.getMaHS(), d.getHoTen());
            lopOf.put(d.getMaHS(), d.getMaLop());
            map1.computeIfAbsent(d.getMaHS(), k -> new java.util.ArrayList<>()).add(d.getDiemTB());
        }
        for (DiemDTO d : hk2rows) {
            names.put(d.getMaHS(), d.getHoTen());
            lopOf.put(d.getMaHS(), d.getMaLop());
            map2.computeIfAbsent(d.getMaHS(), k -> new java.util.ArrayList<>()).add(d.getDiemTB());
        }

        // union keys
        java.util.Set<Integer> keys = new java.util.HashSet<>();
        keys.addAll(map1.keySet());
        keys.addAll(map2.keySet());

        java.util.List<Integer> sorted = new java.util.ArrayList<>(keys);
        java.util.Collections.sort(sorted,
                (a, b) -> names.getOrDefault(a, "").compareToIgnoreCase(names.getOrDefault(b, "")));

        for (Integer maHS : sorted) {
            java.util.List<Double> v1 = map1.getOrDefault(maHS, java.util.Collections.emptyList());
            java.util.List<Double> v2 = map2.getOrDefault(maHS, java.util.Collections.emptyList());
            double avg1 = v1.size() > 0 ? round1(v1.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                    : 0.0;
            double avg2 = v2.size() > 0 ? round1(v2.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                    : 0.0;
            double denom = (w1 + w2);
            double diemCaNam = 0.0;
            if (v1.isEmpty() && v2.isEmpty()) {
                diemCaNam = 0.0;
            } else {
                diemCaNam = Math.round(((avg1 * w1) + (avg2 * w2)) / denom * 10) / 10.0;
            }
            String tenLop = "";
            Integer ml = lopOf.get(maHS);
            if (ml != null) {
                for (LopDTO l : lopList) {
                    if (l.getMaLop() == ml) {
                        tenLop = l.getTenLop();
                        break;
                    }
                }
            } else {
                // fallback: query student
                HocSinhDTO h = hocSinhBUS.getHocSinhByMaHS(maHS);
                if (h != null)
                    tenLop = h.getTenLop();
            }
            Object[] row = new Object[7];
            row[0] = maHS;
            row[1] = names.getOrDefault(maHS, "");
            row[2] = tenLop;
            row[3] = avg1;
            row[4] = avg2;
            row[5] = diemCaNam;
            row[6] = xepLoai(diemCaNam);
            result.add(row);
        }

        return result;
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ===== THANH CÔNG CỤ =====
        var toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.add(new JLabel("Xem theo:"));
        toolbar.add(cboLoaiXem);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Lớp:"));
        toolbar.add(cboLop);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Năm học:"));
        toolbar.add(cboNamHoc);

        var btnLoad = new JButton("Tải dữ liệu");
        var btnExport = new JButton("Xuất CSV");
        var btnClose = new JButton("Đóng");

        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnLoad);
        toolbar.add(btnExport);
        toolbar.add(btnClose);

        root.add(toolbar, BorderLayout.NORTH);

        // ===== BẢNG DỮ LIỆU =====
        model = new DefaultTableModel(new Object[] { "Mã HS", "Họ tên", "Lớp", "HK1", "HK2", "Cả năm", "Xếp loại" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        var scrollPane = new JScrollPane(table);
        root.add(scrollPane, BorderLayout.CENTER);

        // ===== THÔNG TIN THỐNG KÊ =====
        var statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê"));
        var lblStats = new JLabel("Tổng: 0 học sinh");
        statsPanel.add(lblStats);
        root.add(statsPanel, BorderLayout.SOUTH);

        // ===== SỰ KIỆN =====
        cboLoaiXem.addActionListener(e -> updateTableColumns());

        btnLoad.addActionListener(e -> loadData(lblStats));

        btnExport.addActionListener(e -> exportToCSV());

        btnClose.addActionListener(e -> dispose());

        // Cập nhật cột ban đầu
        updateTableColumns();
    }

    /**
     * Cập nhật cột của bảng theo loại xem
     */
    private void updateTableColumns() {
        String loaiXem = (String) cboLoaiXem.getSelectedItem();

        if (loaiXem.equals("Cả năm")) {
            model.setColumnIdentifiers(
                    new Object[] { "Mã HS", "Họ tên", "Lớp", "TB HK1", "TB HK2", "TB Cả năm", "Xếp loại" });
        } else {
            model.setColumnIdentifiers(new Object[] { "Mã HS", "Họ tên", "Lớp", "TB Học kỳ", "Xếp loại" });
        }

        model.setRowCount(0);
    }

    /**
     * Load danh sách lớp vào ComboBox
     */
    private void loadLopData() {
        cboLop.removeAllItems();
        cboLop.addItem("-- Tất cả --");
        lopList = lopBUS.getAllLop();
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                com.sgu.qlhs.dto.NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null) {
                    if ("giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                        int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
                        com.sgu.qlhs.bus.PhanCongDayBUS pcb = new com.sgu.qlhs.bus.PhanCongDayBUS();
                        java.util.List<Integer> assigned = pcb.getDistinctMaLopByGiaoVien(nd.getId(), maNK, null);
                        for (LopDTO lop : lopList) {
                            if (!assigned.contains(lop.getMaLop()))
                                continue;
                            String tenLop = lop.getTenLop();
                            int khoi = lop.getKhoi();
                            cboLop.addItem(tenLop + " (Khối " + khoi + ")");
                        }
                        return;
                    }
                    if ("hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                        // show only the student's class
                        int maHS = nd.getId();
                        HocSinhDTO h = hocSinhBUS.getHocSinhByMaHS(maHS);
                        String tenLop = h != null && h.getTenLop() != null ? h.getTenLop() : "-- Tất cả --";
                        cboLop.removeAllItems();
                        cboLop.addItem(tenLop);
                        cboLop.setSelectedIndex(0);
                        cboLop.setEnabled(false);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            // fallback to full list
        }

        for (LopDTO lop : lopList) {
            String tenLop = lop.getTenLop();
            int khoi = lop.getKhoi();
            cboLop.addItem(tenLop + " (Khối " + khoi + ")");
        }
    }

    /**
     * Load dữ liệu điểm
     */
    private void loadData(JLabel lblStats) {
        model.setRowCount(0);

        String loaiXem = (String) cboLoaiXem.getSelectedItem();
        int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
        // if user selected a specific Năm học from combo, use that MaNK mapping
        int selNk = cboNamHoc.getSelectedIndex();
        if (selNk >= 0 && selNk < nienKhoaIds.size()) {
            maNK = nienKhoaIds.get(selNk);
        }

        try {
            if (loaiXem.equals("Cả năm")) {
                int sel = cboLop.getSelectedIndex();
                if (sel > 0) {
                    // lọc theo lớp
                    String tenLop = lopList.get(sel - 1).getTenLop();
                    loadDiemCaNamFiltered(maNK, tenLop);
                } else {
                    loadDiemCaNam(maNK);
                }
            } else {
                int hocKy = loaiXem.equals("Học kỳ 1") ? 1 : 2;
                int sel = cboLop.getSelectedIndex();
                if (sel > 0) {
                    int maLop = lopList.get(sel - 1).getMaLop();
                    loadDiemHocKyForClass(maLop, hocKy, maNK);
                } else {
                    loadDiemHocKy(hocKy, maNK);
                }
            }

            lblStats.setText("Tổng: " + model.getRowCount() + " học sinh");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Load điểm theo học kỳ
     */
    private void loadDiemHocKy(int hocKy, int maNK) {
        var data = queryDiemTrungBinhHocKy(hocKy, maNK);

        for (Object[] row : data) {
            // Lấy thêm tên lớp (tạm thời để trống, có thể JOIN thêm)
            model.addRow(new Object[] {
                    row[0], // MaHS
                    row[1], // HoTen
                    "", // TenLop (cần JOIN thêm nếu cần)
                    row[2], // DiemTB
                    row[3] // XepLoai
            });
        }
    }

    /**
     * Load điểm học kỳ cho 1 lớp cụ thể: tính TBHK cho từng học sinh trong lớp
     */
    private void loadDiemHocKyForClass(int maLop, int hocKy, int maNK) {
        model.setRowCount(0);
        var data = queryDiemChiTietHocKy(maLop, hocKy, maNK);

        // Aggregate per student: map MaHS -> {hoTen, sum, count}
        Map<Integer, String> name = new HashMap<>();
        Map<Integer, Double> sum = new HashMap<>();
        Map<Integer, Integer> cnt = new HashMap<>();

        for (Object[] r : data) {
            int maHS = (int) r[0];
            String hoTen = (String) r[1];
            double diemTB = ((Number) r[7]).doubleValue();

            name.put(maHS, hoTen);
            sum.put(maHS, sum.getOrDefault(maHS, 0.0) + diemTB);
            cnt.put(maHS, cnt.getOrDefault(maHS, 0) + 1);
        }

        for (Integer maHS : sum.keySet()) {
            double avg = Math.round((sum.get(maHS) / cnt.get(maHS)) * 10) / 10.0;
            String hoTen = name.get(maHS);
            String xep = xepLoai(avg);
            // TenLop: get from lopList by maLop
            String tenLop = "";
            for (LopDTO l : lopList) {
                if (l.getMaLop() == maLop) {
                    tenLop = l.getTenLop();
                    break;
                }
            }
            model.addRow(new Object[] { maHS, hoTen, tenLop, avg, xep });
        }
    }

    /**
     * Load điểm cả năm nhưng chỉ hiển thị học sinh trong lớp tenLop
     */
    private void loadDiemCaNamFiltered(int maNK, String tenLop) {
        model.setRowCount(0);
        var data = queryDiemTrungBinhCaNam(maNK);
        for (Object[] row : data) {
            String lop = (String) row[2];
            if (lop != null && lop.equals(tenLop)) {
                model.addRow(new Object[] {
                        row[0], // MaHS
                        row[1], // HoTen
                        row[2], // TenLop
                        row[3], // DiemHK1
                        row[4], // DiemHK2
                        row[5], // DiemCaNam
                        row[6] // XepLoai
                });
            }
        }
    }

    private String getTenLopByMaHS(int maHS) {
        HocSinhDTO h = hocSinhBUS.getHocSinhByMaHS(maHS);
        if (h != null)
            return h.getTenLop();
        return "";
    }

    /**
     * Load điểm cả năm
     */
    private void loadDiemCaNam(int maNK) {
        var data = queryDiemTrungBinhCaNam(maNK);

        for (Object[] row : data) {
            model.addRow(new Object[] {
                    row[0], // MaHS
                    row[1], // HoTen
                    row[2], // TenLop
                    row[3], // DiemHK1
                    row[4], // DiemHK2
                    row[5], // DiemCaNam
                    row[6] // XepLoai
            });
        }
    }

    // local xếp loại (same as DAO)
    private static String xepLoai(double diemTB) {
        if (diemTB >= 9.0)
            return "Xuất sắc";
        if (diemTB >= 8.0)
            return "Giỏi";
        if (diemTB >= 6.5)
            return "Khá";
        if (diemTB >= 5.0)
            return "Trung bình";
        if (diemTB >= 3.5)
            return "Yếu";
        return "Kém";
    }

    private static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }

    /**
     * Xuất dữ liệu ra file CSV
     */
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file CSV");
        fileChooser.setSelectedFile(new java.io.File("diem_hoc_ky.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Ghi header
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.append(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");

                // Ghi dữ liệu
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        writer.append(value != null ? value.toString() : "");
                        if (j < model.getColumnCount() - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Xuất file thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất file: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
