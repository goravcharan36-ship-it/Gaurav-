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
}
