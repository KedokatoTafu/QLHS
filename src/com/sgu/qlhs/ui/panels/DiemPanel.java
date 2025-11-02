/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.HanhKiemBUS;
import com.sgu.qlhs.bus.ThoiKhoaBieuBUS;
import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.MonHocDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.text.Collator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.util.List;
import java.util.Locale;
// import java.util.Vector; (unused)
import static com.sgu.qlhs.ui.MainDashboard.*;

public class DiemPanel extends JPanel {
    private final DiemBUS diemBUS = new DiemBUS();
    private final LopBUS lopBUS = new LopBUS();
    private final MonBUS monBUS = new MonBUS();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "-- Tất cả --", "HK1", "HK2" });
    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final HanhKiemBUS hanhKiemBUS = new HanhKiemBUS();
    private final ThoiKhoaBieuBUS tkbBUS = new ThoiKhoaBieuBUS(DatabaseConnection.getConnection());
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private List<LopDTO> lopList;
    private List<MonHocDTO> monList;
    // student view flags
    private boolean isStudentView = false;
    private int currentStudentMaHS = -1;
    // keep last fetched rows so popup actions can map table rows to DTOs
    private java.util.List<com.sgu.qlhs.dto.DiemDTO> currentRows = new java.util.ArrayList<>();
    // pagination
    private int pageSize = 100;
    private int currentPage = 0; // zero-based
    private final JButton btnPrev = new JButton("← Trước");
    private final JButton btnNext = new JButton("Tiếp →");
    private final JLabel lblPageInfo = new JLabel("Trang 1");

    public DiemPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        var lbl = new JLabel("Điểm");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        // Top filter bar
        var filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterBar.add(new JLabel("Lớp:"));
        filterBar.add(cboLop);
        filterBar.add(new JLabel("Môn:"));
        filterBar.add(cboMon);
        filterBar.add(new JLabel("Học kỳ:"));
        filterBar.add(cboHK);
        filterBar.add(new JLabel("Tìm:"));
        filterBar.add(txtSearch);
        var btnFilter = new JButton("Lọc");
        var btnRefresh = new JButton("Làm mới");
        filterBar.add(btnFilter);
        filterBar.add(btnRefresh);
        outer.add(filterBar, BorderLayout.PAGE_START);

        // Table model and table
        model = new DefaultTableModel(
                new Object[] { "MaDiem", "Mã HS", "Họ tên", "Lớp", "Môn", "HK", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ",
                        "Hạnh kiểm" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // ensure numeric columns sort numerically
                switch (columnIndex) {
                    case 0: // MaDiem (hidden PK)
                    case 1: // Mã HS
                        return Integer.class;
                    case 5: // HK
                        return Integer.class;
                    case 6: // Miệng
                    case 7: // 15p
                    case 8: // Giữa kỳ
                    case 9: // Cuối kỳ
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // sort Họ tên by the last token (last name) using Vietnamese collation
        Collator collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        sorter.setComparator(2, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String k1 = lastToken(s1).toLowerCase(Locale.forLanguageTag("vi-VN"));
            String k2 = lastToken(s2).toLowerCase(Locale.forLanguageTag("vi-VN"));
            int c = collator.compare(k1, k2);
            if (c != 0)
                return c;
            return collator.compare(s1, s2);
        });

        // sort Lớp like: 6A1,6A2,6B3,7A1,7B2,7C3 by grade number, then letter(s), then
        // trailing number
        Pattern classPattern = Pattern.compile("^(\\d+)([^\\d]*?)(\\d*)$");
        sorter.setComparator(3, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String t1 = s1.replaceAll("\\s+", "");
            String t2 = s2.replaceAll("\\s+", "");
            Matcher m1 = classPattern.matcher(t1);
            Matcher m2 = classPattern.matcher(t2);
            int grade1 = 0, grade2 = 0, idx1 = 0, idx2 = 0;
            String grp1 = "", grp2 = "";
            if (m1.matches()) {
                try {
                    grade1 = Integer.parseInt(m1.group(1));
                } catch (Exception ex) {
                    grade1 = 0;
                }
                grp1 = m1.group(2) == null ? "" : m1.group(2);
                try {
                    idx1 = (m1.group(3) == null || m1.group(3).isEmpty()) ? 0 : Integer.parseInt(m1.group(3));
                } catch (Exception ex) {
                    idx1 = 0;
                }
            }
            if (m2.matches()) {
                try {
                    grade2 = Integer.parseInt(m2.group(1));
                } catch (Exception ex) {
                    grade2 = 0;
                }
                grp2 = m2.group(2) == null ? "" : m2.group(2);
                try {
                    idx2 = (m2.group(3) == null || m2.group(3).isEmpty()) ? 0 : Integer.parseInt(m2.group(3));
                } catch (Exception ex) {
                    idx2 = 0;
                }
            }
            if (grade1 != grade2)
                return Integer.compare(grade1, grade2);
            int c = collator.compare(grp1.toLowerCase(Locale.forLanguageTag("vi-VN")),
                    grp2.toLowerCase(Locale.forLanguageTag("vi-VN")));
            if (c != 0)
                return c;
            return Integer.compare(idx1, idx2);
        });

        // hide the MaDiem column (kept in model for PK operations)
        outer.add(new JScrollPane(table), BorderLayout.CENTER);
        // After adding to viewport, remove the first column from view
        // (it remains accessible in the TableModel)
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (table.getColumnModel().getColumnCount() > 0) {
                try {
                    table.removeColumn(table.getColumnModel().getColumn(0));
                } catch (Exception ex) {
                    // ignore if already removed
                }
            }
        });

        // pagination controls (added to outer so they appear under the table)
        var pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        pager.add(btnPrev);
        pager.add(lblPageInfo);
        pager.add(btnNext);
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        outer.add(pager, BorderLayout.SOUTH);

        add(outer, BorderLayout.CENTER);

        // Load filter options
        loadLopOptions();
        loadMonOptions();

        // initial data
        loadData();

        // Actions
        btnFilter.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboLop.setSelectedIndex(0);
            cboMon.setSelectedIndex(0);
            cboHK.setSelectedIndex(0);
            currentPage = 0;
            loadData();
        });

        // quick-search on type
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }
        });

        // add popup menu for edit/delete
        var popup = new JPopupMenu();
        var miEdit = new JMenuItem("Sửa");
        var miDelete = new JMenuItem("Xóa");
        popup.add(miEdit);
        popup.add(miDelete);

        // Attach popup only for non-student users (students have read-only view)
        boolean attachPopup = true;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    isStudentView = true;
                    currentStudentMaHS = nd.getId(); // assume mapping NguoiDung.id -> HocSinh.MaHS
                    attachPopup = false;
                }
            }
        } catch (Exception ex) {
            // ignore and attach popup as fallback
        }

        if (attachPopup) {
            table.setComponentPopupMenu(popup);
            miDelete.addActionListener(e -> doDeleteSelectedRows());
            miEdit.addActionListener(e -> doEditSelectedRow());
        }

        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        btnNext.addActionListener(e -> {
            currentPage++;
            loadData();
        });
    }

    private void loadLopOptions() {
        cboLop.removeAllItems();
        cboLop.addItem("-- Tất cả --");
        // If the running user is a teacher, restrict classes to those they teach.
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                    // assume NguoiDung.id maps to MaGV
                    java.util.List<String> ds = tkbBUS.getDistinctLopByGiaoVien(nd.getId());
                    for (String tenLop : ds)
                        cboLop.addItem(tenLop);
                    return;
                }
                // if student, show only their class and disable selection
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    isStudentView = true;
                    currentStudentMaHS = nd.getId();
                    HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(currentStudentMaHS);
                    String tenLop = hs != null && hs.getTenLop() != null ? hs.getTenLop() : "-- Tất cả --";
                    cboLop.removeAllItems();
                    cboLop.addItem(tenLop);
                    cboLop.setSelectedIndex(0);
                    cboLop.setEnabled(false);
                    // hide/disable subject search for students
                    cboMon.setEnabled(false);
                    txtSearch.setEnabled(false);
                    return;
                }
            }
        } catch (Exception ex) {
            // if any issue, fallback to showing all classes
            System.err.println("Lỗi lấy lớp theo giáo viên: " + ex.getMessage());
        }

        // fallback: show all classes
        lopList = lopBUS.getAllLop();
        for (LopDTO l : lopList) {
            cboLop.addItem(l.getTenLop());
        }
    }

    private void loadMonOptions() {
        cboMon.removeAllItems();
        cboMon.addItem("-- Tất cả --");
        monList = monBUS.getAllMon();
        for (MonHocDTO m : monList) {
            cboMon.addItem(m.getTenMon());
        }
    }

    private void loadData() {
        model.setRowCount(0);
        int maNK = NienKhoaBUS.current();

        // Server-side filtered fetch
        Integer maLop = null;
        Integer maMon = null;
        Integer hocKy = null;
        int lopIdx = cboLop.getSelectedIndex();
        if (lopIdx > 0) {
            maLop = lopList.get(lopIdx - 1).getMaLop();
        }
        int monIdx = cboMon.getSelectedIndex();
        if (monIdx > 0) {
            maMon = monList.get(monIdx - 1).getMaMon();
        }
        int hkIdx = cboHK.getSelectedIndex();
        if (hkIdx > 0) {
            hocKy = hkIdx; // 1 or 2
        }

        // pagination: fetch pageSize+1 rows to detect next page
        List<com.sgu.qlhs.dto.DiemDTO> rows;
        boolean hasNext = false;
        if (isStudentView) {
            // student view: only fetch this student's records (may be both HK1/HK2)
            rows = new java.util.ArrayList<>();
            if (hocKy != null && hocKy > 0) {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, hocKy, maNK));
            } else {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 1, maNK));
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 2, maNK));
            }
            // no pagination for single student view
            hasNext = false;
        } else {
            int fetchSize = pageSize + 1;
            rows = diemBUS.getDiemFiltered(maLop, maMon, hocKy, maNK, fetchSize, currentPage * pageSize);
            hasNext = rows.size() > pageSize;
            if (hasNext) {
                rows = new java.util.ArrayList<>(rows.subList(0, pageSize));
            }
        }
        currentRows.clear();
        currentRows.addAll(rows);

        // subject filter
        String selMon = (String) cboMon.getSelectedItem();
        boolean filterMon = selMon != null && !selMon.equals("-- Tất cả --");

        int hkFilter = cboHK.getSelectedIndex(); // 0 == all, 1 == HK1, 2 == HK2

        // Prepare batch fetch: group rows by HocKy and collect MaHS per group
        java.util.Map<Integer, java.util.List<Integer>> hsByHocKy = new java.util.HashMap<>();
        for (var d : rows) {
            if (filterMon && !d.getTenMon().equals(selMon))
                continue;
            if (hkFilter > 0 && d.getHocKy() != hkFilter)
                continue;
            hsByHocKy.computeIfAbsent(d.getHocKy(), k -> new java.util.ArrayList<>()).add(d.getMaHS());
        }

        // Fetch Hạnh kiểm per HocKy in batch
        java.util.Map<Integer, java.util.Map<Integer, String>> hkMaps = new java.util.HashMap<>();
        for (var entry : hsByHocKy.entrySet()) {
            int hkVal = entry.getKey();
            java.util.List<Integer> maHsList = entry.getValue();
            try {
                java.util.Map<Integer, String> map = hanhKiemBUS.getHanhKiemForStudents(maHsList, maNK, hkVal);
                hkMaps.put(hkVal, map);
            } catch (Exception ex) {
                hkMaps.put(hkVal, new java.util.HashMap<>());
            }
        }

        for (var d : rows) {
            if (filterMon && !d.getTenMon().equals(selMon))
                continue;
            if (hkFilter > 0 && d.getHocKy() != hkFilter)
                continue;
            String hkStr = "";
            var mapForHK = hkMaps.get(d.getHocKy());
            if (mapForHK != null && mapForHK.containsKey(d.getMaHS()))
                hkStr = mapForHK.get(d.getMaHS());

            model.addRow(new Object[] { d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                    d.getHocKy(), d.getDiemMieng(), d.getDiem15p(), d.getDiemGiuaKy(), d.getDiemCuoiKy(), hkStr });
        }

        updatePageControls(hasNext);
        applyTextFilter();
    }

    private void updatePageControls(boolean hasNext) {
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(hasNext);
        lblPageInfo.setText("Trang " + (currentPage + 1));
    }

    private void applyTextFilter() {
        String txt = txtSearch.getText();
        if (txt == null || txt.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        String pattern = "(?i)" + java.util.regex.Pattern.quote(txt);
        // filter on Họ tên, Lớp and Tên môn (model indices: 2=HoTen,3=TenLop,4=TenMon)
        sorter.setRowFilter(RowFilter.regexFilter(pattern, 2, 3, 4));
    }

    private void doDeleteSelectedRows() {
        int[] sel = table.getSelectedRows();
        if (sel == null || sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hoặc nhiều hàng để xóa.", "Chú ý",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa điểm của các học sinh đã chọn?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        // collect model rows and sort descending to remove safely
        java.util.List<Integer> models = new java.util.ArrayList<>();
        for (int v : sel) {
            models.add(table.convertRowIndexToModel(v));
        }
        models.sort(java.util.Collections.reverseOrder());
        int maNK = NienKhoaBUS.current();
        for (int modelRow : models) {
            if (modelRow < 0 || modelRow >= currentRows.size())
                continue;
            var dto = currentRows.get(modelRow);
            try {
                // resolve current user from MainDashboard (if available) to enforce server-side
                // checks
                NguoiDungDTO nd = null;
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        nd = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                    // ignore
                }

                boolean ok = diemBUS.deleteDiem(dto.getMaHS(), dto.getMaMon(), dto.getHocKy(), maNK, nd);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa điểm này.", "Không có quyền",
                            JOptionPane.WARNING_MESSAGE);
                    continue;
                }
            } catch (Exception ex) {
                System.err.println("Lỗi khi xóa: " + ex.getMessage());
            }
            model.removeRow(modelRow);
            currentRows.remove(modelRow);
        }
    }

    private void doEditSelectedRow() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hàng để sửa.", "Chú ý",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(sel);
        if (modelRow < 0 || modelRow >= currentRows.size())
            return;
        var dto = currentRows.get(modelRow);
        // open DiemNhapDialog pre-selected for this class/subject/hk and student
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        var dlg = new com.sgu.qlhs.ui.dialogs.DiemNhapDialog(w, dto.getMaLop(), dto.getMaMon(), dto.getHocKy(),
                dto.getMaHS());
        dlg.setVisible(true);
        // after dialog closes we can refresh the table to reflect any changes
        loadData();
    }

    private static String lastToken(String s) {
        if (s == null || s.isBlank())
            return "";
        String[] parts = s.trim().split("\\s+");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    // (Removed unused helper parseDoubleOrZero) kept parsing logic centralized in
    // dialogs/DAOs where needed.
}
