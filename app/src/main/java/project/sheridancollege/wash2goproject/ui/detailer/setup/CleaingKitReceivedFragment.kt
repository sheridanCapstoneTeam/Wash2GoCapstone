package project.sheridancollege.wash2goproject.ui.detailer.setup

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentCleaningKitReceivedBinding
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class CleaingKitReceivedFragment : Fragment() {

    private lateinit var binding: FragmentCleaningKitReceivedBinding
    private lateinit var progressDialog: ProgressDialog

    companion object {
        val TAG: String = CleaingKitReceivedFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_cleaning_kit_received,
                container,
                false
            )

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Please Wait...")

        setupViews()
        return binding.root
    }

    private fun setupViews() {

        (activity as DetailerSetupActivity).setTitle("Almost Done!")
        (activity as DetailerSetupActivity).setBody("You are just one step behind to complete your onboarding process and Go Live!")
        (activity as DetailerSetupActivity).hideBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(100)

        binding.doneBtn.setOnClickListener(View.OnClickListener {
            if (binding.activationCodeEt.text.length == 5) {
                doVerifyCode()
            } else {
                binding.activationCodeEt.error = "Please enter 5 digit code"
            }

        })
    }

    private fun doVerifyCode() {
        progressDialog.show()

        var user: User? = SharedPreferenceUtils.getUserDetails()
        user?.isCleaningKitReceive = true

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


                SharedPreferenceUtils.saveUserDetails(user)
                Log.e(CleaningSuppliesFragment.TAG, "Updated User -> " + Gson().toJson(user))


                findNavController().navigate(
                    R.id.action_cleaningKitFragmentToPermission,
                    bundleOf("permissionFrom" to AppEnum.DETAILER)
                )
            })
    }


}