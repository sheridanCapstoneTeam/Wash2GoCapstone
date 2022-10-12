package project.sheridancollege.wash2goproject.ui.customer.ui.order

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentOrderReviewBinding
import project.sheridancollege.wash2goproject.firebase.FCMHandler
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class OrderReviewFragment : Fragment() {

    private lateinit var binding: FragmentOrderReviewBinding
    private lateinit var order: Order
    private lateinit var user: User
    private lateinit var orderReviewViewModel: OrderReviewViewModel
    private lateinit var progressDialog: ProgressDialog

    companion object {
        val TAG: String = OrderReviewFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            order = it.get("order") as Order
        }
        Log.e(TAG, "Order : " + Gson().toJson(order))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        orderReviewViewModel =
            ViewModelProvider(this).get(OrderReviewViewModel::class.java)

        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_order_review,
                container,
                false
            )

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCancelable(false)


        user = SharedPreferenceUtils.getUserDetails()
        binding.customerName.text = user.firstName + " " + user.lastName
        binding.orderNumber.text = order.orderId
        binding.orderDate.text = order.orderDateTime.split(" ")[0]
        binding.orderTime.text = order.orderDateTime.split(" ")[1]
        binding.carType.text = order.carType
        binding.servicePayment.text = "$${order.servicePrice}"
        binding.addOnsPayment.text = "$${order.addOnsPrice}"
        binding.totalPayment.text = "$${order.totalPrice}"


        binding.detailerName.text = "Not Available"
        binding.detailerNumber.text = "Not Available"

        orderReviewViewModel.getDetailerData(order.detailerId)
        orderReviewViewModel.detailer.observe(viewLifecycleOwner) {
            binding.detailerName.text = it?.firstName + " " + it?.lastName
            binding.detailerNumber.text = it?.phone
        }

        binding.cancelBtn.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_go_back_to_home)
        })

        binding.paymentBtn.setOnClickListener(View.OnClickListener {
            progressDialog.show()
            orderReviewViewModel.insertOrder(order)
        })

        orderReviewViewModel.orderResult.observe(viewLifecycleOwner) {
            if (progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(requireContext(), "Order saved successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_go_back_to_home)
        }

        return binding.root
    }
}
