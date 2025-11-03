package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PhanCongDayDAO {
    public List<Integer> getDistinctMaLopByGiaoVien(int maGV, Integer maNK, Integer hocKy) throws Exception {
        String sql = "SELECT DISTINCT pc.MaLop FROM PhanCongDay pc WHERE pc.MaGV = ?";
        if (maNK != null)
            sql += " AND pc.MaNK = ?";
        if (hocKy != null)
            sql += " AND pc.HocKy = ?";
        sql += " ORDER BY pc.MaLop";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            if (maNK != null)
                ps.setInt(idx++, maNK);
            if (hocKy != null)
                ps.setInt(idx++, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
                return out;
            }
        }
    }

    public List<Integer> getDistinctMaMonByGiaoVien(int maGV, Integer maNK, Integer hocKy) throws Exception {
        String sql = "SELECT DISTINCT pc.MaMon FROM PhanCongDay pc WHERE pc.MaGV = ?";
        if (maNK != null)
            sql += " AND pc.MaNK = ?";
        if (hocKy != null)
            sql += " AND pc.HocKy = ?";
        sql += " ORDER BY pc.MaMon";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            if (maNK != null)
                ps.setInt(idx++, maNK);
            if (hocKy != null)
                ps.setInt(idx++, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
                return out;
            }
        }
    }
}
