package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.model.HocSinhTableModel;
import com.sgu.qlhs.ui.dialogs.HocSinhDetailDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.sgu.qlhs.ui.MainDashboard.*;

public class HocSinhPanel extends JPanel {
    private JTable tblHS;
    private TableRowSorter<TableModel> hsSorter;
    private JTextField txtHsSearch;
    private JComboBox<String> cboHsLop, cboHsGioiTinh;

    public HocSinhPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Học sinh");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        var innerWrap = new JPanel(new BorderLayout());
        innerWrap.setOpaque(false);
        innerWrap.setBorder(new EmptyBorder(8, 12, 12, 12));
        innerWrap.add(buildFilter(), BorderLayout.NORTH);
        innerWrap.add(buildTable(), BorderLayout.CENTER);
        outer.add(innerWrap, BorderLayout.CENTER);

        add(outer, BorderLayout.CENTER);

        // apply filters initially (after table created)
        SwingUtilities.invokeLater(this::applyHsFilters);
    }

    private JComponent buildFilter() {
        var filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filter.setOpaque(false);

        txtHsSearch = new JTextField(18);
        txtHsSearch.setBorder(BorderFactory.createTitledBorder("Từ khóa"));

        cboHsLop = new JComboBox<>();
        cboHsLop.setBorder(BorderFactory.createTitledBorder("Lớp"));
        // populate lớp options; if logged-in user is a teacher, restrict to classes
        // they teach
        try {
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                    cboHsLop.addItem("Tất cả");
                    LopBUS lopBUS = new LopBUS();
                    PhanCongDayBUS pc = new PhanCongDayBUS();
                    int maNK = NienKhoaBUS.current();
                    // no hoc ky filter here (show all classes assigned across HKs)
                    java.util.List<Integer> lopIds = pc.getDistinctMaLopByGiaoVien(nd.getId(), maNK, null);
                    for (Integer ml : lopIds) {
                        com.sgu.qlhs.dto.LopDTO l = lopBUS.getLopByMa(ml);
                        if (l != null)
                            cboHsLop.addItem(l.getTenLop());
                    }
                } else {
                    // not a teacher: show all classes
                    cboHsLop.addItem("Tất cả");
                    cboHsLop.addItem("10A1");
                    cboHsLop.addItem("10A2");
                    cboHsLop.addItem("11A1");
                    cboHsLop.addItem("12A1");
                }
            } else {
                cboHsLop.addItem("Tất cả");
                cboHsLop.addItem("10A1");
                cboHsLop.addItem("10A2");
                cboHsLop.addItem("11A1");
                cboHsLop.addItem("12A1");
            }
        } catch (Exception ex) {
            cboHsLop.addItem("Tất cả");
        }

        cboHsGioiTinh = new JComboBox<>(new String[] { "Tất cả", "Nam", "Nữ", "Khác" });
        cboHsGioiTinh.setBorder(BorderFactory.createTitledBorder("Giới tính"));

        var btnClear = new JButton("Xóa lọc");
        var btnDetail = new JButton("Chi tiết");

        filter.add(txtHsSearch);
        filter.add(cboHsLop);
        filter.add(cboHsGioiTinh);
        filter.add(btnClear);
        filter.add(btnDetail);

        // --- Sự kiện lọc ---
        txtHsSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyHsFilters();
            }

            public void removeUpdate(DocumentEvent e) {
                applyHsFilters();
            }

            public void changedUpdate(DocumentEvent e) {
                applyHsFilters();
            }
        });

        cboHsLop.addActionListener(e -> applyHsFilters());
        cboHsGioiTinh.addActionListener(e -> applyHsFilters());

        btnClear.addActionListener(e -> {
            txtHsSearch.setText("");
            cboHsLop.setSelectedIndex(0);
            cboHsGioiTinh.setSelectedIndex(0);
            applyHsFilters();
        });

        // --- Nút Chi tiết ---
        btnDetail.addActionListener(e -> showDetailDialog());

        return filter;
    }

    private JComponent buildTable() {
        tblHS = new JTable(new HocSinhTableModel());
        tblHS.setAutoCreateRowSorter(true);
        hsSorter = new TableRowSorter<>(tblHS.getModel());
        tblHS.setRowSorter(hsSorter);

        // Double-click vào dòng để mở chi tiết
        tblHS.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHS.getSelectedRow() != -1) {
                    showDetailDialog();
                }
            }
        });

        return new JScrollPane(tblHS);
    }

    private void showDetailDialog() {
        int row = tblHS.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một học sinh để xem chi tiết!");
            return;
        }

        int modelRow = tblHS.convertRowIndexToModel(row);
        TableModel model = tblHS.getModel();

        // Lấy toàn bộ dữ liệu của dòng hiện tại
        Object[] hocSinhData = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            hocSinhData[i] = model.getValueAt(modelRow, i);
        }

        // Mở dialog chi tiết
        new HocSinhDetailDialog(
                SwingUtilities.getWindowAncestor(this),
                hocSinhData).setVisible(true);
    }

    private void applyHsFilters() {
        if (hsSorter == null)
            return;

        var filters = new ArrayList<RowFilter<TableModel, Object>>();

        // --- Lọc theo từ khóa ---
        String kw = txtHsSearch.getText().trim();
        if (!kw.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }

        // --- Lọc theo lớp ---
        String lop = String.valueOf(cboHsLop.getSelectedItem());
        if (!"Tất cả".equals(lop)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(lop) + "$", 4));
        }

        // --- Lọc theo giới tính ---
        String gt = String.valueOf(cboHsGioiTinh.getSelectedItem());
        if (!"Tất cả".equals(gt)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(gt) + "$", 3));
        }

        hsSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
}
