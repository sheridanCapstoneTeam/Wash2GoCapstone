package project.sheridancollege.wash2goproject.ui.detailer.setup

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.ServicePrice
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentAddsOnBinding
import project.sheridancollege.wash2goproject.ui.detailer.adapter.ServicePriceAdapter
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class AddsOnFragment : Fragment() {

    private lateinit var binding: FragmentAddsOnBinding
    private var servicePriceList = ArrayList<ServicePrice>()
    private lateinit var addsOnDescription: StringBuilder
    private lateinit var progressDialog: ProgressDialog

    companion object {
        val TAG: String = AddsOnFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            servicePriceList = it.getStringArrayList("servicePriceList") as ArrayList<ServicePrice>
        }

        Log.e(ServicePriceFragment.TAG, "ServicePriceList ->" + Gson().toJson(servicePriceList))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_adds_on, container, false)
        setupViews()

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please Wait...")


        binding.addOnsNextBtn.setOnClickListener(View.OnClickListener {

            saveDetailerServiceDetails()

        })

        return binding.root
    }

    private fun saveDetailerServiceDetails() {
        progressDialog.show()

        val detailerServicePrice = DetailerServicesPrice(
            Integer.parseInt(binding.addonsPriceEt.text.toString()),
            5,
            0,
            servicePriceList
        )

        AppClass.databaseReference.child(Constants.DETAILER_SERVICE_PRICE)
            .child(SharedPreferenceUtils.getUserDetails()?.userId.toString())
            .setValue(detailerServicePrice)
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }

                val user: User? = SharedPreferenceUtils.getUserDetails()
                user?.isSetupCompleted = true

                AppClass.databaseReference.child(Constants.USER)
                    .child(SharedPreferenceUtils.getUserDetails()?.userId.toString())
                    .setValue(user)
                    .addOnCompleteListener(OnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (!task.isSuccessful) {

                            AppClass.databaseReference.child(Constants.DETAILER_SERVICE_PRICE)
                                .child(SharedPreferenceUtils.getUserDetails()?.userId.toString())
                                .removeValue()

                            Toast.makeText(
                                requireContext(),
                                task.exception?.localizedMessage,
                                Toast.LENGTH_LONG
                            ).show()
                            return@OnCompleteListener
                        }

                        Toast.makeText(
                            requireContext(),
                            "You have successfully added your service prices!",
                            Toast.LENGTH_LONG
                        ).show()

                        SharedPreferenceUtils.saveUserDetails(user)
                        Log.e(TAG, "Updated User -> " + Gson().toJson(user))

                        findNavController().navigate(R.id.action_addonsToCleaningSupplies)

                    })

            })
    }

    private fun setupViews() {

        (activity as DetailerSetupActivity).setTitle("Add-Ons")
        (activity as DetailerSetupActivity).setBody("Please set your Add-ons service price")
        (activity as DetailerSetupActivity).showBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(60)

        binding.addonsDisclaimerTv.text =
            "Base add-ons charges are ${SharedPreferenceUtils.getAppConfig()?.baseAddOnsCharges}$"

        addsOnDescription = StringBuilder()
        addsOnDescription.append("Add-Ons Includes:\n\n")
        for (addons in SharedPreferenceUtils.getAppConfig()?.addOnsList!!) {
            addsOnDescription.append("- $addons\n")
        }
        binding.addonsDesc.text = addsOnDescription.toString()

        binding.addonsPriceEt.setText(
            String.format(
                "%d",
                SharedPreferenceUtils.getAppConfig()?.baseAddOnsCharges
            )
        )

        binding.addonsPriceEt.doAfterTextChanged { text ->
            if (!text.isNullOrEmpty()) {
                binding.addOnsNextBtn.visibility = View.VISIBLE
                Log.e(ServicePriceAdapter.TAG, "doAfterTextChanged $text")
            } else {
                binding.addonsPriceEt.setError("Price cannot be empty")
                binding.addOnsNextBtn.visibility = View.GONE
            }
        }
    }

}
