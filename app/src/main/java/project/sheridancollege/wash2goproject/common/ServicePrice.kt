package project.sheridancollege.wash2goproject.common

data class ServicePrice(
    var type: String,
    var price: Int
) {
    constructor() : this("", 0)
}