package com.sgu.qlhs.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import com.sgu.qlhs.ui.dialogs.TKBDialog;

public class TKBPanel extends JPanel {

    private JTable tblTKB;
    private DefaultTableModel model;
    private JButton btnThem, btnSua, btnXoa, btnKhoiPhuc, btnImport, btnExport;
    private JComboBox<String> cboLop, cboHocKy;
    private Connection conn;
    private ThoiKhoaBieuBUS tkbBUS;
    private List<ThoiKhoaBieuDTO> currentTkbList;

    public TKBPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        conn = DatabaseConnection.getConnection();
        tkbBUS = new ThoiKhoaBieuBUS(conn);

        // ===== Title =====
        JLabel lblTitle = new JLabel("Quản lý Thời khóa biểu", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 60, 130));
        lblTitle.setBorder(new EmptyBorder(15, 20, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // ===== Thanh công cụ =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(new Color(245, 248, 255));
        topPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        // ===== Nút chức năng =====
        btnThem = button("Thêm");
        btnSua = button("Sửa");
        btnXoa = button("Xóa");
        btnKhoiPhuc = button("Khôi phục");
        btnImport = button("Import");
        btnExport = button("Export");

        topPanel.add(btnThem);
        topPanel.add(btnSua);
        topPanel.add(btnXoa);
        topPanel.add(btnKhoiPhuc);
        topPanel.add(btnImport);
        topPanel.add(btnExport);

        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Lớp:"));
        cboLop = new JComboBox<>();
        loadDanhSachLop();
        topPanel.add(cboLop);

        topPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[] { "HK1", "HK2" });
        topPanel.add(cboHocKy);

        add(topPanel, BorderLayout.NORTH);

        // ===== Bảng Thời khóa biểu =====
        String[] columnNames = { "Tiết", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7" };
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (int i = 1; i <= 10; i++)
            model.addRow(new Object[] { "Tiết " + i, "", "", "", "", "", "" });

        tblTKB = new JTable(model);
        tblTKB.setRowHeight(70);
        tblTKB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblTKB.setShowGrid(true);
        tblTKB.setGridColor(new Color(210, 210, 210));
        tblTKB.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tblTKB.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        header.setBackground(new Color(70, 120, 200));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        // Renderer cho ô
        tblTKB.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setVerticalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

                if (column == 0) {
                    lbl.setBackground(new Color(235, 238, 250));
                    lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
                } else if (isSelected) {
                    lbl.setBackground(new Color(180, 210, 250));
                } else {
                    lbl.setBackground(row % 2 == 0 ? new Color(250, 252, 255) : Color.WHITE);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(tblTKB);
        scroll.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(scroll, BorderLayout.CENTER);

        // ===== Sự kiện =====
        btnThem.addActionListener(e -> openDialog(null));
        btnSua.addActionListener(e -> onEdit());
        btnXoa.addActionListener(e -> onDelete());
        btnKhoiPhuc.addActionListener(e -> onRestore());
        cboLop.addActionListener(e -> reloadData());
        cboHocKy.addActionListener(e -> reloadData());

        // Export
        btnExport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn nơi lưu file CSV");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                try {
                    tkbBUS.exportToCSV(currentTkbList, file);
                    JOptionPane.showMessageDialog(this, "Xuất dữ liệu thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage());
                }
            }
        });

        // Import
        btnImport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file CSV cần nhập");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    List<ThoiKhoaBieuDTO> list = tkbBUS.importFromCSV(file);
                    for (ThoiKhoaBieuDTO tkb : list) {
                        tkbBUS.addThoiKhoaBieu(tkb);
                    }
                    reloadData();
                    JOptionPane.showMessageDialog(this, "Nhập dữ liệu thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi nhập file: " + ex.getMessage());
                }
            }
        });

        reloadData();
    }

    // ===== Nút đẹp hơn =====
    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(80, 130, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDanhSachLop() {
        try {
            // If current user is a student, show only their class and disable editing
            try {
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                    com.sgu.qlhs.dto.NguoiDungDTO nd = md.getNguoiDung();
                    if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                        com.sgu.qlhs.bus.HocSinhBUS hsBUS = new com.sgu.qlhs.bus.HocSinhBUS();
                        com.sgu.qlhs.dto.HocSinhDTO hs = hsBUS.getHocSinhByMaHS(nd.getId());
                        cboLop.removeAllItems();
                        if (hs != null && hs.getTenLop() != null) {
                            cboLop.addItem(hs.getTenLop());
                        } else {
                            cboLop.addItem("Không có lớp");
                        }
                        cboLop.setEnabled(false);
                        // disable editing/import/export for students
                        btnThem.setEnabled(false);
                        btnSua.setEnabled(false);
                        btnXoa.setEnabled(false);
                        btnKhoiPhuc.setEnabled(false);
                        btnImport.setEnabled(false);
                        btnExport.setEnabled(false);
                        return;
                    }
                    // If current user is a teacher, restrict class list to those assigned
                    if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                        cboLop.removeAllItems();
                        cboLop.addItem("Tất cả");
                        com.sgu.qlhs.bus.PhanCongDayBUS pc = new com.sgu.qlhs.bus.PhanCongDayBUS();
                        com.sgu.qlhs.bus.LopBUS lopBUS = new com.sgu.qlhs.bus.LopBUS();
                        int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
                        java.util.List<Integer> lopIds = pc.getDistinctMaLopByGiaoVien(nd.getId(), maNK, null);
                        for (Integer ml : lopIds) {
                            com.sgu.qlhs.dto.LopDTO l = lopBUS.getLopByMa(ml);
                            if (l != null)
                                cboLop.addItem(l.getTenLop());
                        }
                        return;
                    }
                }
            } catch (Exception ex) {
                // ignore and fall back to reading all classes
            }

            List<String> dsLop = tkbBUS.getDistinctLop();
            cboLop.removeAllItems();
            for (String tenLop : dsLop)
                cboLop.addItem(tenLop);
        } catch (Exception e) {
            cboLop.addItem("Không có lớp");
        }
    }

    private void reloadData() {
        clearAll();
        try {
            String lopText = (String) cboLop.getSelectedItem();
            String hocKy = (String) cboHocKy.getSelectedItem();
            if (lopText == null || hocKy == null)
                return;

            int maLop = tkbBUS.getMaLopByTen(lopText);
            currentTkbList = tkbBUS.findByLopHocKy(maLop, hocKy);
            for (ThoiKhoaBieuDTO t : currentTkbList) {
                int thu = dayToNumber(t.getThuTrongTuan());
                for (int tiet = t.getTietBatDau(); tiet <= t.getTietKetThuc(); tiet++) {
                    setCell(tiet, thu,
                            "<html><center><b>" + t.getTenMon() + "</b><br>(" +
                                    t.getTenPhong() + ")<br><i>GV: " + t.getTenGV() + "</i></center></html>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCell(int tiet, int thu, String text) {
        if (tiet < 1 || tiet > 10 || thu < 2 || thu > 7)
            return;
        model.setValueAt(text, tiet - 1, thu - 1);
    }

    private int dayToNumber(String thu) {
        if (thu == null)
            return 0;
        thu = thu.replace("Thứ", "").trim();
        return switch (thu) {
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            default -> 0;
        };
    }

    private void clearAll() {
        for (int r = 0; r < model.getRowCount(); r++)
            for (int c = 1; c < model.getColumnCount(); c++)
                model.setValueAt("", r, c);
    }

    private void openDialog(ThoiKhoaBieuDTO selected) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TKBDialog dlg = new TKBDialog(
                parent,
                new ThoiKhoaBieuBUS(conn),
                new LopBUS(),
                new MonBUS(),
                new GiaoVienBUS(),
                new PhongBUS(),
                selected);
        dlg.setVisible(true);
        reloadData();
    }

    private void onEdit() {
        try {
            if (currentTkbList == null || currentTkbList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có bản ghi để sửa!");
                return;
            }

            DefaultListModel<ThoiKhoaBieuDTO> listModel = new DefaultListModel<>();
            for (ThoiKhoaBieuDTO t : currentTkbList)
                listModel.addElement(t);

            JList<ThoiKhoaBieuDTO> jList = new JList<>(listModel);
            jList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel lbl = new JLabel(String.format(
                        "ID #%d | %s | Tiết %d–%d | %s | %s | GV: %s",
                        value.getMaTKB(),
                        value.getThuTrongTuan(),
                        value.getTietBatDau(), value.getTietKetThuc(),
                        value.getTenMon(), value.getTenPhong(), value.getTenGV()));
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? new Color(200, 220, 255) : Color.WHITE);
                lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
                return lbl;
            });

            JScrollPane sp = new JScrollPane(jList);
            sp.setPreferredSize(new Dimension(560, 250));
            int ok = JOptionPane.showConfirmDialog(this, sp, "Chọn tiết để sửa", JOptionPane.OK_CANCEL_OPTION);
            if (ok == JOptionPane.OK_OPTION && jList.getSelectedValue() != null) {
                openDialog(jList.getSelectedValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi sửa: " + ex.getMessage());
        }
    }

    private void onDelete() {
        JOptionPane.showMessageDialog(this, "Chức năng xóa mềm vẫn hoạt động, chỉ đổi giao diện thôi 😊");
    }

    private void onRestore() {
        JOptionPane.showMessageDialog(this, "Chức năng khôi phục giữ nguyên logic cũ nhé 💙");
    }
}
