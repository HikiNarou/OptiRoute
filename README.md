# SISTEM OPTIMASI RUTE PENGIRIMAN BARANG BERBASIS MOBILE DENGAN MENGGUNAKAN METODE VEHICLE ROUTING PROGRAM

## Analisis Pemrograman Berorientasi Objek

Proyek ini merupakan bagian dari studi **Program Studi D-IV Manajemen Informatika, Jurusan Manajemen Informatika, Politeknik Negeri Sriwijaya, Palembang 2025**. [cite: 1, 3]

**Anggota Kelompok 8:**
* Andhika Pratama (062340833112) [cite: 2]
* Kgs. M. Raihan Nurhadi (062340833117) [cite: 2]
* Mia Hardianti (062340833120) [cite: 2]
* Natasya Bella Shavira (062340833126) [cite: 2]

## Latar Belakang

Proses distribusi dan pengiriman barang merupakan salah satu komponen biaya operasional yang signifikan bagi UMKM. [cite: 4] Pengelolaan rute pengiriman yang kurang optimal dapat mengakibatkan pemborosan sumber daya seperti waktu tempuh, konsumsi bahan bakar, dan utilisasi tenaga kerja. [cite: 5] Inefisiensi ini berpotensi menurunkan tingkat kepuasan pelanggan akibat keterlambatan pengiriman[cite: 6], yang dapat berdampak buruk pada reputasi bisnis. [cite: 8] Bagi UMKM dengan keterbatasan sumber daya, tantangan dalam mengelola logistik pengiriman secara efektif menjadi lebih terasa. [cite: 9] Perencanaan rute manual, terutama saat volume pengiriman dan jumlah pelanggan meningkat, seringkali menjadi rumit dan berisiko menimbulkan inefisiensi. [cite: 10] Metode manual kurang mampu mengakomodasi faktor lapangan seperti kondisi lalu lintas, prioritas pengiriman, atau batasan muatan kendaraan. [cite: 11] Semakin besar volume pengiriman, semakin kompleks perencanaan yang diperlukan, meningkatkan risiko kesalahan dan pemborosan. [cite: 12, 13]

Penerapan teknologi informasi untuk mengoptimalkan rute pengiriman bisa menjadi solusi penting. [cite: 14] Konsep utama yang sering digunakan adalah *Vehicle Routing Problem* (VRP)[cite: 15], sebuah persoalan optimasi untuk merancang rute perjalanan yang paling efisien bagi armada kendaraan untuk melayani sejumlah pelanggan dengan berbagai batasan operasional. [cite: 16, 17] Penerapan sistem optimasi rute terbukti bisa memberikan manfaat signifikan[cite: 18], dengan potensi penghematan biaya transportasi 5%-20% dan pemanfaatan aset yang lebih baik. [cite: 19] Oleh karena itu, teknologi VRP dapat memberikan keuntungan kompetitif bagi UMKM melalui efisiensi biaya dan peningkatan layanan pelanggan. [cite: 20]

## Rumusan Masalah

Berdasarkan latar belakang di atas, terdapat beberapa rumusan masalah yaitu:
1.  Bagaimana merancang algoritma optimasi rute yang dapat meminimalkan total jarak/tempuh dan biaya operasional? [cite: 22]
2.  Bagaimana membuat sistem optimasi rute dengan menggunakan algoritma VRP? [cite: 23]
3.  Bagaimana mengimplementasikan algoritma VRP tersebut dalam sebuah sistem berbasis aplikasi yang *user-friendly* untuk UMKM? [cite: 24]

## Batasan Masalah

1.  Sistem hanya mendukung armada hingga maksimum 10 kendaraan. [cite: 25]
2.  Setiap kendaraan memiliki kapasitas muatan tetap (misal volume atau berat) yang ditentukan di awal. [cite: 26]
3.  Fokus pada satu kota atau area geografi terbatas (misalnya dalam radius 20 km dari depot). [cite: 27]
4.  Menggunakan koordinat GPS statis; tidak mempertimbangkan perubahan *real-time* (misal *dynamic traffic*). [cite: 28]
5.  Semua titik pelanggan dianggap memiliki prioritas yang sama (*no time windows*). [cite: 29]

## Tujuan

Sistem optimasi rute pengiriman barang memiliki beberapa tujuan utama, diantaranya:
1.  Menyusun model matematis *Vehicle Routing Problem* sesuai kebutuhan UMKM. [cite: 31]
2.  Merancang dan menguji algoritma untuk menemukan rute optimal. [cite: 31]
3.  Membangun antarmuka aplikasi yang memungkinkan input data pelanggan, armada, dan menampilkan hasil rute secara interaktif. [cite: 32]

## Manfaat

Sistem optimasi rute pengiriman barang berbasis aplikasi menawarkan beberapa manfaat, yaitu:
1.  Membantu UMKM mengoptimalkan rute pengiriman sehingga dapat menghemat waktu, bahan bakar, dan tenaga kerja. [cite: 33]
2.  Meningkatkan kepuasan pelanggan melalui pengiriman yang lebih cepat dan terjadwal dengan baik. [cite: 34]
3.  Memberikan solusi praktis berbasis teknologi informasi untuk meningkatkan daya saing UMKM di bidang logistik. [cite: 35]
