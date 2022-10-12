package project.sheridancollege.wash2goproject.common

data class DetailerServicesPrice(
    val addsOnCharges: Int,
    val rating: Int,
    val totalEarning: Int,
    val serviceAndPriceList: ArrayList<ServicePrice>
) {
    constructor() : this(0, 0, 0,ArrayList())
}