package project.sheridancollege.wash2goproject.ui.detailer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.ServicePrice

class DetailerDetailDialogAdapter(
    private val servicePriceList: ArrayList<ServicePrice>?
) :
    RecyclerView.Adapter<mViewHolder>() {

    companion object {
        val TAG: String = DetailerDetailDialogAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): mViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_detailer_detail_dialog_item, parent, false)

        return mViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: mViewHolder,
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
        holder.price.text = String.format("%d", servicePrice?.price)
    }

    override fun getItemCount(): Int {
        return servicePriceList!!.size
    }
}

class mViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var vehicleTypeImg: ImageView = view.findViewById(R.id.car_img)
    var vehicleType: TextView = view.findViewById(R.id.vehicle_type_tv)
    var price: TextView = view.findViewById(R.id.price_tv)
}