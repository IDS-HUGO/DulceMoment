package com.example.dulcemoment.domain

private val ORDER_STATUS_SEQUENCE = listOf("created", "in_oven", "decorating", "on_the_way", "delivered")

fun isValidOrderStatusTransition(currentStatus: String, targetStatus: String): Boolean {
    val currentIndex = ORDER_STATUS_SEQUENCE.indexOf(currentStatus)
    val targetIndex = ORDER_STATUS_SEQUENCE.indexOf(targetStatus)
    if (currentIndex == -1 || targetIndex == -1) return false
    return targetIndex == currentIndex + 1
}

fun sellerNextOrderStatuses(currentStatus: String): List<Pair<String, String>> {
    return when (currentStatus) {
        "created" -> listOf("in_oven" to "En horno")
        "in_oven" -> listOf("decorating" to "Decorando")
        "decorating" -> listOf("on_the_way" to "En camino")
        "on_the_way" -> listOf("delivered" to "Entregado")
        "delivered" -> emptyList()
        else -> emptyList()
    }
}

fun orderRequiresPayment(orderStatus: String, paymentConfirmed: Boolean): Boolean {
    return orderStatus == "created" && !paymentConfirmed
}
