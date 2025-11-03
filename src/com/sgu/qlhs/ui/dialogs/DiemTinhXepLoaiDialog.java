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
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.DiemDTO;

public class DiemTinhXepLoaiDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã HS", "Họ tên", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ", "TB" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c >= 2 && c <= 5;
        } // cho sửa điểm nguồn

        @Override
        public Class<?> getColumnClass(int c) {
            return (c >= 2 && c <= 6) ? Double.class : String.class;
        }
    };

    public DiemTinhXepLoaiDialog(Window owner) {
        super(owner, "Tính điểm TB từng môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 540));
        setLocationRelativeTo(owner);
        build();
        initBuses();
        loadMonData();
        loadLopData();
        pack();
    }

    private LopBUS lopBUS;
    private HocSinhBUS hocSinhBUS;
    private DiemBUS diemBUS;
    private java.util.List<LopDTO> lops = new java.util.ArrayList<>();

    private void initBuses() {
        lopBUS = new LopBUS();
        hocSinhBUS = new HocSinhBUS();
        diemBUS = new DiemBUS();
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
        var btnCalc = new JButton("Tính");
        bar.add(btnCalc);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // initially empty; load when class/subject selected

        btnCalc.addActionListener(e -> {
            for (int r = 0; r < model.getRowCount(); r++) {
                double mieng = val(model.getValueAt(r, 2));
                double p15 = val(model.getValueAt(r, 3));
                double gk = val(model.getValueAt(r, 4));
                double ck = val(model.getValueAt(r, 5));
                double tb = round1(mieng * 0.1 + p15 * 0.2 + gk * 0.3 + ck * 0.4);
                model.setValueAt(tb, r, 6);
                // model.setValueAt(xepLoai(tb), r, 7);
            }
        });

        // Load students + existing scores when class/subject/hk changes
        cboLop.addActionListener(e -> reloadForSelection());
        cboMon.addActionListener(e -> reloadForSelection());
        cboHK.addActionListener(e -> reloadForSelection());

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave = new JButton("Lưu kết quả");
        btnSave.addActionListener(e -> {
            // Save edited scores via DiemBUS
            int hocKy = cboHK.getSelectedIndex() + 1;
            int maNK = NienKhoaBUS.current();
            // map subject name -> maMon using MonBUS
            MonBUS monBUS = new MonBUS();
            java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
            for (var m : monBUS.getAllMon())
                monMap.put(m.getTenMon(), m.getMaMon());
            Integer maMonObj = monMap.get((String) cboMon.getSelectedItem());
            int maMon = maMonObj == null ? 1 : maMonObj;
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

            int failed = 0;
            for (int r = 0; r < model.getRowCount(); r++) {
                Object idObj = model.getValueAt(r, 0);
                if (idObj == null)
                    continue;
                int maHS;
                try {
                    maHS = Integer.parseInt(idObj.toString());
                } catch (Exception ex) {
                    continue;
                }
                double mieng = val(model.getValueAt(r, 2));
                double p15 = val(model.getValueAt(r, 3));
                double gk = val(model.getValueAt(r, 4));
                double ck = val(model.getValueAt(r, 5));
                boolean ok = diemBUS.saveOrUpdateDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, nd);
                if (!ok)
                    failed++;
            }
            if (failed > 0) {
                JOptionPane.showMessageDialog(this, "Một số hàng không được lưu do thiếu quyền.", "Chú ý",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Đã lưu kết quả");
            }
            dispose();
        });
        btnClose.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            dispose();
        });
        btnPane.add(btnClose);
        btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);
    }

    private void loadMonData() {
        cboMon.removeAllItems();
        try {
            MonBUS monBUS = new MonBUS();
            for (var m : monBUS.getAllMon())
                cboMon.addItem(m.getTenMon());
        } catch (Exception ex) {
            cboMon.addItem("Toán");
            cboMon.addItem("Văn");
        }
    }

    private static double val(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    private static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
    // private static String xepLoai(double tb){
    // if(tb>=8.0) return "Giỏi";
    // if(tb>=6.5) return "Khá";
    // if(tb>=5.0) return "Trung bình";
    // return "Yếu";
    // }

    private void loadLopData() {
        lops = lopBUS.getAllLop();
        cboLop.removeAllItems();
        cboLop.addItem("-- Chọn lớp --");
        // Restrict to assigned classes for teachers; if user is student show only
        // their class
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
                        java.util.Set<Integer> allowed = new java.util.HashSet<>(assigned);
                        for (LopDTO l : lops) {
                            if (allowed != null && !allowed.contains(l.getMaLop()))
                                continue;
                            cboLop.addItem(l.getTenLop());
                        }
                        return;
                    }
                    if ("hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                        // student: show only their class and disable selection
                        int maHS = nd.getId();
                        HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);
                        String tenLop = hs != null && hs.getTenLop() != null ? hs.getTenLop() : "-- Tất cả --";
                        cboLop.removeAllItems();
                        cboLop.addItem(tenLop);
                        cboLop.setSelectedIndex(0);
                        cboLop.setEnabled(false);
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            // fallback to showing all classes
        }

        for (LopDTO l : lops) {
            cboLop.addItem(l.getTenLop());
        }
    }

    private void reloadForSelection() {
        int idx = cboLop.getSelectedIndex();
        if (idx <= 0) {
            model.setRowCount(0);
            return;
        }
        LopDTO sel = lops.get(idx - 1);
        int maLop = sel.getMaLop();
        String monName = (String) cboMon.getSelectedItem();
        int hocKy = cboHK.getSelectedIndex() + 1;
        MonBUS monBUS = new MonBUS();
        java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
        for (var m : monBUS.getAllMon())
            monMap.put(m.getTenMon(), m.getMaMon());
        Integer maMonObj = monMap.get(monName);
        int maMon = maMonObj == null ? 1 : maMonObj;
        loadStudentsForLopAndMon(maLop, maMon, monName, hocKy);
    }

    private void loadStudentsForLopAndMon(int maLop, int maMon, String monName, int hocKy) {
        model.setRowCount(0);
        try {
            // resolve current user for permission-aware reads
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

            java.util.List<HocSinhDTO> students = hocSinhBUS.getHocSinhByMaLop(maLop);
            // if student, override to only that student
            if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                int maHSself = nd.getId();
                HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHSself);
                students = new java.util.ArrayList<>();
                if (hs != null)
                    students.add(hs);
            }

            for (HocSinhDTO hs : students) {
                Double mieng = null, p15 = null, gk = null, ck = null;
                int maNK = NienKhoaBUS.current();
                java.util.List<DiemDTO> ds = diemBUS.getDiemByMaHS(hs.getMaHS(), hocKy, maNK, nd);
                for (DiemDTO d : ds) {
                    if ((d.getMaMon() != 0 && d.getMaMon() == maMon)
                            || (d.getTenMon() != null && d.getTenMon().equalsIgnoreCase(monName))) {
                        mieng = d.getDiemMieng();
                        p15 = d.getDiem15p();
                        gk = d.getDiemGiuaKy();
                        ck = d.getDiemCuoiKy();
                        break;
                    }
                }
                model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), mieng, p15, gk, ck, null });
            }
        } catch (Exception ex) {
            java.util.List<HocSinhDTO> students = hocSinhBUS.getHocSinhByMaLop(maLop);
            for (HocSinhDTO hs : students) {
                model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), null, null, null, null, null });
            }
        }
    }

    // Subject mapping replaced by MonBUS lookups above
}