package project.sheridancollege.wash2goproject.ui.detailer.setup

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentCleaningSuppliesBinding
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class CleaningSuppliesFragment : Fragment() {

    private lateinit var binding: FragmentCleaningSuppliesBinding
    private lateinit var cleaninSuppliesDesc: StringBuilder
    private lateinit var progressDialog: ProgressDialog

    companion object {
        val TAG: String = CleaningSuppliesFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_cleaning_supplies, container, false)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please Wait...")

        setupViews()
        return binding.root
    }

    private fun setupViews() {

        (activity as DetailerSetupActivity).setTitle("Cleaning Supplies : $${SharedPreferenceUtils.getAppConfig()?.cleaningSuppliesCharges}")
        (activity as DetailerSetupActivity).setBody("It is Mandatory to have all the below mentioned tools in order to perform the jobs")
        (activity as DetailerSetupActivity).hideBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(80)

        cleaninSuppliesDesc = StringBuilder()
        cleaninSuppliesDesc.append("Cleaning Supplies Includes:\n\n")
        for (supplies in SharedPreferenceUtils.getAppConfig()?.cleaningSuppliesList!!) {
            cleaninSuppliesDesc.append("- $supplies\n\n")
        }
        binding.cleaningSuppliesDesc.text = cleaninSuppliesDesc.toString()
        binding.marketValueTv.text =
            "Market Value : $${SharedPreferenceUtils.getAppConfig()?.cleaningSuppliesMarketPrice}"

        binding.suppliesPaymentBtn.setOnClickListener(View.OnClickListener {
            doPurchaseSupplies()
        })
    }

    private fun doPurchaseSupplies() {

        progressDialog.show()

        val user: User? = SharedPreferenceUtils.getUserDetails()
        user?.haveCleaningKit = true

        AppClass.databaseReference.child(Constants.USER).child(user?.userId.toString())
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->

                progressDialog.dismiss()

                if (!task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }

                Toast.makeText(
                    requireContext(),
                    "Thank you for purchasing cleaning kit. Will deliver to you soon.",
                    Toast.LENGTH_LONG
                ).show()

                SharedPreferenceUtils.saveUserDetails(user)
                Log.e(TAG, "Updated User -> " + Gson().toJson(user))

                findNavController().navigate(R.id.action_cleaningSuppliesToReceived)

            })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}