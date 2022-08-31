package project.sheridancollege.wash2goproject.ui.detailer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.ui.detailer.setup.VehicleTypeListener

class VehicleTypeAdapter(
    private val vehicleTypeList: List<Any>?,
    private val vehicleTypeListener: VehicleTypeListener
) :
    RecyclerView.Adapter<ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_vehicle_type_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicleType = vehicleTypeList?.get(position)
        holder.vehicleTypeCheckBox.text = vehicleType.toString()

        when (vehicleType) {
            "Sedan" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.sedan_car))
            "SUV" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.suv_car))
            "Hatchback" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.hatch_back_car))
            "Mini Van" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.mini_van))
            "Pickup Truck" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.pickup_truck))
            "Cross Road" -> holder.vehicleTypeImg.setImageDrawable(AppClass.instance.getDrawable(R.drawable.cross_road_car))
        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            holder.vehicleTypeCheckBox.isChecked = !holder.vehicleTypeCheckBox.isChecked
        })

        holder.vehicleTypeCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                vehicleTypeListener.addVehicleType(vehicleType.toString())
            } else {
                vehicleTypeListener.removeVehicleType(vehicleType.toString())
            }
        })
    }

    override fun getItemCount(): Int {
        return vehicleTypeList!!.size
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var vehicleTypeImg: ImageView = view.findViewById(R.id.vehicle_img)
    var vehicleTypeCheckBox: CheckBox = view.findViewById(R.id.vehicle_checkbox)
}