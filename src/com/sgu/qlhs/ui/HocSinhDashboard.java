package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.NguoiDungDTO;

public class HocSinhDashboard extends MainDashboard {

    public HocSinhDashboard(NguoiDungDTO nd) {
        super(nd);
        // Ẩn tất cả trừ Thời khóa biểu, Điểm và Thống kê
        if (btnDash != null) btnDash.setVisible(false);
        if (btnHs != null) btnHs.setVisible(false);
        if (btnGv != null) btnGv.setVisible(false);
        if (btnLp != null) btnLp.setVisible(false);
        // if (btnTk != null) btnTk.setVisible(false);

        // Mặc định hiển thị Thời khóa biểu
        cards.show(centerCards, "THOI KHOA BIEU");
    }
}