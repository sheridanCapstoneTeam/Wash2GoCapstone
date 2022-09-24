package project.sheridancollege.wash2goproject.ui.detailer.ui.dialog

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.databinding.LayoutDetailerDetailsDialogBinding
import project.sheridancollege.wash2goproject.ui.customer.listener.CustomerHomeListener
import project.sheridancollege.wash2goproject.ui.detailer.adapter.DetailerDetailDialogAdapter

class DetailerDetailsDialog(public var customerHomeListener: CustomerHomeListener) :
    DialogFragment() {
    companion object {
        val TAG: String = DetailerDetailsDialog::class.java.simpleName
    }

    private var _binding: LayoutDetailerDetailsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var detailerServicePrice: DetailerServicesPrice
    private lateinit var detailerName: String
    private lateinit var detailerUserId: String
    private lateinit var detailerDetailDialogAdapter: DetailerDetailDialogAdapter
    private lateinit var customerLocation:Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            customerLocation = it?.getParcelable<Location>("customerLocation")!!
            detailerName = it.get("detailer_name").toString()
            detailerUserId = it.get("detailer_user_id").toString()
            detailerServicePrice = Gson().fromJson(
                it.getString("detailer_service_price", ""),
                DetailerServicesPrice::class.java
            )
        }

    }

    override fun onStart() {
        super.onStart()

        var width = ViewGroup.LayoutParams.MATCH_PARENT
        var height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.window?.setLayout(width, height)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutDetailerDetailsDialogBinding.inflate(inflater, container, false)

        Log.e(TAG, "Detailer Service Details : " + Gson().toJson(detailerServicePrice))
        binding.detailerNameTv.text = detailerName
        binding.detailerRatingBar.rating = detailerServicePrice.rating.toFloat()
        binding.addOnLabel.text = "Add-Ons Charges : $${detailerServicePrice.addsOnCharges}"

        detailerDetailDialogAdapter =
            DetailerDetailDialogAdapter(detailerServicePrice.serviceAndPriceList)
        binding.servicesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.servicesRv.adapter = detailerDetailDialogAdapter

        binding.closeBtn.setOnClickListener(View.OnClickListener {
            this.dismiss()
        })

        binding.bookBtn.setOnClickListener(View.OnClickListener {
            customerHomeListener.bookAppoinmentClick(detailerUserId, detailerServicePrice,customerLocation)
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}