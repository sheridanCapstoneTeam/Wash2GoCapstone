package project.sheridancollege.wash2goproject.common

data class Config(
    var servicesList: List<Any>,
    var addOnsList: List<Any>,
    var vehicleTypesList: List<Any>,
    var baseServiceCharges: Int,
    var baseAddOnsCharges: Int,
    var cleaningSuppliesCharges: Int,
    var cleaningSuppliesList: List<Any>,
    var cleaningSuppliesMarketPrice: Int,
    var carConditionImagesList: String
) {
}