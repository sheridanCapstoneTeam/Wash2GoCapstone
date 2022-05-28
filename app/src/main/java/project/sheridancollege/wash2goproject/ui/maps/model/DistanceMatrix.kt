package project.sheridancollege.wash2goproject.ui.maps.model

import com.google.gson.annotations.SerializedName


data class DistanceMatrix (

  @SerializedName("destination_addresses" ) var destinationAddresses : ArrayList<String> = arrayListOf(),
  @SerializedName("origin_addresses"      ) var originAddresses      : ArrayList<String> = arrayListOf(),
  @SerializedName("rows"                  ) var rows                 : ArrayList<Rows>   = arrayListOf(),
  @SerializedName("status"                ) var status               : String?           = null

)

data class Distance (

  @SerializedName("text"  ) var text  : String? = null,
  @SerializedName("value" ) var value : Int?    = null

)

data class Duration (

  @SerializedName("text"  ) var text  : String? = null,
  @SerializedName("value" ) var value : Int?    = null

)
data class Elements (

  @SerializedName("distance" ) var distance : Distance? = Distance(),
  @SerializedName("duration" ) var duration : Duration? = Duration(),
  @SerializedName("status"   ) var status   : String?   = null

)


data class Rows (

  @SerializedName("elements" ) var elements : ArrayList<Elements> = arrayListOf()

)