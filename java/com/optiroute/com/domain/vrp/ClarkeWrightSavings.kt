package com.optiroute.com.domain.vrp

import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.domain.model.LatLng
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.associate
import kotlin.collections.filterNot
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.lastOrNull
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.set
import kotlin.collections.sortedByDescending
import kotlin.collections.sumOf
import kotlin.collections.toList
import kotlin.collections.toMutableList
import kotlin.collections.toSet

@Singleton
class ClarkeWrightSavings @Inject constructor() {

    private data class RouteSegment(
        var customers: MutableList<CustomerEntity>, // Urutan pelanggan dalam segmen
        var currentDemand: Double,
        var vehicleId: Int? = null // Kendaraan yang ditugaskan ke rute ini
    ) {
        val firstCustomer: CustomerEntity? get() = customers.firstOrNull()
        val lastCustomer: CustomerEntity? get() = customers.lastOrNull()

        fun calculateTotalDistance(depotLocation: LatLng, distanceCalc: (LatLng, LatLng) -> Double): Double {
            if (customers.isEmpty()) return 0.0
            var dist = distanceCalc(depotLocation, customers.first().location) // Depot to first
            for (i in 0 until customers.size - 1) {
                dist += distanceCalc(customers[i].location, customers[i + 1].location)
            }
            dist += distanceCalc(customers.last().location, depotLocation) // Last to Depot
            return dist
        }
    }

    private data class Saving(val cust1: CustomerEntity, val cust2: CustomerEntity, val value: Double)

    suspend fun solve(
        depot: DepotEntity,
        customers: List<CustomerEntity>,
        vehicles: List<VehicleEntity>
    ): VrpSolution {
        val startTime = System.currentTimeMillis()
        Timber.d("Clarke-Wright (Enhanced w/ 2-opt): Starting. Customers: ${customers.size}, Vehicles: ${vehicles.size}")

        if (customers.isEmpty()) {
            return VrpSolution(emptyList(), emptyList(), 0.0, 0, UUID.randomUUID().toString())
        }
        if (vehicles.isEmpty()) {
            return VrpSolution(emptyList(), customers, 0.0, 0, UUID.randomUUID().toString())
        }

        val depotLoc = depot.location
        val distFunc = DistanceUtils.calculateDistance

        // 1. Hitung semua penghematan (savings)
        val savingsList = mutableListOf<Saving>()
        for (i in customers.indices) {
            for (j in (i + 1) until customers.size) {
                val cust1 = customers[i]
                val cust2 = customers[j]
                val savingVal = distFunc(depotLoc, cust1.location) + distFunc(depotLoc, cust2.location) - distFunc(cust1.location, cust2.location)
                if (savingVal > 0) {
                    savingsList.add(Saving(cust1, cust2, savingVal))
                }
            }
        }
        savingsList.sortByDescending { it.value }
        Timber.d("Calculated ${savingsList.size} positive savings.")

        // 2. Inisialisasi rute: setiap pelanggan dalam rute sendiri
        val activeRoutes = customers.map { customer ->
            RouteSegment(
                customers = mutableListOf(customer),
                currentDemand = customer.demand
            )
        }.toMutableList()

        // 3. Proses penggabungan berdasarkan penghematan
        for (saving in savingsList) {
            val custI = saving.cust1
            val custJ = saving.cust2

            val routeI = activeRoutes.firstOrNull { it.customers.contains(custI) }
            val routeJ = activeRoutes.firstOrNull { it.customers.contains(custJ) }

            if (routeI == null || routeJ == null || routeI === routeJ) continue

            val mergedDemand = routeI.currentDemand + routeJ.currentDemand

            val suitableVehicle = vehicles.firstOrNull { vehicle ->
                vehicle.capacity >= mergedDemand &&
                        (routeI.vehicleId == null || routeI.vehicleId == vehicle.id) &&
                        (routeJ.vehicleId == null || routeJ.vehicleId == vehicle.id)
            }

            if (suitableVehicle != null) {
                var merged = false
                // Coba gabungkan routeJ ke akhir routeI (I_last == custI, J_first == custJ)
                if (routeI.lastCustomer == custI && routeJ.firstCustomer == custJ) {
                    routeI.customers.addAll(routeJ.customers)
                    routeI.currentDemand = mergedDemand
                    routeI.vehicleId = suitableVehicle.id
                    activeRoutes.remove(routeJ)
                    merged = true
                    Timber.v("Merged J to I (I_last, J_first): ${custI.name} -> ${custJ.name}. Veh: ${suitableVehicle.id}")
                } // Coba gabungkan routeI ke akhir routeJ (J_last == custJ, I_first == custI)
                else if (routeJ.lastCustomer == custJ && routeI.firstCustomer == custI && !merged) {
                    routeJ.customers.addAll(routeI.customers)
                    routeJ.currentDemand = mergedDemand
                    routeJ.vehicleId = suitableVehicle.id
                    activeRoutes.remove(routeI)
                    merged = true
                    Timber.v("Merged I to J (J_last, I_first): ${custJ.name} -> ${custI.name}. Veh: ${suitableVehicle.id}")
                }
                // Implementasi yang lebih lengkap bisa mencoba membalik salah satu rute
                // jika endpoint yang cocok tidak ditemukan secara langsung.
                // Contoh: custI adalah akhir routeI, custJ adalah akhir routeJ. Balik routeJ.
                else if (routeI.lastCustomer == custI && routeJ.lastCustomer == custJ && !merged) {
                    routeJ.customers.reverse() // Balik routeJ
                    if (routeJ.firstCustomer == custJ) { // Cek lagi setelah dibalik
                        routeI.customers.addAll(routeJ.customers)
                        routeI.currentDemand = mergedDemand
                        routeI.vehicleId = suitableVehicle.id
                        activeRoutes.remove(routeJ)
                        merged = true
                        Timber.v("Merged reversed J to I (I_last, J_last->J_first): ${custI.name} -> ${custJ.name}. Veh: ${suitableVehicle.id}")
                    } else {
                        routeJ.customers.reverse() // Kembalikan jika tidak berhasil
                    }
                }
                // Kondisi lain: custI adalah awal routeI, custJ adalah awal routeJ. Balik salah satu.
                else if (routeI.firstCustomer == custI && routeJ.firstCustomer == custJ && !merged) {
                    routeI.customers.reverse() // Balik routeI
                    if (routeI.lastCustomer == custI) { // Cek lagi setelah dibalik
                        routeI.customers.addAll(routeJ.customers) // Gabungkan J ke I yang sudah dibalik
                        routeI.currentDemand = mergedDemand
                        routeI.vehicleId = suitableVehicle.id
                        activeRoutes.remove(routeJ)
                        merged = true
                        Timber.v("Merged J to reversed I (I_first->I_last, J_first): ${custI.name} -> ${custJ.name}. Veh: ${suitableVehicle.id}")
                    } else {
                        routeI.customers.reverse() // Kembalikan jika tidak berhasil
                    }
                }
            }
        }
        Timber.d("Finished C&W merging. Active route segments: ${activeRoutes.size}")

        // 4. Finalisasi Rute dan Penugasan Kendaraan (jika belum) + 2-Opt
        val finalRouteDetails = mutableListOf<RouteDetail>()
        val assignedCustomerIds = mutableSetOf<Int>()
        // Initialize a mutable list of vehicles, sorted by capacity ascending.
        // This list will be used to pick from available vehicles.
        val availableVehicles = vehicles.sortedBy { it.capacity }.toMutableList() // Urutkan dari kapasitas terkecil

        // Strategy for final vehicle assignment:
        // 1. Process route segments sorted by their total demand in descending order (largest demand first).
        // This prioritizes finding vehicles for routes that carry the most load.
        activeRoutes.sortByDescending { it.currentDemand }

        for (segment in activeRoutes) {
            if (segment.customers.isEmpty()) continue

            val vehicleForSegment: VehicleEntity? = if (segment.vehicleId != null) {
                // If a vehicle was already assigned to this segment during the merging phase, use it.
                // This ensures consistency for routes formed with specific vehicle constraints.
                vehicles.firstOrNull { it.id == segment.vehicleId } // Gunakan kendaraan yang sudah ditugaskan jika ada
            } else {
                // For segments without a pre-assigned vehicle:
                // Attempt to find an available vehicle. The strategy is greedy:
                // - Filter `availableVehicles` to those that can meet the `segment.currentDemand`.
                // - From the suitable vehicles, pick the one with the smallest capacity (`minByOrNull { it.capacity }`).
                //   This is a "tightest fit" or "best fit" heuristic for the current segment.
                //
                // Potential limitations of this greedy "tightest fit for largest demand segment" approach:
                // - Suboptimality: While locally optimal (minimizes unused capacity for the *current* segment),
                //   it might not be globally optimal for the entire fleet.
                // - Inefficient large vehicle use: Smaller vehicles might be assigned to large routes if they just fit,
                //   potentially leaving larger vehicles underutilized for subsequent, smaller routes.
                // - Increased unassigned customers: Exhausting smaller vehicles early on large routes (that larger
                //   vehicles could have handled) might leave no suitable vehicles for other (potentially numerous)
                //   smaller routes, leading to more unassigned customers.
                availableVehicles.filter { it.capacity >= segment.currentDemand }
                    .minByOrNull { it.capacity }
            }

            if (vehicleForSegment != null) {
                // Terapkan 2-opt untuk menyempurnakan urutan dalam segmen
                val optimizedCustomerOrder = applyTwoOpt(segment.customers.toMutableList(), depotLoc, distFunc)
                // Buat salinan baru untuk RouteDetail agar tidak memodifikasi state 'segment' secara langsung jika 'segment' dipakai lagi
                val finalCustomersForRoute = optimizedCustomerOrder.toList()


                // Hitung ulang jarak dengan urutan yang sudah dioptimalkan
                var routeDistance = 0.0
                if (finalCustomersForRoute.isNotEmpty()) {
                    routeDistance = distFunc(depotLoc, finalCustomersForRoute.first().location) // Depot to first
                    for (i in 0 until finalCustomersForRoute.size - 1) {
                        routeDistance += distFunc(finalCustomersForRoute[i].location, finalCustomersForRoute[i + 1].location)
                    }
                    routeDistance += distFunc(finalCustomersForRoute.last().location, depotLoc) // Last to Depot
                }


                finalRouteDetails.add(
                    RouteDetail(
                        vehicle = vehicleForSegment,
                        stops = finalCustomersForRoute,
                        totalDistance = routeDistance,
                        totalDemand = segment.currentDemand // Demand tidak berubah oleh 2-opt
                    )
                )
                finalCustomersForRoute.forEach { assignedCustomerIds.add(it.id) }
                if (segment.vehicleId == null) { // Jika kendaraan baru ditugaskan dari pool
                    availableVehicles.remove(vehicleForSegment)
                }
            } else {
                Timber.w("No suitable vehicle for segment with demand ${segment.currentDemand}. Customers: ${segment.customers.map {it.name}}")
            }
        }

        val limitedFinalRoutes = if (finalRouteDetails.size > vehicles.size) {
            // Scenario: More routes have been successfully formed and assigned vehicles
            // than the number of physical vehicles available in the input `vehicles` list.
            // This typically occurs if multiple merged route segments (RouteSegment) ended up
            // being associated with the same vehicleId during the merging phase, leading to
            // more RouteDetail objects than actual vehicles.
            Timber.w("Generated ${finalRouteDetails.size} routes, but only ${vehicles.size} vehicles selected/available. Truncating by taking routes with most customers.")

            // Truncation Strategy:
            // The current strategy is a heuristic:
            // 1. Sort all generated routes (`finalRouteDetails`) in descending order based on their `totalDemand`.
            // 2. Select the top `vehicles.size` routes from this sorted list.
            // This prioritizes routes that serve the highest cumulative demand.
            //
            // The existing comment "// Mengurutkan kembali ..." also notes that sorting by number of stops
            // might not be ideal and other strategies like re-merging could be considered.
            //
            // Potential limitations and suboptimality of this heuristic:
            // - Ignores Vehicle Capacity Utilization: A selected high-demand route might use a large vehicle
            //   inefficiently. Discarded routes, if combined, might have utilized the fleet capacity better
            //   and served more customers or a similar total demand more effectively.
            // - Ignores Route Distances/Operational Costs: The primary sorting key is `totalDemand`.
            //   A very long, high-demand route might be kept over shorter, more operationally efficient routes.
            // - Fixed Set Selection: The selection is made from the currently formed `finalRouteDetails`.
            //   It doesn't attempt to re-optimize by breaking up or re-combining customers from discarded
            //   routes with parts of selected routes.
            // - Combinatorial Complexity: Finding the absolute "best" subset of routes that maximizes a
            //   multi-faceted objective (e.g., demand served, low cost, high utilization) under vehicle
            //   constraints is a complex combinatorial problem (akin to knapsack or set-covering problems).
            //   Simple heuristics are common for tractability.
            finalRouteDetails.sortedByDescending { it.totalDemand } // Atau berdasarkan jarak, atau jumlah stop
                .take(vehicles.size)
        } else {
            // If the number of formed routes is within the vehicle limit, use all of them.
            finalRouteDetails
        }

        // Populate the set of customer IDs that are part of the final, limited routes.
        // This is used to determine unassigned customers.
        val trulyAssignedCustomerIds = mutableSetOf<Int>()
        limitedFinalRoutes.forEach { route ->
            route.stops.forEach { customer ->
                trulyAssignedCustomerIds.add(customer.id)
            }
        }

        val unassignedCustomersList = customers.filterNot { it.id in trulyAssignedCustomerIds }
        val totalOverallDistance = limitedFinalRoutes.sumOf { it.totalDistance }
        val calculationTime = System.currentTimeMillis() - startTime

        Timber.i("Clarke-Wright (Enhanced w/ 2-opt) Solution: Routes: ${limitedFinalRoutes.size}, Unassigned: ${unassignedCustomersList.size}, Total Distance: $totalOverallDistance, Time: $calculationTime ms")

        return VrpSolution(
            routes = limitedFinalRoutes,
            unassignedCustomers = unassignedCustomersList,
            totalOverallDistance = totalOverallDistance,
            calculationTimeMillis = calculationTime,
            planId = UUID.randomUUID().toString()
        )
    }

    private fun applyTwoOpt(
        currentRouteCustomers: MutableList<CustomerEntity>,
        depotLocation: LatLng,
        distanceCalc: (LatLng, LatLng) -> Double
    ): MutableList<CustomerEntity> {
        if (currentRouteCustomers.size < 2) return currentRouteCustomers

        var bestRoute = currentRouteCustomers.toMutableList()
        var bestDistance = calculateRouteDistanceForOpt(bestRoute, depotLocation, distanceCalc)
        var improved = true
        var iterationCounter = 0

        while (improved && iterationCounter < 200 * currentRouteCustomers.size) { // Batas iterasi dinamis
            iterationCounter++
            improved = false
            for (i in 0 until bestRoute.size - 1) {
                for (j in i + 2 until bestRoute.size) {
                    val newRoute = bestRoute.toMutableList()

                    // Balik segmen dari (i+1) sampai j (inklusif)
                    // Misal rute: C0-C1-C2-C3-C4. Jika i=0, j=2.
                    // Edge yang dihapus: (C0,C1) dan (C2,C3)
                    // Segmen yang dibalik: C1-C2 menjadi C2-C1
                    // Rute baru: C0-C2-C1-C3-C4
                    // Indeks subList: i+1 sampai j+1 (eksklusif untuk akhir)
                    if (i + 1 > j ) continue // Tidak ada segmen untuk dibalik jika i+1 > j

                    val segmentToReverse = newRoute.subList(i + 1, j + 1).asReversed()
                    var k = 0
                    for(idx in i+1 .. j) {
                        newRoute[idx] = segmentToReverse[k++]
                    }

                    val newDistance = calculateRouteDistanceForOpt(newRoute, depotLocation, distanceCalc)

                    if (newDistance < bestDistance - 0.001) {
                        bestDistance = newDistance
                        bestRoute = newRoute // newRoute sudah merupakan salinan yang dimodifikasi
                        improved = true
                        // Timber.v("2-opt improvement: New distance $bestDistance by swapping edges involving indices $i and $j")
                    }
                }
            }
        }
        if(iterationCounter > 1 && bestRoute.size > 1) Timber.d("2-opt finished for a route of size ${bestRoute.size}. Iterations: $iterationCounter, Final distance: $bestDistance")
        return bestRoute
    }

    private fun calculateRouteDistanceForOpt(
        customerList: List<CustomerEntity>,
        depotLocation: LatLng,
        distanceCalc: (LatLng, LatLng) -> Double
    ): Double {
        if (customerList.isEmpty()) return 0.0
        var totalDist = distanceCalc(depotLocation, customerList.first().location)
        for (k in 0 until customerList.size - 1) {
            totalDist += distanceCalc(customerList[k].location, customerList[k + 1].location)
        }
        totalDist += distanceCalc(customerList.last().location, depotLocation)
        return totalDist
    }
}
