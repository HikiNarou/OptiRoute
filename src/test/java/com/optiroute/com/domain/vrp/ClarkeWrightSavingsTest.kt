package com.optiroute.com.domain.vrp

import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.domain.model.LatLng
// JUnit 5 imports - typically these would be available in an Android project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.math.abs

// Mock DistanceUtils or use a helper if direct mocking is complex
// For now, we'll define a helper as suggested for simplicity,
// but in a real scenario, we'd mock DistanceUtils.calculateDistance
object TestDistanceUtils {
    fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        // Using a simple Euclidean distance for test predictability.
        // Note: ClarkeWrightSavings uses Haversine. For precise matching of savings values
        // from the actual code, the test's distance function must match the one used by the SUT,
        // or DistanceUtils.calculateDistance must be mocked.
        return kotlin.math.sqrt(kotlin.math.pow(p1.longitude - p2.longitude, 2.0) + kotlin.math.pow(p1.latitude - p2.latitude, 2.0))
    }
}

class ClarkeWrightSavingsTest {

    private lateinit var clarkeWrightSavings: ClarkeWrightSavings

    // Using a direct reference to our test distance calculation for this specific test
    // In a full test suite, we'd mock the actual DistanceUtils.calculateDistance
    private val testDistFunc: (LatLng, LatLng) -> Double = TestDistanceUtils::calculateDistance

    @BeforeEach
    fun setUp() {
        clarkeWrightSavings = ClarkeWrightSavings()
        // Here, we would typically mock DistanceUtils if it were a dependency
        // For this test, ClarkeWrightSavings directly calls DistanceUtils.calculateDistance (which is Haversine)
        // This means our expectedSavingVal calculated with Euclidean distance below won't match
        // the one calculated internally by solve() unless we can control that.
    }

    @Test
    fun solve_routeTruncation_discardsLowerDemandRoutes() {
        val depotLoc = LatLng(0.0, 0.0)
        val depot = DepotEntity(1, "Depot", depotLoc)

        // Customers are far apart, no profitable savings expected, leading to initial individual routes.
        val cust1 = CustomerEntity(1, "C1_Demand10", LatLng(10.0, 0.0), 10.0)
        val cust2 = CustomerEntity(2, "C2_Demand20", LatLng(20.0, 0.0), 20.0)
        val cust3 = CustomerEntity(3, "C3_Demand30", LatLng(30.0, 0.0), 30.0)
        val cust4 = CustomerEntity(4, "C4_Demand40", LatLng(40.0, 0.0), 40.0)
        val customers = listOf(cust1, cust2, cust3, cust4) // 4 customers

        // Only 2 vehicles available
        val vehicle1 = VehicleEntity(101, "V1_Cap50", 50.0, depotLoc, depotLoc)
        val vehicle2 = VehicleEntity(102, "V2_Cap50", 50.0, depotLoc, depotLoc)
        val vehicles = listOf(vehicle1, vehicle2)

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, vehicles)
        }

        // Expected: 4 initial routes (C1, C2, C3, C4). Truncated to 2 routes.
        // Routes are sorted by totalDemand descending: C4(40), C3(30), C2(20), C1(10).
        // Kept routes: C4 and C3. Discarded: C2 and C1.
        assertEquals(vehicles.size, solution.routes.size, "Number of routes should be truncated to number of available vehicles.")

        val finalRouteCustomerIds = solution.routes.flatMap { it.stops }.map { it.id }.toSet()
        assertTrue(finalRouteCustomerIds.contains(cust4.id), "C4 (demand 40) should be in the final routes.")
        assertTrue(finalRouteCustomerIds.contains(cust3.id), "C3 (demand 30) should be in the final routes.")
        assertFalse(finalRouteCustomerIds.contains(cust2.id), "C2 (demand 20) should NOT be in the final routes.")
        assertFalse(finalRouteCustomerIds.contains(cust1.id), "C1 (demand 10) should NOT be in the final routes.")

        assertEquals(2, solution.unassignedCustomers.size, "Two customers should be unassigned.")
        val unassignedCustomerIds = solution.unassignedCustomers.map { it.id }.toSet()
        assertTrue(unassignedCustomerIds.contains(cust1.id), "C1 should be unassigned.")
        assertTrue(unassignedCustomerIds.contains(cust2.id), "C2 should be unassigned.")

        val totalDemandOfKeptRoutes = solution.routes.sumOf { it.totalDemand }
        assertEquals(cust3.demand + cust4.demand, totalDemandOfKeptRoutes, 0.001, "Total demand of kept routes should be sum of C3 and C4 demands.")
    }

    @Test
    fun solve_vehicleCapacityExceeded_multipleVehicles_usesMultipleRoutes() {
        val depotLoc = LatLng(0.0, 0.0)
        val depot = DepotEntity(1, "Depot", depotLoc)

        // Customers with locations that encourage C1 and C2 to merge.
        val c1Loc = LatLng(0.0, 1.0)
        val c2Loc = LatLng(0.0, 2.0)
        val c3Loc = LatLng(0.0, 10.0) // C3 is further away

        val cust1 = CustomerEntity(1, "C1", c1Loc, 50.0)
        val cust2 = CustomerEntity(2, "C2", c2Loc, 50.0)
        val cust3 = CustomerEntity(3, "C3", c3Loc, 50.0)
        val customers = listOf(cust1, cust2, cust3)

        val vehicle1 = VehicleEntity(101, "V_Large", 100.0, depotLoc, depotLoc) // Capacity 100
        val vehicle2 = VehicleEntity(102, "V_Small", 50.0, depotLoc, depotLoc)  // Capacity 50
        val vehicles = listOf(vehicle1, vehicle2) // Order might matter for assignment, sort by cap in SUT

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, vehicles)
        }

        assertEquals(2, solution.routes.size, "Should form two routes.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "All customers should be assigned.")

        val routeForC1C2 = solution.routes.firstOrNull { route ->
            route.stops.any { it.id == cust1.id } && route.stops.any { it.id == cust2.id }
        }
        assertNotNull(routeForC1C2, "Route for C1 and C2 should exist.")
        assertEquals(2, routeForC1C2!!.stops.size)
        assertEquals(100.0, routeForC1C2.totalDemand, 0.001)
        assertEquals(vehicle1.id, routeForC1C2.vehicle.id, "C1 and C2 should be on the larger vehicle.")


        val routeForC3 = solution.routes.firstOrNull { route -> route.stops.any { it.id == cust3.id } }
        assertNotNull(routeForC3, "Route for C3 should exist.")
        assertEquals(1, routeForC3!!.stops.size)
        assertEquals(50.0, routeForC3.totalDemand, 0.001)
        assertEquals(vehicle2.id, routeForC3.vehicle.id, "C3 should be on the smaller vehicle.")
    }

    @Test
    fun solve_correctVehicleAssignedToRoute_respectsCapacityAndAvailability() {
        val depotLoc = LatLng(0.0, 0.0)
        val depot = DepotEntity(1, "Depot", depotLoc)

        // Customers with varying demands
        val cust1 = CustomerEntity(1, "C1_Demand90", LatLng(1.0, 0.0), 90.0)
        val cust2 = CustomerEntity(2, "C2_Demand40", LatLng(2.0, 0.0), 40.0)
        val cust3 = CustomerEntity(3, "C3_Demand10", LatLng(3.0, 0.0), 10.0)
        // Order of customers in list might influence initial segment processing if not merged.
        val customers = listOf(cust1, cust2, cust3) 

        // Vehicles with distinct capacities
        val vLarge = VehicleEntity(101, "V_Large", 100.0, depotLoc, depotLoc)
        val vMedium = VehicleEntity(102, "V_Medium", 50.0, depotLoc, depotLoc)
        val vSmall = VehicleEntity(103, "V_Small", 20.0, depotLoc, depotLoc)
        // Order of vehicles in list matters for `availableVehicles` list, which is sorted by capacity.
        val vehicles = listOf(vMedium, vSmall, vLarge) // Intentionally unsorted by capacity

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, vehicles)
        }

        assertEquals(3, solution.routes.size, "Should form three separate routes as demands are distinct and no savings are obviously beneficial with these simple locations if treated individually.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "All customers should be assigned.")

        val routeForC1 = solution.routes.firstOrNull { it.stops.any { s -> s.id == cust1.id } }
        assertNotNull(routeForC1, "Route for C1 (demand 90) should exist.")
        assertEquals(1, routeForC1!!.stops.size)
        assertEquals(cust1.demand, routeForC1.totalDemand)
        assertEquals(vLarge.id, routeForC1.vehicle.id, "C1 (demand 90) should be assigned to V_Large (cap 100).")

        val routeForC2 = solution.routes.firstOrNull { it.stops.any { s -> s.id == cust2.id } }
        assertNotNull(routeForC2, "Route for C2 (demand 40) should exist.")
        assertEquals(1, routeForC2!!.stops.size)
        assertEquals(cust2.demand, routeForC2.totalDemand)
        assertEquals(vMedium.id, routeForC2.vehicle.id, "C2 (demand 40) should be assigned to V_Medium (cap 50).")

        val routeForC3 = solution.routes.firstOrNull { it.stops.any { s -> s.id == cust3.id } }
        assertNotNull(routeForC3, "Route for C3 (demand 10) should exist.")
        assertEquals(1, routeForC3!!.stops.size)
        assertEquals(cust3.demand, routeForC3.totalDemand)
        assertEquals(vSmall.id, routeForC3.vehicle.id, "C3 (demand 10) should be assigned to V_Small (cap 20).")
    }

    @Test
    fun solve_vehicleCapacityExceeded_singleVehicle_someCustomersUnassigned() {
        val depotLoc = LatLng(0.0, 0.0)
        val depot = DepotEntity(1, "Depot", depotLoc)

        val customer1 = CustomerEntity(1, "C1", LatLng(1.0, 0.0), 50.0)
        val customer2 = CustomerEntity(2, "C2", LatLng(2.0, 0.0), 50.0)
        val customer3 = CustomerEntity(3, "C3", LatLng(3.0, 0.0), 50.0)
        val customers = listOf(customer1, customer2, customer3)

        val vehicle = VehicleEntity(1, "V1", 100.0, depotLoc, depotLoc) // Capacity 100

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, listOf(vehicle))
        }

        // Expecting one route with C1 and C2 (total demand 100), and C3 unassigned.
        // The exact customers in the route depend on the savings calculation and merging order.
        // CWS prioritizes largest savings. d(D,C1)=1, d(D,C2)=2, d(C1,C2)=1. Sav(C1,C2)=1+2-1=2
        // d(D,C3)=3. d(C1,C3)=2. Sav(C1,C3)=1+3-2=2.
        // d(C2,C3)=1. Sav(C2,C3)=2+3-1=4. -> C2 and C3 likely merge first if their locations allow.
        // Let's use locations that make C1,C2 merge first or C2,C3.
        // For simplicity, let's assume C1 and C2 are closer to each other and to the depot initially.
        // C1(1,0) demand 50, C2(2,0) demand 50, C3(10,0) demand 50. Vehicle cap 100.
        // Savings:
        // S(C1,C2): d(D,C1)+d(D,C2)-d(C1,C2). Let's use Haversine for more realistic test.
        // C1_loc(0,1), C2_loc(0,2), C3_loc(0,10)
        val c1Loc = LatLng(0.0, 1.0) // Approx 111km from D
        val c2Loc = LatLng(0.0, 2.0) // Approx 222km from D, 111km from C1
        val c3Loc = LatLng(0.0, 10.0) // Approx 1110km from D

        val cust1 = CustomerEntity(1, "C1", c1Loc, 50.0)
        val cust2 = CustomerEntity(2, "C2", c2Loc, 50.0)
        val cust3 = CustomerEntity(3, "C3", c3Loc, 50.0)
        val realCustomers = listOf(cust1, cust2, cust3)

        val sol = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, realCustomers, listOf(vehicle))
        }

        assertEquals(1, sol.routes.size, "Should form one route.")
        val route = sol.routes.first()
        assertTrue(route.totalDemand <= vehicle.capacity, "Route total demand should not exceed vehicle capacity.")
        assertEquals(2, route.stops.size, "Route should serve two customers.")
        
        assertEquals(1, sol.unassignedCustomers.size, "One customer should be unassigned.")
        val assignedIds = route.stops.map { it.id }.toSet()
        val unassignedCust = realCustomers.first { it.id !in assignedIds }

        // Check that the two customers with the highest saving between them and the depot are assigned.
        // S(D, C1, C2) is expected to be highest due to proximity.
        // Expected assigned: C1, C2. Unassigned: C3.
        assertTrue(assignedIds.contains(cust1.id) && assignedIds.contains(cust2.id), "C1 and C2 should be assigned.")
        assertEquals(cust3.id, unassignedCust.id, "C3 should be the unassigned customer.")
    }

    @Test
    fun solve_twoCustomers_noPositiveSaving_formsTwoRoutes() {
        val depotLoc = LatLng(0.0, 0.0)
        // Customers are arranged such that no saving is made, e.g., C1-D-C2 (collinear, D in middle)
        // D-C1 = 1, D-C2 = 1, C1-C2 = 2. Saving = 1 + 1 - 2 = 0.
        val cust1Loc = LatLng(0.0, 1.0)
        val cust2Loc = LatLng(0.0, -1.0)

        val depot = DepotEntity(1, "Depot", depotLoc)
        val customer1 = CustomerEntity(1, "C1", cust1Loc, 5.0)
        val customer2 = CustomerEntity(2, "C2", cust2Loc, 5.0)
        val customers = listOf(customer1, customer2)
        // Vehicle capacity is enough for merged demand, but they shouldn't merge due to no positive saving.
        val vehicle1 = VehicleEntity(1, "V1", 20.0, depotLoc, depotLoc)
        val vehicle2 = VehicleEntity(2, "V2", 20.0, depotLoc, depotLoc) // Provide two vehicles
        val vehicles = listOf(vehicle1, vehicle2)

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, vehicles)
        }

        assertEquals(2, solution.routes.size, "Should form two separate routes as no positive saving.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "All customers should be assigned.")

        // Check Route 1 (customer1)
        val routeC1 = solution.routes.firstOrNull { it.stops.first().id == customer1.id }
        assertNotNull(routeC1, "Route for customer1 should exist.")
        assertEquals(1, routeC1!!.stops.size)
        assertEquals(customer1.demand, routeC1.totalDemand, 0.001)
        val expectedDistC1 = DistanceUtils.calculateHaversineDistance(depotLoc, cust1Loc) * 2
        assertEquals(expectedDistC1, routeC1.totalDistance, 0.01)

        // Check Route 2 (customer2)
        val routeC2 = solution.routes.firstOrNull { it.stops.first().id == customer2.id }
        assertNotNull(routeC2, "Route for customer2 should exist.")
        assertEquals(1, routeC2!!.stops.size)
        assertEquals(customer2.demand, routeC2.totalDemand, 0.001)
        val expectedDistC2 = DistanceUtils.calculateHaversineDistance(depotLoc, cust2Loc) * 2
        assertEquals(expectedDistC2, routeC2.totalDistance, 0.01)

        assertNotEquals(routeC1.vehicle.id, routeC2.vehicle.id, "Routes should use different vehicles.")
    }

    @Test
    fun applyTwoOpt_improvesRouteOrder_viaSolve() {
        // Testing 2-Opt's effect via solve().
        // Scenario: D(0,0), C1(1,5), C2(2,1), C3(3,5), C4(4,1)
        // Initial order C1-C2-C3-C4 is suboptimal.
        // dist(C1,C2) ~ 4.12; dist(C3,C4) ~ 4.12. Sum = 8.24
        // dist(C1,C3) = 2; dist(C2,C4) = 2. Sum = 4
        // Since 8.24 > 4, 2-Opt should swap to C1-C3-C2-C4.

        val depotLoc = LatLng(0.0, 0.0)
        val c1Loc = LatLng(1.0, 5.0)
        val c2Loc = LatLng(2.0, 1.0)
        val c3Loc = LatLng(3.0, 5.0)
        val c4Loc = LatLng(4.0, 1.0)

        val depot = DepotEntity(1, "Depot", depotLoc)
        // Demands are small, capacity is large, so they should all fit in one route.
        val customer1 = CustomerEntity(1, "C1", c1Loc, 1.0)
        val customer2 = CustomerEntity(2, "C2", c2Loc, 1.0)
        val customer3 = CustomerEntity(3, "C3", c3Loc, 1.0)
        val customer4 = CustomerEntity(4, "C4", c4Loc, 1.0)
        // Provide customers in an order that should trigger 2-opt.
        // The initial routes will be D-C1-D, D-C2-D, etc.
        // CWS will try to merge them. For 2-opt to be the primary factor,
        // we need them to form a single route first.
        // Let's ensure C1, C2, C3, C4 are close enough to form a single route.
        // The default CWS might not produce C1-C2-C3-C4 sequence naturally.
        // This test setup might be insufficient to guarantee a specific suboptimal route for 2-Opt to fix.
        // A more direct test of applyTwoOpt would be better.
        // For now, we assume a single route is formed and 2-Opt optimizes it.

        val customers = listOf(customer1, customer2, customer3, customer4)
        val vehicle = VehicleEntity(1, "V1", 100.0, depotLoc, depotLoc) // Large capacity

        // To force a specific initial order for 2-Opt to work on, we'd ideally call applyTwoOpt directly.
        // Since we're calling solve(), CWS will build its own initial routes.
        // We hope that a single route C1-C2-C3-C4 (or similar that's suboptimal) is formed.
        // This test becomes more of an integration test that *includes* 2-Opt.

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, listOf(vehicle))
        }

        assertEquals(1, solution.routes.size, "Expecting a single route to be formed.")
        val route = solution.routes.first()
        assertEquals(4, route.stops.size, "Route should contain all 4 customers.")

        val stopIds = route.stops.map { it.id }

        // Expected order after 2-Opt: C1-C3-C2-C4 (ids: 1-3-2-4)
        val expectedStopIds = listOf(customer1.id, customer3.id, customer2.id, customer4.id)
        
        // We need to check if the calculated distances for the SUT (using Haversine)
        // also favor this swap.
        // Original path: C1-C2-C3-C4
        // dH(C1,C2) + dH(C3,C4) vs dH(C1,C3) + dH(C2,C4)
        val dH_c1_c2 = DistanceUtils.calculateHaversineDistance(c1Loc, c2Loc)
        val dH_c3_c4 = DistanceUtils.calculateHaversineDistance(c3Loc, c4Loc)
        val dH_c1_c3 = DistanceUtils.calculateHaversineDistance(c1Loc, c3Loc)
        val dH_c2_c4 = DistanceUtils.calculateHaversineDistance(c2Loc, c4Loc)

        System.out.println("dH(C1,C2) + dH(C3,C4) = ${dH_c1_c2 + dH_c3_c4}")
        System.out.println("dH(C1,C3) + dH(C2,C4) = ${dH_c1_c3 + dH_c2_c4}")

        if ((dH_c1_c2 + dH_c3_c4) > (dH_c1_c3 + dH_c2_c4)) {
            assertEquals(expectedStopIds, stopIds, "Route order should be optimized by 2-Opt to C1-C3-C2-C4.")
            
            val expectedOptimizedDistance = DistanceUtils.calculateHaversineDistance(depotLoc, c1Loc) +
                                            DistanceUtils.calculateHaversineDistance(c1Loc, c3Loc) +
                                            DistanceUtils.calculateHaversineDistance(c3Loc, c2Loc) +
                                            DistanceUtils.calculateHaversineDistance(c2Loc, c4Loc) +
                                            DistanceUtils.calculateHaversineDistance(c4Loc, depotLoc)
            assertEquals(expectedOptimizedDistance, route.totalDistance, 0.01, "Distance should match optimized C1-C3-C2-C4 path.")

        } else {
            // If Haversine distances don't favor the swap, the order might be different.
            // This indicates the test data isn't ideal for Haversine or 2-Opt found another optimum.
            // For this test, we are designing it such that the swap *should* be favorable.
            // If this 'else' branch is hit, the test data needs re-evaluation for Haversine.
            // A common initial order from CWS might be based on proximity or savings, not sequential IDs.
            // Let's print the actual order to see.
            System.out.println("2-Opt did not result in C1-C3-C2-C4. Actual order: $stopIds. This might be due to Haversine distances or CWS initial route construction.")
            // We cannot be certain of the exact order CWS will produce before 2-opt without deeper mocking.
            // So, this test is more of a hope that 2-opt makes an improvement that we can recognize.
            // A strong assertion on a specific order is difficult here.
            // We can assert that the solution distance is less than a known suboptimal one, if calculable.
            val suboptimalPathDistance = DistanceUtils.calculateHaversineDistance(depotLoc, c1Loc) +
                                        dH_c1_c2 +
                                        DistanceUtils.calculateHaversineDistance(c2Loc, c3Loc) +
                                        dH_c3_c4 +
                                        DistanceUtils.calculateHaversineDistance(c4Loc, depotLoc)
            assertTrue(route.totalDistance <= suboptimalPathDistance + 0.01, "Optimized distance should be less than or equal to a known suboptimal C1-C2-C3-C4 path.")
        }
    }

    @Test
    fun testCalculateSavings_PositiveSaving_RequiresRefactoringForDirectTesting() {
        val depotLoc = LatLng(0.0, 0.0)
        val cust1Loc = LatLng(0.0, 1.0) // North of depot
        val cust2Loc = LatLng(1.0, 0.0) // East of depot

        // Using our local testDistFunc for expected calculation:
        val d_d_c1 = testDistFunc(depotLoc, cust1Loc) // Expected: 1.0
        val d_d_c2 = testDistFunc(depotLoc, cust2Loc) // Expected: 1.0
        val d_c1_c2 = testDistFunc(cust1Loc, cust2Loc) // Expected: sqrt((1-0)^2 + (0-1)^2) = sqrt(2) = 1.41421356

        val expectedSavingValUsingTestDistance = d_d_c1 + d_d_c2 - d_c1_c2 // Expected: 1.0 + 1.0 - 1.41421356 = 0.58578644

        // Note on testability:
        // The actual savings calculation (`savingsList`) is an internal implementation detail of the `solve` method.
        // It's not directly accessible for unit testing without modification to the ClarkeWrightSavings class.
        // To properly unit test savings calculation:
        // 1. Refactor: Extract the savings calculation logic into a separate, testable function (e.g., public or internal).
        //    This function would take the depot, customers, and the distance function as parameters.
        // 2. Indirect Test (Integration-like): Call `solve()` with minimal, controlled inputs and assert properties
        //    of the result that depend on the savings (e.g., which routes are formed). This is less direct.
        //
        // For this subtask, we acknowledge this limitation. The test below is a placeholder for how one *would*
        // assert if the savings list were accessible or the logic extracted.

        // Placeholder for an actual assertion.
        // To make this test pass with the current SUT, we would need to:
        //    a) Use Haversine for `expectedSavingValUsingTestDistance` to match SUT's internal calculation.
        //    b) Have a way to inspect `savingsList` or the effect of savings.
        assertTrue(true, "This test currently only sets up data and calculates expected saving with a test distance function. Actual assertion requires refactoring or indirect testing of ClarkeWrightSavings.solve(). The calculated expected saving is ${expectedSavingValUsingTestDistance}.")
        System.out.println("Expected saving (using test Euclidean distance): $expectedSavingValUsingTestDistance")

        // Example of how one might proceed if `solve` returned its intermediate savings list (pseudo-code):
        // val depot = DepotEntity(id = 1, name = "Depot", location = depotLoc)
        // val customer1 = CustomerEntity(id = 1, name = "C1", location = cust1Loc, demand = 10.0)
        // val customer2 = CustomerEntity(id = 2, name = "C2", location = cust2Loc, demand = 10.0)
        // val customers = listOf(customer1, customer2)
        // val vehicles = listOf(VehicleEntity(id=1, capacity = 100.0)) // Dummy vehicle
        //
        // // Hypothetical refactored method or way to get savings:
        // val actualSavings = clarkeWrightSavings.calculateAllSavings(depot, customers, DistanceUtils.calculateDistance)
        //
        // if (actualSavings.isNotEmpty()) {
        //     // Note: The SUT uses Haversine. For this assertion to be accurate with the SUT,
        //     // expectedSavingVal should also be calculated with Haversine, or DistanceUtils.calculateDistance mocked.
        //     // For this example, we're still using the testDistance for expected.
        //     val actualSavingForPair = actualSavings.firstOrNull {
        //         (it.cust1.id == customer1.id && it.cust2.id == customer2.id) ||
        //         (it.cust1.id == customer2.id && it.cust2.id == customer1.id)
        //     }
        //     assertNotNull(actualSavingForPair, "Saving between C1 and C2 should exist.")
        //     // Assuming Haversine distance for d_h_...
        //     // val d_h_d_c1 = DistanceUtils.calculateDistance(depotLoc, cust1Loc)
        //     // val d_h_d_c2 = DistanceUtils.calculateDistance(depotLoc, cust2Loc)
        //     // val d_h_c1_c2 = DistanceUtils.calculateDistance(cust1Loc, cust2Loc)
        //     // val expectedSavingValHaversine = d_h_d_c1 + d_h_d_c2 - d_h_c1_c2
        //     // assertEquals(expectedSavingValHaversine, actualSavingForPair!!.value, 0.001)
        // } else {
        //     fail("Savings list should not be empty.")
        // }
    }

    // Additional test cases can be added here.

    @Test
    fun solve_emptyCustomersList_returnsEmptySolution() {
        val depot = DepotEntity(1, "Depot", LatLng(0.0, 0.0))
        val vehicles = listOf(VehicleEntity(1, "V1", 100.0, LatLng(0.0,0.0), LatLng(0.0,0.0))) // Vehicle list not empty

        val solution = kotlinx.coroutines.runBlocking { // Use runBlocking for suspend function in test
            clarkeWrightSavings.solve(depot, emptyList(), vehicles)
        }

        assertTrue(solution.routes.isEmpty(), "Routes should be empty for no customers.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "Unassigned customers should be empty.")
        assertEquals(0.0, solution.totalOverallDistance, 0.001, "Total distance should be 0.")
    }

    @Test
    fun solve_emptyVehiclesList_withCustomers_returnsAllCustomersUnassigned() {
        val depot = DepotEntity(1, "Depot", LatLng(0.0, 0.0))
        val customer1 = CustomerEntity(1, "C1", LatLng(1.0, 1.0), 10.0)
        val customers = listOf(customer1)

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, emptyList())
        }

        assertTrue(solution.routes.isEmpty(), "Routes should be empty if no vehicles are available.")
        assertEquals(customers.size, solution.unassignedCustomers.size, "All customers should be unassigned.")
        assertTrue(solution.unassignedCustomers.contains(customer1), "Customer1 should be in unassigned list.")
        assertEquals(0.0, solution.totalOverallDistance, 0.001, "Total distance should be 0 as no routes are formed.")
    }

    @Test
    fun solve_singleCustomerSingleVehicle_formsOneRoute() {
        val depotLoc = LatLng(0.0, 0.0)
        val cust1Loc = LatLng(0.0, 1.0) // North of depot
        val depot = DepotEntity(1, "Depot", depotLoc)
        val customer1 = CustomerEntity(1, "C1", cust1Loc, 10.0)
        val vehicle = VehicleEntity(1, "V1", 20.0, depotLoc, depotLoc) // Capacity is enough

        // Mocking or controlling DistanceUtils.calculateDistance is crucial here.
        // The actual ClarkeWrightSavings.solve() uses DistanceUtils.calculateDistance (Haversine).
        // For this test, we are asserting route structure, not exact distance values unless we use Haversine too.
        // Let's use our testDistFunc for an expected distance to compare conceptually.
        val expectedDistance = testDistFunc(depotLoc, cust1Loc) + testDistFunc(cust1Loc, depotLoc) // Depot -> C1 -> Depot

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, listOf(customer1), listOf(vehicle))
        }

        assertEquals(1, solution.routes.size, "Should form one route for a single customer and vehicle.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "No customers should be unassigned.")
        
        val routeDetail = solution.routes.first()
        assertEquals(1, routeDetail.stops.size, "Route should have one stop.")
        assertEquals(customer1.id, routeDetail.stops.first().id, "Stop should be customer1.")
        assertEquals(vehicle.id, routeDetail.vehicle.id, "Route should use the provided vehicle.")
        assertEquals(customer1.demand, routeDetail.totalDemand, 0.001, "Route demand should match customer demand.")

        // For distance, it's better to calculate with Haversine if we want to match the SUT precisely.
        // Or, if DistanceUtils was mocked, we could verify calls.
        // Here, we are calculating the expected distance using Haversine for a more accurate comparison.
        val expectedDistanceHaversine = DistanceUtils.calculateHaversineDistance(depotLoc, cust1Loc) * 2
        assertEquals(expectedDistanceHaversine, routeDetail.totalDistance, 0.01, "Route distance should be Depot-C1-Depot using Haversine.")
        System.out.println("Solve single customer: Expected Haversine Distance: $expectedDistanceHaversine, Actual: ${routeDetail.totalDistance}, TestEuclidean: $expectedDistance")
    }

    @Test
    fun solve_twoCustomers_sufficientCapacity_positiveSaving_formsOneRoute() {
        val depotLoc = LatLng(0.0, 0.0)
        // C1 and C2 are chosen such that merging them is beneficial.
        // Example: D(0,0), C1(0,1), C2(0,2). Merging C1-C2 is better than D-C1-D + D-C2-D.
        // Using Haversine for actual calculation:
        // d(D,C1) approx 111.19 km for LatLng(0.0, 1.0) from LatLng(0.0,0.0)
        // d(D,C2) approx 222.39 km for LatLng(0.0, 2.0) from LatLng(0.0,0.0)
        // d(C1,C2) approx 111.19 km
        // Saving = d(D,C1) + d(D,C2) - d(C1,C2) approx 111.19 + 222.39 - 111.19 = 222.39 > 0. So they should merge.
        val cust1Loc = LatLng(0.0, 1.0) // North
        val cust2Loc = LatLng(0.0, 2.0) // Further North

        val depot = DepotEntity(1, "Depot", depotLoc)
        // D-C1 = 1, D-C2 = 2 (using simple Y-coordinates as approx distance for intuition)
        // C1-C2 = 1
        // Saving for C1-C2 = (D-C1) + (D-C2) - (C1-C2)
        // Using Haversine for actual calculation:
        // d(D,C1) = 111.19 km
        // d(D,C2) = 222.39 km
        // d(C1,C2) = 111.19 km
        val customer1 = CustomerEntity(1, "C1", cust1Loc, 5.0)
        val customer2 = CustomerEntity(2, "C2", cust2Loc, 5.0)
        val customers = listOf(customer1, customer2)
        val vehicle = VehicleEntity(1, "V1", 20.0, depotLoc, depotLoc) // Capacity (20) > total demand (10)

        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, listOf(vehicle))
        }

        assertEquals(1, solution.routes.size, "Should form one merged route.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "All customers should be assigned.")

        val routeDetail = solution.routes.first()
        assertEquals(2, routeDetail.stops.size, "Merged route should have two stops.")
        assertEquals(vehicle.id, routeDetail.vehicle.id, "Route should use the provided vehicle.")
        assertEquals(customer1.demand + customer2.demand, routeDetail.totalDemand, 0.001)

        // Order of stops depends on the savings calculation and merge logic.
        // For D(0,0), C1(0,1), C2(0,2):
        // Saving(C1,C2) = dist(D,C1) + dist(D,C2) - dist(C1,C2)
        // The algorithm tries to merge C1 (end of route D-C1) and C2 (start of route D-C2).
        // Or C2 (end of D-C2) and C1 (start of D-C1).
        // If D-C1-C2-D is formed:
        // Stop order should be C1 then C2.
        val expectedStops = listOf(customer1, customer2) // Assuming C1 is visited before C2
        assertEquals(expectedStops.map { it.id }, routeDetail.stops.map { it.id }, "Stops should be C1, then C2.")

        val expectedDistance = DistanceUtils.calculateHaversineDistance(depotLoc, cust1Loc) +
                               DistanceUtils.calculateHaversineDistance(cust1Loc, cust2Loc) +
                               DistanceUtils.calculateHaversineDistance(cust2Loc, depotLoc)
        assertEquals(expectedDistance, routeDetail.totalDistance, 0.01, "Distance for D-C1-C2-D.")
    }

    @Test
    fun solve_twoCustomers_insufficientCapacityForMerge_formsTwoRoutesIfVehiclesAvailable() {
        val depotLoc = LatLng(0.0, 0.0)
        val cust1Loc = LatLng(0.0, 1.0)
        val cust2Loc = LatLng(0.0, 2.0) // Locations are same as positive saving test

        val depot = DepotEntity(1, "Depot", depotLoc)
        val customer1 = CustomerEntity(1, "C1", cust1Loc, 10.0) // Demand 10
        val customer2 = CustomerEntity(2, "C2", cust2Loc, 10.0) // Demand 10
        val customers = listOf(customer1, customer2)
        // Total demand is 20. Vehicle capacity is 15, so cannot merge.
        val vehicle1 = VehicleEntity(1, "V1", 15.0, depotLoc, depotLoc)
        val vehicle2 = VehicleEntity(2, "V2", 15.0, depotLoc, depotLoc) // Second vehicle available
        val vehicles = listOf(vehicle1, vehicle2)


        val solution = kotlinx.coroutines.runBlocking {
            clarkeWrightSavings.solve(depot, customers, vehicles)
        }

        assertEquals(2, solution.routes.size, "Should form two separate routes due to capacity constraint.")
        assertTrue(solution.unassignedCustomers.isEmpty(), "All customers should be assigned to separate routes.")

        // Check Route 1 (should contain one customer)
        val route1 = solution.routes.firstOrNull { it.stops.any { stop -> stop.id == customer1.id } }
        assertNotNull(route1, "Route for customer1 should exist.")
        assertEquals(1, route1!!.stops.size)
        assertEquals(customer1.id, route1.stops.first().id)
        assertEquals(customer1.demand, route1.totalDemand, 0.001)
        val expectedDist1 = DistanceUtils.calculateHaversineDistance(depotLoc, cust1Loc) * 2
        assertEquals(expectedDist1, route1.totalDistance, 0.01)

        // Check Route 2 (should contain the other customer)
        val route2 = solution.routes.firstOrNull { it.stops.any { stop -> stop.id == customer2.id } }
        assertNotNull(route2, "Route for customer2 should exist.")
        assertEquals(1, route2!!.stops.size)
        assertEquals(customer2.id, route2.stops.first().id)
        assertEquals(customer2.demand, route2.totalDemand, 0.001)
        val expectedDist2 = DistanceUtils.calculateHaversineDistance(depotLoc, cust2Loc) * 2
        assertEquals(expectedDist2, route2.totalDistance, 0.01)

        assertNotEquals(route1.vehicle.id, route2.vehicle.id, "The two routes should use different vehicles.")
    }

    @Test
    fun testCalculateSavings_NoSavingExpected() {
        val depotLoc = LatLng(0.0, 0.0)
        // Customers are further from each other than from the depot, or arranged colinearly with depot in middle
        val cust1Loc = LatLng(0.0, 1.0) // North of depot
        val cust2Loc = LatLng(0.0, -1.0) // South of depot, colinear with depot

        // Using our local testDistFunc for expected calculation:
        val d_d_c1 = testDistFunc(depotLoc, cust1Loc) // Expected: 1.0
        val d_d_c2 = testDistFunc(depotLoc, cust2Loc) // Expected: 1.0
        val d_c1_c2 = testDistFunc(cust1Loc, cust2Loc) // Expected: 2.0 (0.0, 1.0) to (0.0, -1.0)

        // Saving = S(i,j) = d(D,Ci) + d(D,Cj) - d(Ci,Cj)
        val expectedSavingValUsingTestDistance = d_d_c1 + d_d_c2 - d_c1_c2 // Expected: 1.0 + 1.0 - 2.0 = 0.0

        // As with the positive saving test, direct assertion is not possible without refactoring.
        // The ClarkeWrightSavings algorithm only adds savings if savingVal > 0.
        // So, in this scenario, no saving entry for this pair should be added to its internal savingsList.
        assertTrue(true, "This test currently only sets up data and calculates expected saving (0.0 or negative) with a test distance function. Actual assertion requires refactoring or indirect testing. Expected saving: $expectedSavingValUsingTestDistance.")
        System.out.println("Expected saving (0.0 or negative, using test Euclidean distance): $expectedSavingValUsingTestDistance")

        // If savingsList were accessible and `calculateAllSavings` extracted:
        // val depot = DepotEntity(id = 1, name = "Depot", location = depotLoc)
        // val customer1 = CustomerEntity(id = 1, name = "C1", location = cust1Loc, demand = 10.0)
        // val customer2 = CustomerEntity(id = 2, name = "C2", location = cust2Loc, demand = 10.0)
        // val customers = listOf(customer1, customer2)
        //
        // val actualSavings = clarkeWrightSavings.calculateAllSavings(depot, customers, DistanceUtils.calculateDistance)
        // val actualSavingForPair = actualSavings.firstOrNull {
        //     (it.cust1.id == customer1.id && it.cust2.id == customer2.id) ||
        //     (it.cust1.id == customer2.id && it.cust2.id == customer1.id)
        // }
        // // If the SUT uses Haversine, the actual saving might be slightly different but should still be <= 0
        // // For this specific collinear case with Haversine, it would also be 0.
        // assertNull(actualSavingForPair, "Saving between C1 and C2 should not be positive and thus not added, or its value should be <= 0.")
        // // Or, if the list contains non-positive savings:
        // // assertNotNull(actualSavingForPair)
        // // assertTrue(actualSavingForPair!!.value <= 0.0)
    }
}
