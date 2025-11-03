/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.BarChartCanvas;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.dto.LopDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ThongKeGioiTinhDialog extends JDialog {
    public ThongKeGioiTinhDialog(Window owner) {
        super(owner, "Thống kê giới tính", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(640, 420));
        setLocationRelativeTo(owner);
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // Load real data via HocSinhBUS
        int nam = 0, nu = 0, khac = 0;
        try {
            HocSinhBUS hsBus = new HocSinhBUS();
            // If current user is a teacher, restrict to students in their assigned classes
            java.util.List<HocSinhDTO> all;
            try {
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                    NguoiDungDTO nd = md.getNguoiDung();
                    if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                        int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
                        // get assigned class ids and map them to names
                        PhanCongDayBUS phanCong = new PhanCongDayBUS();
                        LopBUS lopBus = new LopBUS();
                        java.util.List<Integer> assigned = phanCong.getDistinctMaLopByGiaoVien(nd.getId(), maNK,
                                null);
                        java.util.Set<String> allowedLopNames = new java.util.HashSet<>();
                        for (Integer ml : assigned) {
                            LopDTO l = lopBus.getLopByMa(ml);
                            if (l != null && l.getTenLop() != null)
                                allowedLopNames.add(l.getTenLop());
                        }
                        all = new java.util.ArrayList<>();
                        for (HocSinhDTO h : hsBus.getAllHocSinh()) {
                            if (h.getTenLop() != null && allowedLopNames.contains(h.getTenLop()))
                                all.add(h);
                        }
                    } else {
                        all = hsBus.getAllHocSinh();
                    }
                } else {
                    all = hsBus.getAllHocSinh();
                }
            } catch (Exception ex) {
                all = hsBus.getAllHocSinh();
            }
            for (HocSinhDTO h : all) {
                String g = h.getGioiTinh();
                if (g == null)
                    khac++;
                else if (g.trim().equalsIgnoreCase("Nam"))
                    nam++;
                else if (g.trim().equalsIgnoreCase("Nữ") || g.trim().equalsIgnoreCase("Nu"))
                    nu++;
                else
                    khac++;
            }
        } catch (Exception ex) {
            // fallback to zeros on error
            System.err.println("Lỗi khi tải dữ liệu học sinh: " + ex.getMessage());
        }
        String[] cats = { "Nam", "Nữ", "Khác" };
        int[] vals = { nam, nu, khac };
        root.add(new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, vals), BorderLayout.CENTER);

        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var close = new JButton("Đóng");
        close.addActionListener(e -> dispose());
        south.add(close);
        root.add(south, BorderLayout.SOUTH);
        pack();
    }
}
