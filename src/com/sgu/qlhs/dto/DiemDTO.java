package com.sgu.qlhs.dto;

public class DiemDTO {
    private int maDiem;
    private int maHS;
    private String hoTen;
    private int maMon;
    private String tenMon;
    private int maLop;
    private String tenLop;
    private int hocKy;
    private double diemMieng;
    private double diem15p;
    private double diemGiuaKy;
    private double diemCuoiKy;
    private double diemTB;
    private String ghiChu;
    private String xepLoai;

    // === MỚI: Thêm 2 trường để xử lý môn Đánh giá (Đ/KĐ) ===
    private String loaiMon; // (Lấy từ MonHoc.LoaiMon: 'TinhDiem' hoặc 'DanhGia')
    private String ketQuaDanhGia; // (Lấy từ Diem.KetQuaDanhGia: 'Đ' hoặc 'KĐ')
    // ====================================================

    public DiemDTO() {
    }

    // constructor for full row (from getAllDiem)
    public DiemDTO(int maDiem, String hoTen, String tenMon, int hocKy, double diemMieng, double diem15p,
            double diemGiuaKy, double diemCuoiKy) {
        this.maDiem = maDiem;
        this.hoTen = hoTen;
        this.tenMon = tenMon;
        this.hocKy = hocKy;
        this.diemMieng = diemMieng;
        this.diem15p = diem15p;
        this.diemGiuaKy = diemGiuaKy;
        this.diemCuoiKy = diemCuoiKy;
    }

    // constructor for class-level rows (with MaHS and MaMon)
    public DiemDTO(int maHS, String hoTen, int maMon, String tenMon, double diemMieng, double diem15p,
            double diemGiuaKy, double diemCuoiKy) {
        this.maHS = maHS;
        this.hoTen = hoTen;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.diemMieng = diemMieng;
        this.diem15p = diem15p;
        this.diemGiuaKy = diemGiuaKy;
        this.diemCuoiKy = diemCuoiKy;
    }

    // getters/setters
    public int getMaDiem() {
        return maDiem;
    }

    public void setMaDiem(int maDiem) {
        this.maDiem = maDiem;
    }

    public int getMaHS() {
        return maHS;
    }

    public void setMaHS(int maHS) {
        this.maHS = maHS;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public int getMaMon() {
        return maMon;
    }

    public void setMaMon(int maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public int getHocKy() {
        return hocKy;
    }

    public void setHocKy(int hocKy) {
        this.hocKy = hocKy;
    }

    public double getDiemMieng() {
        return diemMieng;
    }

    public void setDiemMieng(double diemMieng) {
        this.diemMieng = diemMieng;
    }

    public double getDiem15p() {
        return diem15p;
    }

    public void setDiem15p(double diem15p) {
        this.diem15p = diem15p;
    }

    public double getDiemGiuaKy() {
        return diemGiuaKy;
    }

    public void setDiemGiuaKy(double diemGiuaKy) {
        this.diemGiuaKy = diemGiuaKy;
    }

    public double getDiemCuoiKy() {
        return diemCuoiKy;
    }

    public void setDiemCuoiKy(double diemCuoiKy) {
        this.diemCuoiKy = diemCuoiKy;
    }

    public int getMaLop() {
        return maLop;
    }

    public void setMaLop(int maLop) {
        this.maLop = maLop;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public double getDiemTB() {
        return diemTB;
    }

    public void setDiemTB(double diemTB) {
        this.diemTB = diemTB;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getXepLoai() {
        return xepLoai;
    }

    public void setXepLoai(String xepLoai) {
        this.xepLoai = xepLoai;
    }

    // === MỚI: Getters/Setters cho 2 trường mới ===
    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public String getKetQuaDanhGia() {
        return ketQuaDanhGia;
    }

    public void setKetQuaDanhGia(String ketQuaDanhGia) {
        this.ketQuaDanhGia = ketQuaDanhGia;
    }
    // ===========================================

    @Override
    public String toString() {
        return "DiemDTO{" + "maDiem=" + maDiem + ", maHS=" + maHS + ", hoTen='" + hoTen + '\'' + ", maMon=" + maMon
                + ", tenMon='" + tenMon + '\'' + ", hocKy=" + hocKy + '}';
    }
}