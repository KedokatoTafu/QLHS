package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.components.*;
import com.sgu.qlhs.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainDashboard extends JFrame {

    private NguoiDungDTO nguoiDung; // Dùng cho thông tin đăng nhập

    // ===== Palette =====
    public static final Color SIDEBAR_BG = new Color(29, 35, 66);
    public static final Color SIDEBAR_BTN = new Color(48, 58, 98);
    public static final Color SIDEBAR_BTN_ACTIVE = new Color(59, 72, 120);
    public static final Color INDICATOR = new Color(110, 140, 220);
    public static final Color TEXT_WHITE = Color.WHITE;

    public static final Color PAGE_BG = new Color(246, 248, 251);
    public static final Color TITLE = new Color(29, 35, 66);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = new Color(224, 229, 238);
    public static final Color ICON_BG = new Color(230, 240, 255);
    public static final Color ICON_FG = new Color(33, 84, 170);

    // Card keys
    private static final String CARD_DASH = "DASHBOARD";
    private static final String CARD_HS = "HS";
    private static final String CARD_GV = "GV";
    private static final String CARD_LP = "LP";
    private static final String CARD_DIEM = "DIEM";
    private static final String CARD_TKB = "THOI KHOA BIEU";
    private static final String CARD_TK = "TK";

    protected JPanel centerCards;
    protected CardLayout cards;

    // Sidebar buttons
    protected SidebarButton btnDash, btnHs, btnGv, btnLp, btntkb, btnDiem, btnTk;

    // === Constructor có thông tin người dùng (dùng khi đăng nhập thành công) ===
    public MainDashboard(NguoiDungDTO nd) {
        super("QUẢN LÝ HỌC SINH");
        this.nguoiDung = nd;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
        buildUI();
    }

    // Expose the logged-in user to child panels/dialogs so they can
    // enforce role-based behavior (e.g. teacher-scoped views).
    public NguoiDungDTO getNguoiDung() {
        return this.nguoiDung;
    }

    private void buildUI() {
        var root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        setContentPane(root);

        // ===== Sidebar =====
        var sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));
        sidebar.setPreferredSize(new Dimension(230, 0));
        root.add(sidebar, BorderLayout.WEST);

        var avatar = new CircleAvatar(36, new Color(255, 255, 255, 240));
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);
        var lblUser = label(
                nguoiDung != null ? nguoiDung.getHoTen() + " (" + nguoiDung.getVaiTro() + ")" : "Khách",
                15f,
                TEXT_WHITE);
        lblUser.setBorder(new EmptyBorder(6, 8, 6, 8));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        var head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(avatar);
        head.add(lblUser);

        sidebar.add(head);
        sidebar.add(Box.createVerticalStrut(10));

        btnDash = new SidebarButton("Dashboard", true);
        btnHs = new SidebarButton("Học sinh", false);
        btnGv = new SidebarButton("Giáo viên", false);
        btnLp = new SidebarButton("Lớp / Phòng", false);
        btntkb = new SidebarButton("Thời Khóa Biểu", false);
        btnDiem = new SidebarButton("Điểm", false);
        btnTk = new SidebarButton("Thống kê", false);

        sidebar.add(btnDash);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnHs);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnGv);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnLp);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btntkb);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnDiem);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnTk);
        sidebar.add(Box.createVerticalGlue());

        SidebarButton[] sideButtons = { btnDash, btnHs, btnGv, btnLp, btntkb, btnDiem, btnTk };

        // ===== Main area =====
        var mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(PAGE_BG);
        mainArea.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(mainArea, BorderLayout.CENTER);

        var title = label("QUẢN LÝ HỌC SINH", 30f, TITLE);
        title.setBorder(new EmptyBorder(0, 4, 8, 0));
        mainArea.add(title, BorderLayout.NORTH);

        // Card layout trung tâm
        cards = new CardLayout();
        centerCards = new JPanel(cards);
        centerCards.setOpaque(false);
        mainArea.add(centerCards, BorderLayout.CENTER);

        // Thêm các panel chức năng
        centerCards.add(new DashboardPanel(), CARD_DASH);
        centerCards.add(new HocSinhPanel(), CARD_HS);
        centerCards.add(new GiaoVienPanel(), CARD_GV);
        centerCards.add(new LopPhongPanel(), CARD_LP);
        centerCards.add(new TKBPanel(), CARD_TKB);
        centerCards.add(new DiemPanel(), CARD_DIEM);
        centerCards.add(new ThongKePanel(), CARD_TK);

        // Status bar
        var status = new JPanel(new BorderLayout());
        status.setBackground(new Color(245, 245, 248));
        status.setBorder(new EmptyBorder(6, 12, 6, 12));
        String tenNguoiDung = nguoiDung != null ? nguoiDung.getHoTen() : "Không xác định";
        status.add(new JLabel("Người đăng nhập: " + tenNguoiDung), BorderLayout.WEST);
        status.add(new JLabel(java.time.LocalDate.now().toString()), BorderLayout.EAST);
        root.add(status, BorderLayout.SOUTH);

        // Sự kiện sidebar
        btnDash.addActionListener(e -> showCard(CARD_DASH, sideButtons, btnDash));
        btnHs.addActionListener(e -> showCard(CARD_HS, sideButtons, btnHs));
        btnGv.addActionListener(e -> showCard(CARD_GV, sideButtons, btnGv));
        btnLp.addActionListener(e -> showCard(CARD_LP, sideButtons, btnLp));
        btntkb.addActionListener(e -> showCard(CARD_TKB, sideButtons, btntkb));
        btnDiem.addActionListener(e -> showCard(CARD_DIEM, sideButtons, btnDiem));
        btnTk.addActionListener(e -> showCard(CARD_TK, sideButtons, btnTk));

        cards.show(centerCards, CARD_DASH);
    }

    private void showCard(String key, SidebarButton[] all, SidebarButton active) {
        for (SidebarButton b : all)
            b.setActive(false);
        active.setActive(true);
        cards.show(centerCards, key);
    }

    private JLabel label(String text, float size, Color color) {
        var lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, size));
        lbl.setForeground(color);
        return lbl;
    }
}
