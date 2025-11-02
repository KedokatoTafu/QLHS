/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.dto.MonHocDTO;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;

public class DiemNhapDialog extends JDialog {
    private final DiemBUS diemBUS = new DiemBUS();
    private final LopBUS lopBUS = new LopBUS();
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private final MonBUS monBUS = new MonBUS();
    // cached list of subjects for quick lookup
    private java.util.List<MonHocDTO> monList = new java.util.ArrayList<>();
    private final JComboBox<String> cboNamHoc = new JComboBox<>();
    private java.util.List<Integer> nienKhoaIds = new java.util.ArrayList<>();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã HS", "Họ tên", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c >= 2;
        } // chỉ cho nhập điểm

        @Override
        public Class<?> getColumnClass(int c) {
            return c >= 2 ? Double.class : String.class;
        }
    };

    // make table a field so we can select rows programmatically
    private JTable tbl;

    public DiemNhapDialog(Window owner) {
        super(owner, "Nhập điểm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(owner);
        build();
        loadMonData();
        loadLopData();
        // load school years (Niên khóa) into combo
        loadNienKhoa();
        pack();
    }

    /**
     * Convenience constructor to open the dialog pre-selected for a class, subject,
     * hk and optionally select a student.
     */
    public DiemNhapDialog(Window owner, int maLop, int maMon, int hocKy, int selectMaHS) {
        this(owner);
        // set selections after UI built; indices may differ so find indices
        // set MaNK/hocKy -> cboHK is HK1/HK2 -> index = hocKy-1
        if (hocKy >= 1 && hocKy <= 2) {
            cboHK.setSelectedIndex(hocKy - 1);
        }
        // select subject
        for (int i = 0; i < monList.size(); i++) {
            if (monList.get(i).getMaMon() == maMon) {
                cboMon.setSelectedIndex(i);
                break;
            }
        }
        // select class
        // cboLop has a first item "-- Chọn lớp --" then lop list
        java.util.List<LopDTO> lops = lopBUS.getAllLop();
        for (int i = 0; i < lops.size(); i++) {
            if (lops.get(i).getMaLop() == maLop) {
                cboLop.setSelectedIndex(i + 1);
                break;
            }
        }

        // After selections trigger load, select the student row if present
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (selectMaHS <= 0)
                return;
            for (int r = 0; r < model.getRowCount(); r++) {
                Object idObj = model.getValueAt(r, 0);
                if (idObj == null)
                    continue;
                try {
                    int maHS = Integer.parseInt(idObj.toString());
                    if (maHS == selectMaHS) {
                        // convert model row to view row and select
                        int viewRow = tbl.convertRowIndexToView(r);
                        tbl.setRowSelectionInterval(viewRow, viewRow);
                        tbl.scrollRectToVisible(tbl.getCellRect(viewRow, 0, true));
                        break;
                    }
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        });
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.add(new JLabel("Lớp:"));
        bar.add(cboLop);
        bar.add(new JLabel("Môn:"));
        bar.add(cboMon);
        bar.add(new JLabel("Học kỳ:"));
        bar.add(cboHK);
        bar.add(new JLabel("Năm học:"));
        bar.add(cboNamHoc);
        root.add(bar, BorderLayout.NORTH);

        tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // initial demo or empty until a class is selected
        // model rows will be loaded when user selects a class

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave = new JButton("Lưu");
        var btnDelete = new JButton("Xóa");
        btnSave.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            // map selected môn -> maMon using MonBUS
            MonBUS monBUS = new MonBUS();
            java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
            for (var m : monBUS.getAllMon())
                monMap.put(m.getTenMon(), m.getMaMon());
            Integer maMonObj = monMap.get(cboMon.getSelectedItem().toString());
            int maMon = maMonObj == null ? 1 : maMonObj;
            int hocKy = cboHK.getSelectedIndex() + 1; // HK1 -> 1, HK2 -> 2
            int maNK = NienKhoaBUS.current();
            int selNk = cboNamHoc.getSelectedIndex();
            if (selNk >= 0 && selNk < nienKhoaIds.size())
                maNK = nienKhoaIds.get(selNk);

            // resolve current user (if dialog owned by MainDashboard)
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

            int failedSave = 0;
            for (int r = 0; r < model.getRowCount(); r++) {
                Object idObj = model.getValueAt(r, 0);
                if (idObj == null)
                    continue;
                int maHS;
                try {
                    maHS = Integer.parseInt(idObj.toString());
                } catch (NumberFormatException ex) {
                    // skip rows where first column isn't an integer id
                    continue;
                }

                double mieng = valueToDouble(model.getValueAt(r, 2));
                double p15 = valueToDouble(model.getValueAt(r, 3));
                double gk = valueToDouble(model.getValueAt(r, 4));
                double ck = valueToDouble(model.getValueAt(r, 5));

                // Call BUS to save the record (delegates to DAO internally) with permission
                // check
                boolean ok = diemBUS.saveOrUpdateDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, nd);
                if (!ok)
                    failedSave++;
            }
            if (failedSave > 0) {
                JOptionPane.showMessageDialog(this, "Một số hàng không được lưu do thiếu quyền.", "Chú ý",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lưu điểm xong");
            }
            dispose();
        });
        btnClose.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            dispose();
        });
        btnDelete.addActionListener((java.awt.event.ActionEvent __) -> {
            // delete selected rows' diem for selected Mon/HK
            int[] sels = tbl.getSelectedRows();
            if (sels == null || sels.length == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hoặc nhiều hàng để xóa.", "Chú ý",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Integer maMonObj = getSelectedMaMon();
            if (maMonObj == null) {
                JOptionPane.showMessageDialog(this, "Môn chưa chọn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int maMon = maMonObj;
            int hocKy = cboHK.getSelectedIndex() + 1;
            int maNK = NienKhoaBUS.current();
            int selNk3 = cboNamHoc.getSelectedIndex();
            if (selNk3 >= 0 && selNk3 < nienKhoaIds.size())
                maNK = nienKhoaIds.get(selNk3);

            int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa điểm của các học sinh đã chọn?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION)
                return;

            // resolve current user for permission checks
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

            int failedDelete = 0;
            for (int r : sels) {
                int modelRow = tbl.convertRowIndexToModel(r);
                Object idObj = model.getValueAt(modelRow, 0);
                if (idObj == null)
                    continue;
                int maHS;
                try {
                    maHS = Integer.parseInt(idObj.toString());
                } catch (NumberFormatException ex) {
                    continue;
                }
                boolean ok = diemBUS.deleteDiem(maHS, maMon, hocKy, maNK, nd);
                if (!ok) {
                    failedDelete++;
                    continue;
                }
                // clear cells
                model.setValueAt(null, modelRow, 2);
                model.setValueAt(null, modelRow, 3);
                model.setValueAt(null, modelRow, 4);
                model.setValueAt(null, modelRow, 5);
            }
            if (failedDelete > 0) {
                JOptionPane.showMessageDialog(this, "Một số hàng không được xóa do thiếu quyền.", "Chú ý",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Xóa xong");
            }
        });
        btnPane.add(btnClose);
        btnPane.add(btnDelete);
        btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnSave);
    }

    private void loadMonData() {
        cboMon.removeAllItems();
        try {
            monList = monBUS.getAllMon();
            for (var m : monList) {
                cboMon.addItem(m.getTenMon());
            }
        } catch (Exception ex) {
            // fallback: keep empty or default list
            cboMon.addItem("Toán");
            cboMon.addItem("Văn");
        }
    }

    private void loadLopData() {
        cboLop.removeAllItems();
        cboLop.addItem("-- Chọn lớp --");
        java.util.List<LopDTO> lops = lopBUS.getAllLop();
        for (LopDTO l : lops) {
            cboLop.addItem(l.getTenLop());
        }

        cboLop.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            int idx = cboLop.getSelectedIndex();
            if (idx <= 0) {
                model.setRowCount(0);
                return;
            }
            LopDTO selected = lops.get(idx - 1);
            loadStudentsForLop(selected.getMaLop());
            // overlay existing scores for selected mon/hk using selected Niên khóa if any
            Integer maMonObj = getSelectedMaMon();
            if (maMonObj != null) {
                int maNKload = NienKhoaBUS.current();
                int sel = cboNamHoc.getSelectedIndex();
                if (sel >= 0 && sel < nienKhoaIds.size())
                    maNKload = nienKhoaIds.get(sel);
                loadExistingScoresForSelection(selected.getMaLop(), maMonObj, cboHK.getSelectedIndex() + 1,
                        maNKload);
            }
        });

        cboMon.addActionListener(e -> {
            int idx = cboLop.getSelectedIndex();
            if (idx <= 0)
                return;
            LopDTO selected = lops.get(idx - 1);
            loadStudentsForLop(selected.getMaLop());
            Integer maMonObj = getSelectedMaMon();
            if (maMonObj != null)
                loadExistingScoresForSelection(selected.getMaLop(), maMonObj, cboHK.getSelectedIndex() + 1,
                        NienKhoaBUS.current());
        });

        cboHK.addActionListener(e -> {
            int idx = cboLop.getSelectedIndex();
            if (idx <= 0)
                return;
            LopDTO selected = lops.get(idx - 1);
            loadStudentsForLop(selected.getMaLop());
            Integer maMonObj = getSelectedMaMon();
            if (maMonObj != null) {
                int maNKload = NienKhoaBUS.current();
                int sel = cboNamHoc.getSelectedIndex();
                if (sel >= 0 && sel < nienKhoaIds.size())
                    maNKload = nienKhoaIds.get(sel);
                loadExistingScoresForSelection(selected.getMaLop(), maMonObj, cboHK.getSelectedIndex() + 1,
                        maNKload);
            }
        });

        // when year selection changes, reload existing scores overlay
        cboNamHoc.addActionListener(e -> {
            int idx = cboLop.getSelectedIndex();
            if (idx <= 0)
                return;
            LopDTO selected = lops.get(idx - 1);
            Integer maMonObj2 = getSelectedMaMon();
            if (maMonObj2 != null) {
                int maNKload = NienKhoaBUS.current();
                int sel = cboNamHoc.getSelectedIndex();
                if (sel >= 0 && sel < nienKhoaIds.size())
                    maNKload = nienKhoaIds.get(sel);
                loadExistingScoresForSelection(selected.getMaLop(), maMonObj2, cboHK.getSelectedIndex() + 1,
                        maNKload);
            }
        });
    }

    /** Load Niên khóa options from DB into cboNamHoc and nienKhoaIds */
    private void loadNienKhoa() {
        cboNamHoc.removeAllItems();
        nienKhoaIds.clear();
        String sql = "SELECT MaNK, NamBatDau, NamKetThuc FROM NienKhoa ORDER BY NamBatDau ASC, MaNK ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int maNK = rs.getInt("MaNK");
                int nb = rs.getInt("NamBatDau");
                int nk = rs.getInt("NamKetThuc");
                String label = nb + "-" + nk;
                cboNamHoc.addItem(label);
                nienKhoaIds.add(maNK);
            }
        } catch (SQLException ex) {
            // fallback to a few recent labels if DB read fails
            cboNamHoc.addItem("2024-2025");
            cboNamHoc.addItem("2023-2024");
            cboNamHoc.addItem("2022-2023");
        }
        // select current MaNK if present
        int current = com.sgu.qlhs.bus.NienKhoaBUS.current();
        int idx = nienKhoaIds.indexOf(current);
        if (idx >= 0)
            cboNamHoc.setSelectedIndex(idx);
        else if (cboNamHoc.getItemCount() > 0)
            cboNamHoc.setSelectedIndex(0);
    }

    private Integer getSelectedMaMon() {
        Object sel = cboMon.getSelectedItem();
        if (sel == null)
            return null;
        String name = sel.toString();
        for (MonHocDTO m : monList) {
            if (m.getTenMon().equals(name))
                return m.getMaMon();
        }
        return null;
    }

    private void loadExistingScoresForSelection(int maLop, int maMon, int hocKy, int maNK) {
        // Use server-side filtered query to fetch only rows matching class, subject,
        // hk, nien khoa
        java.util.List<com.sgu.qlhs.dto.DiemDTO> rows = diemBUS.getDiemFiltered(maLop, maMon, hocKy, maNK, null, null);
        java.util.Map<Integer, com.sgu.qlhs.dto.DiemDTO> map = new java.util.HashMap<>();
        for (var d : rows) {
            // getDiemFiltered returns rows already filtered by maMon when provided
            map.put(d.getMaHS(), d);
        }

        // apply to table
        for (int r = 0; r < model.getRowCount(); r++) {
            Object idObj = model.getValueAt(r, 0);
            if (idObj == null)
                continue;
            int maHS;
            try {
                maHS = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException ex) {
                continue;
            }
            com.sgu.qlhs.dto.DiemDTO d = map.get(maHS);
            if (d != null) {
                model.setValueAt(d.getDiemMieng(), r, 2);
                model.setValueAt(d.getDiem15p(), r, 3);
                model.setValueAt(d.getDiemGiuaKy(), r, 4);
                model.setValueAt(d.getDiemCuoiKy(), r, 5);
            } else {
                model.setValueAt(null, r, 2);
                model.setValueAt(null, r, 3);
                model.setValueAt(null, r, 4);
                model.setValueAt(null, r, 5);
            }
        }
    }

    private void loadStudentsForLop(int maLop) {
        model.setRowCount(0);
        java.util.List<HocSinhDTO> students = hocSinhBUS.getHocSinhByMaLop(maLop);
        for (HocSinhDTO hs : students) {
            model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), null, null, null, null });
        }
    }

    // subject mapping removed; MonBUS is used dynamically where needed

    private double valueToDouble(Object o) {
        if (o == null)
            return 0.0;
        if (o instanceof Number)
            return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
            return 0.0;
        }
    }
}