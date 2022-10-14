package project.sheridancollege.wash2goproject.ui.detailer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.Order

class OrderAdapter(
    private val orderList: ArrayList<Order>?,
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

        holder.orderId.text = order?.orderId
        holder.carType.text = order?.carType
        holder.carCondition.text = order?.carCondition.toString()
        holder.addOns.text = if(order?.addOnsInclude == true) "YES" else "NO"
        holder.totalAmount.text = "$${order?.totalPrice}"
        holder.jobStatus.text = order?.status
        holder.jobDate.text = order?.orderDateTime!!.split(" ")[0]
        holder.jobTime.text = order.orderDateTime.split(" ")[1]
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

}