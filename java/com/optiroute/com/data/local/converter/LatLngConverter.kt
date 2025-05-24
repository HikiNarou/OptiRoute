package com.optiroute.com.data.local.converter

import androidx.room.TypeConverter
import com.optiroute.com.domain.model.LatLng
import com.google.gson.Gson // Kita akan tambahkan Gson ke build.gradle jika belum ada
import com.google.gson.reflect.TypeToken

/**
 * TypeConverter untuk Room Database.
 * Mengonversi objek LatLng kustom menjadi String (format JSON) untuk disimpan di database,
 * dan sebaliknya, mengonversi String kembali menjadi objek LatLng saat dibaca dari database.
 *
 * Ini diperlukan karena Room hanya dapat menyimpan tipe data primitif atau tipe yang dikenali secara default.
 */
class LatLngConverter {

    private val gson = Gson()

    /**
     * Mengonversi objek LatLng menjadi representasi String (JSON).
     *
     * @param latLng Objek LatLng yang akan dikonversi.
     * @return String JSON yang merepresentasikan LatLng, atau null jika inputnya null.
     */
    @TypeConverter
    fun fromLatLng(latLng: LatLng?): String? {
        return latLng?.let { gson.toJson(it) }
    }

    /**
     * Mengonversi representasi String (JSON) kembali menjadi objek LatLng.
     *
     * @param jsonString String JSON yang akan dikonversi.
     * @return Objek LatLng hasil konversi, atau null jika input String null atau kosong.
     */
    @TypeConverter
    fun toLatLng(jsonString: String?): LatLng? {
        if (jsonString.isNullOrEmpty()) {
            return null
        }
        // Mendefinisikan tipe target untuk deserialisasi Gson.
        val type = object : TypeToken<LatLng>() {}.type
        return gson.fromJson(jsonString, type)
    }
}
