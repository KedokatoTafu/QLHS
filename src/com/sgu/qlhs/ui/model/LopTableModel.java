package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LopTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã lớp", "Tên lớp", "Khối", "Phòng" };
    private final List<LopDTO> data;

    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();

    public LopTableModel() {
        LopBUS lopBUS = new LopBUS();
        this.data = lopBUS.getAllLop();
    }

    /**
     * Construct a table model containing only the classes in the provided id list.
     * If allowedMaLop is null or empty, falls back to all classes.
     */
    public LopTableModel(java.util.List<Integer> allowedMaLop) {
        LopBUS lopBUS = new LopBUS();
        if (allowedMaLop == null || allowedMaLop.isEmpty()) {
            this.data = lopBUS.getAllLop();
        } else {
            java.util.List<LopDTO> list = new java.util.ArrayList<>();
            for (Integer id : allowedMaLop) {
                LopDTO l = lopBUS.getLopByMa(id);
                if (l != null)
                    list.add(l);
            }
            this.data = list;
        }
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int c) {
        return cols[c];
    }

    @Override
    public Object getValueAt(int r, int c) {
        LopDTO l = data.get(r);
        switch (c) {
            case 0:
                return l.getMaLop();
            case 1:
                return l.getTenLop();
            case 2:
                return l.getKhoi();
            case 3:
                return l.getTenPhong();
            default:
                return null;
        }
    }

    public int getMaLop(int row) {
        return data.get(row).getMaLop();
    }

    public String getTenLop(int row) {
        return data.get(row).getTenLop();
    }

    public List<HocSinhDTO> getHocSinhByLop(int maLop) {
        return hocSinhBUS.getHocSinhByMaLop(maLop);
    }
}
