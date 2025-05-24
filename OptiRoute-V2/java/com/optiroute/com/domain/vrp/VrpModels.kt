package com.optiroute.com.domain.vrp

import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.domain.model.LatLng

/**
 * Detail satu rute dalam solusi VRP.
 *
 * @property vehicle Kendaraan yang ditugaskan untuk rute ini.
 * @property stops Daftar pelanggan yang dikunjungi secara berurutan dalam rute ini.
 * @property totalDistance Total jarak tempuh untuk rute ini.
 * @property totalDemand Total permintaan yang dilayani oleh rute ini.
 * @property routePath Daftar LatLng yang merepresentasikan jalur rute (opsional, untuk penggambaran di peta).
 */
data class RouteDetail(
    val vehicle: VehicleEntity,
    val stops: List<CustomerEntity>, // Urutan pemberhentian, dimulai dan diakhiri di depot (secara implisit)
    val totalDistance: Double,
    val totalDemand: Double,
    val routePath: List<LatLng>? = null // Opsional, untuk menggambar polyline
)

/**
 * Solusi keseluruhan untuk Vehicle Routing Problem.
 *
 * @property routes Daftar semua rute yang dihasilkan.
 * @property unassignedCustomers Daftar pelanggan yang tidak dapat dimasukkan ke dalam rute mana pun.
 * @property totalOverallDistance Total jarak dari semua rute.
 * @property calculationTimeMillis Waktu yang dibutuhkan untuk menghitung solusi (opsional).
 */
data class VrpSolution(
    val routes: List<RouteDetail>,
    val unassignedCustomers: List<CustomerEntity>,
    val totalOverallDistance: Double,
    val calculationTimeMillis: Long? = null,
    val planId: String // ID unik untuk rencana rute ini
)
