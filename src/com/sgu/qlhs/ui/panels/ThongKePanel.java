/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.sgu.qlhs.ui.MainDashboard.*;

// === IMPORT THÊM ===
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.MainDashboard;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
// ===================

public class ThongKePanel extends JPanel {

    private boolean isStudentView = false;
    private NguoiDungDTO currentUser;
    private int currentMaHS = -1;
    private HocSinhDTO currentHocSinh;

    private JComboBox<String> cboThongKe;
    private JComboBox<String> cboHocKy; // Thêm combobox chọn học kỳ
    private JPanel chartContainer; // Panel chính để chứa CardLayout
    private CardLayout chartCards;

    private DiemBUS diemBUS;
    private HocSinhBUS hocSinhBUS;
    private LopBUS lopBUS;

    // Card keys
    private final String CARD_RANKING = "RANKING";
    private final String CARD_AVERAGES = "AVERAGES";
    private final String CARD_ADMIN = "ADMIN_DEFAULT";
    private final String CARD_EMPTY = "EMPTY";
    private final String CARD_ERROR = "ERROR";

    public ThongKePanel() {
        // Sẽ được khởi tạo trong listener, nhưng ta tạo sẵn layout
        super(new BorderLayout());
        setOpaque(false);

        // Thêm listener để phát hiện khi panel được thêm vào (và có thể lấy user)
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                // Chỉ chạy khi panel có parent (được thêm vào MainDashboard)
                if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 &&
                        SwingUtilities.getWindowAncestor(ThongKePanel.this) instanceof MainDashboard) {

                    MainDashboard md = (MainDashboard) SwingUtilities.getWindowAncestor(ThongKePanel.this);
                    if (md == null)
                        return;

                    currentUser = md.getNguoiDung();

                    // Dựa vào vai trò để xây dựng giao diện tương ứng
                    if (currentUser != null && "hoc_sinh".equalsIgnoreCase(currentUser.getVaiTro())) {
                        isStudentView = true;
                        currentMaHS = currentUser.getId();
                        initStudentView();
                    } else {
                        isStudentView = false;
                        initAdminGiaoVienView();
                    }
                    // Xóa listener sau khi chạy lần đầu
                    removeHierarchyListener(this);
                }
            }
        });
    }

    /**
     * Giao diện cho Admin/Giáo viên (như cũ)
     */
    private void initAdminGiaoVienView() {
        this.removeAll(); // Xóa mọi thứ (nếu có)

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Thống kê (Chung)");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        // TODO: Dữ liệu giới tính này nên được tải động từ HocSinhBUS
        String[] cats = { "Nam", "Nữ" };
        int[] values = { 58, 42 };
        var chart = new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, values);
        outer.add(chart, BorderLayout.CENTER);

        this.add(outer, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    /**
     * Giao diện mới cho Học sinh
     */
    private void initStudentView() {
        this.removeAll(); // Xóa mọi thứ (nếu có)

        // Khởi tạo BUS
        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();
        currentHocSinh = hocSinhBUS.getHocSinhByMaHS(currentMaHS);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        String tenHS = (currentHocSinh != null) ? currentHocSinh.getHoTen() : currentUser.getHoTen();
        var lbl = new JLabel("Thống kê: " + tenHS);
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        // outer.add(lbl, BorderLayout.NORTH); // Không thêm ở đây vội, thêm vào topPanel

        // Panel bộ lọc
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 12, 5, 12));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Loại thống kê:"));
        cboThongKe = new JComboBox<>(new String[] {
                "Thứ hạng ĐTB theo môn", // Giống trong hình
                "Điểm TB các môn" // Thêm theo yêu cầu
        });
        filterPanel.add(cboThongKe);

        filterPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2" });
        filterPanel.add(cboHocKy);

        topPanel.add(lbl, BorderLayout.NORTH); // Tiêu đề
        topPanel.add(filterPanel, BorderLayout.CENTER); // Bộ lọc

        outer.add(topPanel, BorderLayout.NORTH); // Đặt bộ lọc bên trong RoundedPanel, ở trên

        // Panel chứa các biểu đồ
        chartCards = new CardLayout();
        chartContainer = new JPanel(chartCards);
        chartContainer.setOpaque(false);
        chartContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Thêm các panel trống
        chartContainer.add(new JLabel("Đang tải...", SwingConstants.CENTER), CARD_EMPTY);
        chartContainer.add(new JLabel("Không tìm thấy dữ liệu.", SwingConstants.CENTER), CARD_ERROR);

        outer.add(chartContainer, BorderLayout.CENTER);
        this.add(outer, BorderLayout.CENTER);

        // Gắn sự kiện
        cboThongKe.addActionListener(e -> loadStudentChart());
        cboHocKy.addActionListener(e -> loadStudentChart());

        // Tải biểu đồ lần đầu
        loadStudentChart();

        this.revalidate();
        this.repaint();
    }

    /**
     * Tải biểu đồ tương ứng cho học sinh
     */
    private void loadStudentChart() {
        if (!isStudentView || diemBUS == null || currentHocSinh == null) {
            chartCards.show(chartContainer, CARD_EMPTY);
            return;
        }

        String loaiTK = (String) cboThongKe.getSelectedItem();
        int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
        int maNK = NienKhoaBUS.current();

        // Lấy MaLop từ TenLop của HocSinhDTO
        int maLop = -1;
        String tenLop = currentHocSinh.getTenLop();
        if (tenLop != null) {
            maLop = lopBUS.getAllLop().stream()
                    .filter(lop -> tenLop.equals(lop.getTenLop()))
                    .map(com.sgu.qlhs.dto.LopDTO::getMaLop)
                    .findFirst()
                    .orElse(-1);
        }

        if (maLop == -1) {
            chartContainer.add(new JLabel("Không tìm thấy thông tin lớp của học sinh."), "LopError");
            chartCards.show(chartContainer, "LopError");
            return;
        }

        String cardKey = loaiTK + "_" + hocKy;

        if ("Thứ hạng ĐTB theo môn".equals(loaiTK)) {
            JComponent chart = createRankingChart(currentMaHS, maLop, hocKy, maNK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        } else if ("Điểm TB các môn".equals(loaiTK)) {
            JComponent chart = createAverageScoreChart(currentMaHS, hocKy, maNK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        }
    }

    /**
     * Biểu đồ 1: Điểm TB cá nhân của học sinh
     */
    private JComponent createAverageScoreChart(int maHS, int hocKy, int maNK) {
        List<DiemDTO> scores = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, currentUser);

        if (scores == null || scores.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm cho học kỳ này.", SwingConstants.CENTER);
        }

        // === SỬA LỖI: Lọc bỏ môn Đánh Giá ra khỏi biểu đồ điểm ===
        List<DiemDTO> scoresTinhDiem = scores.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.toList());
        // ========================================================

        if (scoresTinhDiem.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm (tính số) cho học kỳ này.", SwingConstants.CENTER);
        }

        String[] cats = scoresTinhDiem.stream().map(DiemDTO::getTenMon).toArray(String[]::new);
        // BarChartCanvas dùng int[], nên ta nhân 10
        int[] values = scoresTinhDiem.stream().mapToInt(d -> (int) (d.getDiemTB() * 10)).toArray();

        String title = "Điểm TB các môn (x10) - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }

    /**
     * Biểu đồ 2: Thứ hạng ĐTB theo môn (như trong ảnh)
     */
    private JComponent createRankingChart(int maHS, int maLop, int hocKy, int maNK) {
        // 1. Lấy điểm TB của tất cả học sinh trong lớp
        List<DiemDTO> allScoresInClass = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);

        if (allScoresInClass == null || allScoresInClass.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm của lớp cho học kỳ này.", SwingConstants.CENTER);
        }

        // === SỬA LỖI: Chỉ group các môn TinhDiem ===
        // 2. Group điểm theo môn (CHỈ LỌC CÁC MÔN TÍNH ĐIỂM)
        Map<String, List<DiemDTO>> scoresBySubject = allScoresInClass.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.groupingBy(DiemDTO::getTenMon));
        // ===========================================

        Map<String, Integer> ranks = new HashMap<>();

        // 3. Tính rank cho từng môn
        for (Map.Entry<String, List<DiemDTO>> entry : scoresBySubject.entrySet()) {
            String tenMon = entry.getKey();
            List<DiemDTO> subjectScores = entry.getValue();

            // Sắp xếp điểm môn này giảm dần
            subjectScores.sort(Comparator.comparing(DiemDTO::getDiemTB).reversed());

            // Tìm rank của học sinh hiện tại
            int rank = -1;
            for (int i = 0; i < subjectScores.size(); i++) {
                if (subjectScores.get(i).getMaHS() == maHS) {
                    rank = i + 1; // Rank bắt đầu từ 1
                    break;
                }
            }
            ranks.put(tenMon, rank);
        }

        // 4. Chuẩn bị dữ liệu cho BarChartCanvas
        // Lấy danh sách môn của chính học sinh đó để đảm bảo đúng thứ tự
        List<DiemDTO> myScores = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, currentUser);
        if (myScores.isEmpty()) {
            return new JLabel("Không thể tải thứ hạng (HS chưa có điểm).", SwingConstants.CENTER);
        }

        // === SỬA LỖI: Lọc bỏ môn Đánh Giá ===
        List<DiemDTO> myScoresTinhDiem = myScores.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.toList());
        // =====================================

        if (myScoresTinhDiem.isEmpty()) {
            return new JLabel("Không có môn tính điểm để xếp hạng.", SwingConstants.CENTER);
        }
        
        String[] cats = myScoresTinhDiem.stream().map(DiemDTO::getTenMon).toArray(String[]::new);
        int[] values = new int[cats.length];
        for (int i = 0; i < cats.length; i++) {
            values[i] = ranks.getOrDefault(cats[i], 0); // 0 nếu không có rank
        }

        String title = "Thứ hạng ĐTB theo môn - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }
}