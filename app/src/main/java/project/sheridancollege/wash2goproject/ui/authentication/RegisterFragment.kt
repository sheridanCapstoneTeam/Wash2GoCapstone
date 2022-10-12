package project.sheridancollege.wash2goproject.ui.authentication

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.databinding.FragmentRegisterBinding
import project.sheridancollege.wash2goproject.util.Constants


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    var currentUserId: String? = ""
    var isProvider: Boolean = false
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Registration")
        progressDialog.setMessage("Please Wait...")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid

        binding.registerBtn.setOnClickListener {
            checkCredentials()
        }

    }

    @SuppressLint("ResourceType")
    private fun checkCredentials() {
        val firstName: EditText = binding.userName
        val lastName: EditText = binding.lastName
        val email: EditText = binding.inputEmail
        val password: EditText = binding.inputPassword
        val conformPassword: EditText = binding.inputConfirmPassword
        val streetNumber: EditText = binding.streetNumber
        val streetName: EditText = binding.streetName
        val phone: EditText = binding.phone
        val city: EditText = binding.city
        val isUserProvider: CheckBox = binding.isProviderCB

        val inputFirstName = firstName.text.toString()
        val inputLastName = lastName.text.toString()
        val inputEmail = email.text.toString()
        val inputPass = password.text.toString()
        val inputConformPass = conformPassword.text.toString()
        val inputStreetNumber = streetNumber.text.toString()
        val inputStreetName = streetName.text.toString()
        val inputPhone = phone.text.toString()
        val inputCity = city.text.toString()

        if (inputFirstName.isEmpty() || inputFirstName.length > 30) {
            showError(firstName, "your First Name is not valid")
        } else if (inputLastName.isEmpty() || inputLastName.length > 30) {
            showError(lastName, "your Last Name is not valid")
        } else if (inputEmail.isEmpty() || !inputEmail.contains("@")) {
            showError(email, "Enter a valid email")
        } else if (inputPass.isEmpty() || inputPass.length < 7) {
            showError(password, "Password must be 7 character")
        } else if (inputConformPass.isEmpty() || inputConformPass != inputPass) {
            showError(conformPassword, "password not Match")
        } else if (inputStreetNumber.isEmpty()) {
            showError(streetNumber, "Please enter a street number")
        } else if (inputStreetName.isEmpty()) {
            showError(streetName, "Please enter a street name")
        } else if (inputPhone.isEmpty()) {
            showError(phone, "Please enter your phone")
        } else if (inputCity.isEmpty()) {
            showError(city, "Please enter your city")
        } else {
            //Add the user to the authentication list000

            progressDialog.show()

            mAuth.createUserWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->

                    if (!task.isSuccessful) {
                        dismissDialog()
                        Toast.makeText(
                            requireContext(), task.exception?.localizedMessage, Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    if (isUserProvider.isChecked) {
                        isProvider = true
                        //Add the user to his appropriate table Provider / Customer
                        //val provider = Provider()
                    }

                    //add user to the realtime database
                    var currentUserId = mAuth.currentUser?.uid.toString()


                    //Add the user to User table
                    val user = User(
                        currentUserId,
                        inputFirstName,
                        inputLastName,
                        inputEmail,
                        inputStreetNumber,
                        inputStreetName,
                        inputCity,
                        inputPhone,
                        isProvider,
                        isSetupCompleted = false,
                        haveCleaningKit = false,
                        isCleaningKitReceive = false,
                        status = UserStatus.OFFLINE,
                        currentLat = 0.0,
                        currentLong = 0.0
                    )

                    // Convert the location to corrdinates
                    //send the coordinates to updateFunction
                    AppClass.databaseReference.child(Constants.USER).child(currentUserId)
                        .setValue(user)
                        .addOnCompleteListener(OnCompleteListener { task ->
                            dismissDialog()
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
                                "Your are successfully registered ",
                                Toast.LENGTH_LONG
                            ).show()
                            findNavController().navigateUp()
                        })


                }
        }
    }

    private fun dismissDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun showError(input: EditText, s: String) {
        input.error = s
        input.requestFocus()
    }

}