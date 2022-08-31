package project.sheridancollege.wash2goproject.common

data class DetailerServicesPrice(
    val addsOnCharges: Int,
    val serviceAndPriceList: List<ServicePrice>
) {
}