package project.sheridancollege.wash2goproject.common


data class User(
    val userId: String = "",
    val firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var streetNum: String = "",
    var streetName: String = "",
    var city: String = "",
    var phone: String = "",
    var isProvider: Boolean = false,
    var isSetupCompleted: Boolean = false,
    var haveCleaningKit: Boolean = false,
    var isCleaningKitReceive: Boolean = false,
    var status: UserStatus = UserStatus.OFFLINE,
    var currentLat: Double = 0.0,
    var currentLong: Double = 0.0,
    var fcmToken: String = "N/A"
)
