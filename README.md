# Kasir HM - Point of Sales

Aplikasi Kasir (Point of Sales) untuk Toko Kelontong/Kantin berbasis Java Swing dengan tampilan modern menggunakan FlatLaf Dark Theme.

## 📋 Deskripsi

Kasir HM adalah aplikasi kasir desktop yang dirancang untuk membantu pengelolaan transaksi penjualan di toko kelontong atau kantin. Aplikasi ini memiliki fitur lengkap mulai dari manajemen produk, transaksi penjualan, hingga cetak struk.

## ✨ Fitur Utama

### 🛒 Point of Sales (POS)
- Keranjang belanja dengan fitur menambah beberapa item sebelum pembayaran
- Penghitungan total, diskon (persentase), dan uang kembalian secara otomatis
- **Barcode Scanner** terintegrasi menggunakan webcam untuk mempercepat input barang
- Mendukung berbagai kamera (webcam built-in, USB camera, atau HP Android via USB)
- Continuous scanning - scan multiple items tanpa perlu restart kamera

### 📦 Manajemen Stok Barang
- CRUD produk (Create, Read, Update, Delete)
- Notifikasi produk dengan stok hampir habis (stok minimum)
- Pencarian produk berdasarkan nama atau barcode
- Kategori produk

### 📊 Dashboard & Laporan
- Ringkasan penjualan harian (jumlah transaksi & total pendapatan)
- Grafik penjualan 7 hari terakhir menggunakan JFreeChart
- Daftar produk dengan stok rendah
- Indikator performa toko

### 📋 Riwayat Transaksi
- Daftar seluruh transaksi dengan filter tanggal
- Detail transaksi lengkap
- Pencarian transaksi

### 🧾 Cetak Struk
- Preview struk sebelum cetak
- Cetak ke printer thermal/receipt
- Format struk profesional

### 🔐 Autentikasi
- Login dengan username dan password
- Role pengguna (Admin & Cashier)

### ⚙️ Konfigurasi
- File konfigurasi eksternal (`config.properties`)
- Setting database MySQL dapat diubah tanpa compile ulang

## 🛠️ Teknologi yang Digunakan

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Java 17 |
| Build Tool | Maven |
| Database | MySQL 8.x |
| UI Framework | Java Swing + [FlatLaf](https://www.formdev.com/flatlaf/) Dark Theme |
| Barcode Scanner | [ZXing](https://github.com/zxing/zxing) + [Webcam Capture](https://github.com/sarxos/webcam-capture) |
| Grafik | [JFreeChart](https://www.jfree.org/jfreechart/) |

## 📁 Struktur Project

```
kasir-hm/
├── src/main/java/com/itsazni/kasir/hm/
│   ├── KasirHm.java           # Main class
│   ├── dao/                   # Data Access Objects
│   │   ├── DatabaseConnection.java
│   │   ├── ProductDAO.java
│   │   ├── TransactionDAO.java
│   │   └── UserDAO.java
│   ├── models/                # Entity models
│   │   ├── Product.java
│   │   ├── Transaction.java
│   │   ├── TransactionItem.java
│   │   └── User.java
│   ├── ui/                    # User Interface
│   │   ├── LoginPanel.java
│   │   ├── MainFrame.java
│   │   ├── DashboardPanel.java
│   │   ├── POSPanel.java
│   │   ├── ProductManagementPanel.java
│   │   ├── TransactionHistoryPanel.java
│   │   ├── BarcodeScannerDialog.java
│   │   └── UIConstants.java
│   └── utils/                 # Utilities
│       ├── AppConfig.java
│       ├── BarcodeScanner.java
│       ├── CurrencyUtils.java
│       └── ReceiptPrinter.java
├── src/main/resources/
│   └── database_schema.sql    # SQL schema
├── pom.xml                    # Maven configuration
└── README.md
```

## 🚀 Instalasi & Menjalankan

### Prasyarat
- Java JDK 17 atau lebih baru
- MySQL Server 8.x
- Maven 3.x
- Webcam (optional, untuk fitur barcode scanner)

### Langkah Instalasi

1. **Clone repository**
   ```bash
   git clone https://github.com/ItsAzni/KasirHM.git
   cd KasirHM
   ```

2. **Buat database MySQL**
   ```bash
   mysql -u root -p < database_schema.sql
   ```
   
   Atau jalankan SQL berikut di MySQL client:
   ```sql
   CREATE DATABASE IF NOT EXISTS kasir_hm;
   USE kasir_hm;
   -- (lihat file database_schema.sql untuk schema lengkap)
   ```

3. **Build project**
   ```bash
   mvn clean install
   ```

4. **Jalankan aplikasi**
   ```bash
   java -jar target/kasir-hm-1.0-SNAPSHOT.jar
   ```

### Konfigurasi Database

Saat pertama kali dijalankan, file `config.properties` akan dibuat otomatis di folder yang sama dengan JAR. Edit file ini untuk menyesuaikan koneksi database:

```properties
# Kasir HM Configuration
db.host=localhost
db.port=3306
db.name=kasir_hm
db.user=root
db.password=
app.name=Kasir HM
app.version=1.0
```

## 👤 Login Default

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |

## 📸 Screenshot

*Coming soon*

## 📝 Database Schema

Aplikasi menggunakan 4 tabel utama:
- **users** - Data pengguna (admin/kasir)
- **products** - Data produk dengan barcode
- **transactions** - Header transaksi
- **transaction_items** - Detail item per transaksi

## 🔧 Development

### Build dari source
```bash
mvn clean compile
```

### Jalankan dari IDE
Main class: `com.itsazni.kasir.hm.KasirHm`

### Package JAR dengan dependencies
```bash
mvn clean package
```

## 📄 Lisensi

Project ini dibuat untuk keperluan Tugas Akhir (TA).

## 👨‍💻 Author

**[ItsAzni](https://github.com/ItsAzni)**

---

*Kasir HM - Solusi Point of Sales untuk Toko Kelontong Modern*
