-- === KHỞI TẠO DB ===
CREATE DATABASE QLHS CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE QLHS;

-- === BẢNG DANH MỤC / THỰC THỂ CHÍNH ===
CREATE TABLE PhongHoc (
    MaPhong INT PRIMARY KEY AUTO_INCREMENT,
    TenPhong VARCHAR(50),
    LoaiPhong VARCHAR(50),
    SucChua INT,
    ViTri VARCHAR(100)
);

CREATE TABLE Lop (
    MaLop INT PRIMARY KEY AUTO_INCREMENT,
    TenLop VARCHAR(50),
    Khoi INT,
    MaPhong INT NULL,
    CONSTRAINT fk_lop_phong FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE NienKhoa (
    MaNK INT PRIMARY KEY AUTO_INCREMENT,
    NamBatDau INT NOT NULL,
    NamKetThuc INT NOT NULL
);

CREATE TABLE GiaoVien (
    MaGV INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100)
);

CREATE TABLE MonHoc (
    MaMon INT PRIMARY KEY AUTO_INCREMENT,
    TenMon VARCHAR(100),
    SoTiet INT,
    GhiChu TEXT DEFAULT NULL
);

CREATE TABLE HocSinh (
    MaHS INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    DiaChi VARCHAR(255),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    MaLop INT NULL,
    CONSTRAINT fk_hs_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE SET NULL ON UPDATE CASCADE
);

-- === QUAN HỆ / NGHIỆP VỤ ===
CREATE TABLE PhuHuynh (
    MaPH INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    DiaChi VARCHAR(255)
);

CREATE TABLE HocSinh_PhuHuynh (
    MaHS INT,
    MaPH INT,
    QuanHe VARCHAR(50),
    PRIMARY KEY (MaHS, MaPH),
    CONSTRAINT fk_hsph_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hsph_ph FOREIGN KEY (MaPH) REFERENCES PhuHuynh (MaPH) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE DanhGia (
    MaDG INT PRIMARY KEY AUTO_INCREMENT,
    MaHS INT,
    Loai VARCHAR(50), -- 'KhenThuong' / 'KyLuat'
    TieuDe VARCHAR(100),
    NoiDung TEXT,
    NgayApDung DATE,
    CONSTRAINT fk_dg_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE PhanCongDay (
    MaPC INT PRIMARY KEY AUTO_INCREMENT,
    MaGV INT,
    MaMon INT,
    MaLop INT,
    MaNK INT,
    HocKy INT,
    CONSTRAINT fk_pc_gv FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_mon FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE ChuNhiem (
    MaCN INT PRIMARY KEY AUTO_INCREMENT,
    MaGV INT,
    MaLop INT,
    MaNK INT,
    NgayNhanNhiem DATE,
    NgayKetThuc DATE,
    CONSTRAINT fk_cn_gv FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cn_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cn_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE ThoiKhoaBieu (
    MaTKB INT PRIMARY KEY AUTO_INCREMENT, -- Mã thời khóa biểu
    MaLop INT NOT NULL, -- Lớp áp dụng (khóa ngoại -> Lop)
    MaGV INT NOT NULL, -- Giáo viên dạy (khóa ngoại -> GiaoVien)
    MaMon INT NOT NULL, -- Môn học (khóa ngoại -> MonHoc)
    MaPhong INT NOT NULL, -- Phòng học (khóa ngoại -> PhongHoc)
    HocKy VARCHAR(10) NOT NULL, -- Học kỳ (VD: HK1, HK2)
    NamHoc VARCHAR(9) NOT NULL, -- Năm học (VD: 2024-2025)
    ThuTrongTuan VARCHAR(10) NOT NULL, -- Thứ trong tuần (VD: Thứ 2, Thứ 3,...)
    TietBatDau INT NOT NULL, -- Tiết bắt đầu (VD: 1)
    TietKetThuc INT NOT NULL, -- Tiết kết thúc (VD: 3)
    TrangThai TINYINT DEFAULT 1, -- 1: hoạt động, 0: xóa mềm
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo bản ghi
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON UPDATE CASCADE,
    FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON UPDATE CASCADE,
    FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON UPDATE CASCADE,
    FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON UPDATE CASCADE
);

-- === BẢNG ĐIỂM (GỘP) + CỘT SINH ===
CREATE TABLE Diem (
    MaDiem INT PRIMARY KEY AUTO_INCREMENT,
    MaHS INT,
    MaMon INT,
    HocKy INT,
    MaNK INT,
    DiemMieng DECIMAL(3, 1),
    Diem15p DECIMAL(3, 1),
    DiemGiuaKy DECIMAL(3, 1),
    DiemCuoiKy DECIMAL(3, 1),
    CONSTRAINT fk_diem_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diem_mon FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diem_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_diem UNIQUE (MaHS, MaMon, HocKy, MaNK)
);

-- Cột sinh: TB & Xếp loại (theo trọng số thành phần 10/20/30/40)
ALTER TABLE Diem
ADD COLUMN DiemTB DECIMAL(3, 1) GENERATED ALWAYS AS (
    CASE
        WHEN DiemMieng IS NULL
        OR Diem15p IS NULL
        OR DiemGiuaKy IS NULL
        OR DiemCuoiKy IS NULL THEN NULL
        ELSE ROUND(
            DiemMieng * 0.10 + Diem15p * 0.20 + DiemGiuaKy * 0.30 + DiemCuoiKy * 0.40,
            1
        )
    END
) STORED,
ADD COLUMN XepLoai VARCHAR(20) GENERATED ALWAYS AS (
    CASE
        WHEN DiemTB IS NULL THEN NULL
        WHEN DiemTB >= 8.0 THEN 'Giỏi'
        WHEN DiemTB >= 6.5 THEN 'Khá'
        WHEN DiemTB >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END
) VIRTUAL;

-- === TRỌNG SỐ NĂM (KHÔNG DÙNG BIẾN PHIÊN) ===
CREATE TABLE TrongSoNam (
    MaNK INT PRIMARY KEY,
    wHK1 DECIMAL(4, 3) NOT NULL,
    wHK2 DECIMAL(4, 3) NOT NULL,
    CONSTRAINT fk_ts_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

-- TRIGGER: mọi niên khóa mới thêm vào sẽ tự có 40/60
DELIMITER /
/

CREATE TRIGGER trg_NienKhoa_DefaultTrongSo
AFTER INSERT ON NienKhoa
FOR EACH ROW
BEGIN
  INSERT INTO TrongSoNam (MaNK, wHK1, wHK2)
  VALUES (NEW.MaNK, 0.4, 0.6)
  ON DUPLICATE KEY UPDATE wHK1 = VALUES(wHK1), wHK2 = VALUES(wHK2);
END
/
/

-- === Hạnh kiểm ===
CREATE TABLE HanhKiem (
    MaHK INT PRIMARY KEY AUTO_INCREMENT, -- Mã hạnh kiểm
    MaHS INT NOT NULL, -- Học sinh
    MaNK INT NOT NULL, -- Niên khóa
    HocKy INT NOT NULL, -- Học kỳ (1, 2)
    XepLoai ENUM(
        'Tốt',
        'Khá',
        'Trung bình',
        'Yếu'
    ) NOT NULL, -- Xếp loại hạnh kiểm
    GhiChu TEXT DEFAULT NULL, -- Ghi chú thêm (nếu có)
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hk_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hk_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_hanhkiem UNIQUE (MaHS, MaNK, HocKy) -- Mỗi học sinh chỉ có 1 bản ghi hạnh kiểm mỗi HK
);

INSERT INTO
    PhongHoc (
        MaPhong,
        TenPhong,
        LoaiPhong,
        SucChua,
        ViTri
    )
VALUES (
        101,
        'P101',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        102,
        'P102',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        201,
        'P201',
        'Lý thuyết',
        45,
        'Tầng 2'
    ),
    (
        301,
        'P301',
        'Lý thuyết',
        45,
        'Tầng 3'
    ),
    (
        202,
        'P202',
        'Thí nghiệm',
        30,
        'Tầng 2'
    ),
    (
        103,
        'P103',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        203,
        'P203',
        'Lý thuyết',
        45,
        'Tầng 2'
    ),
    (
        302,
        'P302',
        'Lý thuyết',
        45,
        'Tầng 3'
    );

INSERT INTO
    Lop (MaLop, TenLop, Khoi, MaPhong)
VALUES (1, '10A1', 10, 101),
    (2, '10A2', 10, 102),
    (3, '11A1', 11, 201),
    (4, '12A1', 12, 301),
    (5, '10A3', 10, 103),
    (6, '11A2', 11, 203),
    (7, '12A2', 12, 302);

-- *** ĐÃ SỬA LẠI TÊN GIÁO VIÊN CHO THỰC TẾ ***
INSERT INTO
    GiaoVien (
        MaGV,
        HoTen,
        SoDienThoai,
        Email,
        NgaySinh,
        GioiTinh
    )
VALUES (
        101,
        'Trần Quốc Tuấn',
        '0901111222',
        'tuantq@gv.example.com',
        '1980-05-20',
        'Nam'
    ),
    (
        102,
        'Lê Thị Kim Ngân',
        '0902222333',
        'nganltk@gv.example.com',
        '1985-11-15',
        'Nữ'
    ),
    (
        103,
        'Nguyễn Hữu Thắng',
        '0903333444',
        'thangnh@gv.example.com',
        '1978-02-28',
        'Nam'
    ),
    (
        104,
        'Hoàng Minh Dũng',
        '0904444555',
        'dunghm@gv.example.com',
        '1982-09-09',
        'Nam'
    ),
    (
        105,
        'Bùi Thanh Mai',
        '0905555666',
        'maibt@gv.example.com',
        '1987-03-12',
        'Nữ'
    ),
    (
        106,
        'Đặng Mai Lan',
        '0906666777',
        'landm@gv.example.com',
        '1986-07-30',
        'Nữ'
    ),
    (
        107,
        'Phan Việt Anh',
        '0907777888',
        'anhpv@gv.example.com',
        '1979-01-18',
        'Nam'
    ),
    (
        108,
        'Ngô Hải Long',
        '0908888999',
        'longnh@gv.example.com',
        '1983-04-22',
        'Nam'
    ),
    (
        109,
        'Võ Thu Thảo',
        '0910000111',
        'thaovt@gv.example.com',
        '1984-12-05',
        'Nữ'
    ),
    (
        110,
        'Hồ Minh Quân',
        '0911111222',
        'quanhm@gv.example.com',
        '1990-02-10',
        'Nam'
    ),
    (
        111,
        'Đoàn Công Thành',
        '0912222333',
        'thanhdc@gv.example.com',
        '1985-05-15',
        'Nam'
    ),
    (
        112,
        'Lý Anh Dũng',
        '0913333444',
        'dungla@gv.example.com',
        '1981-08-08',
        'Nam'
    ),
    (
        113,
        'Nguyễn Đức Quang',
        '0914444777',
        'quangnd@gv.example.com',
        '1988-02-10',
        'Nam'
    ),
    (
        114,
        'Phạm Thị Hòa',
        '0915555888',
        'hoapt@gv.example.com',
        '1989-06-12',
        'Nữ'
    ),
    (
        115,
        'Trần Văn Lực',
        '0916666999',
        'luctv@gv.example.com',
        '1984-03-15',
        'Nam'
    );

INSERT INTO
    HocSinh (
        MaHS,
        HoTen,
        NgaySinh,
        GioiTinh,
        MaLop,
        DiaChi,
        SoDienThoai,
        Email
    )
VALUES
    -- Lớp 10A1 (MaLop=1) - Thêm 35 HS (10 -> 44)
    (
        1,
        'Bùi Tấn Trường',
        '2007-09-01',
        'Nam',
        1,
        '123 Đường ABC, Quận 1',
        '0123456789',
        'truongbt@example.com'
    ),
    (
        2,
        'Trần Thùy Linh',
        '2007-12-11',
        'Nữ',
        1,
        '456 Đường XYZ, Quận 2',
        '0987654321',
        'linhtt@example.com'
    ),
    (
        3,
        'Phạm Minh Khang',
        '2007-05-10',
        'Nam',
        2,
        '789 Đường DEF, Quận 3',
        '0901234567',
        'khangpm@example.com'
    ),
    (
        4,
        'Lê Thu Thảo',
        '2007-03-22',
        'Nữ',
        3,
        '111 Đường GHI, Quận 4',
        '0912345678',
        'thaolt@example.com'
    ),
    (
        5,
        'Đỗ Hải Phong',
        '2006-10-09',
        'Nam',
        4,
        '222 Đường JKL, Quận 5',
        '0923456789',
        'phongdh@example.com'
    ),
    (
        6,
        'Phạm Gia Hân',
        '2007-04-20',
        'Nữ',
        5,
        'Q1',
        '0933333001',
        'hanpg@example.com'
    ),
    (
        7,
        'Ngô Nhật Quang',
        '2007-09-15',
        'Nam',
        5,
        'Q2',
        '0933333002',
        'quangnn@example.com'
    ),
    (
        8,
        'Vũ Hoàng Khang',
        '2006-10-10',
        'Nam',
        6,
        'Q3',
        '0933333003',
        'khangvh@example.com'
    ),
    (
        9,
        'Trần Minh Khôi',
        '2005-12-12',
        'Nam',
        7,
        'Q5',
        '0933333004',
        'khoitm@example.com'
    ),
    (
        10,
        'Nguyễn Văn An',
        '2007-01-10',
        'Nam',
        1,
        '101 Q1',
        '0910000001',
        'hs10@edu.com'
    ),
    (
        11,
        'Trần Thị Bình',
        '2007-02-11',
        'Nữ',
        1,
        '102 Q1',
        '0910000002',
        'hs11@edu.com'
    ),
    (
        12,
        'Lê Văn Cường',
        '2007-03-12',
        'Nam',
        1,
        '103 Q1',
        '0910000003',
        'hs12@edu.com'
    ),
    (
        13,
        'Phạm Thị Dung',
        '2007-04-13',
        'Nữ',
        1,
        '104 Q1',
        '0910000004',
        'hs13@edu.com'
    ),
    (
        14,
        'Hoàng Minh Hiếu',
        '2007-05-14',
        'Nam',
        1,
        '105 Q1',
        '0910000005',
        'hs14@edu.com'
    ),
    (
        15,
        'Vũ Thu Giang',
        '2007-06-15',
        'Nữ',
        1,
        '106 Q1',
        '0910000006',
        'hs15@edu.com'
    ),
    (
        16,
        'Đặng Quốc Hùng',
        '2007-07-16',
        'Nam',
        1,
        '107 Q1',
        '0910000007',
        'hs16@edu.com'
    ),
    (
        17,
        'Bùi Thị Khánh',
        '2007-08-17',
        'Nữ',
        1,
        '108 Q1',
        '0910000008',
        'hs17@edu.com'
    ),
    (
        18,
        'Đỗ Gia Long',
        '2007-09-18',
        'Nam',
        1,
        '109 Q1',
        '0910000009',
        'hs18@edu.com'
    ),
    (
        19,
        'Ngô Phương Mai',
        '2007-10-19',
        'Nữ',
        1,
        '110 Q1',
        '0910000010',
        'hs19@edu.com'
    ),
    (
        20,
        'Dương Tấn Nam',
        '2007-11-20',
        'Nam',
        1,
        '111 Q1',
        '0910000011',
        'hs20@edu.com'
    ),
    (
        21,
        'Lý Kim Oanh',
        '2007-12-21',
        'Nữ',
        1,
        '112 Q1',
        '0910000012',
        'hs21@edu.com'
    ),
    (
        22,
        'Nguyễn Phong Phú',
        '2007-01-22',
        'Nam',
        1,
        '113 Q1',
        '0910000013',
        'hs22@edu.com'
    ),
    (
        23,
        'Trần Bảo Quân',
        '2007-02-23',
        'Nam',
        1,
        '114 Q1',
        '0910000014',
        'hs23@edu.com'
    ),
    (
        24,
        'Lê Quỳnh Như',
        '2007-03-24',
        'Nữ',
        1,
        '115 Q1',
        '0910000015',
        'hs24@edu.com'
    ),
    (
        25,
        'Phạm Văn Sơn',
        '2007-04-25',
        'Nam',
        1,
        '116 Q1',
        '0910000016',
        'hs25@edu.com'
    ),
    (
        26,
        'Hoàng Thị Thanh',
        '2007-05-26',
        'Nữ',
        1,
        '117 Q1',
        '0910000017',
        'hs26@edu.com'
    ),
    (
        27,
        'Vũ Minh Tuấn',
        '2007-06-27',
        'Nam',
        1,
        '118 Q1',
        '0910000018',
        'hs27@edu.com'
    ),
    (
        28,
        'Đặng Thùy Uyên',
        '2007-07-28',
        'Nữ',
        1,
        '119 Q1',
        '0910000019',
        'hs28@edu.com'
    ),
    (
        29,
        'Bùi Quốc Việt',
        '2007-08-29',
        'Nam',
        1,
        '120 Q1',
        '0910000020',
        'hs29@edu.com'
    ),
    (
        30,
        'Đỗ Hải Yến',
        '2007-09-30',
        'Nữ',
        1,
        '121 Q1',
        '0910000021',
        'hs30@edu.com'
    ),
    (
        31,
        'Ngô Tấn Tài',
        '2007-10-01',
        'Nam',
        1,
        '122 Q1',
        '0910000022',
        'hs31@edu.com'
    ),
    (
        32,
        'Dương Mỹ Linh',
        '2007-11-02',
        'Nữ',
        1,
        '123 Q1',
        '0910000023',
        'hs32@edu.com'
    ),
    (
        33,
        'Lý Thành Danh',
        '2007-12-03',
        'Nam',
        1,
        '124 Q1',
        '0910000024',
        'hs33@edu.com'
    ),
    (
        34,
        'Nguyễn Ngọc Hà',
        '2007-01-04',
        'Nữ',
        1,
        '125 Q1',
        '0910000025',
        'hs34@edu.com'
    ),
    (
        35,
        'Trần Công Minh',
        '2007-02-05',
        'Nam',
        1,
        '126 Q1',
        '0910000026',
        'hs35@edu.com'
    ),
    (
        36,
        'Lê Thúy Nga',
        '2007-03-06',
        'Nữ',
        1,
        '127 Q1',
        '0910000027',
        'hs36@edu.com'
    ),
    (
        37,
        'Phạm Quang Vinh',
        '2007-04-07',
        'Nam',
        1,
        '128 Q1',
        '0910000028',
        'hs37@edu.com'
    ),
    (
        38,
        'Hoàng Anh Thư',
        '2007-05-08',
        'Nữ',
        1,
        '129 Q1',
        '0910000029',
        'hs38@edu.com'
    ),
    (
        39,
        'Vũ Đức Thắng',
        '2007-06-09',
        'Nam',
        1,
        '130 Q1',
        '0910000030',
        'hs39@edu.com'
    ),
    (
        40,
        'Đặng Kiều Trinh',
        '2007-07-10',
        'Nữ',
        1,
        '131 Q1',
        '0910000031',
        'hs40@edu.com'
    ),
    (
        41,
        'Bùi Trung Kiên',
        '2007-08-11',
        'Nam',
        1,
        '132 Q1',
        '0910000032',
        'hs41@edu.com'
    ),
    (
        42,
        'Đỗ Thị Thảo',
        '2007-09-12',
        'Nữ',
        1,
        '133 Q1',
        '0910000033',
        'hs42@edu.com'
    ),
    (
        43,
        'Ngô Mạnh Hùng',
        '2007-10-13',
        'Nam',
        1,
        '134 Q1',
        '0910000034',
        'hs43@edu.com'
    ),
    (
        44,
        'Dương Lan Hương',
        '2007-11-14',
        'Nữ',
        1,
        '135 Q1',
        '0910000035',
        'hs44@edu.com'
    ),
    -- Lớp 10A2 (MaLop=2) - Thêm 35 HS (45 -> 79)
    (
        45,
        'Lý Văn Tám',
        '2007-01-15',
        'Nam',
        2,
        '201 Q2',
        '0920000001',
        'hs45@edu.com'
    ),
    (
        46,
        'Nguyễn Thu Trang',
        '2007-02-16',
        'Nữ',
        2,
        '202 Q2',
        '0920000002',
        'hs46@edu.com'
    ),
    (
        47,
        'Trần Minh Hoàng',
        '2007-03-17',
        'Nam',
        2,
        '203 Q2',
        '0920000003',
        'hs47@edu.com'
    ),
    (
        48,
        'Lê Thị Hồng',
        '2007-04-18',
        'Nữ',
        2,
        '204 Q2',
        '0920000004',
        'hs48@edu.com'
    ),
    (
        49,
        'Phạm Đức Anh',
        '2007-05-19',
        'Nam',
        2,
        '205 Q2',
        '0920000005',
        'hs49@edu.com'
    ),
    (
        50,
        'Hoàng Lan Anh',
        '2007-06-20',
        'Nữ',
        2,
        '206 Q2',
        '0920000006',
        'hs50@edu.com'
    ),
    (
        51,
        'Vũ Tuấn Kiệt',
        '2007-07-21',
        'Nam',
        2,
        '207 Q2',
        '0920000007',
        'hs51@edu.com'
    ),
    (
        52,
        'Đặng Ngọc Bích',
        '2007-08-22',
        'Nữ',
        2,
        '208 Q2',
        '0920000008',
        'hs52@edu.com'
    ),
    (
        53,
        'Bùi Quang Huy',
        '2007-09-23',
        'Nam',
        2,
        '209 Q2',
        '0920000009',
        'hs53@edu.com'
    ),
    (
        54,
        'Đỗ Thu Phương',
        '2007-10-24',
        'Nữ',
        2,
        '210 Q2',
        '0920000010',
        'hs54@edu.com'
    ),
    (
        55,
        'Ngô Đình Trọng',
        '2007-11-25',
        'Nam',
        2,
        '211 Q2',
        '0920000011',
        'hs55@edu.com'
    ),
    (
        56,
        'Dương Thùy Dương',
        '2007-12-26',
        'Nữ',
        2,
        '212 Q2',
        '0920000012',
        'hs56@edu.com'
    ),
    (
        57,
        'Lý Hoàng Nam',
        '2007-01-27',
        'Nam',
        2,
        '213 Q2',
        '0920000013',
        'hs57@edu.com'
    ),
    (
        58,
        'Nguyễn Thị Mai',
        '2007-02-28',
        'Nữ',
        2,
        '214 Q2',
        '0920000014',
        'hs58@edu.com'
    ),
    (
        59,
        'Trần Văn Long',
        '2007-03-01',
        'Nam',
        2,
        '215 Q2',
        '0920000015',
        'hs59@edu.com'
    ),
    (
        60,
        'Lê Kiều Diễm',
        '2007-04-02',
        'Nữ',
        2,
        '216 Q2',
        '0920000016',
        'hs60@edu.com'
    ),
    (
        61,
        'Phạm Xuân Trường',
        '2007-05-03',
        'Nam',
        2,
        '217 Q2',
        '0920000017',
        'hs61@edu.com'
    ),
    (
        62,
        'Hoàng Thị Kim',
        '2007-06-04',
        'Nữ',
        2,
        '218 Q2',
        '0920000018',
        'hs62@edu.com'
    ),
    (
        63,
        'Vũ Thanh Tùng',
        '2007-07-05',
        'Nam',
        2,
        '219 Q2',
        '0920000019',
        'hs63@edu.com'
    ),
    (
        64,
        'Đặng Minh Nguyệt',
        '2007-08-06',
        'Nữ',
        2,
        '220 Q2',
        '0920000020',
        'hs64@edu.com'
    ),
    (
        65,
        'Bùi Đình Phúc',
        '2007-09-07',
        'Nam',
        2,
        '221 Q2',
        '0920000021',
        'hs65@edu.com'
    ),
    (
        66,
        'Đỗ Ngọc Ánh',
        '2007-10-08',
        'Nữ',
        2,
        '222 Q2',
        '0920000022',
        'hs66@edu.com'
    ),
    (
        67,
        'Ngô Bá Quý',
        '2007-11-09',
        'Nam',
        2,
        '223 Q2',
        '0920000023',
        'hs67@edu.com'
    ),
    (
        68,
        'Dương Phương Thảo',
        '2007-12-10',
        'Nữ',
        2,
        '224 Q2',
        '0920000024',
        'hs68@edu.com'
    ),
    (
        69,
        'Lý Trung Hiếu',
        '2007-01-11',
        'Nam',
        2,
        '225 Q2',
        '0920000025',
        'hs69@edu.com'
    ),
    (
        70,
        'Nguyễn Thúy Hằng',
        '2007-02-12',
        'Nữ',
        2,
        '226 Q2',
        '0920000026',
        'hs70@edu.com'
    ),
    (
        71,
        'Trần Quang Khải',
        '2007-03-13',
        'Nam',
        2,
        '227 Q2',
        '0920000027',
        'hs71@edu.com'
    ),
    (
        72,
        'Lê Bảo Châu',
        '2007-04-14',
        'Nữ',
        2,
        '228 Q2',
        '0920000028',
        'hs72@edu.com'
    ),
    (
        73,
        'Phạm Tuấn Anh',
        '2007-05-15',
        'Nam',
        2,
        '229 Q2',
        '0920000029',
        'hs73@edu.com'
    ),
    (
        74,
        'Hoàng Mỹ Duyên',
        '2007-06-16',
        'Nữ',
        2,
        '230 Q2',
        '0920000030',
        'hs74@edu.com'
    ),
    (
        75,
        'Vũ Duy Khánh',
        '2007-07-17',
        'Nam',
        2,
        '231 Q2',
        '0920000031',
        'hs75@edu.com'
    ),
    (
        76,
        'Đặng Thị Kim',
        '2007-08-18',
        'Nữ',
        2,
        '232 Q2',
        '0920000032',
        'hs76@edu.com'
    ),
    (
        77,
        'Bùi Việt Hoàng',
        '2007-09-19',
        'Nam',
        2,
        '233 Q2',
        '0920000033',
        'hs77@edu.com'
    ),
    (
        78,
        'Đỗ Phương Anh',
        '2007-10-20',
        'Nữ',
        2,
        '234 Q2',
        '0920000034',
        'hs78@edu.com'
    ),
    (
        79,
        'Ngô Xuân Bách',
        '2007-11-21',
        'Nam',
        2,
        '235 Q2',
        '0920000035',
        'hs79@edu.com'
    ),
    -- Lớp 11A1 (MaLop=3) - Thêm 35 HS (80 -> 114)
    (
        80,
        'Dương Văn Cảnh',
        '2006-01-01',
        'Nam',
        3,
        '301 Q3',
        '0930000001',
        'hs80@edu.com'
    ),
    (
        81,
        'Lý Thảo Vy',
        '2006-02-02',
        'Nữ',
        3,
        '302 Q3',
        '0930000002',
        'hs81@edu.com'
    ),
    (
        82,
        'Nguyễn Thành Đạt',
        '2006-03-03',
        'Nam',
        3,
        '303 Q3',
        '0930000003',
        'hs82@edu.com'
    ),
    (
        83,
        'Trần Thị Hiền',
        '2006-04-04',
        'Nữ',
        3,
        '304 Q3',
        '0930000004',
        'hs83@edu.com'
    ),
    (
        84,
        'Lê Bá Lộc',
        '2006-05-05',
        'Nam',
        3,
        '305 Q3',
        '0930000005',
        'hs84@edu.com'
    ),
    (
        85,
        'Phạm Nguyệt Ánh',
        '2006-06-06',
        'Nữ',
        3,
        '306 Q3',
        '0930000006',
        'hs85@edu.com'
    ),
    (
        86,
        'Hoàng Xuân Mạnh',
        '2006-07-07',
        'Nam',
        3,
        '307 Q3',
        '0930000007',
        'hs86@edu.com'
    ),
    (
        87,
        'Vũ Thị Diệp',
        '2006-08-08',
        'Nữ',
        3,
        '308 Q3',
        '0930000008',
        'hs87@edu.com'
    ),
    (
        88,
        'Đặng Tấn Phát',
        '2006-09-09',
        'Nam',
        3,
        '309 Q3',
        '0930000009',
        'hs88@edu.com'
    ),
    (
        89,
        'Bùi Cẩm Tú',
        '2006-10-10',
        'Nữ',
        3,
        '310 Q3',
        '0930000010',
        'hs89@edu.com'
    ),
    (
        90,
        'Đỗ Thanh Tùng',
        '2006-11-11',
        'Nam',
        3,
        '311 Q3',
        '0930000011',
        'hs90@edu.com'
    ),
    (
        91,
        'Ngô Thanh Trúc',
        '2006-12-12',
        'Nữ',
        3,
        '312 Q3',
        '0930000012',
        'hs91@edu.com'
    ),
    (
        92,
        'Dương Hoàng Anh',
        '2006-01-13',
        'Nam',
        3,
        '313 Q3',
        '0930000013',
        'hs92@edu.com'
    ),
    (
        93,
        'Lý Ngọc Diệp',
        '2006-02-14',
        'Nữ',
        3,
        '314 Q3',
        '0930000014',
        'hs93@edu.com'
    ),
    (
        94,
        'Nguyễn Sỹ Hùng',
        '2006-03-15',
        'Nam',
        3,
        '315 Q3',
        '0930000015',
        'hs94@edu.com'
    ),
    (
        95,
        'Trần Hà My',
        '2006-04-16',
        'Nữ',
        3,
        '316 Q3',
        '0930000016',
        'hs95@edu.com'
    ),
    (
        96,
        'Lê Huy Hoàng',
        '2006-05-17',
        'Nam',
        3,
        '317 Q3',
        '0930000017',
        'hs96@edu.com'
    ),
    (
        97,
        'Phạm Thị Huệ',
        '2006-06-18',
        'Nữ',
        3,
        '318 Q3',
        '0930000018',
        'hs97@edu.com'
    ),
    (
        98,
        'Hoàng Trung Hiếu',
        '2006-07-19',
        'Nam',
        3,
        '319 Q3',
        '0930000019',
        'hs98@edu.com'
    ),
    (
        99,
        'Vũ Khánh Linh',
        '2006-08-20',
        'Nữ',
        3,
        '320 Q3',
        '0930000020',
        'hs99@edu.com'
    ),
    (
        100,
        'Đặng Minh Quang',
        '2006-09-21',
        'Nam',
        3,
        '321 Q3',
        '0930000021',
        'hs100@edu.com'
    ),
    (
        101,
        'Bùi Thùy Linh',
        '2006-10-22',
        'Nữ',
        3,
        '322 Q3',
        '0930000022',
        'hs101@edu.com'
    ),
    (
        102,
        'Đỗ Duy Mạnh',
        '2006-11-23',
        'Nam',
        3,
        '323 Q3',
        '0930000023',
        'hs102@edu.com'
    ),
    (
        103,
        'Ngô Lan Nhi',
        '2006-12-24',
        'Nữ',
        3,
        '324 Q3',
        '0930000024',
        'hs103@edu.com'
    ),
    (
        104,
        'Dương Tiến Dũng',
        '2006-01-25',
        'Nam',
        3,
        '325 Q3',
        '0930000025',
        'hs104@edu.com'
    ),
    (
        105,
        'Lý Thanh Thảo',
        '2006-02-26',
        'Nữ',
        3,
        '326 Q3',
        '0930000026',
        'hs105@edu.com'
    ),
    (
        106,
        'Nguyễn Văn Toàn',
        '2006-03-27',
        'Nam',
        3,
        '327 Q3',
        '0930000027',
        'hs106@edu.com'
    ),
    (
        107,
        'Trần Huyền Trang',
        '2006-04-28',
        'Nữ',
        3,
        '328 Q3',
        '0930000028',
        'hs107@edu.com'
    ),
    (
        108,
        'Lê Đình Trọng',
        '2006-05-29',
        'Nam',
        3,
        '329 Q3',
        '0930000029',
        'hs108@edu.com'
    ),
    (
        109,
        'Phạm Thu Uyên',
        '2006-06-30',
        'Nữ',
        3,
        '330 Q3',
        '0930000030',
        'hs109@edu.com'
    ),
    (
        110,
        'Hoàng Công Phượng',
        '2006-07-01',
        'Nam',
        3,
        '331 Q3',
        '0930000031',
        'hs110@edu.com'
    ),
    (
        111,
        'Vũ Ngọc Mai',
        '2006-08-02',
        'Nữ',
        3,
        '332 Q3',
        '0930000032',
        'hs111@edu.com'
    ),
    (
        112,
        'Đặng Văn Lâm',
        '2006-09-03',
        'Nam',
        3,
        '333 Q3',
        '0930000033',
        'hs112@edu.com'
    ),
    (
        113,
        'Bùi Thị Nhung',
        '2006-10-04',
        'Nữ',
        3,
        '334 Q3',
        '0930000034',
        'hs113@edu.com'
    ),
    (
        114,
        'Đỗ Hùng Dũng',
        '2006-11-05',
        'Nam',
        3,
        '335 Q3',
        '0930000035',
        'hs114@edu.com'
    ),
    -- Lớp 12A1 (MaLop=4) - Thêm 35 HS (115 -> 149)
    (
        115,
        'Ngô Văn Hậu',
        '2005-01-01',
        'Nam',
        4,
        '401 Q4',
        '0940000001',
        'hs115@edu.com'
    ),
    (
        116,
        'Dương Thị Vân',
        '2005-02-02',
        'Nữ',
        4,
        '402 Q4',
        '0940000002',
        'hs116@edu.com'
    ),
    (
        117,
        'Lý Quang Hải',
        '2005-03-03',
        'Nam',
        4,
        '403 Q4',
        '0940000003',
        'hs117@edu.com'
    ),
    (
        118,
        'Nguyễn Tuyết Dung',
        '2005-04-04',
        'Nữ',
        4,
        '404 Q4',
        '0940000004',
        'hs118@edu.com'
    ),
    (
        119,
        'Trần Văn Đức',
        '2005-05-05',
        'Nam',
        4,
        '405 Q4',
        '0940000005',
        'hs119@edu.com'
    ),
    (
        120,
        'Lê Thị Kiều',
        '2005-06-06',
        'Nữ',
        4,
        '406 Q4',
        '0940000006',
        'hs120@edu.com'
    ),
    (
        121,
        'Phạm Xuân Mạnh',
        '2005-07-07',
        'Nam',
        4,
        '407 Q4',
        '0940000007',
        'hs121@edu.com'
    ),
    (
        122,
        'Hoàng Thị Loan',
        '2005-08-08',
        'Nữ',
        4,
        '408 Q4',
        '0940000008',
        'hs122@edu.com'
    ),
    (
        123,
        'Vũ Văn Thanh',
        '2005-09-09',
        'Nam',
        4,
        '409 Q4',
        '0940000009',
        'hs123@edu.com'
    ),
    (
        124,
        'Đặng Thị Trang',
        '2005-10-10',
        'Nữ',
        4,
        '410 Q4',
        '0940000010',
        'hs124@edu.com'
    ),
    (
        125,
        'Bùi Tiến Dũng',
        '2005-11-11',
        'Nam',
        4,
        '411 Q4',
        '0940000011',
        'hs125@edu.com'
    ),
    (
        126,
        'Đỗ Thị Thúy',
        '2005-12-12',
        'Nữ',
        4,
        '412 Q4',
        '0940000012',
        'hs126@edu.com'
    ),
    (
        127,
        'Ngô Hoàng Thịnh',
        '2005-01-13',
        'Nam',
        4,
        '413 Q4',
        '0940000013',
        'hs127@edu.com'
    ),
    (
        128,
        'Dương Thảo Trang',
        '2005-02-14',
        'Nữ',
        4,
        '414 Q4',
        '0940000014',
        'hs128@edu.com'
    ),
    (
        129,
        'Lý Công Phượng',
        '2005-03-15',
        'Nam',
        4,
        '415 Q4',
        '0940000015',
        'hs129@edu.com'
    ),
    (
        130,
        'Nguyễn Thị Vạn',
        '2005-04-16',
        'Nữ',
        4,
        '416 Q4',
        '0940000016',
        'hs130@edu.com'
    ),
    (
        131,
        'Trần Hữu Thắng',
        '2005-05-17',
        'Nam',
        4,
        '417 Q4',
        '0940000017',
        'hs131@edu.com'
    ),
    (
        132,
        'Lê Ánh Dương',
        '2005-06-18',
        'Nữ',
        4,
        '418 Q4',
        '0940000018',
        'hs132@edu.com'
    ),
    (
        133,
        'Phạm Thành Lương',
        '2005-07-19',
        'Nam',
        4,
        '419 Q4',
        '0940000019',
        'hs133@edu.com'
    ),
    (
        134,
        'Hoàng Bích Thùy',
        '2005-08-20',
        'Nữ',
        4,
        '420 Q4',
        '0940000020',
        'hs134@edu.com'
    ),
    (
        135,
        'Vũ Minh Tuấn',
        '2005-09-21',
        'Nam',
        4,
        '421 Q4',
        '0940000021',
        'hs135@edu.com'
    ),
    (
        136,
        'Đặng Hải Yến',
        '2005-10-22',
        'Nữ',
        4,
        '422 Q4',
        '0940000022',
        'hs136@edu.com'
    ),
    (
        137,
        'Bùi Tấn Tài',
        '2005-11-23',
        'Nam',
        4,
        '423 Q4',
        '0940000023',
        'hs137@edu.com'
    ),
    (
        138,
        'Đỗ Thị Ngọc',
        '2005-12-24',
        'Nữ',
        4,
        '424 Q4',
        '0940000024',
        'hs138@edu.com'
    ),
    (
        139,
        'Ngô Anh Đức',
        '2005-01-25',
        'Nam',
        4,
        '425 Q4',
        '0940000025',
        'hs139@edu.com'
    ),
    (
        140,
        'Dương Cẩm Lệ',
        '2005-02-26',
        'Nữ',
        4,
        '426 Q4',
        '0940000026',
        'hs140@edu.com'
    ),
    (
        141,
        'Lý Văn Quyết',
        '2005-03-27',
        'Nam',
        4,
        '427 Q4',
        '0940000027',
        'hs141@edu.com'
    ),
    (
        142,
        'Nguyễn Thị Liễu',
        '2005-04-28',
        'Nữ',
        4,
        '428 Q4',
        '0940000028',
        'hs142@edu.com'
    ),
    (
        143,
        'Trần Nguyên Mạnh',
        '2005-05-29',
        'Nam',
        4,
        '429 Q4',
        '0940000029',
        'hs143@edu.com'
    ),
    (
        144,
        'Lê Hoài Thu',
        '2005-06-30',
        'Nữ',
        4,
        '430 Q4',
        '0940000030',
        'hs144@edu.com'
    ),
    (
        145,
        'Phạm Văn Toàn',
        '2005-07-01',
        'Nam',
        4,
        '431 Q4',
        '0940000031',
        'hs145@edu.com'
    ),
    (
        146,
        'Hoàng Thị Huỳnh',
        '2005-08-02',
        'Nữ',
        4,
        '432 Q4',
        '0940000032',
        'hs146@edu.com'
    ),
    (
        147,
        'Vũ Trọng Hoàng',
        '2005-09-03',
        'Nam',
        4,
        '433 Q4',
        '0940000033',
        'hs147@edu.com'
    ),
    (
        148,
        'Đặng Thị Thùy',
        '2005-10-04',
        'Nữ',
        4,
        '434 Q4',
        '0940000034',
        'hs148@edu.com'
    ),
    (
        149,
        'Bùi Hoàng Việt',
        '2005-11-05',
        'Nam',
        4,
        '435 Q4',
        '0940000035',
        'hs149@edu.com'
    ),
    -- Lớp 10A3 (MaLop=5) - Thêm 35 HS (150 -> 184)
    (
        150,
        'Đỗ Anh Khoa',
        '2007-01-01',
        'Nam',
        5,
        '501 Q5',
        '0950000001',
        'hs150@edu.com'
    ),
    (
        151,
        'Ngô Tố Uyên',
        '2007-02-02',
        'Nữ',
        5,
        '502 Q5',
        '0950000002',
        'hs151@edu.com'
    ),
    (
        152,
        'Dương Văn Bách',
        '2007-03-03',
        'Nam',
        5,
        '503 Q5',
        '0950000003',
        'hs152@edu.com'
    ),
    (
        153,
        'Lý Phương Tâm',
        '2007-04-04',
        'Nữ',
        5,
        '504 Q5',
        '0950000004',
        'hs153@edu.com'
    ),
    (
        154,
        'Nguyễn Đình Bảo',
        '2007-05-05',
        'Nam',
        5,
        '505 Q5',
        '0950000005',
        'hs154@edu.com'
    ),
    (
        155,
        'Trần Thanh Thúy',
        '2007-06-06',
        'Nữ',
        5,
        '506 Q5',
        '0950000006',
        'hs155@edu.com'
    ),
    (
        156,
        'Lê Quốc Cường',
        '2007-07-07',
        'Nam',
        5,
        '507 Q5',
        '0950000007',
        'hs156@edu.com'
    ),
    (
        157,
        'Phạm Khánh Ly',
        '2007-08-08',
        'Nữ',
        5,
        '508 Q5',
        '0950000008',
        'hs157@edu.com'
    ),
    (
        158,
        'Hoàng Văn Định',
        '2007-09-09',
        'Nam',
        5,
        '509 Q5',
        '0950000009',
        'hs158@edu.com'
    ),
    (
        159,
        'Vũ Thị Hồng',
        '2007-10-10',
        'Nữ',
        5,
        '510 Q5',
        '0950000010',
        'hs159@edu.com'
    ),
    (
        160,
        'Đặng Thái Sơn',
        '2007-11-11',
        'Nam',
        5,
        '511 Q5',
        '0950000011',
        'hs160@edu.com'
    ),
    (
        161,
        'Bùi Thị Thu',
        '2007-12-12',
        'Nữ',
        5,
        '512 Q5',
        '0950000012',
        'hs161@edu.com'
    ),
    (
        162,
        'Đỗ Duy Khánh',
        '2007-01-13',
        'Nam',
        5,
        '513 Q5',
        '0950000013',
        'hs162@edu.com'
    ),
    (
        163,
        'Ngô Thùy Chi',
        '2007-02-14',
        'Nữ',
        5,
        '514 Q5',
        '0950000014',
        'hs163@edu.com'
    ),
    (
        164,
        'Dương Xuân Hiếu',
        '2007-03-15',
        'Nam',
        5,
        '515 Q5',
        '0950000015',
        'hs164@edu.com'
    ),
    (
        165,
        'Lý Anh Thư',
        '2007-04-16',
        'Nữ',
        5,
        '516 Q5',
        '0950000016',
        'hs165@edu.com'
    ),
    (
        166,
        'Nguyễn Tiến Linh',
        '2007-05-17',
        'Nam',
        5,
        '517 Q5',
        '0950000017',
        'hs166@edu.com'
    ),
    (
        167,
        'Trần Phương Anh',
        '2007-06-18',
        'Nữ',
        5,
        '518 Q5',
        '0950000018',
        'hs167@edu.com'
    ),
    (
        168,
        'Lê Văn Thắng',
        '2007-07-19',
        'Nam',
        5,
        '519 Q5',
        '0950000019',
        'hs168@edu.com'
    ),
    (
        169,
        'Phạm Thị Yến',
        '2007-08-20',
        'Nữ',
        5,
        '520 Q5',
        '0950000020',
        'hs169@edu.com'
    ),
    (
        170,
        'Hoàng Đức Huy',
        '2007-09-21',
        'Nam',
        5,
        '521 Q5',
        '0950000021',
        'hs170@edu.com'
    ),
    (
        171,
        'Vũ Ngọc Ánh',
        '2007-10-22',
        'Nữ',
        5,
        '522 Q5',
        '0950000022',
        'hs171@edu.com'
    ),
    (
        172,
        'Đặng Văn Tới',
        '2007-11-23',
        'Nam',
        5,
        '523 Q5',
        '0950000023',
        'hs172@edu.com'
    ),
    (
        173,
        'Bùi Hoàng Yến',
        '2007-12-24',
        'Nữ',
        5,
        '524 Q5',
        '0950000024',
        'hs173@edu.com'
    ),
    (
        174,
        'Đỗ Thanh Thịnh',
        '2007-01-25',
        'Nam',
        5,
        '525 Q5',
        '0950000025',
        'hs174@edu.com'
    ),
    (
        175,
        'Ngô Thị Thúy',
        '2007-02-26',
        'Nữ',
        5,
        '526 Q5',
        '0950000026',
        'hs175@edu.com'
    ),
    (
        176,
        'Dương Thanh Hào',
        '2007-03-27',
        'Nam',
        5,
        '527 Q5',
        '0950000027',
        'hs176@edu.com'
    ),
    (
        177,
        'Lý Bích Thủy',
        '2007-04-28',
        'Nữ',
        5,
        '528 Q5',
        '0950000028',
        'hs177@edu.com'
    ),
    (
        178,
        'Nguyễn Trọng Hùng',
        '2007-05-29',
        'Nam',
        5,
        '529 Q5',
        '0950000029',
        'hs178@edu.com'
    ),
    (
        179,
        'Trần Thị Duyên',
        '2007-06-30',
        'Nữ',
        5,
        '530 Q5',
        '0950000030',
        'hs179@edu.com'
    ),
    (
        180,
        'Lê Sỹ Minh',
        '2007-07-01',
        'Nam',
        5,
        '531 Q5',
        '0950000031',
        'hs180@edu.com'
    ),
    (
        181,
        'Phạm Hải Yến',
        '2007-08-02',
        'Nữ',
        5,
        '532 Q5',
        '0950000032',
        'hs181@edu.com'
    ),
    (
        182,
        'Hoàng Văn Khánh',
        '2007-09-03',
        'Nam',
        5,
        '533 Q5',
        '0950000033',
        'hs182@edu.com'
    ),
    (
        183,
        'Vũ Thị Hoa',
        '2007-10-04',
        'Nữ',
        5,
        '534 Q5',
        '0950000034',
        'hs183@edu.com'
    ),
    (
        184,
        'Đặng Ngọc Tuấn',
        '2007-11-05',
        'Nam',
        5,
        '535 Q5',
        '0950000035',
        'hs184@edu.com'
    ),
    -- Lớp 11A2 (MaLop=6) - Thêm 35 HS (185 -> 219)
    (
        185,
        'Bùi Tiến Dụng',
        '2006-01-01',
        'Nam',
        6,
        '601 Q6',
        '0960000001',
        'hs185@edu.com'
    ),
    (
        186,
        'Đỗ Thị Lan',
        '2006-02-02',
        'Nữ',
        6,
        '602 Q6',
        '0960000002',
        'hs186@edu.com'
    ),
    (
        187,
        'Ngô Xuân Nam',
        '2006-03-03',
        'Nam',
        6,
        '603 Q6',
        '0960000003',
        'hs187@edu.com'
    ),
    (
        188,
        'Dương Thị Thảo',
        '2006-04-04',
        'Nữ',
        6,
        '604 Q6',
        '0960000004',
        'hs188@edu.com'
    ),
    (
        189,
        'Lý Văn Xuân',
        '2006-05-05',
        'Nam',
        6,
        '605 Q6',
        '0960000005',
        'hs189@edu.com'
    ),
    (
        190,
        'Nguyễn Thị Tuyết',
        '2006-06-06',
        'Nữ',
        6,
        '606 Q6',
        '0960000006',
        'hs190@edu.com'
    ),
    (
        191,
        'Trần Danh Trung',
        '2006-07-07',
        'Nam',
        6,
        '607 Q6',
        '0960000007',
        'hs191@edu.com'
    ),
    (
        192,
        'Lê Thị Huệ',
        '2006-08-08',
        'Nữ',
        6,
        '608 Q6',
        '0960000008',
        'hs192@edu.com'
    ),
    (
        193,
        'Phạm Văn Luân',
        '2006-09-09',
        'Nam',
        6,
        '609 Q6',
        '0960000009',
        'hs193@edu.com'
    ),
    (
        194,
        'Hoàng Thị Mơ',
        '2006-10-10',
        'Nữ',
        6,
        '610 Q6',
        '0960000010',
        'hs194@edu.com'
    ),
    (
        195,
        'Vũ Viết Triều',
        '2006-11-11',
        'Nam',
        6,
        '611 Q6',
        '0960000011',
        'hs195@edu.com'
    ),
    (
        196,
        'Đặng Thị Lành',
        '2006-12-12',
        'Nữ',
        6,
        '612 Q6',
        '0960000012',
        'hs196@edu.com'
    ),
    (
        197,
        'Bùi Anh Tuấn',
        '2006-01-13',
        'Nam',
        6,
        '613 Q6',
        '0960000013',
        'hs197@edu.com'
    ),
    (
        198,
        'Đỗ Thị Hiền',
        '2006-02-14',
        'Nữ',
        6,
        '614 Q6',
        '0960000014',
        'hs198@edu.com'
    ),
    (
        199,
        'Ngô Tùng Quốc',
        '2006-03-15',
        'Nam',
        6,
        '615 Q6',
        '0960000015',
        'hs199@edu.com'
    ),
    (
        200,
        'Dương Thị Hằng',
        '2006-04-16',
        'Nữ',
        6,
        '616 Q6',
        '0960000016',
        'hs200@edu.com'
    ),
    (
        201,
        'Lý Trung Hiếu',
        '2006-05-17',
        'Nam',
        6,
        '617 Q6',
        '0960000017',
        'hs201@edu.com'
    ),
    (
        202,
        'Nguyễn Thị Nga',
        '2006-06-18',
        'Nữ',
        6,
        '618 Q6',
        '0960000018',
        'hs202@edu.com'
    ),
    (
        203,
        'Trần Bảo Toàn',
        '2006-07-19',
        'Nam',
        6,
        '619 Q6',
        '0960000019',
        'hs203@edu.com'
    ),
    (
        204,
        'Lê Thị Diễm',
        '2006-08-20',
        'Nữ',
        6,
        '620 Q6',
        '0960000020',
        'hs204@edu.com'
    ),
    (
        205,
        'Phạm Đức Thông',
        '2006-09-21',
        'Nam',
        6,
        '621 Q6',
        '0960000021',
        'hs205@edu.com'
    ),
    (
        206,
        'Hoàng Thị Thùy',
        '2006-10-22',
        'Nữ',
        6,
        '622 Q6',
        '0960000022',
        'hs206@edu.com'
    ),
    (
        207,
        'Vũ Hồng Quân',
        '2006-11-23',
        'Nam',
        6,
        '623 Q6',
        '0960000023',
        'hs207@edu.com'
    ),
    (
        208,
        'Đặng Thị Nga',
        '2006-12-24',
        'Nữ',
        6,
        '624 Q6',
        '0960000024',
        'hs208@edu.com'
    ),
    (
        209,
        'Bùi Đức Lợi',
        '2006-01-25',
        'Nam',
        6,
        '625 Q6',
        '0960000025',
        'hs209@edu.com'
    ),
    (
        210,
        'Đỗ Thị Minh',
        '2006-02-26',
        'Nữ',
        6,
        '626 Q6',
        '0960000026',
        'hs210@edu.com'
    ),
    (
        211,
        'Ngô Hoàng Anh',
        '2006-03-27',
        'Nam',
        6,
        '627 Q6',
        '0960000027',
        'hs211@edu.com'
    ),
    (
        212,
        'Dương Thị Kim',
        '2006-04-28',
        'Nữ',
        6,
        '628 Q6',
        '0960000028',
        'hs212@edu.com'
    ),
    (
        213,
        'Lý Hùng Cường',
        '2006-05-29',
        'Nam',
        6,
        '629 Q6',
        '0960000029',
        'hs213@edu.com'
    ),
    (
        214,
        'Nguyễn Thị Thu',
        '2006-06-30',
        'Nữ',
        6,
        '630 Q6',
        '0960000030',
        'hs214@edu.com'
    ),
    (
        215,
        'Trần Đình Khương',
        '2006-07-01',
        'Nam',
        6,
        '631 Q6',
        '0960000031',
        'hs215@edu.com'
    ),
    (
        216,
        'Lê Thị Mai',
        '2006-08-02',
        'Nữ',
        6,
        '632 Q6',
        '0960000032',
        'hs216@edu.com'
    ),
    (
        217,
        'Phạm Xuân Hưng',
        '2006-09-03',
        'Nam',
        6,
        '633 Q6',
        '0960000033',
        'hs217@edu.com'
    ),
    (
        218,
        'Hoàng Thị Hồng',
        '2006-10-04',
        'Nữ',
        6,
        '634 Q6',
        '0960000034',
        'hs218@edu.com'
    ),
    (
        219,
        'Vũ Tiến Long',
        '2006-11-05',
        'Nam',
        6,
        '635 Q6',
        '0960000035',
        'hs219@edu.com'
    ),
    -- Lớp 12A2 (MaLop=7) - Thêm 35 HS (220 -> 254)
    (
        220,
        'Đặng Văn Tùng',
        '2005-01-01',
        'Nam',
        7,
        '701 Q7',
        '0970000001',
        'hs220@edu.com'
    ),
    (
        221,
        'Bùi Thị Tuyết',
        '2005-02-02',
        'Nữ',
        7,
        '702 Q7',
        '0970000002',
        'hs221@edu.com'
    ),
    (
        222,
        'Đỗ Văn Thuận',
        '2005-03-03',
        'Nam',
        7,
        '703 Q7',
        '0970000003',
        'hs222@edu.com'
    ),
    (
        223,
        'Ngô Thị Thùy',
        '2005-04-04',
        'Nữ',
        7,
        '704 Q7',
        '0970000004',
        'hs223@edu.com'
    ),
    (
        224,
        'Dương Văn Trung',
        '2005-05-05',
        'Nam',
        7,
        '705 Q7',
        '0970000005',
        'hs224@edu.com'
    ),
    (
        225,
        'Lý Thị Lệ',
        '2005-06-06',
        'Nữ',
        7,
        '706 Q7',
        '0970000006',
        'hs225@edu.com'
    ),
    (
        226,
        'Nguyễn Hữu Dũng',
        '2005-07-07',
        'Nam',
        7,
        '707 Q7',
        '0970000007',
        'hs226@edu.com'
    ),
    (
        227,
        'Trần Thị Kim',
        '2005-08-08',
        'Nữ',
        7,
        '708 Q7',
        '0970000008',
        'hs227@edu.com'
    ),
    (
        228,
        'Lê Văn Đô',
        '2005-09-09',
        'Nam',
        7,
        '709 Q7',
        '0970000009',
        'hs228@edu.com'
    ),
    (
        229,
        'Phạm Thị Hằng',
        '2005-10-10',
        'Nữ',
        7,
        '710 Q7',
        '0970000010',
        'hs229@edu.com'
    ),
    (
        230,
        'Hoàng Xuân Tân',
        '2005-11-11',
        'Nam',
        7,
        '711 Q7',
        '0970000011',
        'hs230@edu.com'
    ),
    (
        231,
        'Vũ Thị Thúy',
        '2005-12-12',
        'Nữ',
        7,
        '712 Q7',
        '0970000012',
        'hs231@edu.com'
    ),
    (
        232,
        'Đặng Quang Huy',
        '2005-01-13',
        'Nam',
        7,
        '713 Q7',
        '0970000013',
        'hs232@edu.com'
    ),
    (
        233,
        'Bùi Thị Trang',
        '2005-02-14',
        'Nữ',
        7,
        '714 Q7',
        '0970000014',
        'hs233@edu.com'
    ),
    (
        234,
        'Đỗ Thanh Hậu',
        '2005-03-15',
        'Nam',
        7,
        '715 Q7',
        '0970000015',
        'hs234@edu.com'
    ),
    (
        235,
        'Ngô Thị Hường',
        '2005-04-16',
        'Nữ',
        7,
        '716 Q7',
        '0970000016',
        'hs235@edu.com'
    ),
    (
        236,
        'Dương Văn Lắm',
        '2005-05-17',
        'Nam',
        7,
        '717 Q7',
        '0970000017',
        'hs236@edu.com'
    ),
    (
        237,
        'Lý Thị Dịu',
        '2005-06-18',
        'Nữ',
        7,
        '718 Q7',
        '0970000018',
        'hs237@edu.com'
    ),
    (
        238,
        'Nguyễn Trọng Đại',
        '2005-07-19',
        'Nam',
        7,
        '719 Q7',
        '0970000019',
        'hs238@edu.com'
    ),
    (
        239,
        'Trần Thị Thu',
        '2005-08-20',
        'Nữ',
        7,
        '720 Q7',
        '0970000020',
        'hs239@edu.com'
    ),
    (
        240,
        'Lê Xuân Tú',
        '2005-09-21',
        'Nam',
        7,
        '721 Q7',
        '0970000021',
        'hs240@edu.com'
    ),
    (
        241,
        'Phạm Thị Tươi',
        '2005-10-22',
        'Nữ',
        7,
        '722 Q7',
        '0970000022',
        'hs241@edu.com'
    ),
    (
        242,
        'Hoàng Anh Tuấn',
        '2005-11-23',
        'Nam',
        7,
        '723 Q7',
        '0970000023',
        'hs242@edu.com'
    ),
    (
        243,
        'Vũ Thị Nhung',
        '2005-12-24',
        'Nữ',
        7,
        '724 Q7',
        '0970000024',
        'hs243@edu.com'
    ),
    (
        244,
        'Đặng Văn Lâm',
        '2005-01-25',
        'Nam',
        7,
        '725 Q7',
        '0970000025',
        'hs244@edu.com'
    ),
    (
        245,
        'Bùi Thị Hạnh',
        '2005-02-26',
        'Nữ',
        7,
        '726 Q7',
        '0970000026',
        'hs245@edu.com'
    ),
    (
        246,
        'Đỗ Duy Nam',
        '2005-03-27',
        'Nam',
        7,
        '727 Q7',
        '0970000027',
        'hs246@edu.com'
    ),
    (
        247,
        'Ngô Thị Thắm',
        '2005-04-28',
        'Nữ',
        7,
        '728 Q7',
        '0970000028',
        'hs247@edu.com'
    ),
    (
        248,
        'Dương Văn Hào',
        '2005-05-29',
        'Nam',
        7,
        '729 Q7',
        '0970000029',
        'hs248@edu.com'
    ),
    (
        249,
        'Lý Thị Chinh',
        '2005-06-30',
        'Nữ',
        7,
        '730 Q7',
        '0970000030',
        'hs249@edu.com'
    ),
    (
        250,
        'Nguyễn Thành Chung',
        '2005-07-01',
        'Nam',
        7,
        '731 Q7',
        '0970000031',
        'hs250@edu.com'
    ),
    (
        251,
        'Trần Thị Hồng',
        '2005-08-02',
        'Nữ',
        7,
        '732 Q7',
        '0970000032',
        'hs251@edu.com'
    ),
    (
        252,
        'Lê Trọng Hóa',
        '2005-09-03',
        'Nam',
        7,
        '733 Q7',
        '0970000033',
        'hs252@edu.com'
    ),
    (
        253,
        'Phạm Thị Huê',
        '2005-10-04',
        'Nữ',
        7,
        '734 Q7',
        '0970000034',
        'hs253@edu.com'
    ),
    (
        254,
        'Hoàng Văn Toản',
        '2005-11-05',
        'Nam',
        7,
        '735 Q7',
        '0970000035',
        'hs254@edu.com'
    );

INSERT INTO
    MonHoc (MaMon, TenMon, SoTiet, GhiChu)
VALUES (101, 'Toán', 90, NULL),
    (102, 'Văn', 90, NULL),
    (103, 'Anh', 90, NULL),
    (104, 'Vật lý', 70, NULL),
    (
        105,
        'Hóa học',
        70,
        'Bắt đầu từ khối 8'
    ),
    (106, 'Sinh học', 70, NULL),
    (107, 'Lịch sử', 70, NULL),
    (108, 'Địa lý', 70, NULL),
    (
        109,
        'Giáo dục công dân',
        52,
        'GDCD'
    ),
    (110, 'Tin học', 70, NULL),
    (111, 'Công nghệ', 70, NULL),
    (
        112,
        'Thể dục',
        70,
        'Giáo dục thể chất'
    );

INSERT INTO
    NienKhoa (MaNK, NamBatDau, NamKetThuc)
VALUES (1, 2024, 2025);

INSERT INTO
    Diem (
        MaHS,
        MaMon,
        HocKy,
        MaNK,
        DiemMieng,
        Diem15p,
        DiemGiuaKy,
        DiemCuoiKy
    )
VALUES
    -- HS 10 (Lớp 1)
    (
        1,
        101,
        1,
        1,
        8.5,
        7.0,
        8.0,
        8.5
    ),
    (
        2,
        102,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        3,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        9.0
    ),
    -- 10A3 (MaLop=5), học sinh 6 và 7
    (
        6,
        101,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        6,
        102,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.0
    ),
    (
        7,
        101,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        7,
        102,
        1,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        6,
        101,
        2,
        1,
        8.5,
        8.0,
        8.0,
        9.0
    ),
    (
        6,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        7,
        101,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        7,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        8,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        8.0
    ),
    (
        8,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        8,
        103,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        8,
        101,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        8,
        102,
        2,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        8,
        103,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        9,
        101,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        103,
        1,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        9,
        101,
        2,
        1,
        8.5,
        8.5,
        8.0,
        9.0
    ),
    (
        9,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        103,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        10,
        101,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        10,
        101,
        2,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        10,
        102,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        10,
        102,
        2,
        1,
        7.5,
        7.5,
        8.0,
        8.0
    ),
    (
        10,
        103,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.5
    ),
    (
        10,
        103,
        2,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        10,
        104,
        1,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        10,
        104,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    (
        10,
        105,
        1,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        10,
        105,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        10,
        106,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        10,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    -- HS 11 (Lớp 1)
    (
        11,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        11,
        101,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        11,
        102,
        1,
        1,
        8.0,
        8.5,
        8.0,
        8.0
    ),
    (
        11,
        102,
        2,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        11,
        103,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        11,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        11,
        104,
        1,
        1,
        7.0,
        7.0,
        6.5,
        7.0
    ),
    (
        11,
        104,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.5
    ),
    (
        11,
        105,
        1,
        1,
        6.5,
        7.0,
        7.0,
        7.0
    ),
    (
        11,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        11,
        106,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        11,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    -- HS 12 (Lớp 1)
    (
        12,
        101,
        1,
        1,
        6.0,
        6.5,
        6.0,
        7.0
    ),
    (
        12,
        101,
        2,
        1,
        6.5,
        6.0,
        7.0,
        7.0
    ),
    (
        12,
        102,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        12,
        102,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        12,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        12,
        103,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        12,
        104,
        1,
        1,
        5.0,
        5.5,
        6.0,
        6.0
    ),
    (
        12,
        104,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        12,
        105,
        1,
        1,
        6.0,
        6.0,
        6.5,
        7.0
    ),
    (
        12,
        105,
        2,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        12,
        106,
        1,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        12,
        106,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    -- HS 13 (Lớp 1)
    (
        13,
        101,
        1,
        1,
        8.0,
        8.5,
        8.0,
        8.0
    ),
    (
        13,
        101,
        2,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        13,
        102,
        1,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        13,
        102,
        2,
        1,
        8.0,
        8.5,
        9.0,
        9.0
    ),
    (
        13,
        103,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        13,
        103,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        13,
        104,
        1,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        13,
        104,
        2,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        13,
        105,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        13,
        105,
        2,
        1,
        7.0,
        7.5,
        8.0,
        8.0
    ),
    (
        13,
        106,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        13,
        106,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    -- HS 14 (Lớp 1)
    (
        14,
        101,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.0
    ),
    (
        14,
        101,
        2,
        1,
        8.5,
        9.0,
        9.0,
        9.5
    ),
    (
        14,
        102,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        14,
        102,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        14,
        103,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        14,
        103,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        14,
        104,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        14,
        104,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        14,
        105,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        14,
        105,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        14,
        106,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        14,
        106,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    -- ... (Tiếp tục cho HS 15 đến 44 - Lớp 1)
    (
        15,
        101,
        1,
        1,
        6.5,
        7.0,
        6.5,
        7.0
    ),
    (
        15,
        101,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.5
    ),
    (
        15,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        15,
        102,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    ),
    (
        15,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        15,
        103,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        15,
        104,
        1,
        1,
        6.0,
        6.0,
        6.0,
        6.5
    ),
    (
        15,
        104,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        15,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        15,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        15,
        106,
        1,
        1,
        7.5,
        7.5,
        7.0,
        7.5
    ),
    (
        15,
        106,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        16,
        101,
        1,
        1,
        5.0,
        5.5,
        5.0,
        6.0
    ),
    (
        16,
        101,
        2,
        1,
        5.5,
        5.0,
        6.0,
        6.0
    ),
    (
        16,
        102,
        1,
        1,
        6.0,
        6.0,
        6.5,
        6.0
    ),
    (
        16,
        102,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        16,
        103,
        1,
        1,
        5.5,
        6.0,
        5.5,
        6.0
    ),
    (
        16,
        103,
        2,
        1,
        6.0,
        5.5,
        6.0,
        6.5
    ),
    (
        16,
        104,
        1,
        1,
        4.0,
        4.5,
        5.0,
        5.0
    ),
    (
        16,
        104,
        2,
        1,
        4.5,
        5.0,
        5.0,
        5.5
    ),
    (
        16,
        105,
        1,
        1,
        5.0,
        5.0,
        5.0,
        5.0
    ),
    (
        16,
        105,
        2,
        1,
        5.0,
        5.0,
        5.5,
        5.0
    ),
    (
        16,
        106,
        1,
        1,
        6.0,
        5.5,
        6.0,
        6.0
    ),
    (
        16,
        106,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        17,
        101,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        17,
        101,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        17,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        17,
        102,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        17,
        103,
        1,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        17,
        103,
        2,
        1,
        9.0,
        8.5,
        9.0,
        9.5
    ),
    (
        17,
        104,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        17,
        104,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        17,
        105,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        17,
        105,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.0
    ),
    (
        17,
        106,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        17,
        106,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    ),
    (
        18,
        101,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        18,
        101,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        18,
        102,
        1,
        1,
        6.5,
        7.0,
        6.5,
        7.0
    ),
    (
        18,
        102,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        18,
        103,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        18,
        103,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        18,
        104,
        1,
        1,
        6.0,
        6.0,
        6.5,
        6.0
    ),
    (
        18,
        104,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        18,
        105,
        1,
        1,
        6.5,
        6.0,
        6.5,
        6.5
    ),
    (
        18,
        105,
        2,
        1,
        6.0,
        6.5,
        6.5,
        7.0
    ),
    (
        18,
        106,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        18,
        106,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        19,
        101,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        19,
        101,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        19,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        19,
        102,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    ),
    (
        19,
        103,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        19,
        103,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        19,
        104,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        19,
        104,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        19,
        105,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        19,
        105,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        19,
        106,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        19,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    -- ... (Giả sử đã thêm đủ 35 HS cho Lớp 1)
    (
        44,
        101,
        1,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        44,
        101,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        44,
        102,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        44,
        102,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        44,
        103,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        44,
        103,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        44,
        104,
        1,
        1,
        6.5,
        7.0,
        6.5,
        7.0
    ),
    (
        44,
        104,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        44,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        44,
        105,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        44,
        106,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        44,
        106,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    -- HS 45 (Lớp 2)
    (
        45,
        101,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        45,
        101,
        2,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        45,
        102,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        45,
        102,
        2,
        1,
        7.5,
        7.5,
        8.0,
        8.0
    ),
    (
        45,
        103,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.5
    ),
    (
        45,
        103,
        2,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        45,
        104,
        1,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        45,
        104,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    (
        45,
        105,
        1,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        45,
        105,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        45,
        106,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        45,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    -- HS 46 (Lớp 2)
    (
        46,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        46,
        101,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        46,
        102,
        1,
        1,
        8.0,
        8.5,
        8.0,
        8.0
    ),
    (
        46,
        102,
        2,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        46,
        103,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        46,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        46,
        104,
        1,
        1,
        7.0,
        7.0,
        6.5,
        7.0
    ),
    (
        46,
        104,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.5
    ),
    (
        46,
        105,
        1,
        1,
        6.5,
        7.0,
        7.0,
        7.0
    ),
    (
        46,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        46,
        106,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        46,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    -- HS 47 (Lớp 2)
    (
        47,
        101,
        1,
        1,
        6.0,
        6.5,
        6.0,
        7.0
    ),
    (
        47,
        101,
        2,
        1,
        6.5,
        6.0,
        7.0,
        7.0
    ),
    (
        47,
        102,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        47,
        102,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        47,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        47,
        103,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        47,
        104,
        1,
        1,
        5.0,
        5.5,
        6.0,
        6.0
    ),
    (
        47,
        104,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        47,
        105,
        1,
        1,
        6.0,
        6.0,
        6.5,
        7.0
    ),
    (
        47,
        105,
        2,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        47,
        106,
        1,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        47,
        106,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    -- ... (Tiếp tục cho HS 48 đến 79 - Lớp 2)
    (
        79,
        101,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        79,
        101,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        79,
        102,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        79,
        102,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        79,
        103,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        79,
        103,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        79,
        104,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        79,
        104,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        79,
        105,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        79,
        105,
        2,
        1,
        7.0,
        7.5,
        8.0,
        8.0
    ),
    (
        79,
        106,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        79,
        106,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    -- HS 80 (Lớp 3)
    (
        80,
        101,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.0
    ),
    (
        80,
        101,
        2,
        1,
        8.5,
        9.0,
        9.0,
        9.5
    ),
    (
        80,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        80,
        102,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    ),
    (
        80,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        80,
        103,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        80,
        104,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        80,
        104,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        80,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        80,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        80,
        106,
        1,
        1,
        7.5,
        7.5,
        7.0,
        7.5
    ),
    (
        80,
        106,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    -- ... (Tiếp tục cho HS 81 đến 114 - Lớp 3)
    (
        114,
        101,
        1,
        1,
        6.0,
        6.0,
        6.5,
        6.0
    ),
    (
        114,
        101,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        114,
        102,
        1,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        114,
        102,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    (
        114,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        114,
        103,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        114,
        104,
        1,
        1,
        5.0,
        5.0,
        5.0,
        5.0
    ),
    (
        114,
        104,
        2,
        1,
        5.0,
        5.0,
        5.5,
        5.0
    ),
    (
        114,
        105,
        1,
        1,
        6.0,
        5.5,
        6.0,
        6.0
    ),
    (
        114,
        105,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        114,
        106,
        1,
        1,
        6.5,
        6.0,
        6.5,
        6.5
    ),
    (
        114,
        106,
        2,
        1,
        6.0,
        6.5,
        6.5,
        7.0
    ),
    -- HS 115 (Lớp 4)
    (
        115,
        101,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        115,
        101,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        115,
        102,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        115,
        102,
        2,
        1,
        7.0,
        7.5,
        8.0,
        8.0
    ),
    (
        115,
        103,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        115,
        103,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        115,
        104,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        115,
        104,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        115,
        105,
        1,
        1,
        7.5,
        7.5,
        7.0,
        7.5
    ),
    (
        115,
        105,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        115,
        106,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        115,
        106,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    -- ... (Tiếp tục cho HS 116 đến 149 - Lớp 4)
    (
        149,
        101,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        149,
        101,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        149,
        102,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        149,
        102,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        149,
        103,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        149,
        103,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        149,
        104,
        1,
        1,
        6.0,
        6.5,
        6.0,
        6.0
    ),
    (
        149,
        104,
        2,
        1,
        6.5,
        6.0,
        6.0,
        6.5
    ),
    (
        149,
        105,
        1,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        149,
        105,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.0
    ),
    (
        149,
        106,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        149,
        106,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    -- HS 150 (Lớp 5)
    (
        150,
        101,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        150,
        101,
        2,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        150,
        102,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        150,
        102,
        2,
        1,
        7.5,
        7.5,
        8.0,
        8.0
    ),
    (
        150,
        103,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.5
    ),
    (
        150,
        103,
        2,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        150,
        104,
        1,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        150,
        104,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    (
        150,
        105,
        1,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        150,
        105,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        150,
        106,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        150,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    -- ... (Tiếp tục cho HS 151 đến 184 - Lớp 5)
    (
        184,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        184,
        101,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        184,
        102,
        1,
        1,
        8.0,
        8.5,
        8.0,
        8.0
    ),
    (
        184,
        102,
        2,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        184,
        103,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        184,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        184,
        104,
        1,
        1,
        7.0,
        7.0,
        6.5,
        7.0
    ),
    (
        184,
        104,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.5
    ),
    (
        184,
        105,
        1,
        1,
        6.5,
        7.0,
        7.0,
        7.0
    ),
    (
        184,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        184,
        106,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        184,
        106,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    -- HS 185 (Lớp 6)
    (
        185,
        101,
        1,
        1,
        6.0,
        6.5,
        6.0,
        7.0
    ),
    (
        185,
        101,
        2,
        1,
        6.5,
        6.0,
        7.0,
        7.0
    ),
    (
        185,
        102,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        185,
        102,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        185,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        185,
        103,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        185,
        104,
        1,
        1,
        5.0,
        5.5,
        6.0,
        6.0
    ),
    (
        185,
        104,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        185,
        105,
        1,
        1,
        6.0,
        6.0,
        6.5,
        7.0
    ),
    (
        185,
        105,
        2,
        1,
        6.0,
        6.5,
        7.0,
        7.0
    ),
    (
        185,
        106,
        1,
        1,
        7.0,
        6.5,
        7.0,
        7.0
    ),
    (
        185,
        106,
        2,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    -- ... (Tiếp tục cho HS 186 đến 219 - Lớp 6)
    (
        219,
        101,
        1,
        1,
        8.0,
        8.5,
        8.0,
        8.0
    ),
    (
        219,
        101,
        2,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        219,
        102,
        1,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        219,
        102,
        2,
        1,
        8.0,
        8.5,
        9.0,
        9.0
    ),
    (
        219,
        103,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        219,
        103,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        219,
        104,
        1,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        219,
        104,
        2,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        219,
        105,
        1,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        219,
        105,
        2,
        1,
        7.0,
        7.5,
        8.0,
        8.0
    ),
    (
        219,
        106,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        219,
        106,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    -- HS 220 (Lớp 7)
    (
        220,
        101,
        1,
        1,
        9.0,
        8.5,
        9.0,
        9.0
    ),
    (
        220,
        101,
        2,
        1,
        8.5,
        9.0,
        9.0,
        9.5
    ),
    (
        220,
        102,
        1,
        1,
        7.5,
        8.0,
        7.5,
        8.0
    ),
    (
        220,
        102,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        220,
        103,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        220,
        103,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        220,
        104,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        220,
        104,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        220,
        105,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        220,
        105,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        220,
        106,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        220,
        106,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    -- ... (Tiếp tục cho HS 221 đến 254 - Lớp 7)
    (
        221,
        101,
        1,
        1,
        6.5,
        7.0,
        6.5,
        7.0
    ),
    (
        221,
        101,
        2,
        1,
        7.0,
        6.5,
        7.0,
        7.5
    ),
    (
        221,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        221,
        102,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    ),
    (
        221,
        103,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        221,
        103,
        2,
        1,
        7.0,
        7.5,
        7.0,
        7.5
    ),
    (
        221,
        104,
        1,
        1,
        6.0,
        6.0,
        6.0,
        6.5
    ),
    (
        221,
        104,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        221,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.0
    ),
    (
        221,
        105,
        2,
        1,
        7.0,
        7.0,
        7.5,
        7.0
    ),
    (
        221,
        106,
        1,
        1,
        7.5,
        7.5,
        7.0,
        7.5
    ),
    (
        221,
        106,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        222,
        101,
        1,
        1,
        5.0,
        5.5,
        5.0,
        6.0
    ),
    (
        222,
        101,
        2,
        1,
        5.5,
        5.0,
        6.0,
        6.0
    ),
    (
        222,
        102,
        1,
        1,
        6.0,
        6.0,
        6.5,
        6.0
    ),
    (
        222,
        102,
        2,
        1,
        6.0,
        6.5,
        6.0,
        6.5
    ),
    (
        222,
        103,
        1,
        1,
        5.5,
        6.0,
        5.5,
        6.0
    ),
    (
        222,
        103,
        2,
        1,
        6.0,
        5.5,
        6.0,
        6.5
    ),
    (
        222,
        104,
        1,
        1,
        4.0,
        4.5,
        5.0,
        5.0
    ),
    (
        222,
        104,
        2,
        1,
        4.5,
        5.0,
        5.0,
        5.5
    ),
    (
        222,
        105,
        1,
        1,
        5.0,
        5.0,
        5.0,
        5.0
    ),
    (
        222,
        105,
        2,
        1,
        5.0,
        5.0,
        5.5,
        5.0
    ),
    (
        222,
        106,
        1,
        1,
        6.0,
        5.5,
        6.0,
        6.0
    ),
    (
        222,
        106,
        2,
        1,
        5.5,
        6.0,
        6.0,
        6.5
    ),
    (
        254,
        101,
        1,
        1,
        8.5,
        8.0,
        8.5,
        8.5
    ),
    (
        254,
        101,
        2,
        1,
        8.0,
        8.5,
        8.5,
        9.0
    ),
    (
        254,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        254,
        102,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        254,
        103,
        1,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        254,
        103,
        2,
        1,
        9.0,
        8.5,
        9.0,
        9.5
    ),
    (
        254,
        104,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.5
    ),
    (
        254,
        104,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        254,
        105,
        1,
        1,
        8.0,
        7.5,
        8.0,
        8.0
    ),
    (
        254,
        105,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.0
    ),
    (
        254,
        106,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        254,
        106,
        2,
        1,
        8.0,
        8.5,
        8.0,
        8.5
    );

INSERT INTO
    PhuHuynh (
        MaPH,
        HoTen,
        SoDienThoai,
        Email,
        DiaChi
    )
VALUES (
        1,
        'Bà Nguyễn Thị Lan',
        '0899999000',
        'phlan@example.com',
        'Địa chỉ A'
    ),
    (
        2,
        'Ông Trần Văn Hùng',
        '0899999111',
        'phhung@example.com',
        'Địa chỉ B'
    ),
    (
        3,
        'Bà Lê Thị Xuân',
        '0935555001',
        'xuanlt@example.com',
        'Q1'
    ),
    (
        4,
        'Ông Phan Văn Tài',
        '0935555002',
        'taipv@example.com',
        'Q2'
    ),
    (
        5,
        'Bà Nguyễn Mỹ An',
        '0935555003',
        'anmy@example.com',
        'Q3'
    ),
    (
        6,
        'Phụ huynh (PH6)',
        '0900002006',
        'ph6@example.com',
        'Địa chỉ PH 6'
    ),
    (
        7,
        'Phụ huynh (PH7)',
        '0900002007',
        'ph7@example.com',
        'Địa chỉ PH 7'
    ),
    (
        8,
        'Phụ huynh (PH8)',
        '0900002008',
        'ph8@example.com',
        'Địa chỉ PH 8'
    ),
    (
        9,
        'Phụ huynh (PH9)',
        '0900002009',
        'ph9@example.com',
        'Địa chỉ PH 9'
    ),
    (
        10,
        'Phụ huynh (PH10)',
        '0900002010',
        'ph10@example.com',
        'Địa chỉ PH 10'
    ),
    (
        11,
        'Phụ huynh (PH11)',
        '0900002011',
        'ph11@example.com',
        'Địa chỉ PH 11'
    ),
    (
        12,
        'Phụ huynh (PH12)',
        '0900002012',
        'ph12@example.com',
        'Địa chỉ PH 12'
    ),
    (
        13,
        'Phụ huynh (PH13)',
        '0900002013',
        'ph13@example.com',
        'Địa chỉ PH 13'
    ),
    (
        14,
        'Phụ huynh (PH14)',
        '0900002014',
        'ph14@example.com',
        'Địa chỉ PH 14'
    ),
    (
        15,
        'Phụ huynh (PH15)',
        '0900002015',
        'ph15@example.com',
        'Địa chỉ PH 15'
    ),
    (
        16,
        'Phụ huynh (PH16)',
        '0900002016',
        'ph16@example.com',
        'Địa chỉ PH 16'
    ),
    (
        17,
        'Phụ huynh (PH17)',
        '0900002017',
        'ph17@example.com',
        'Địa chỉ PH 17'
    ),
    (
        18,
        'Phụ huynh (PH18)',
        '0900002018',
        'ph18@example.com',
        'Địa chỉ PH 18'
    ),
    (
        19,
        'Phụ huynh (PH19)',
        '0900002019',
        'ph19@example.com',
        'Địa chỉ PH 19'
    ),
    (
        20,
        'Phụ huynh (PH20)',
        '0900002020',
        'ph20@example.com',
        'Địa chỉ PH 20'
    ),
    (
        21,
        'Phụ huynh (PH21)',
        '0900002021',
        'ph21@example.com',
        'Địa chỉ PH 21'
    ),
    (
        22,
        'Phụ huynh (PH22)',
        '0900002022',
        'ph22@example.com',
        'Địa chỉ PH 22'
    ),
    (
        23,
        'Phụ huynh (PH23)',
        '0900002023',
        'ph23@example.com',
        'Địa chỉ PH 23'
    ),
    (
        24,
        'Phụ huynh (PH24)',
        '0900002024',
        'ph24@example.com',
        'Địa chỉ PH 24'
    ),
    (
        25,
        'Phụ huynh (PH25)',
        '0900002025',
        'ph25@example.com',
        'Địa chỉ PH 25'
    ),
    (
        26,
        'Phụ huynh (PH26)',
        '0900002026',
        'ph26@example.com',
        'Địa chỉ PH 26'
    ),
    (
        27,
        'Phụ huynh (PH27)',
        '0900002027',
        'ph27@example.com',
        'Địa chỉ PH 27'
    ),
    (
        28,
        'Phụ huynh (PH28)',
        '0900002028',
        'ph28@example.com',
        'Địa chỉ PH 28'
    ),
    (
        29,
        'Phụ huynh (PH29)',
        '0900002029',
        'ph29@example.com',
        'Địa chỉ PH 29'
    ),
    (
        30,
        'Phụ huynh (PH30)',
        '0900002030',
        'ph30@example.com',
        'Địa chỉ PH 30'
    ),
    (
        31,
        'Phụ huynh (PH31)',
        '0900002031',
        'ph31@example.com',
        'Địa chỉ PH 31'
    ),
    (
        32,
        'Phụ huynh (PH32)',
        '0900002032',
        'ph32@example.com',
        'Địa chỉ PH 32'
    ),
    (
        33,
        'Phụ huynh (PH33)',
        '0900002033',
        'ph33@example.com',
        'Địa chỉ PH 33'
    ),
    (
        34,
        'Phụ huynh (PH34)',
        '0900002034',
        'ph34@example.com',
        'Địa chỉ PH 34'
    ),
    (
        35,
        'Phụ huynh (PH35)',
        '0900002035',
        'ph35@example.com',
        'Địa chỉ PH 35'
    ),
    (
        36,
        'Phụ huynh (PH36)',
        '0900002036',
        'ph36@example.com',
        'Địa chỉ PH 36'
    ),
    (
        37,
        'Phụ huynh (PH37)',
        '0900002037',
        'ph37@example.com',
        'Địa chỉ PH 37'
    ),
    (
        38,
        'Phụ huynh (PH38)',
        '0900002038',
        'ph38@example.com',
        'Địa chỉ PH 38'
    ),
    (
        39,
        'Phụ huynh (PH39)',
        '0900002039',
        'ph39@example.com',
        'Địa chỉ PH 39'
    ),
    (
        40,
        'Phụ huynh (PH40)',
        '0900002040',
        'ph40@example.com',
        'Địa chỉ PH 40'
    ),
    (
        41,
        'Phụ huynh (PH41)',
        '0900002041',
        'ph41@example.com',
        'Địa chỉ PH 41'
    ),
    (
        42,
        'Phụ huynh (PH42)',
        '0900002042',
        'ph42@example.com',
        'Địa chỉ PH 42'
    ),
    (
        43,
        'Phụ huynh (PH43)',
        '0900002043',
        'ph43@example.com',
        'Địa chỉ PH 43'
    ),
    (
        44,
        'Phụ huynh (PH44)',
        '0900002044',
        'ph44@example.com',
        'Địa chỉ PH 44'
    ),
    (
        45,
        'Phụ huynh (PH45)',
        '0900002045',
        'ph45@example.com',
        'Địa chỉ PH 45'
    ),
    (
        46,
        'Phụ huynh (PH46)',
        '0900002046',
        'ph46@example.com',
        'Địa chỉ PH 46'
    ),
    (
        47,
        'Phụ huynh (PH47)',
        '0900002047',
        'ph47@example.com',
        'Địa chỉ PH 47'
    ),
    (
        48,
        'Phụ huynh (PH48)',
        '0900002048',
        'ph48@example.com',
        'Địa chỉ PH 48'
    ),
    (
        49,
        'Phụ huynh (PH49)',
        '0900002049',
        'ph49@example.com',
        'Địa chỉ PH 49'
    ),
    (
        50,
        'Phụ huynh (PH50)',
        '0900002050',
        'ph50@example.com',
        'Địa chỉ PH 50'
    ),
    (
        51,
        'Phụ huynh (PH51)',
        '0900002051',
        'ph51@example.com',
        'Địa chỉ PH 51'
    ),
    (
        52,
        'Phụ huynh (PH52)',
        '0900002052',
        'ph52@example.com',
        'Địa chỉ PH 52'
    ),
    (
        53,
        'Phụ huynh (PH53)',
        '0900002053',
        'ph53@example.com',
        'Địa chỉ PH 53'
    ),
    (
        54,
        'Phụ huynh (PH54)',
        '0900002054',
        'ph54@example.com',
        'Địa chỉ PH 54'
    ),
    (
        55,
        'Phụ huynh (PH55)',
        '0900002055',
        'ph55@example.com',
        'Địa chỉ PH 55'
    ),
    (
        56,
        'Phụ huynh (PH56)',
        '0900002056',
        'ph56@example.com',
        'Địa chỉ PH 56'
    ),
    (
        57,
        'Phụ huynh (PH57)',
        '0900002057',
        'ph57@example.com',
        'Địa chỉ PH 57'
    ),
    (
        58,
        'Phụ huynh (PH58)',
        '0900002058',
        'ph58@example.com',
        'Địa chỉ PH 58'
    ),
    (
        59,
        'Phụ huynh (PH59)',
        '0900002059',
        'ph59@example.com',
        'Địa chỉ PH 59'
    ),
    (
        60,
        'Phụ huynh (PH60)',
        '0900002060',
        'ph60@example.com',
        'Địa chỉ PH 60'
    ),
    (
        61,
        'Phụ huynh (PH61)',
        '0900002061',
        'ph61@example.com',
        'Địa chỉ PH 61'
    ),
    (
        62,
        'Phụ huynh (PH62)',
        '0900002062',
        'ph62@example.com',
        'Địa chỉ PH 62'
    ),
    (
        63,
        'Phụ huynh (PH63)',
        '0900002063',
        'ph63@example.com',
        'Địa chỉ PH 63'
    ),
    (
        64,
        'Phụ huynh (PH64)',
        '0900002064',
        'ph64@example.com',
        'Địa chỉ PH 64'
    ),
    (
        65,
        'Phụ huynh (PH65)',
        '0900002065',
        'ph65@example.com',
        'Địa chỉ PH 65'
    ),
    (
        66,
        'Phụ huynh (PH66)',
        '0900002066',
        'ph66@example.com',
        'Địa chỉ PH 66'
    ),
    (
        67,
        'Phụ huynh (PH67)',
        '0900002067',
        'ph67@example.com',
        'Địa chỉ PH 67'
    ),
    (
        68,
        'Phụ huynh (PH68)',
        '0900002068',
        'ph68@example.com',
        'Địa chỉ PH 68'
    ),
    (
        69,
        'Phụ huynh (PH69)',
        '0900002069',
        'ph69@example.com',
        'Địa chỉ PH 69'
    ),
    (
        70,
        'Phụ huynh (PH70)',
        '0900002070',
        'ph70@example.com',
        'Địa chỉ PH 70'
    ),
    (
        71,
        'Phụ huynh (PH71)',
        '0900002071',
        'ph71@example.com',
        'Địa chỉ PH 71'
    ),
    (
        72,
        'Phụ huynh (PH72)',
        '0900002072',
        'ph72@example.com',
        'Địa chỉ PH 72'
    ),
    (
        73,
        'Phụ huynh (PH73)',
        '0900002073',
        'ph73@example.com',
        'Địa chỉ PH 73'
    ),
    (
        74,
        'Phụ huynh (PH74)',
        '0900002074',
        'ph74@example.com',
        'Địa chỉ PH 74'
    ),
    (
        75,
        'Phụ huynh (PH75)',
        '0900002075',
        'ph75@example.com',
        'Địa chỉ PH 75'
    ),
    (
        76,
        'Phụ huynh (PH76)',
        '0900002076',
        'ph76@example.com',
        'Địa chỉ PH 76'
    ),
    (
        77,
        'Phụ huynh (PH77)',
        '0900002077',
        'ph77@example.com',
        'Địa chỉ PH 77'
    ),
    (
        78,
        'Phụ huynh (PH78)',
        '0900002078',
        'ph78@example.com',
        'Địa chỉ PH 78'
    ),
    (
        79,
        'Phụ huynh (PH79)',
        '0900002079',
        'ph79@example.com',
        'Địa chỉ PH 79'
    ),
    (
        80,
        'Phụ huynh (PH80)',
        '0900002080',
        'ph80@example.com',
        'Địa chỉ PH 80'
    ),
    (
        81,
        'Phụ huynh (PH81)',
        '0900002081',
        'ph81@example.com',
        'Địa chỉ PH 81'
    ),
    (
        82,
        'Phụ huynh (PH82)',
        '0900002082',
        'ph82@example.com',
        'Địa chỉ PH 82'
    ),
    (
        83,
        'Phụ huynh (PH83)',
        '0900002083',
        'ph83@example.com',
        'Địa chỉ PH 83'
    ),
    (
        84,
        'Phụ huynh (PH84)',
        '0900002084',
        'ph84@example.com',
        'Địa chỉ PH 84'
    ),
    (
        85,
        'Phụ huynh (PH85)',
        '0900002085',
        'ph85@example.com',
        'Địa chỉ PH 85'
    ),
    (
        86,
        'Phụ huynh (PH86)',
        '0900002086',
        'ph86@example.com',
        'Địa chỉ PH 86'
    ),
    (
        87,
        'Phụ huynh (PH87)',
        '0900002087',
        'ph87@example.com',
        'Địa chỉ PH 87'
    ),
    (
        88,
        'Phụ huynh (PH88)',
        '0900002088',
        'ph88@example.com',
        'Địa chỉ PH 88'
    ),
    (
        89,
        'Phụ huynh (PH89)',
        '0900002089',
        'ph89@example.com',
        'Địa chỉ PH 89'
    ),
    (
        90,
        'Phụ huynh (PH90)',
        '0900002090',
        'ph90@example.com',
        'Địa chỉ PH 90'
    ),
    (
        91,
        'Phụ huynh (PH91)',
        '0900002091',
        'ph91@example.com',
        'Địa chỉ PH 91'
    ),
    (
        92,
        'Phụ huynh (PH92)',
        '0900002092',
        'ph92@example.com',
        'Địa chỉ PH 92'
    ),
    (
        93,
        'Phụ huynh (PH93)',
        '0900002093',
        'ph93@example.com',
        'Địa chỉ PH 93'
    ),
    (
        94,
        'Phụ huynh (PH94)',
        '0900002094',
        'ph94@example.com',
        'Địa chỉ PH 94'
    ),
    (
        95,
        'Phụ huynh (PH95)',
        '0900002095',
        'ph95@example.com',
        'Địa chỉ PH 95'
    ),
    (
        96,
        'Phụ huynh (PH96)',
        '0900002096',
        'ph96@example.com',
        'Địa chỉ PH 96'
    ),
    (
        97,
        'Phụ huynh (PH97)',
        '0900002097',
        'ph97@example.com',
        'Địa chỉ PH 97'
    ),
    (
        98,
        'Phụ huynh (PH98)',
        '0900002098',
        'ph98@example.com',
        'Địa chỉ PH 98'
    ),
    (
        99,
        'Phụ huynh (PH99)',
        '0900002099',
        'ph99@example.com',
        'Địa chỉ PH 99'
    ),
    (
        100,
        'Phụ huynh (PH100)',
        '0900002100',
        'ph100@example.com',
        'Địa chỉ PH 100'
    ),
    (
        101,
        'Phụ huynh (PH101)',
        '0900002101',
        'ph101@example.com',
        'Địa chỉ PH 101'
    ),
    (
        102,
        'Phụ huynh (PH102)',
        '0900002102',
        'ph102@example.com',
        'Địa chỉ PH 102'
    ),
    (
        103,
        'Phụ huynh (PH103)',
        '0900002103',
        'ph103@example.com',
        'Địa chỉ PH 103'
    ),
    (
        104,
        'Phụ huynh (PH104)',
        '0900002104',
        'ph104@example.com',
        'Địa chỉ PH 104'
    ),
    (
        105,
        'Phụ huynh (PH105)',
        '0900002105',
        'ph105@example.com',
        'Địa chỉ PH 105'
    ),
    (
        106,
        'Phụ huynh (PH106)',
        '0900002106',
        'ph106@example.com',
        'Địa chỉ PH 106'
    ),
    (
        107,
        'Phụ huynh (PH107)',
        '0900002107',
        'ph107@example.com',
        'Địa chỉ PH 107'
    ),
    (
        108,
        'Phụ huynh (PH108)',
        '0900002108',
        'ph108@example.com',
        'Địa chỉ PH 108'
    ),
    (
        109,
        'Phụ huynh (PH109)',
        '0900002109',
        'ph109@example.com',
        'Địa chỉ PH 109'
    ),
    (
        110,
        'Phụ huynh (PH110)',
        '0900002110',
        'ph110@example.com',
        'Địa chỉ PH 110'
    ),
    (
        111,
        'Phụ huynh (PH111)',
        '0900002111',
        'ph111@example.com',
        'Địa chỉ PH 111'
    ),
    (
        112,
        'Phụ huynh (PH112)',
        '0900002112',
        'ph112@example.com',
        'Địa chỉ PH 112'
    ),
    (
        113,
        'Phụ huynh (PH113)',
        '0900002113',
        'ph113@example.com',
        'Địa chỉ PH 113'
    ),
    (
        114,
        'Phụ huynh (PH114)',
        '0900002114',
        'ph114@example.com',
        'Địa chỉ PH 114'
    ),
    (
        115,
        'Phụ huynh (PH115)',
        '0900002115',
        'ph115@example.com',
        'Địa chỉ PH 115'
    ),
    (
        116,
        'Phụ huynh (PH116)',
        '0900002116',
        'ph116@example.com',
        'Địa chỉ PH 116'
    ),
    (
        117,
        'Phụ huynh (PH117)',
        '0900002117',
        'ph117@example.com',
        'Địa chỉ PH 117'
    ),
    (
        118,
        'Phụ huynh (PH118)',
        '0900002118',
        'ph118@example.com',
        'Địa chỉ PH 118'
    ),
    (
        119,
        'Phụ huynh (PH119)',
        '0900002119',
        'ph119@example.com',
        'Địa chỉ PH 119'
    ),
    (
        120,
        'Phụ huynh (PH120)',
        '0900002120',
        'ph120@example.com',
        'Địa chỉ PH 120'
    ),
    (
        121,
        'Phụ huynh (PH121)',
        '0900002121',
        'ph121@example.com',
        'Địa chỉ PH 121'
    ),
    (
        122,
        'Phụ huynh (PH122)',
        '0900002122',
        'ph122@example.com',
        'Địa chỉ PH 122'
    ),
    (
        123,
        'Phụ huynh (PH123)',
        '0900002123',
        'ph123@example.com',
        'Địa chỉ PH 123'
    ),
    (
        124,
        'Phụ huynh (PH124)',
        '0900002124',
        'ph124@example.com',
        'Địa chỉ PH 124'
    ),
    (
        125,
        'Phụ huynh (PH125)',
        '0900002125',
        'ph125@example.com',
        'Địa chỉ PH 125'
    ),
    (
        126,
        'Phụ huynh (PH126)',
        '0900002126',
        'ph126@example.com',
        'Địa chỉ PH 126'
    ),
    (
        127,
        'Phụ huynh (PH127)',
        '0900002127',
        'ph127@example.com',
        'Địa chỉ PH 127'
    ),
    (
        128,
        'Phụ huynh (PH128)',
        '0900002128',
        'ph128@example.com',
        'Địa chỉ PH 128'
    ),
    (
        129,
        'Phụ huynh (PH129)',
        '0900002129',
        'ph129@example.com',
        'Địa chỉ PH 129'
    ),
    (
        130,
        'Phụ huynh (PH130)',
        '0900002130',
        'ph130@example.com',
        'Địa chỉ PH 130'
    ),
    (
        131,
        'Phụ huynh (PH131)',
        '0900002131',
        'ph131@example.com',
        'Địa chỉ PH 131'
    ),
    (
        132,
        'Phụ huynh (PH132)',
        '0900002132',
        'ph132@example.com',
        'Địa chỉ PH 132'
    ),
    (
        133,
        'Phụ huynh (PH133)',
        '0900002133',
        'ph133@example.com',
        'Địa chỉ PH 133'
    ),
    (
        134,
        'Phụ huynh (PH134)',
        '0900002134',
        'ph134@example.com',
        'Địa chỉ PH 134'
    ),
    (
        135,
        'Phụ huynh (PH135)',
        '0900002135',
        'ph135@example.com',
        'Địa chỉ PH 135'
    ),
    (
        136,
        'Phụ huynh (PH136)',
        '0900002136',
        'ph136@example.com',
        'Địa chỉ PH 136'
    ),
    (
        137,
        'Phụ huynh (PH137)',
        '0900002137',
        'ph137@example.com',
        'Địa chỉ PH 137'
    ),
    (
        138,
        'Phụ huynh (PH138)',
        '0900002138',
        'ph138@example.com',
        'Địa chỉ PH 138'
    ),
    (
        139,
        'Phụ huynh (PH139)',
        '0900002139',
        'ph139@example.com',
        'Địa chỉ PH 139'
    ),
    (
        140,
        'Phụ huynh (PH140)',
        '0900002140',
        'ph140@example.com',
        'Địa chỉ PH 140'
    ),
    (
        141,
        'Phụ huynh (PH141)',
        '0900002141',
        'ph141@example.com',
        'Địa chỉ PH 141'
    ),
    (
        142,
        'Phụ huynh (PH142)',
        '0900002142',
        'ph142@example.com',
        'Địa chỉ PH 142'
    ),
    (
        143,
        'Phụ huynh (PH143)',
        '0900002143',
        'ph143@example.com',
        'Địa chỉ PH 143'
    ),
    (
        144,
        'Phụ huynh (PH144)',
        '0900002144',
        'ph144@example.com',
        'Địa chỉ PH 144'
    ),
    (
        145,
        'Phụ huynh (PH145)',
        '0900002145',
        'ph145@example.com',
        'Địa chỉ PH 145'
    ),
    (
        146,
        'Phụ huynh (PH146)',
        '0900002146',
        'ph146@example.com',
        'Địa chỉ PH 146'
    ),
    (
        147,
        'Phụ huynh (PH147)',
        '0900002147',
        'ph147@example.com',
        'Địa chỉ PH 147'
    ),
    (
        148,
        'Phụ huynh (PH148)',
        '0900002148',
        'ph148@example.com',
        'Địa chỉ PH 148'
    ),
    (
        149,
        'Phụ huynh (PH149)',
        '0900002149',
        'ph149@example.com',
        'Địa chỉ PH 149'
    ),
    (
        150,
        'Phụ huynh (PH150)',
        '0900002150',
        'ph150@example.com',
        'Địa chỉ PH 150'
    ),
    (
        151,
        'Phụ huynh (PH151)',
        '0900002151',
        'ph151@example.com',
        'Địa chỉ PH 151'
    ),
    (
        152,
        'Phụ huynh (PH152)',
        '0900002152',
        'ph152@example.com',
        'Địa chỉ PH 152'
    ),
    (
        153,
        'Phụ huynh (PH153)',
        '0900002153',
        'ph153@example.com',
        'Địa chỉ PH 153'
    ),
    (
        154,
        'Phụ huynh (PH154)',
        '0900002154',
        'ph154@example.com',
        'Địa chỉ PH 154'
    ),
    (
        155,
        'Phụ huynh (PH155)',
        '0900002155',
        'ph155@example.com',
        'Địa chỉ PH 155'
    ),
    (
        156,
        'Phụ huynh (PH156)',
        '0900002156',
        'ph156@example.com',
        'Địa chỉ PH 156'
    ),
    (
        157,
        'Phụ huynh (PH157)',
        '0900002157',
        'ph157@example.com',
        'Địa chỉ PH 157'
    ),
    (
        158,
        'Phụ huynh (PH158)',
        '0900002158',
        'ph158@example.com',
        'Địa chỉ PH 158'
    ),
    (
        159,
        'Phụ huynh (PH159)',
        '0900002159',
        'ph159@example.com',
        'Địa chỉ PH 159'
    ),
    (
        160,
        'Phụ huynh (PH160)',
        '0900002160',
        'ph160@example.com',
        'Địa chỉ PH 160'
    ),
    (
        161,
        'Phụ huynh (PH161)',
        '0900002161',
        'ph161@example.com',
        'Địa chỉ PH 161'
    ),
    (
        162,
        'Phụ huynh (PH162)',
        '0900002162',
        'ph162@example.com',
        'Địa chỉ PH 162'
    ),
    (
        163,
        'Phụ huynh (PH163)',
        '0900002163',
        'ph163@example.com',
        'Địa chỉ PH 163'
    ),
    (
        164,
        'Phụ huynh (PH164)',
        '0900002164',
        'ph164@example.com',
        'Địa chỉ PH 164'
    ),
    (
        165,
        'Phụ huynh (PH165)',
        '0900002165',
        'ph165@example.com',
        'Địa chỉ PH 165'
    ),
    (
        166,
        'Phụ huynh (PH166)',
        '0900002166',
        'ph166@example.com',
        'Địa chỉ PH 166'
    ),
    (
        167,
        'Phụ huynh (PH167)',
        '0900002167',
        'ph167@example.com',
        'Địa chỉ PH 167'
    ),
    (
        168,
        'Phụ huynh (PH168)',
        '0900002168',
        'ph168@example.com',
        'Địa chỉ PH 168'
    ),
    (
        169,
        'Phụ huynh (PH169)',
        '0900002169',
        'ph169@example.com',
        'Địa chỉ PH 169'
    ),
    (
        170,
        'Phụ huynh (PH170)',
        '0900002170',
        'ph170@example.com',
        'Địa chỉ PH 170'
    ),
    (
        171,
        'Phụ huynh (PH171)',
        '0900002171',
        'ph171@example.com',
        'Địa chỉ PH 171'
    ),
    (
        172,
        'Phụ huynh (PH172)',
        '0900002172',
        'ph172@example.com',
        'Địa chỉ PH 172'
    ),
    (
        173,
        'Phụ huynh (PH173)',
        '0900002173',
        'ph173@example.com',
        'Địa chỉ PH 173'
    ),
    (
        174,
        'Phụ huynh (PH174)',
        '0900002174',
        'ph174@example.com',
        'Địa chỉ PH 174'
    ),
    (
        175,
        'Phụ huynh (PH175)',
        '0900002175',
        'ph175@example.com',
        'Địa chỉ PH 175'
    ),
    (
        176,
        'Phụ huynh (PH176)',
        '0900002176',
        'ph176@example.com',
        'Địa chỉ PH 176'
    ),
    (
        177,
        'Phụ huynh (PH177)',
        '0900002177',
        'ph177@example.com',
        'Địa chỉ PH 177'
    ),
    (
        178,
        'Phụ huynh (PH178)',
        '0900002178',
        'ph178@example.com',
        'Địa chỉ PH 178'
    ),
    (
        179,
        'Phụ huynh (PH179)',
        '0900002179',
        'ph179@example.com',
        'Địa chỉ PH 179'
    ),
    (
        180,
        'Phụ huynh (PH180)',
        '0900002180',
        'ph180@example.com',
        'Địa chỉ PH 180'
    ),
    (
        181,
        'Phụ huynh (PH181)',
        '0900002181',
        'ph181@example.com',
        'Địa chỉ PH 181'
    ),
    (
        182,
        'Phụ huynh (PH182)',
        '0900002182',
        'ph182@example.com',
        'Địa chỉ PH 182'
    ),
    (
        183,
        'Phụ huynh (PH183)',
        '0900002183',
        'ph183@example.com',
        'Địa chỉ PH 183'
    ),
    (
        184,
        'Phụ huynh (PH184)',
        '0900002184',
        'ph184@example.com',
        'Địa chỉ PH 184'
    ),
    (
        185,
        'Phụ huynh (PH185)',
        '0900002185',
        'ph185@example.com',
        'Địa chỉ PH 185'
    ),
    (
        186,
        'Phụ huynh (PH186)',
        '0900002186',
        'ph186@example.com',
        'Địa chỉ PH 186'
    ),
    (
        187,
        'Phụ huynh (PH187)',
        '0900002187',
        'ph187@example.com',
        'Địa chỉ PH 187'
    ),
    (
        188,
        'Phụ huynh (PH188)',
        '0900002188',
        'ph188@example.com',
        'Địa chỉ PH 188'
    ),
    (
        189,
        'Phụ huynh (PH189)',
        '0900002189',
        'ph189@example.com',
        'Địa chỉ PH 189'
    ),
    (
        190,
        'Phụ huynh (PH190)',
        '0900002190',
        'ph190@example.com',
        'Địa chỉ PH 190'
    ),
    (
        191,
        'Phụ huynh (PH191)',
        '0900002191',
        'ph191@example.com',
        'Địa chỉ PH 191'
    ),
    (
        192,
        'Phụ huynh (PH192)',
        '0900002192',
        'ph192@example.com',
        'Địa chỉ PH 192'
    ),
    (
        193,
        'Phụ huynh (PH193)',
        '0900002193',
        'ph193@example.com',
        'Địa chỉ PH 193'
    ),
    (
        194,
        'Phụ huynh (PH194)',
        '0900002194',
        'ph194@example.com',
        'Địa chỉ PH 194'
    ),
    (
        195,
        'Phụ huynh (PH195)',
        '0900002195',
        'ph195@example.com',
        'Địa chỉ PH 195'
    ),
    (
        196,
        'Phụ huynh (PH196)',
        '0900002196',
        'ph196@example.com',
        'Địa chỉ PH 196'
    ),
    (
        197,
        'Phụ huynh (PH197)',
        '0900002197',
        'ph197@example.com',
        'Địa chỉ PH 197'
    ),
    (
        198,
        'Phụ huynh (PH198)',
        '0900002198',
        'ph198@example.com',
        'Địa chỉ PH 198'
    ),
    (
        199,
        'Phụ huynh (PH199)',
        '0900002199',
        'ph199@example.com',
        'Địa chỉ PH 199'
    ),
    (
        200,
        'Phụ huynh (PH200)',
        '0900002200',
        'ph200@example.com',
        'Địa chỉ PH 200'
    ),
    (
        201,
        'Phụ huynh (PH201)',
        '0900002201',
        'ph201@example.com',
        'Địa chỉ PH 201'
    ),
    (
        202,
        'Phụ huynh (PH202)',
        '0900002202',
        'ph202@example.com',
        'Địa chỉ PH 202'
    ),
    (
        203,
        'Phụ huynh (PH203)',
        '0900002203',
        'ph203@example.com',
        'Địa chỉ PH 203'
    ),
    (
        204,
        'Phụ huynh (PH204)',
        '0900002204',
        'ph204@example.com',
        'Địa chỉ PH 204'
    ),
    (
        205,
        'Phụ huynh (PH205)',
        '0900002205',
        'ph205@example.com',
        'Địa chỉ PH 205'
    ),
    (
        206,
        'Phụ huynh (PH206)',
        '0900002206',
        'ph206@example.com',
        'Địa chỉ PH 206'
    ),
    (
        207,
        'Phụ huynh (PH207)',
        '0900002207',
        'ph207@example.com',
        'Địa chỉ PH 207'
    ),
    (
        208,
        'Phụ huynh (PH208)',
        '0900002208',
        'ph208@example.com',
        'Địa chỉ PH 208'
    ),
    (
        209,
        'Phụ huynh (PH209)',
        '0900002209',
        'ph209@example.com',
        'Địa chỉ PH 209'
    ),
    (
        210,
        'Phụ huynh (PH210)',
        '0900002210',
        'ph210@example.com',
        'Địa chỉ PH 210'
    ),
    (
        211,
        'Phụ huynh (PH211)',
        '0900002211',
        'ph211@example.com',
        'Địa chỉ PH 211'
    ),
    (
        212,
        'Phụ huynh (PH212)',
        '0900002212',
        'ph212@example.com',
        'Địa chỉ PH 212'
    ),
    (
        213,
        'Phụ huynh (PH213)',
        '0900002213',
        'ph213@example.com',
        'Địa chỉ PH 213'
    ),
    (
        214,
        'Phụ huynh (PH214)',
        '0900002214',
        'ph214@example.com',
        'Địa chỉ PH 214'
    ),
    (
        215,
        'Phụ huynh (PH215)',
        '0900002215',
        'ph215@example.com',
        'Địa chỉ PH 215'
    ),
    (
        216,
        'Phụ huynh (PH216)',
        '0900002216',
        'ph216@example.com',
        'Địa chỉ PH 216'
    ),
    (
        217,
        'Phụ huynh (PH217)',
        '0900002217',
        'ph217@example.com',
        'Địa chỉ PH 217'
    ),
    (
        218,
        'Phụ huynh (PH218)',
        '0900002218',
        'ph218@example.com',
        'Địa chỉ PH 218'
    ),
    (
        219,
        'Phụ huynh (PH219)',
        '0900002219',
        'ph219@example.com',
        'Địa chỉ PH 219'
    ),
    (
        220,
        'Phụ huynh (PH220)',
        '0900002220',
        'ph220@example.com',
        'Địa chỉ PH 220'
    ),
    (
        221,
        'Phụ huynh (PH221)',
        '0900002221',
        'ph221@example.com',
        'Địa chỉ PH 221'
    ),
    (
        222,
        'Phụ huynh (PH222)',
        '0900002222',
        'ph222@example.com',
        'Địa chỉ PH 222'
    ),
    (
        223,
        'Phụ huynh (PH223)',
        '0900002223',
        'ph223@example.com',
        'Địa chỉ PH 223'
    ),
    (
        224,
        'Phụ huynh (PH224)',
        '0900002224',
        'ph224@example.com',
        'Địa chỉ PH 224'
    ),
    (
        225,
        'Phụ huynh (PH225)',
        '0900002225',
        'ph225@example.com',
        'Địa chỉ PH 225'
    ),
    (
        226,
        'Phụ huynh (PH226)',
        '0900002226',
        'ph226@example.com',
        'Địa chỉ PH 226'
    ),
    (
        227,
        'Phụ huynh (PH227)',
        '0900002227',
        'ph227@example.com',
        'Địa chỉ PH 227'
    ),
    (
        228,
        'Phụ huynh (PH228)',
        '0900002228',
        'ph228@example.com',
        'Địa chỉ PH 228'
    ),
    (
        229,
        'Phụ huynh (PH229)',
        '0900002229',
        'ph229@example.com',
        'Địa chỉ PH 229'
    ),
    (
        230,
        'Phụ huynh (PH230)',
        '0900002230',
        'ph230@example.com',
        'Địa chỉ PH 230'
    ),
    (
        231,
        'Phụ huynh (PH231)',
        '0900002231',
        'ph231@example.com',
        'Địa chỉ PH 231'
    ),
    (
        232,
        'Phụ huynh (PH232)',
        '0900002232',
        'ph232@example.com',
        'Địa chỉ PH 232'
    ),
    (
        233,
        'Phụ huynh (PH233)',
        '0900002233',
        'ph233@example.com',
        'Địa chỉ PH 233'
    ),
    (
        234,
        'Phụ huynh (PH234)',
        '0900002234',
        'ph234@example.com',
        'Địa chỉ PH 234'
    ),
    (
        235,
        'Phụ huynh (PH235)',
        '0900002235',
        'ph235@example.com',
        'Địa chỉ PH 235'
    ),
    (
        236,
        'Phụ huynh (PH236)',
        '0900002236',
        'ph236@example.com',
        'Địa chỉ PH 236'
    ),
    (
        237,
        'Phụ huynh (PH237)',
        '0900002237',
        'ph237@example.com',
        'Địa chỉ PH 237'
    ),
    (
        238,
        'Phụ huynh (PH238)',
        '0900002238',
        'ph238@example.com',
        'Địa chỉ PH 238'
    ),
    (
        239,
        'Phụ huynh (PH239)',
        '0900002239',
        'ph239@example.com',
        'Địa chỉ PH 239'
    ),
    (
        240,
        'Phụ huynh (PH240)',
        '0900002240',
        'ph240@example.com',
        'Địa chỉ PH 240'
    ),
    (
        241,
        'Phụ huynh (PH241)',
        '0900002241',
        'ph241@example.com',
        'Địa chỉ PH 241'
    ),
    (
        242,
        'Phụ huynh (PH242)',
        '0900002242',
        'ph242@example.com',
        'Địa chỉ PH 242'
    ),
    (
        243,
        'Phụ huynh (PH243)',
        '0900002243',
        'ph243@example.com',
        'Địa chỉ PH 243'
    ),
    (
        244,
        'Phụ huynh (PH244)',
        '0900002244',
        'ph244@example.com',
        'Địa chỉ PH 244'
    ),
    (
        245,
        'Phụ huynh (PH245)',
        '0900002245',
        'ph245@example.com',
        'Địa chỉ PH 245'
    ),
    (
        246,
        'Phụ huynh (PH246)',
        '0900002246',
        'ph246@example.com',
        'Địa chỉ PH 246'
    ),
    (
        247,
        'Phụ huynh (PH247)',
        '0900002247',
        'ph247@example.com',
        'Địa chỉ PH 247'
    ),
    (
        248,
        'Phụ huynh (PH248)',
        '0900002248',
        'ph248@example.com',
        'Địa chỉ PH 248'
    ),
    (
        249,
        'Phụ huynh (PH249)',
        '0900002249',
        'ph249@example.com',
        'Địa chỉ PH 249'
    ),
    (
        250,
        'Phụ huynh (PH250)',
        '0900002250',
        'ph250@example.com',
        'Địa chỉ PH 250'
    );

INSERT INTO
    HocSinh_PhuHuynh (MaHS, MaPH, QuanHe)
VALUES (10, 6, 'Mẹ'),
    (11, 7, 'Bố'),
    (12, 8, 'Mẹ'),
    (13, 9, 'Bố'),
    (14, 10, 'Mẹ'),
    (15, 11, 'Bố'),
    (16, 12, 'Mẹ'),
    (17, 13, 'Bố'),
    (18, 14, 'Mẹ'),
    (19, 15, 'Bố'),
    (20, 16, 'Mẹ'),
    (21, 17, 'Bố'),
    (22, 18, 'Mẹ'),
    (23, 19, 'Bố'),
    (24, 20, 'Mẹ'),
    (25, 21, 'Bố'),
    (26, 22, 'Mẹ'),
    (27, 23, 'Bố'),
    (28, 24, 'Mẹ'),
    (29, 25, 'Bố'),
    (30, 26, 'Mẹ'),
    (31, 27, 'Bố'),
    (32, 28, 'Mẹ'),
    (33, 29, 'Bố'),
    (34, 30, 'Mẹ'),
    (35, 31, 'Bố'),
    (36, 32, 'Mẹ'),
    (37, 33, 'Bố'),
    (38, 34, 'Mẹ'),
    (39, 35, 'Bố'),
    (40, 36, 'Mẹ'),
    (41, 37, 'Bố'),
    (42, 38, 'Mẹ'),
    (43, 39, 'Bố'),
    (44, 40, 'Mẹ'),
    (45, 41, 'Bố'),
    (46, 42, 'Mẹ'),
    (47, 43, 'Bố'),
    (48, 44, 'Mẹ'),
    (49, 45, 'Bố'),
    (50, 46, 'Mẹ'),
    (51, 47, 'Bố'),
    (52, 48, 'Mẹ'),
    (53, 49, 'Bố'),
    (54, 50, 'Mẹ'),
    (55, 51, 'Bố'),
    (56, 52, 'Mẹ'),
    (57, 53, 'Bố'),
    (58, 54, 'Mẹ'),
    (59, 55, 'Bố'),
    (60, 56, 'Mẹ'),
    (61, 57, 'Bố'),
    (62, 58, 'Mẹ'),
    (63, 59, 'Bố'),
    (64, 60, 'Mẹ'),
    (65, 61, 'Bố'),
    (66, 62, 'Mẹ'),
    (67, 63, 'Bố'),
    (68, 64, 'Mẹ'),
    (69, 65, 'Bố'),
    (70, 66, 'Mẹ'),
    (71, 67, 'Bố'),
    (72, 68, 'Mẹ'),
    (73, 69, 'Bố'),
    (74, 70, 'Mẹ'),
    (75, 71, 'Bố'),
    (76, 72, 'Mẹ'),
    (77, 73, 'Bố'),
    (78, 74, 'Mẹ'),
    (79, 75, 'Bố'),
    (80, 76, 'Mẹ'),
    (81, 77, 'Bố'),
    (82, 78, 'Mẹ'),
    (83, 79, 'Bố'),
    (84, 80, 'Mẹ'),
    (85, 81, 'Bố'),
    (86, 82, 'Mẹ'),
    (87, 83, 'Bố'),
    (88, 84, 'Mẹ'),
    (89, 85, 'Bố'),
    (90, 86, 'Mẹ'),
    (91, 87, 'Bố'),
    (92, 88, 'Mẹ'),
    (93, 89, 'Bố'),
    (94, 90, 'Mẹ'),
    (95, 91, 'Bố'),
    (96, 92, 'Mẹ'),
    (97, 93, 'Bố'),
    (98, 94, 'Mẹ'),
    (99, 95, 'Bố'),
    (100, 96, 'Mẹ'),
    (101, 97, 'Bố'),
    (102, 98, 'Mẹ'),
    (103, 99, 'Bố'),
    (104, 100, 'Mẹ'),
    (105, 101, 'Bố'),
    (106, 102, 'Mẹ'),
    (107, 103, 'Bố'),
    (108, 104, 'Mẹ'),
    (109, 105, 'Bố'),
    (110, 106, 'Mẹ'),
    (111, 107, 'Bố'),
    (112, 108, 'Mẹ'),
    (113, 109, 'Bố'),
    (114, 110, 'Mẹ'),
    (115, 111, 'Bố'),
    (116, 112, 'Mẹ'),
    (117, 113, 'Bố'),
    (118, 114, 'Mẹ'),
    (119, 115, 'Bố'),
    (120, 116, 'Mẹ'),
    (121, 117, 'Bố'),
    (122, 118, 'Mẹ'),
    (123, 119, 'Bố'),
    (124, 120, 'Mẹ'),
    (125, 121, 'Bố'),
    (126, 122, 'Mẹ'),
    (127, 123, 'Bố'),
    (128, 124, 'Mẹ'),
    (129, 125, 'Bố'),
    (130, 126, 'Mẹ'),
    (131, 127, 'Bố'),
    (132, 128, 'Mẹ'),
    (133, 129, 'Bố'),
    (134, 130, 'Mẹ'),
    (135, 131, 'Bố'),
    (136, 132, 'Mẹ'),
    (137, 133, 'Bố'),
    (138, 134, 'Mẹ'),
    (139, 135, 'Bố'),
    (140, 136, 'Mẹ'),
    (141, 137, 'Bố'),
    (142, 138, 'Mẹ'),
    (143, 139, 'Bố'),
    (144, 140, 'Mẹ'),
    (145, 141, 'Bố'),
    (146, 142, 'Mẹ'),
    (147, 143, 'Bố'),
    (148, 144, 'Mẹ'),
    (149, 145, 'Bố'),
    (150, 146, 'Mẹ'),
    (151, 147, 'Bố'),
    (152, 148, 'Mẹ'),
    (153, 149, 'Bố'),
    (154, 150, 'Mẹ'),
    (155, 151, 'Bố'),
    (156, 152, 'Mẹ'),
    (157, 153, 'Bố'),
    (158, 154, 'Mẹ'),
    (159, 155, 'Bố'),
    (160, 156, 'Mẹ'),
    (161, 157, 'Bố'),
    (162, 158, 'Mẹ'),
    (163, 159, 'Bố'),
    (164, 160, 'Mẹ'),
    (165, 161, 'Bố'),
    (166, 162, 'Mẹ'),
    (167, 163, 'Bố'),
    (168, 164, 'Mẹ'),
    (169, 165, 'Bố'),
    (170, 166, 'Mẹ'),
    (171, 167, 'Bố'),
    (172, 168, 'Mẹ'),
    (173, 169, 'Bố'),
    (174, 170, 'Mẹ'),
    (175, 171, 'Bố'),
    (176, 172, 'Mẹ'),
    (177, 173, 'Bố'),
    (178, 174, 'Mẹ'),
    (179, 175, 'Bố'),
    (180, 176, 'Mẹ'),
    (181, 177, 'Bố'),
    (182, 178, 'Mẹ'),
    (183, 179, 'Bố'),
    (184, 180, 'Mẹ'),
    (185, 181, 'Bố'),
    (186, 182, 'Mẹ'),
    (187, 183, 'Bố'),
    (188, 184, 'Mẹ'),
    (189, 185, 'Bố'),
    (190, 186, 'Mẹ'),
    (191, 187, 'Bố'),
    (192, 188, 'Mẹ'),
    (193, 189, 'Bố'),
    (194, 190, 'Mẹ'),
    (195, 191, 'Bố'),
    (196, 192, 'Mẹ'),
    (197, 193, 'Bố'),
    (198, 194, 'Mẹ'),
    (199, 195, 'Bố'),
    (200, 196, 'Mẹ'),
    (201, 197, 'Bố'),
    (202, 198, 'Mẹ'),
    (203, 199, 'Bố'),
    (204, 200, 'Mẹ'),
    (205, 201, 'Bố'),
    (206, 202, 'Mẹ'),
    (207, 203, 'Bố'),
    (208, 204, 'Mẹ'),
    (209, 205, 'Bố'),
    (210, 206, 'Mẹ'),
    (211, 207, 'Bố'),
    (212, 208, 'Mẹ'),
    (213, 209, 'Bố'),
    (214, 210, 'Mẹ'),
    (215, 211, 'Bố'),
    (216, 212, 'Mẹ'),
    (217, 213, 'Bố'),
    (218, 214, 'Mẹ'),
    (219, 215, 'Bố'),
    (220, 216, 'Mẹ'),
    (221, 217, 'Bố'),
    (222, 218, 'Mẹ'),
    (223, 219, 'Bố'),
    (224, 220, 'Mẹ'),
    (225, 221, 'Bố'),
    (226, 222, 'Mẹ'),
    (227, 223, 'Bố'),
    (228, 224, 'Mẹ'),
    (229, 225, 'Bố'),
    (230, 226, 'Mẹ'),
    (231, 227, 'Bố'),
    (232, 228, 'Mẹ'),
    (233, 229, 'Bố'),
    (234, 230, 'Mẹ'),
    (235, 231, 'Bố'),
    (236, 232, 'Mẹ'),
    (237, 233, 'Bố'),
    (238, 234, 'Mẹ'),
    (239, 235, 'Bố'),
    (240, 236, 'Mẹ'),
    (241, 237, 'Bố'),
    (242, 238, 'Mẹ'),
    (243, 239, 'Bố'),
    (244, 240, 'Mẹ'),
    (245, 241, 'Bố'),
    (246, 242, 'Mẹ'),
    (247, 243, 'Bố'),
    (248, 244, 'Mẹ'),
    (249, 245, 'Bố'),
    (250, 246, 'Mẹ'),
    (251, 247, 'Bố'),
    (252, 248, 'Mẹ'),
    (253, 249, 'Bố'),
    (254, 250, 'Mẹ');

INSERT INTO
    ThoiKhoaBieu (
        MaLop,
        MaGV,
        MaMon,
        MaPhong,
        HocKy,
        NamHoc,
        ThuTrongTuan,
        TietBatDau,
        TietKetThuc
    )
VALUES
    -- 12A1 (MaLop=4, MaPhong=301) - HK1
    (
        4,
        101,
        101,
        301,
        'HK1',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ),
    (
        4,
        102,
        102,
        301,
        'HK1',
        '2024-2025',
        'Thứ 2',
        3,
        4
    ),
    (
        4,
        103,
        103,
        301,
        'HK1',
        '2024-2025',
        'Thứ 3',
        1,
        2
    ),
    (
        4,
        104,
        104,
        301,
        'HK1',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ),
    (
        4,
        105,
        105,
        301,
        'HK1',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ),
    -- 12A1 (MaLop=4, MaPhong=301) - HK2
    (
        4,
        101,
        101,
        301,
        'HK2',
        '2024-2025',
        'Thứ 5',
        1,
        2
    ),
    (
        4,
        102,
        102,
        301,
        'HK2',
        '2024-2025',
        'Thứ 5',
        3,
        4
    ),
    (
        4,
        103,
        103,
        301,
        'HK2',
        '2024-2025',
        'Thứ 6',
        1,
        2
    ),
    (
        4,
        104,
        104,
        301,
        'HK2',
        '2024-2025',
        'Thứ 6',
        3,
        4
    ),
    -- 10A3 (MaLop=5, MaPhong=102) - HK1 
    (
        5,
        107,
        101,
        103,
        'HK1',
        '2024-2025',
        'Thứ 5',
        1,
        2
    ),
    (
        5,
        108,
        102,
        103,
        'HK1',
        '2024-2025',
        'Thứ 5',
        3,
        4
    ),
    (
        5,
        109,
        103,
        103,
        'HK1',
        '2024-2025',
        'Thứ 6',
        1,
        2
    ),
    (
        5,
        110,
        104,
        103,
        'HK1',
        '2024-2025',
        'Thứ 6',
        3,
        4
    ),
    -- 10A3 (MaLop=5, MaPhong=102/202) - HK2
    (
        5,
        107,
        101,
        103,
        'HK2',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ),
    (
        5,
        108,
        102,
        103,
        'HK2',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ),
    (
        5,
        109,
        103,
        103,
        'HK2',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ),
    -- 11A2 (MaLop=6, MaPhong=201) - HK1 
    (
        6,
        111,
        101,
        203,
        'HK1',
        '2024-2025',
        'Thứ 5',
        1,
        2
    ),
    (
        6,
        112,
        102,
        203,
        'HK1',
        '2024-2025',
        'Thứ 5',
        3,
        4
    ),
    (
        6,
        113,
        103,
        203,
        'HK1',
        '2024-2025',
        'Thứ 6',
        1,
        2
    ),
    (
        6,
        114,
        104,
        203,
        'HK1',
        '2024-2025',
        'Thứ 6',
        3,
        4
    ),
    -- 11A2 (MaLop=6, MaPhong=201) - HK2
    (
        6,
        111,
        101,
        203,
        'HK2',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ),
    (
        6,
        112,
        102,
        203,
        'HK2',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ),
    (
        6,
        113,
        103,
        203,
        'HK2',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ),
    -- 12A2 (MaLop=7, MaPhong=301) - HK1 
    (
        7,
        115,
        101,
        302,
        'HK1',
        '2024-2025',
        'Thứ 5',
        1,
        2
    ),
    (
        7,
        105,
        102,
        302,
        'HK1',
        '2024-2025',
        'Thứ 5',
        3,
        4
    ),
    (
        7,
        106,
        103,
        302,
        'HK1',
        '2024-2025',
        'Thứ 6',
        1,
        2
    ),
    (
        7,
        107,
        104,
        302,
        'HK1',
        '2024-2025',
        'Thứ 6',
        3,
        4
    ),
    -- 12A2 (MaLop=7, MaPhong=301) - HK2
    (
        7,
        115,
        101,
        302,
        'HK2',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ),
    (
        7,
        105,
        102,
        302,
        'HK2',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ),
    (
        7,
        106,
        103,
        302,
        'HK2',
        '2024-2025',
        'Thứ 4',
        1,
        2
    );

-- Thêm dữ liệu mẫu hạnh kiểm
INSERT INTO
    HanhKiem (
        MaHS,
        MaNK,
        HocKy,
        XepLoai,
        GhiChu
    )
VALUES
    -- Lớp 10A1
    (
        1,
        1,
        1,
        'Tốt',
        'Tích cực trong học tập và sinh hoạt.'
    ),
    (
        1,
        1,
        2,
        'Tốt',
        'Giữ vững phong độ.'
    ),
    (
        2,
        1,
        1,
        'Khá',
        'Đôi khi còn nói chuyện riêng.'
    ),
    (
        2,
        1,
        2,
        'Tốt',
        'Tiến bộ rõ rệt.'
    ),
    -- Lớp 11A1
    (
        3,
        1,
        1,
        'Trung bình',
        'Thiếu tập trung.'
    ),
    (
        3,
        1,
        2,
        'Khá',
        'Đã cải thiện tốt.'
    ),
    -- Lớp 12A1
    (
        4,
        1,
        1,
        'Khá',
        'Gương mẫu trong lớp.'
    ),
    (
        4,
        1,
        2,
        'Tốt',
        'Tham gia tích cực các hoạt động.'
    ),
    -- Lớp 12A1 (HS khác)
    (
        5,
        1,
        1,
        'Yếu',
        'Thường xuyên đi trễ.'
    ),
    (
        5,
        1,
        2,
        'Trung bình',
        'Đã cố gắng hơn.'
    );

INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
SELECT DISTINCT
    MaGV,
    MaMon,
    MaLop,
    1 AS MaNK, -- Giả định tất cả thuộc Niên Khóa 1
    CASE
        WHEN HocKy = 'HK1' THEN 1
        WHEN HocKy = 'HK2' THEN 2
        ELSE 0 -- Xử lý nếu có Hè (nhưng ở đây là 1 hoặc 2)
    END AS HocKyInt
FROM ThoiKhoaBieu
WHERE
    NamHoc = '2024-2025';

-- === VIEW PHỤC VỤ HIỂN THỊ/BÁO CÁO ===

-- Điểm từng môn theo HK (từ Diem)
CREATE OR REPLACE VIEW v_DiemMon_HK AS
SELECT
    d.MaDiem,
    d.MaHS,
    hs.HoTen AS TenHS,
    d.MaMon,
    m.TenMon,
    d.HocKy,
    d.MaNK,
    d.DiemMieng,
    d.Diem15p,
    d.DiemGiuaKy,
    d.DiemCuoiKy,
    d.DiemTB,
    d.XepLoai
FROM
    Diem d
    JOIN HocSinh hs ON hs.MaHS = d.MaHS
    JOIN MonHoc m ON m.MaMon = d.MaMon;

-- TB học kỳ (trung bình các môn theo HS)
CREATE OR REPLACE VIEW v_TBHocKy_TheoHS AS
SELECT
    d.MaHS,
    hs.HoTen AS TenHS,
    d.MaNK,
    d.HocKy,
    ROUND(AVG(d.DiemTB), 1) AS DiemTBHK,
    CASE
        WHEN AVG(d.DiemTB) IS NULL THEN NULL
        WHEN AVG(d.DiemTB) >= 8.0 THEN 'Giỏi'
        WHEN AVG(d.DiemTB) >= 6.5 THEN 'Khá'
        WHEN AVG(d.DiemTB) >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiHK
FROM Diem d
    JOIN HocSinh hs ON hs.MaHS = d.MaHS
GROUP BY
    d.MaHS,
    hs.HoTen,
    d.MaNK,
    d.HocKy;

-- TB cả năm theo TỪNG MÔN (kết hợp HK1/HK2 theo trọng số trong bảng TrongSoNam)
CREATE OR REPLACE VIEW v_DiemMon_CaNam AS
SELECT
    hk1.MaHS,
    hs.HoTen AS TenHS,
    hk1.MaMon,
    m.TenMon,
    hk1.MaNK,
    hk1.DiemTB AS TB_HK1,
    hk2.DiemTB AS TB_HK2,
    ROUND(
        CASE
            WHEN hk1.DiemTB IS NOT NULL
            AND hk2.DiemTB IS NOT NULL THEN hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
            WHEN hk1.DiemTB IS NOT NULL THEN hk1.DiemTB
            WHEN hk2.DiemTB IS NOT NULL THEN hk2.DiemTB
            ELSE NULL
        END,
        1
    ) AS DiemTBCaNam,
    CASE
        WHEN (
            hk1.DiemTB IS NULL
            AND hk2.DiemTB IS NULL
        ) THEN NULL
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 8.0
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 8.0
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 8.0
            )
        ) THEN 'Giỏi'
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 6.5
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 6.5
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 6.5
            )
        ) THEN 'Khá'
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 5.0
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 5.0
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 5.0
            )
        ) THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 1
    ) hk1
    LEFT JOIN (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 2
    ) hk2 ON hk1.MaHS = hk2.MaHS
    AND hk1.MaMon = hk2.MaMon
    AND hk1.MaNK = hk2.MaNK
    LEFT JOIN HocSinh hs ON hs.MaHS = hk1.MaHS
    LEFT JOIN MonHoc m ON m.MaMon = hk1.MaMon
    LEFT JOIN TrongSoNam ts ON ts.MaNK = hk1.MaNK
UNION
SELECT
    hk2.MaHS,
    hs.HoTen AS TenHS,
    hk2.MaMon,
    m.TenMon,
    hk2.MaNK,
    NULL AS TB_HK1,
    hk2.DiemTB AS TB_HK2,
    ROUND(hk2.DiemTB, 1) AS DiemTBCaNam,
    CASE
        WHEN hk2.DiemTB IS NULL THEN NULL
        WHEN hk2.DiemTB >= 8.0 THEN 'Giỏi'
        WHEN hk2.DiemTB >= 6.5 THEN 'Khá'
        WHEN hk2.DiemTB >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 2
    ) hk2
    LEFT JOIN (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 1
    ) hk1 ON hk2.MaHS = hk1.MaHS
    AND hk2.MaMon = hk1.MaMon
    AND hk2.MaNK = hk1.MaNK
    LEFT JOIN HocSinh hs ON hs.MaHS = hk2.MaHS
    LEFT JOIN MonHoc m ON m.MaMon = hk2.MaMon
    LEFT JOIN TrongSoNam ts ON ts.MaNK = hk2.MaNK
WHERE
    hk1.MaHS IS NULL;

-- TB cả năm theo HỌC SINH (trung bình các môn)
CREATE OR REPLACE VIEW v_TBCaNam_TheoHS AS
SELECT
    MaHS,
    TenHS,
    MaNK,
    ROUND(AVG(DiemTBCaNam), 1) AS DiemTBCaNam,
    CASE
        WHEN AVG(DiemTBCaNam) IS NULL THEN NULL
        WHEN AVG(DiemTBCaNam) >= 8.0 THEN 'Giỏi'
        WHEN AVG(DiemTBCaNam) >= 6.5 THEN 'Khá'
        WHEN AVG(DiemTBCaNam) >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM v_DiemMon_CaNam
GROUP BY
    MaHS,
    TenHS,
    MaNK;

-- === INDEX GỢI Ý (tăng tốc truy vấn) ===
CREATE INDEX idx_diem_hs_mon_hk_nk ON Diem (MaHS, MaMon, HocKy, MaNK);

CREATE INDEX idx_diem_hs_nk_hk ON Diem (MaHS, MaNK, HocKy);

-- view tổng hợp điểm + hạnh kiểm
CREATE OR REPLACE VIEW v_XepLoaiTongHop AS
SELECT
    hs.MaHS,
    hs.HoTen AS TenHS,
    hk.MaNK,
    nk.NamBatDau,
    nk.NamKetThuc,
    hk.HocKy,
    hk.XepLoai AS HanhKiem,
    d.XepLoaiHK AS HocLuc
FROM
    HanhKiem hk
    JOIN HocSinh hs ON hs.MaHS = hk.MaHS
    JOIN NienKhoa nk ON nk.MaNK = hk.MaNK
    LEFT JOIN v_TBHocKy_TheoHS d ON d.MaHS = hk.MaHS
    AND d.MaNK = hk.MaNK
    AND d.HocKy = hk.HocKy;

ALTER TABLE Diem ADD COLUMN GhiChu TEXT DEFAULT NULL;

-- === BẢNG NHẬN XÉT (Nhận xét chung của giáo viên cho một học sinh theo HK/NK) ===
CREATE TABLE DiemNhanXet (
    MaHS INT,
    MaNK INT,
    HocKy INT,
    GhiChu TEXT DEFAULT NULL,
    PRIMARY KEY (MaHS, MaNK, HocKy),
    CONSTRAINT fk_nx_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_nx_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

-- CREATE TABLE nguoidung (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
--     mat_khau VARCHAR(255) NOT NULL,
--     ho_ten VARCHAR(100),
--     vai_tro ENUM('quan_tri_vien', 'giao_vien', 'hoc_sinh') NOT NULL
-- );

-- INSERT INTO nguoidung (ten_dang_nhap, mat_khau, ho_ten, vai_tro)
-- VALUES
-- ('admin', '123456', 'Nguyễn Quản Trị', 'quan_tri_vien'),
-- ('gv001', '123456', 'Trần Giáo Viên', 'giao_vien'),
-- ('hs001', '123456', 'Lê Học Sinh', 'hoc_sinh');

-- Tạo bảng TaiKhoan (tài khoản đăng nhập)
CREATE TABLE TaiKhoan (
    MaTK INT PRIMARY KEY AUTO_INCREMENT,
    TenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    MatKhau VARCHAR(255) NOT NULL,
    VaiTro ENUM(
        'Admin',
        'GiaoVien',
        'HocSinh'
    ) NOT NULL,
    TrangThai TINYINT DEFAULT 1, -- 1: hoạt động, 0: bị khóa
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- Tạo bảng liên kết TaiKhoan_HocSinh và TaiKhoan_GiaoVien
CREATE TABLE TaiKhoan_HocSinh (
    MaTK INT PRIMARY KEY,
    MaHS INT NOT NULL,
    FOREIGN KEY (MaTK) REFERENCES TaiKhoan (MaTK) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE TaiKhoan_GiaoVien (
    MaTK INT PRIMARY KEY,
    MaGV INT NOT NULL,
    FOREIGN KEY (MaTK) REFERENCES TaiKhoan (MaTK) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON DELETE CASCADE ON UPDATE CASCADE
);
-- Thêm dữ liệu mẫu (demo đăng nhập)
-- Admin
INSERT INTO
    TaiKhoan (TenDangNhap, MatKhau, VaiTro)
VALUES ('admin', '123456', 'Admin');

-- Giáo viên có tài khoản riêng
INSERT INTO
    TaiKhoan (TenDangNhap, MatKhau, VaiTro)
VALUES ('gv101', '123456', 'GiaoVien'),
    ('gv102', '123456', 'GiaoVien'),
    ('gv103', '123456', 'GiaoVien');

-- Học sinh có tài khoản riêng
INSERT INTO
    TaiKhoan (TenDangNhap, MatKhau, VaiTro)
VALUES ('hs001', '123456', 'HocSinh'),
    ('hs002', '123456', 'HocSinh'),
    ('hs003', '123456', 'HocSinh');

-- Liên kết
INSERT INTO
    TaiKhoan_GiaoVien (MaTK, MaGV)
VALUES (2, 101),
    (3, 102),
    (4, 103);

INSERT INTO
    TaiKhoan_HocSinh (MaTK, MaHS)
VALUES (5, 1),
    (6, 2),
    (7, 3);

-- Truy vấn test tài khoản liên kết
-- Lấy danh sách tài khoản giáo viên
-- SELECT tk.MaTK, tk.TenDangNhap, gv.HoTen, gv.Emailphancongday
-- FROM TaiKhoan tk
-- JOIN TaiKhoan_GiaoVien tkgv ON tkgv.MaTK = tk.MaTK
-- JOIN GiaoVien gv ON gv.MaGV = tkgv.MaGV;
-- Lấy danh sách tài khoản học sinh
-- SELECT tk.MaTK, tk.TenDangNhap, hs.HoTen, hs.Email
-- FROM TaiKhoan tk
-- JOIN TaiKhoan_HocSinh tkhs ON tkhs.MaTK = tk.MaTK
-- JOIN HocSinh hs ON hs.MaHS = tkhs.MaHS;

-- Bước 1: Thêm cột vào MonHoc để phân loại môn
ALTER TABLE MonHoc
ADD COLUMN LoaiMon ENUM('TinhDiem', 'DanhGia') NOT NULL DEFAULT 'TinhDiem'
COMMENT 'TinhDiem = Môn tính điểm số, DanhGia = Môn chỉ đánh giá Đ/KĐ';

-- Cập nhật các môn đánh giá (Thể dục, GDCD)
UPDATE MonHoc
SET LoaiMon = 'DanhGia'
WHERE TenMon IN ('Thể dục', 'Giáo dục công dân');

-- Bước 2: Thêm cột vào Diem để lưu kết quả Đ/KĐ
ALTER TABLE Diem
ADD COLUMN KetQuaDanhGia ENUM('Đ', 'KĐ') DEFAULT NULL
COMMENT 'Chỉ dùng cho các môn có LoaiMon = DanhGia';