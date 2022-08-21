package project.sheridancollege.wash2goproject.common

import com.google.firebase.database.PropertyName

data class Config(
    var addOnsList: List<Any>,
    var servicesList: List<Any>,
    var vehicleTypesList: List<Any>
) {
}