package com.optiroute.com

import android.app.Application
import timber.log.Timber
import com.optiroute.com.BuildConfig // Pastikan ini yang Anda pilih atau tambahkan
import dagger.hilt.android.HiltAndroidApp

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
        // Timber adalah pustaka logging yang kuat dan fleksibel.
        // Pada build rilis, ini tidak akan melakukan apa-apa jika tidak ada Tree yang di-plant.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("OptiRouteApplication onCreate: Timber initialized for debug build.")
        } else {
            // Di build rilis, Anda mungkin ingin menanam Tree yang berbeda untuk
            // melaporkan error ke layanan seperti Crashlytics.
            // Timber.plant(CrashReportingTree())
            // Untuk saat ini, kita tidak akan melakukan logging di rilis untuk kesederhanaan.
        }

        Timber.i("OptiRouteApplication: Application instance created.")
    }

    /**
     * Contoh Tree untuk Crashlytics (jika Anda menggunakannya di masa mendatang).
     * Ini hanya contoh dan tidak akan digunakan kecuali Anda mengintegrasikan Crashlytics.
     */
    // private class CrashReportingTree : Timber.Tree() {
    //     override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    //         if (priority == Log.VERBOSE || priority == Log.DEBUG) {
    //             return
    //         }
    //
    //         // Kirim log error ke Crashlytics atau sistem pelaporan lainnya
    //         // FirebaseCrashlytics.getInstance().log(message)
    //         // if (t != null) {
    //         //     FirebaseCrashlytics.getInstance().recordException(t)
    //         // }
    //     }
    // }
}
