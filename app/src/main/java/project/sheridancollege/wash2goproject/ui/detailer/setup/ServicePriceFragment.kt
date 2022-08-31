package project.sheridancollege.wash2goproject.ui.detailer.setup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.ServicePrice
import project.sheridancollege.wash2goproject.databinding.FragmentServicePriceBinding
import project.sheridancollege.wash2goproject.ui.detailer.adapter.ServicePriceAdapter
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class ServicePriceFragment : Fragment(),ServicePriceListener {

    private lateinit var binding: FragmentServicePriceBinding
    private var selectedVehicleTypeList = ArrayList<String>()
    private lateinit var serviceDescription: StringBuilder
    private var servicePriceList = ArrayList<ServicePrice>()

    private lateinit var servicePriceAdapter: ServicePriceAdapter

    companion object {
        val TAG: String = ServicePriceFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selectedVehicleTypeList = it.getStringArrayList("vehicleTypeList") as ArrayList<String>
        }

        Log.e(TAG, "VehicleTypeList ->" + Gson().toJson(selectedVehicleTypeList))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_service_price, container, false)
        setupViews()
        return binding.root
    }


    private fun setupViews() {
        (activity as DetailerSetupActivity).setTitle("Service Prices")
        (activity as DetailerSetupActivity).setBody("Please set the service price for each vehicle type")
        (activity as DetailerSetupActivity).showBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(40)

        binding.disclaimerTv.text =
            "Base service charges for all vehicle categories are ${SharedPreferenceUtils.getAppConfig()?.baseServiceCharges}$"
        serviceDescription = StringBuilder()
        serviceDescription.append("Service Includes:\n\n")
        for (service in SharedPreferenceUtils.getAppConfig()?.servicesList!!) {
            serviceDescription.append("- $service\n")
        }
        binding.serviceDesc.text = serviceDescription.toString()

        setupServicePriceWithBaseFare()

        binding.servicePriceNextBtn.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.action_servicePriceToAddOns,
            bundleOf("servicePriceList" to servicePriceList))
        })

    }

    private fun setupServicePriceWithBaseFare() {
        servicePriceList.clear()
        for (vehicleType in selectedVehicleTypeList) {
            var servicePrice = ServicePrice(
                vehicleType,
                SharedPreferenceUtils.getAppConfig()?.baseServiceCharges!!
            )
            servicePriceList.add(servicePrice)
        }

        servicePriceAdapter =
            ServicePriceAdapter(servicePriceList,this)
        binding.vehicleTypeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.vehicleTypeRv.adapter = servicePriceAdapter
    }

    override fun setNextBtnVisibility(viewVisibility: Int) {
        binding.servicePriceNextBtn.visibility = viewVisibility
    }

}
interface ServicePriceListener{
    fun setNextBtnVisibility(viewVisibility: Int)
}
