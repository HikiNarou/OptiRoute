package com.optiroute.com.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.optiroute.com.data.local.converter.LatLngConverter // Akan kita buat selanjutnya
import com.optiroute.com.data.local.dao.CustomerDao
import com.optiroute.com.data.local.dao.DepotDao
import com.optiroute.com.data.local.dao.VehicleDao
import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.data.local.entity.VehicleEntity

/**
 * Kelas database utama untuk aplikasi OptiRoute menggunakan Room.
 *
 * Anotasi @Database mendefinisikan entitas yang termasuk dalam database dan versi database.
 * Setiap kali skema database diubah (misalnya, menambah kolom, mengubah tipe data),
 * versi database harus dinaikkan dan strategi migrasi harus disediakan.
 *
 * Anotasi @TypeConverters digunakan untuk mendaftarkan konverter tipe kustom,
 * misalnya untuk mengubah objek LatLng menjadi tipe data yang dapat disimpan oleh SQLite.
 *
 * Properti 'exportSchema' disetel ke false untuk menghindari ekspor skema database ke
 * file JSON selama kompilasi. Ini berguna untuk proyek yang lebih kecil atau jika Anda tidak
 * berencana untuk memeriksa skema ke dalam sistem kontrol versi Anda. Untuk proyek produksi
 * yang lebih besar, disarankan untuk menyetelnya ke true dan mengelola file skema.
 */
@Database(
    entities = [
        DepotEntity::class,
        VehicleEntity::class,
        CustomerEntity::class
    ],
    version = 1, // Versi awal database
    exportSchema = false // Setel ke true jika Anda ingin mengekspor skema untuk migrasi
)
@TypeConverters(LatLngConverter::class) // Mendaftarkan LatLngConverter
abstract class OptiRouteDatabase : RoomDatabase() {

    /**
     * Menyediakan akses ke DepotDao.
     * Room akan mengimplementasikan metode ini secara otomatis.
     *
     * @return Instans dari DepotDao.
     */
    abstract fun depotDao(): DepotDao

    /**
     * Menyediakan akses ke VehicleDao.
     *
     * @return Instans dari VehicleDao.
     */
    abstract fun vehicleDao(): VehicleDao

    /**
     * Menyediakan akses ke CustomerDao.
     *
     * @return Instans dari CustomerDao.
     */
    abstract fun customerDao(): CustomerDao

    companion object {
        // Nama file database SQLite.
        const val DATABASE_NAME = "optiroute_db"

        // Instance volatile dari database untuk memastikan visibilitas antar thread.
        // Volatile berarti bahwa penulisan ke variabel ini segera terlihat oleh thread lain.
        @Volatile
        private var INSTANCE: OptiRouteDatabase? = null

        /**
         * Mendapatkan instance singleton dari OptiRouteDatabase.
         * Menggunakan pola singleton untuk memastikan hanya ada satu instance database
         * yang dibuat di seluruh aplikasi. Ini penting untuk performa dan konsistensi data.
         *
         * @param context Context aplikasi.
         * @return Instance singleton dari OptiRouteDatabase.
         */
        fun getInstance(context: Context): OptiRouteDatabase {
            // Jika INSTANCE sudah ada, kembalikan.
            // Jika belum, buat instance baru dalam blok synchronized untuk thread-safety.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OptiRouteDatabase::class.java,
                    DATABASE_NAME
                )
                    // Strategi migrasi (jika diperlukan di masa mendatang).
                    // Untuk versi awal, kita bisa menggunakan fallbackToDestructiveMigration
                    // yang akan menghapus dan membuat ulang database jika versi berubah.
                    // Ini HANYA untuk pengembangan. Untuk rilis, Anda harus menyediakan
                    // Migrasi yang tepat.
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Contoh migrasi
                    .fallbackToDestructiveMigration() // HATI-HATI: Menghapus data saat skema berubah tanpa migrasi
                    .build()
                INSTANCE = instance
                // Kembalikan instance yang baru dibuat.
                instance
            }
        }

        // Contoh placeholder untuk migrasi (jika diperlukan di masa mendatang)
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         // Implementasi migrasi dari versi 1 ke 2
        //         // Contoh: db.execSQL("ALTER TABLE vehicles ADD COLUMN new_feature TEXT")
        //     }
        // }
    }
}
