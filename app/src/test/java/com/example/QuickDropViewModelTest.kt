package com.example

import com.example.data.QuickDropCatalog
import com.example.model.AppMode
import com.example.viewmodel.QuickDropViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickDropViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial cart calculates subtotal, discount, and free delivery milestone accurately`() {
        val vm = QuickDropViewModel()
        val state = vm.uiState.value
        assertEquals(4, state.totalCartItemsCount)
        assertEquals(448, state.bill.itemSubtotal)
        assertEquals(52, state.bill.amountNeededForFreeDelivery)
        assertEquals(100, state.bill.couponDiscount)
    }

    @Test
    fun `adding restaurant dish updates cart and placing order creates live tracked order`() {
        val vm = QuickDropViewModel()
        vm.setAppMode(AppMode.FOOD)
        assertEquals(AppMode.FOOD, vm.uiState.value.appMode)

        val dish = QuickDropCatalog.restaurants.first().menu.first()
        vm.addDishToCart(dish)
        assertEquals(5, vm.uiState.value.totalCartItemsCount)

        vm.placeOrder("Google Pay UPI")
        val after = vm.uiState.value
        assertEquals(0, after.totalCartItemsCount)
        assertNotNull(after.activeTrackingOrder)
        assertTrue(after.orders.first().isFoodOrder)
    }

    @Test
    fun `admin panel stays locked on wrong password and unlocks with gorav998365`() {
        val vm = QuickDropViewModel()
        vm.openAdminPanel()
        assertTrue(vm.uiState.value.isAdminMode)
        assertFalse(vm.uiState.value.isAdminUnlocked)

        vm.unlockAdmin("wrong123")
        assertFalse(vm.uiState.value.isAdminUnlocked)
        assertNotNull(vm.uiState.value.adminUnlockError)

        vm.unlockAdmin("gorav998365")
        assertTrue(vm.uiState.value.isAdminUnlocked)
        assertNull(vm.uiState.value.adminUnlockError)

        assertTrue(vm.changeAdminPassword("gorav998365", "newpass99"))
        vm.lockAdmin()
        assertFalse(vm.uiState.value.isAdminUnlocked)
        assertFalse(vm.uiState.value.isAdminMode)

        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        assertFalse(vm.uiState.value.isAdminUnlocked)
        vm.unlockAdmin("newpass99")
        assertTrue(vm.uiState.value.isAdminUnlocked)
    }

    @Test
    fun `admin full crud edits variant prices synced to cart and adds or removes items`() {
        val vm = QuickDropViewModel()
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")

        // Edit milk product variant price and verify live cart syncs
        val milk = vm.uiState.value.products.first { it.id == "prod_amul_milk" }
        val updatedVariants = milk.variants.map {
            if (it.id == "amul_milk_500") it.copy(price = 35, mrp = 40) else it
        }
        vm.updateProductFull(
            productId = milk.id,
            brand = "Amul Gold",
            name = "Amul Toned Fresh Milk",
            categoryId = milk.categoryId,
            subcategory = milk.subcategory,
            etaMinutes = 8,
            isVeg = true,
            isInStock = true,
            isBestseller = true,
            isStealDeal = false,
            badgeText = "HOT",
            variants = updatedVariants
        )
        val cartMilk = vm.uiState.value.cartItems.first { it.productId == "prod_amul_milk" }
        assertEquals(35, cartMilk.price)
        assertEquals("Amul Toned Fresh Milk", cartMilk.title)

        // Add & delete product
        val countBeforeAdd = vm.uiState.value.products.size
        vm.addNewProduct("QuickDrop", "Organic Avocado", "fruits_veg", "Exotics", "2 pcs", 149, 199, true)
        assertEquals(countBeforeAdd + 1, vm.uiState.value.products.size)
        val addedProdId = vm.uiState.value.products.first().id
        vm.deleteProduct(addedProdId)
        assertEquals(countBeforeAdd, vm.uiState.value.products.size)

        // Add, edit, delete restaurant menu dish
        val rest = vm.uiState.value.restaurants.first()
        val dishesBefore = rest.menu.size
        vm.addDishToRestaurant(rest.id, "Chef Thali", "Rich platter", "Mains", 310, 360, true, true, false)
        val newDish = vm.uiState.value.restaurants.first { it.id == rest.id }.menu.first()
        assertEquals(dishesBefore + 1, vm.uiState.value.restaurants.first { it.id == rest.id }.menu.size)
        vm.updateDishInRestaurant(rest.id, newDish.id, "Royal Thali", "Updated", "Mains", 350, 400, true, true, true)
        assertEquals(350, vm.uiState.value.restaurants.first { it.id == rest.id }.menu.first { it.id == newDish.id }.price)
        vm.deleteDishFromRestaurant(rest.id, newDish.id)
        assertEquals(dishesBefore, vm.uiState.value.restaurants.first { it.id == rest.id }.menu.size)

        // Fee config update
        vm.updateFeeConfig(baseDeliveryFee = 35, handlingFee = 7, weatherSurgeFee = 20, freeThreshold = 799)
        vm.toggleWeatherSurge()
        assertEquals(35, vm.uiState.value.bill.deliveryFee)
        assertEquals(7, vm.uiState.value.bill.handlingFee)
        assertEquals(20, vm.uiState.value.bill.surgeFee)

        // Coupon add & delete
        vm.addNewCoupon("ADMIN50", "Flat ₹50", "Admin test coupon", 50, 100, false)
        assertTrue(vm.uiState.value.activeCoupons.any { it.code == "ADMIN50" })
        vm.deleteCoupon("ADMIN50")
        assertFalse(vm.uiState.value.coupons.any { it.code == "ADMIN50" })
    }

    @Test
    fun `only admin can edit order status and assign rider`() {
        val vm = QuickDropViewModel()
        vm.placeOrder("Google Pay UPI")
        val placed = vm.uiState.value.orders.first()
        assertEquals(0, placed.stageIndex)

        // Non-admin attempts should be blocked
        vm.advanceOrderStage(placed.orderId)
        vm.assignOrderRider(placed.orderId, "Deepak Verma")
        vm.updateOrderDetails(placed.orderId, etaMinutes = 1, stageIndex = 3, deliveryAddress = "Fake Street")
        val afterBlocked = vm.uiState.value.orders.first { it.orderId == placed.orderId }
        assertEquals(0, afterBlocked.stageIndex)
        assertEquals(placed.riderName, afterBlocked.riderName)
        assertEquals(placed.deliveryAddress, afterBlocked.deliveryAddress)

        // Unlock Admin -> updates succeed
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        vm.advanceOrderStage(placed.orderId)
        vm.assignOrderRider(placed.orderId, "Deepak Verma")
        val afterAdmin = vm.uiState.value.orders.first { it.orderId == placed.orderId }
        assertEquals(1, afterAdmin.stageIndex)
        assertEquals("Deepak Verma", afterAdmin.riderName)
    }

    @Test
    fun `extra items can be added to cart and pre-dispatch orders`() {
        val vm = QuickDropViewModel()

        // Custom extra item directly to cart
        val beforeCartCount = vm.uiState.value.totalCartItemsCount
        val beforeSubtotal = vm.uiState.value.bill.itemSubtotal
        vm.addCustomItemToCart("Fresh Coriander Bunch", 25)
        assertEquals(beforeCartCount + 1, vm.uiState.value.totalCartItemsCount)
        assertEquals(beforeSubtotal + 25, vm.uiState.value.bill.itemSubtotal)

        // Place order -> stageIndex is 0 (before Out for Delivery)
        vm.placeOrder("Google Pay UPI")
        val order = vm.uiState.value.orders.first()
        val prod = vm.uiState.value.products.first()
        val prodPrice = prod.variants[prod.defaultVariantIndex].price

        // Customer adds extra grocery product, food dish, and custom item before dispatch
        vm.addExtraProductToOrder(order.orderId, prod)
        vm.addCustomExtraToOrder(order.orderId, "Ice Cubes", 40)
        val updatedOrder = vm.uiState.value.orders.first { it.orderId == order.orderId }
        assertEquals(order.itemsCount + 2, updatedOrder.itemsCount)
        assertEquals(order.totalPaid + prodPrice + 40, updatedOrder.totalPaid)

        // Admin advances order to stageIndex = 2 (Out for Delivery) -> non-admin extra add blocked
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        vm.updateOrderDetails(order.orderId, etaMinutes = 4, stageIndex = 2, deliveryAddress = order.deliveryAddress)
        vm.lockAdmin()

        val countBeforeBlockedExtra = vm.uiState.value.orders.first { it.orderId == order.orderId }.itemsCount
        vm.addCustomExtraToOrder(order.orderId, "Late Item", 50)
        assertEquals(countBeforeBlockedExtra, vm.uiState.value.orders.first { it.orderId == order.orderId }.itemsCount)
    }

    @Test
    fun `order generates 4-digit delivery OTP and rider completes ride only with valid OTP`() {
        val vm = QuickDropViewModel()
        vm.placeOrder("Google Pay UPI")
        val placed = vm.uiState.value.orders.first()

        // Valid 4-digit OTP assigned immediately on order placement and not yet verified
        assertEquals(4, placed.deliveryOtp.length)
        assertTrue(placed.deliveryOtp.all { it.isDigit() })
        assertFalse(placed.isOtpVerified)
        assertEquals(0, placed.stageIndex)

        // Cannot verify OTP while not unlocked as Admin/Rider dispatch
        assertFalse(vm.verifyDeliveryOtpAndCompleteOrder(placed.orderId, placed.deliveryOtp))
        assertEquals(0, vm.uiState.value.orders.first { it.orderId == placed.orderId }.stageIndex)

        // Unlock Admin -> advance 0 -> 1 -> 2, cannot advance past 2 without OTP
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        vm.advanceOrderStage(placed.orderId) // 1
        vm.advanceOrderStage(placed.orderId) // 2 (Out for Delivery)
        vm.advanceOrderStage(placed.orderId) // Stays at 2 until OTP verified
        assertEquals(2, vm.uiState.value.orders.first { it.orderId == placed.orderId }.stageIndex)

        // Wrong OTP rejected
        val wrongOtp = if (placed.deliveryOtp == "0000") "9999" else "0000"
        assertFalse(vm.verifyDeliveryOtpAndCompleteOrder(placed.orderId, wrongOtp))
        assertEquals(2, vm.uiState.value.orders.first { it.orderId == placed.orderId }.stageIndex)
        assertFalse(vm.uiState.value.orders.first { it.orderId == placed.orderId }.isOtpVerified)

        // Correct customer OTP finishes ride immediately (stageIndex = 3, eta = 0, verified = true)
        assertTrue(vm.verifyDeliveryOtpAndCompleteOrder(placed.orderId, placed.deliveryOtp))
        val completed = vm.uiState.value.orders.first { it.orderId == placed.orderId }
        assertEquals(3, completed.stageIndex)
        assertEquals(0, completed.etaMinutes)
        assertTrue(completed.isOtpVerified)
    }

    @Test
    fun `gps detection toggles progress flag and applies or refreshes Current GPS address cleanly`() {
        val vm = QuickDropViewModel()
        assertFalse(vm.uiState.value.isDetectingGpsLocation)

        vm.setGpsDetecting(true)
        assertTrue(vm.uiState.value.isDetectingGpsLocation)

        vm.applyGpsDetectedLocation(
            areaShort = "HSR Layout, Bengaluru",
            fullAddress = "27th Main Rd, HSR Layout Sector 1, Bengaluru"
        )
        var state = vm.uiState.value
        assertFalse(state.isDetectingGpsLocation)
        assertEquals("Current GPS", state.selectedAddress.label)
        assertEquals("HSR Layout, Bengaluru", state.selectedAddress.areaShort)
        assertEquals("27th Main Rd, HSR Layout Sector 1, Bengaluru", state.selectedAddress.fullAddress)
        assertEquals("addr_gps_live", state.addresses.first().id)

        val countAfterFirst = state.addresses.size

        // Refreshing GPS again updates the same entry in-place without duplicating
        vm.applyGpsDetectedLocation(
            areaShort = "Koramangala, Bengaluru",
            fullAddress = "80 Feet Rd, Koramangala 4th Block, Bengaluru"
        )
        state = vm.uiState.value
        assertEquals(countAfterFirst, state.addresses.size)
        assertEquals(1, state.addresses.count { it.id == "addr_gps_live" })
        assertEquals("Koramangala, Bengaluru", state.selectedAddress.areaShort)
        assertEquals("80 Feet Rd, Koramangala 4th Block, Bengaluru", state.selectedAddress.fullAddress)
    }

    @Test
    fun `shopkeeper applies and submits listings, admin approves to go live, and dual OTP completes order`() {
        val vm = QuickDropViewModel()
        val prodCountBefore = vm.uiState.value.products.size

        // 1. Shopkeeper applies for new Grocery shop with starter listing
        val appId = vm.submitShopApplication(
            shopName = "Verma Organic Store",
            ownerName = "Amit Verma",
            phone = "9988776655",
            shopType = com.example.model.ShopType.GROCERY,
            area = "HSR Layout",
            fullAddress = "14th Main, HSR Sector 2",
            etaMins = 12,
            gstOrLicenseNote = "GSTIN 29VERMA9999Z1",
            initialListings = listOf(
                com.example.model.ShopListingDraft(
                    id = "d_1",
                    title = "Organic Kashmiri Walnuts",
                    categoryOrCuisine = "dairy",
                    unitOrPortion = "250 g",
                    price = 299,
                    mrp = 349,
                    isVeg = true
                )
            )
        )
        assertNotNull(appId)
        val pendingShop = vm.uiState.value.shopApplications.first { it.id == appId }
        assertEquals(com.example.model.ShopApplicationStatus.PENDING_REVIEW, pendingShop.status)

        // Not live yet -> customer catalog unchanged
        assertEquals(prodCountBefore, vm.uiState.value.products.size)
        assertFalse(vm.uiState.value.products.any { it.name == "Organic Kashmiri Walnuts" })

        // Non-admin cannot approve
        vm.approveShopApplication(appId!!)
        assertEquals(
            com.example.model.ShopApplicationStatus.PENDING_REVIEW,
            vm.uiState.value.shopApplications.first { it.id == appId }.status
        )

        // 2. Admin unlocks & approves shop -> items immediately go LIVE
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        vm.approveShopApplication(appId)
        val approvedShop = vm.uiState.value.shopApplications.first { it.id == appId }
        assertEquals(com.example.model.ShopApplicationStatus.APPROVED, approvedShop.status)
        val liveProduct = vm.uiState.value.products.firstOrNull { it.sellerShopId == appId }
        assertNotNull(liveProduct)
        assertEquals("Organic Kashmiri Walnuts", liveProduct!!.name)

        // Approved shopkeeper updates listing price -> live product reflects price change
        val listingId = approvedShop.listings.first().id
        vm.updateShopListingPrice(appId, listingId, 279, 349)
        assertEquals(279, vm.uiState.value.products.first { it.id == liveProduct.id }.variants.first().price)

        // 3. Customer orders from approved shopkeeper -> order routes to shopkeeper with Pickup OTP + Delivery OTP
        vm.lockAdmin()
        vm.emptyCart()
        val updatedLiveProd = vm.uiState.value.products.first { it.id == liveProduct.id }
        vm.addGroceryToCart(updatedLiveProd)
        vm.placeOrder("PhonePe UPI")

        val routedOrder = vm.uiState.value.orders.first()
        assertEquals(appId, routedOrder.shopId)
        assertEquals("Verma Organic Store", routedOrder.hubOrKitchen)
        assertEquals(4, routedOrder.shopPickupOtp.length)
        assertEquals(4, routedOrder.deliveryOtp.length)
        assertFalse(routedOrder.isShopPickupOtpVerified)

        // Shopkeeper marks packed -> stage 1
        vm.shopkeeperMarkOrderPacked(appId, routedOrder.orderId)
        assertEquals(1, vm.uiState.value.orders.first { it.orderId == routedOrder.orderId }.stageIndex)

        // Rider verifies Shopkeeper's Pickup OTP -> stage 2 & pickup verified
        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        assertFalse(vm.verifyShopPickupOtpAndDispatch(routedOrder.orderId, "0000".takeIf { it != routedOrder.shopPickupOtp } ?: "9999"))
        assertTrue(vm.verifyShopPickupOtpAndDispatch(routedOrder.orderId, routedOrder.shopPickupOtp))
        val afterPickup = vm.uiState.value.orders.first { it.orderId == routedOrder.orderId }
        assertEquals(2, afterPickup.stageIndex)
        assertTrue(afterPickup.isShopPickupOtpVerified)

        // Rider verifies Customer's Delivery OTP -> ride completed (stage 3)
        assertTrue(vm.verifyDeliveryOtpAndCompleteOrder(routedOrder.orderId, routedOrder.deliveryOtp))
        val delivered = vm.uiState.value.orders.first { it.orderId == routedOrder.orderId }
        assertEquals(3, delivered.stageIndex)
        assertTrue(delivered.isOtpVerified)
    }

    @Test
    fun `approving food shop publishes restaurant menu and revoking hides shop items`() {
        val vm = QuickDropViewModel()
        val appId = vm.submitShopApplication(
            shopName = "Punjabi Dhaba Express",
            ownerName = "Harpreet Singh",
            phone = "9811223344",
            shopType = com.example.model.ShopType.FOOD,
            area = "Koramangala",
            fullAddress = "80ft Rd, Koramangala",
            etaMins = 22,
            gstOrLicenseNote = "FSSAI-112233",
            initialListings = listOf(
                com.example.model.ShopListingDraft(
                    id = "fd_1",
                    title = "Dal Makhani Bowl",
                    categoryOrCuisine = "North Indian",
                    unitOrPortion = "1 bowl",
                    price = 199,
                    mrp = 249,
                    isVeg = true
                )
            )
        )!!

        vm.openAdminPanel()
        vm.unlockAdmin("gorav998365")
        vm.approveShopApplication(appId)

        val liveRest = vm.uiState.value.restaurants.firstOrNull { it.sellerShopId == appId }
        assertNotNull(liveRest)
        assertEquals("Punjabi Dhaba Express", liveRest!!.name)
        assertEquals(1, liveRest.menu.size)

        // Revoke/Reject removes the restaurant from live customer discovery
        vm.rejectShopApplication(appId, "Temporary kitchen maintenance")
        assertNull(vm.uiState.value.restaurants.firstOrNull { it.sellerShopId == appId })
        assertEquals(
            com.example.model.ShopApplicationStatus.REJECTED,
            vm.uiState.value.shopApplications.first { it.id == appId }.status
        )
    }
}
