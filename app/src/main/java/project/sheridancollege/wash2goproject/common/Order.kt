package project.sheridancollege.wash2goproject.common

import java.io.Serializable

data class Order(
    val orderId: String = "",
    val customerId: String = "",
    var detailerId: String = "",
    var orderDateTime: String = "",
    var carType: String = "",
    var carCondition: Int = 0,
    var addOnsInclude: Boolean = false,
    var servicePrice: Int = 0,
    var addOnsPrice: Int = 0,
    var totalPrice: Int = 0,
    var status: String = AppEnum.NEW.toString(),
    var customerLat: Double = 0.0,
    var customerLong: Double = 0.0,
    var isPaid: Boolean = false
) : Serializable