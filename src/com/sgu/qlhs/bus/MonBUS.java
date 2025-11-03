package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.MonHocDTO;
import com.sgu.qlhs.database.MonDAO;
import java.util.ArrayList;
import java.util.List;

public class MonBUS {
    private MonDAO dao;

    public MonBUS() {
        dao = new MonDAO();
    }

    public List<MonHocDTO> getAllMon() {
        List<MonHocDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllMon();
        for (Object[] r : rows) {
            int ma = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String ten = r[1] != null ? r[1].toString() : "";
            int soTiet = (r.length > 2 && r[2] != null)
                    ? ((r[2] instanceof Integer) ? (Integer) r[2] : Integer.parseInt(r[2].toString()))
                    : 0;
            String ghi = (r.length > 3 && r[3] != null) ? r[3].toString() : "";
            // <-- LẤY DỮ LIỆU CỘT MỚI -->
            String loaiMon = (r.length > 4 && r[4] != null) ? r[4].toString() : "TinhDiem";

            // <-- SỬ DỤNG CONSTRUCTOR MỚI -->
            list.add(new MonHocDTO(ma, ten, soTiet, ghi, loaiMon));
        }
        return list;
    }

    public void saveMon(String tenMon, int soTiet, String ghiChu) {
        dao.insertMon(tenMon, soTiet, ghiChu);
    }

    public void updateMon(int maMon, String tenMon, int soTiet, String ghiChu) {
        dao.updateMon(maMon, tenMon, soTiet, ghiChu);
    }

    public void deleteMon(int maMon) {
        dao.deleteMon(maMon);
    }

    public MonHocDTO getMonByMa(int maMon) {
        for (MonHocDTO m : getAllMon()) {
            if (m.getMaMon() == maMon)
                return m;
        }
        return null;
    }
}