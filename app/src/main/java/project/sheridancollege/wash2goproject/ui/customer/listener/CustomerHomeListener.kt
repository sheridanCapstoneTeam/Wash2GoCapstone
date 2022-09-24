package project.sheridancollege.wash2goproject.ui.customer.listener

import android.location.Location
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice

interface CustomerHomeListener {
    fun bookAppoinmentClick(detailerId: String, detailerServicePrice: DetailerServicesPrice,customerLocation: Location)
}