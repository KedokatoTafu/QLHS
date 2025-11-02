package com.sgu.qlhs.ui;

import com.sgu.qlhs.bus.NguoiDungBUS;
import com.sgu.qlhs.dto.NguoiDungDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DangNhapUI extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    // ===== Màu sắc đồng bộ với MainDashboard =====
    private static final Color PRIMARY = new Color(33, 84, 170);
    private static final Color BG = new Color(246, 248, 251);
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color FIELD_BORDER = new Color(210, 215, 230);

    public DangNhapUI() {
        setTitle("Đăng nhập hệ thống");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        // ===== Root panel =====
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // ===== Header =====
        JLabel lblTitle = new JLabel("QUẢN LÝ HỌC SINH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);
        lblTitle.setBorder(new EmptyBorder(25, 0, 5, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // ===== Center form =====
        JPanel form = new JPanel();
        form.setBackground(BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 60, 20, 60));
        root.add(form, BorderLayout.CENTER);

       
        // Username
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.DARK_GRAY);
        form.add(lblUser);

        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBackground(FIELD_BG);
        txtUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)
        ));
        form.add(txtUser);
        form.add(Box.createVerticalStrut(12));

        // Password
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setForeground(Color.DARK_GRAY);
        form.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBackground(FIELD_BG);
        txtPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)
        ));
        form.add(txtPass);
        form.add(Box.createVerticalStrut(20));

        // Login button
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(PRIMARY);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        btnLogin.addChangeListener(e -> {
            if (btnLogin.getModel().isRollover()) {
                btnLogin.setBackground(new Color(25, 70, 145));
            } else {
                btnLogin.setBackground(PRIMARY);
            }
        });

        form.add(btnLogin);
        form.add(Box.createVerticalStrut(10));

        // Action
        btnLogin.addActionListener(e -> xuLyDangNhap());
    }

    private void xuLyDangNhap() {
        try {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            NguoiDungBUS bus = new NguoiDungBUS();
            NguoiDungDTO nd = bus.dangNhap(user, pass);

            if (nd != null) {
                JOptionPane.showMessageDialog(this,
                        "Xin chào " + nd.getHoTen() + " (" + nd.getVaiTro() + ")",
                        "Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);
                moGiaoDienTheoVaiTro(nd);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sai tên đăng nhập hoặc mật khẩu!",
                        "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moGiaoDienTheoVaiTro(NguoiDungDTO nd) {
        SwingUtilities.invokeLater(() -> {
            switch (nd.getVaiTro()) {
                case "quan_tri_vien":
                    new MainDashboard(nd).setVisible(true);
                    break;
                case "giao_vien":
                    new GiaoVienDashboard(nd).setVisible(true);
                    break;
                case "hoc_sinh":
                    new HocSinhDashboard(nd).setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this,
                            "Vai trò không hợp lệ: " + nd.getVaiTro());
                    return;
            }
            this.dispose();
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {}
            new DangNhapUI().setVisible(true);
        });
    }
}
