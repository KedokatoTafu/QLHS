package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.PhanCongDayDAO;

import java.util.ArrayList;
import java.util.List;

public class PhanCongDayBUS {
    private final PhanCongDayDAO dao;

    public PhanCongDayBUS() {
        dao = new PhanCongDayDAO();
    }

    public List<Integer> getDistinctMaLopByGiaoVien(int maGV, Integer maNK, Integer hocKy) {
        try {
            List<Integer> l = dao.getDistinctMaLopByGiaoVien(maGV, maNK, hocKy);
            return l != null ? l : new ArrayList<>();
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy PhanCongDay.MaLop: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Integer> getDistinctMaMonByGiaoVien(int maGV, Integer maNK, Integer hocKy) {
        try {
            List<Integer> l = dao.getDistinctMaMonByGiaoVien(maGV, maNK, hocKy);
            return l != null ? l : new ArrayList<>();
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy PhanCongDay.MaMon: " + ex.getMessage());
            return new ArrayList<>();
        }
    }
}
