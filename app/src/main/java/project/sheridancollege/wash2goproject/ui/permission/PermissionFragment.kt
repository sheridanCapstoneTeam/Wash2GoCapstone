package project.sheridancollege.wash2goproject.ui.permission

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.databinding.FragmentPermissionBinding
import project.sheridancollege.wash2goproject.ui.detailer.DetailerActivity
import project.sheridancollege.wash2goproject.ui.detailer.setup.DetailerSetupActivity
import project.sheridancollege.wash2goproject.util.Permission.requestLocationPermission

class PermissionFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding!!
    private lateinit var PERMISSION_FROM: AppEnum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            PERMISSION_FROM = it.get("permissionFrom") as AppEnum
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {
            requestLocationPermission(this)
        }

        if (PERMISSION_FROM == AppEnum.DETAILER) {
            setupViews()
        }

        return binding.root
    }

    private fun setupViews() {
        (activity as DetailerSetupActivity).setTitle("Access Location")
        (activity as DetailerSetupActivity).hideBody()
        (activity as DetailerSetupActivity).hideBackBtn()
        (activity as DetailerSetupActivity).setToolbarProgress(100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

        when(PERMISSION_FROM) {
            AppEnum.DETAILER -> {
                startActivity(Intent(requireContext(), DetailerActivity::class.java))
                requireActivity().finish()
            }
            AppEnum.CUSTOMER -> {
                findNavController().navigate(R.id.action_permissionToCustomerHome)
            }
            else -> {}
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}