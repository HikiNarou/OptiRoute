package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.DepotDao
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.domain.repository.DepotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi konkret dari DepotRepository.
 * Kelas ini bertanggung jawab untuk berinteraksi dengan DepotDao untuk operasi data depot.
 * Menggunakan Dispatchers.IO untuk semua operasi database untuk memastikan tidak memblokir main thread.
 *
 * @param depotDao Instance dari DepotDao yang diinjeksi oleh Hilt.
 */
@Singleton // Repository biasanya singleton karena mereka stateless dan mengelola akses ke data source singleton (database)
class DepotRepositoryImpl @Inject constructor(
    private val depotDao: DepotDao
) : DepotRepository {

    override fun getDepot(): Flow<DepotEntity?> {
        Timber.d("DepotRepository: Getting depot from DAO.")
        return depotDao.getDepot()
    }

    override suspend fun saveDepot(
        name: String,
        location: LatLng,
        address: String?,
        notes: String?
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validasi dasar sebelum menyimpan
            if (name.isBlank()) {
                Timber.w("DepotRepository: Save depot failed - name is blank.")
                return@withContext AppResult.Error(IllegalArgumentException("Nama depot tidak boleh kosong."))
            }
            if (!location.isValid()) {
                Timber.w("DepotRepository: Save depot failed - location is invalid.")
                return@withContext AppResult.Error(IllegalArgumentException("Lokasi depot tidak valid."))
            }

            val depotEntity = DepotEntity(
                id = DepotEntity.DEFAULT_DEPOT_ID, // Selalu gunakan ID default untuk depot tunggal
                name = name,
                location = location,
                address = address,
                notes = notes
            )
            depotDao.upsertDepot(depotEntity)
            Timber.i("DepotRepository: Depot saved/updated successfully: %s", name)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "DepotRepository: Error saving/updating depot.")
            AppResult.Error(e, "Gagal menyimpan depot: ${e.message}")
        }
    }

    override suspend fun deleteDepot(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            depotDao.deleteDepot()
            Timber.i("DepotRepository: Depot deleted successfully.")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "DepotRepository: Error deleting depot.")
            AppResult.Error(e, "Gagal menghapus depot: ${e.message}")
        }
    }

    override suspend fun clearAllDepots(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            depotDao.clearAllDepots()
            Timber.i("DepotRepository: All depots cleared successfully.")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "DepotRepository: Error clearing all depots.")
            AppResult.Error(e, "Gagal membersihkan data depot: ${e.message}")
        }
    }
}
