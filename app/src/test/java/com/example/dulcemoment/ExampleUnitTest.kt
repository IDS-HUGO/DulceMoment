package com.example.dulcemoment

import com.example.dulcemoment.domain.isValidOrderStatusTransition
import com.example.dulcemoment.domain.orderRequiresPayment
import com.example.dulcemoment.domain.sellerNextOrderStatuses
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun valid_transition_is_accepted() {
        assertTrue(isValidOrderStatusTransition("created", "in_oven"))
        assertTrue(isValidOrderStatusTransition("in_oven", "decorating"))
    }

    @Test
    fun invalid_transition_is_rejected() {
        assertFalse(isValidOrderStatusTransition("created", "on_the_way"))
        assertFalse(isValidOrderStatusTransition("decorating", "in_oven"))
        assertFalse(isValidOrderStatusTransition("delivered", "created"))
    }

    @Test
    fun seller_next_statuses_follow_sequence() {
        assertEquals(listOf("in_oven" to "En horno"), sellerNextOrderStatuses("created"))
        assertEquals(listOf("delivered" to "Entregado"), sellerNextOrderStatuses("on_the_way"))
        assertTrue(sellerNextOrderStatuses("delivered").isEmpty())
    }

    @Test
    fun payment_required_only_for_unpaid_created_orders() {
        assertTrue(orderRequiresPayment("created", paymentConfirmed = false))
        assertFalse(orderRequiresPayment("created", paymentConfirmed = true))
        assertFalse(orderRequiresPayment("in_oven", paymentConfirmed = false))
    }
}