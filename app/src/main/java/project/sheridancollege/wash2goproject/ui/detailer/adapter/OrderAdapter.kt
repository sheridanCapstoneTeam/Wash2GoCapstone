package project.sheridancollege.wash2goproject.ui.detailer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.ui.detailer.bottomsheet.ItemClickListener

class OrderAdapter(
    private val orderList: ArrayList<Order>?,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<OrderViewHolder>() {

    companion object {
        val TAG: String = OrderAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_order_item, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: OrderViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val order = orderList?.get(position)


        var backgroundID = android.R.color.holo_blue_light

        when(order?.status){
            AppEnum.NEW.toString() ->{
                backgroundID = android.R.color.holo_blue_light
            }
            AppEnum.ACTIVE.toString() ->{
                backgroundID = android.R.color.holo_orange_light
            }
            AppEnum.COMPLETED.toString() -> {
                holder.jobViewBtn.visibility = View.GONE
                backgroundID = android.R.color.holo_green_light
            }
            AppEnum.DECLINED.toString() ->{
                holder.jobViewBtn.visibility = View.GONE
                backgroundID = android.R.color.holo_red_light
            }
        }

        holder.orderId.setBackgroundResource(backgroundID)
        holder.orderId.text = order?.orderId
        holder.carType.text = order?.carType
        holder.carCondition.text = order?.carCondition.toString()
        holder.addOns.text = if(order?.addOnsInclude == true) "YES" else "NO"
        holder.totalAmount.text = "$${order?.totalPrice}"
        holder.jobStatus.text = order?.status
        holder.jobDate.text = order?.orderDateTime!!.split(" ")[0]
        holder.jobTime.text = order.orderDateTime.split(" ")[1]

        holder.jobViewBtn.setOnClickListener(View.OnClickListener {
            itemClickListener.OrderViewBtnClick(order)
        })
    }

    override fun getItemCount(): Int {
        return orderList!!.size
    }
}

class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var orderId: TextView = view.findViewById(R.id.order_id_tv)
    var carType: TextView = view.findViewById(R.id.carTypeTv)
    var carCondition: TextView = view.findViewById(R.id.carConditionTv)
    var addOns: TextView = view.findViewById(R.id.addsOnTv)
    var totalAmount: TextView = view.findViewById(R.id.totalTv)
    var jobStatus: TextView = view.findViewById(R.id.statusTv)
    var jobDate: TextView = view.findViewById(R.id.dateTv)
    var jobTime: TextView = view.findViewById(R.id.timeTv)
    var jobViewBtn: Button = view.findViewById(R.id.jobViewBtn)

}