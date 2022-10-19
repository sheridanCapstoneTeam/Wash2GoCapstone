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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.databinding.FragmentVehicleTypeBinding
import project.sheridancollege.wash2goproject.ui.detailer.adapter.VehicleTypeAdapter
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class VehicleTypeFragment : Fragment(), VehicleTypeListener {

    private lateinit var binding: FragmentVehicleTypeBinding
    private lateinit var vehicleTypeAdapter: VehicleTypeAdapter
    private var selectedVehicleTypeList = ArrayList<String>()

    companion object {
        val TAG: String = VehicleTypeFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SharedPreferenceUtils.getUserDetails()?.isSetupCompleted == true) {
            if (SharedPreferenceUtils.getUserDetails()?.haveCleaningKit == true) {
                if (SharedPreferenceUtils.getUserDetails()?.isCleaningKitReceive == false) {
                    findNavController().navigate(R.id.action_vehicleTypeToCleaningKitReceived)
                    return
                }
                findNavController().navigate(
                    R.id.action_vehicleTypeToPermissionFragment,
                    bundleOf("permissionFrom" to AppEnum.DETAILER)
                )
                return
            }
            findNavController().navigate(R.id.action_vehicleTypeToCleaningSupplies)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_vehicle_type, container, false)
        setupViews()
        return binding.root
    }

    private fun setupViews() {

        selectedVehicleTypeList.clear()

        (activity as DetailerSetupActivity).setTitle("Vehicle Type")
        (activity as DetailerSetupActivity).setBody("Please select the vehicle type(s) you are operating currently")
        (activity as DetailerSetupActivity).hideBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(20)

        vehicleTypeAdapter =
            VehicleTypeAdapter(SharedPreferenceUtils.getAppConfig()?.vehicleTypesList, this)
        binding.vehicleTypeRv.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.vehicleTypeRv.adapter = vehicleTypeAdapter

        binding.vtNextButton.setOnClickListener(View.OnClickListener {
            findNavController().navigate(
                R.id.action_vehicleTypeToservicePrice,
                bundleOf("vehicleTypeList" to selectedVehicleTypeList)
            )
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun addVehicleType(vehicleType: String) {
        selectedVehicleTypeList.add(vehicleType)
        Log.e(TAG, Gson().toJson(selectedVehicleTypeList))
        setupNextBtn();
    }

    private fun setupNextBtn() {

        if (selectedVehicleTypeList.size == 0)
            binding.vtNextButton.visibility = View.GONE
        else
            binding.vtNextButton.visibility = View.VISIBLE

    }


    override fun removeVehicleType(vehicleType: String) {
        selectedVehicleTypeList.remove(vehicleType)
        Log.e(TAG, Gson().toJson(selectedVehicleTypeList))
        setupNextBtn()
    }
}

interface VehicleTypeListener {
    fun addVehicleType(vehicleType: String)
    fun removeVehicleType(vehicleType: String)
}