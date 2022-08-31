package project.sheridancollege.wash2goproject.ui.detailer.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.ServicePrice
import project.sheridancollege.wash2goproject.ui.detailer.setup.ServicePriceListener

class ServicePriceAdapter(
    private val servicePriceList: ArrayList<ServicePrice>?,
    private val servicePriceListener: ServicePriceListener
) :
    RecyclerView.Adapter<MyViewHolder>() {

    companion object {
        val TAG: String = ServicePriceAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_service_price_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val servicePrice = servicePriceList?.get(position)

        when (servicePrice?.type) {
            "Sedan" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.sedan_car))
            "SUV" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.suv_car))
            "Hatchback" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.hatch_back_car))
            "Mini Van" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.mini_van))
            "Pickup Truck" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.pickup_truck))
            "Cross Road" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.cross_road_car))
        }

        holder.vehicleType.text = servicePrice?.type
        holder.priceEdittext.setText(String.format("%d", servicePrice?.price))

        holder.priceEdittext.doAfterTextChanged { text ->
            if (!text.isNullOrEmpty()) {
                servicePriceListener.setNextBtnVisibility(View.VISIBLE)
                Log.e(TAG, "doAfterTextChanged $text")
                servicePrice?.price = Integer.parseInt(text.toString())
                Log.e(TAG, "Service Price List -> " + Gson().toJson(servicePriceList))
            } else {
                holder.priceEdittext.setError("Price cannot be empty")
                servicePriceListener.setNextBtnVisibility(View.GONE)
            }
        }
    }

    override fun getItemCount(): Int {
        return servicePriceList!!.size
    }
}

class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var vehicleTypeImg: ImageView = view.findViewById(R.id.car_img)
    var vehicleType: TextView = view.findViewById(R.id.vehicle_type_tv)
    var priceEdittext: EditText = view.findViewById(R.id.price_et)
}