package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.NguoiDungDTO;

public class GiaoVienDashboard extends MainDashboard {

    public GiaoVienDashboard(NguoiDungDTO nd) {
        super(nd);
        // Ẩn các nút không dành cho giáo viên
        if (btnDash != null) btnDash.setVisible(false);
        if (btnGv != null) btnGv.setVisible(false);
        if (btnTk != null) btnTk.setVisible(false);

        // Mặc định hiển thị phần Học sinh hoặc Thời khóa biểu
        cards.show(centerCards, "HS");
    }
}
