package com.optiroute.com.domain.model

/**
 * Sealed class generik untuk merepresentasikan hasil dari suatu operasi,
 * yang bisa berupa Success (berhasil) atau Error (gagal).
 *
 * @param T Tipe data dari hasil jika operasi berhasil.
 */
sealed class AppResult<out T> {
    /**
     * Merepresentasikan hasil operasi yang berhasil.
     * @property data Data hasil operasi.
     */
    data class Success<out T>(val data: T) : AppResult<T>()

    /**
     * Merepresentasikan hasil operasi yang gagal.
     * @property exception Throwable yang menyebabkan kegagalan.
     * @property message Pesan error kustom (opsional).
     */
    data class Error(val exception: Throwable, val message: String? = null) : AppResult<Nothing>()
}
