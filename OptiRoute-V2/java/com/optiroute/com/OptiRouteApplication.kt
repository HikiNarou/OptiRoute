package com.optiroute.com

import android.app.Application
import com.optiroute.com.BuildConfig // Pastikan impor ini benar
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Kelas Aplikasi kustom untuk OptiRoute.
 *
 * Anotasi @HiltAndroidApp mengaktifkan injeksi dependensi Hilt di seluruh aplikasi.
 * Kelas ini merupakan titik masuk utama untuk aplikasi dan digunakan untuk inisialisasi
 * pustaka atau konfigurasi tingkat aplikasi, seperti Timber untuk logging.
 */
@HiltAndroidApp
class OptiRouteApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Timber untuk logging jika ini adalah build debug.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("OptiRouteApplication onCreate: Timber initialized for debug build.")
        } else {
            // Di build rilis, Anda mungkin ingin menanam Tree yang berbeda untuk
            // melaporkan error ke layanan seperti Crashlytics.
            // Timber.plant(CrashReportingTree())
        }
        Timber.i("OptiRouteApplication: Application instance created.")
    }
}
