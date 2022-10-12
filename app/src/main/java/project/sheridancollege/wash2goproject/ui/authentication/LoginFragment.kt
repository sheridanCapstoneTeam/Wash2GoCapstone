package project.sheridancollege.wash2goproject.ui.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.ui.customer.CustomerActivity
import project.sheridancollege.wash2goproject.ui.detailer.DetailerActivity
import project.sheridancollege.wash2goproject.ui.detailer.setup.DetailerSetupActivity
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.Permission
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils


class LoginFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth;
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123

    //private lateinit var viewModel: LoginViewModel
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Login")
        progressDialog.setMessage("Please Wait...")
        return inflater.inflate(R.layout.login_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        val googleButton: Button = view.findViewById(R.id.btnGoogle)
        val btnLogin: Button = view.findViewById(R.id.btnLogin)
        val btn: TextView = view.findViewById(R.id.textViewSignup)


        btn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterActivity())
        }

        btnLogin.setOnClickListener {
            checkCredentials(view)
        }
        mAuth = FirebaseAuth.getInstance()


        // Configure Google Sign In
        // Configure Google Sign In inside onCreate mentod
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
// getting the value of gso inside the GoogleSigninClient
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)



        googleButton.setOnClickListener {
            signInGoogle()
        }


        /*viewModel.coOrdinates.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                onPostExecute(response)
                viewModel.resetResponse();
            }
        }*/
    }


    private fun checkCredentials(view: View) {
        val email: EditText = view.findViewById(R.id.inputEmail)
        val password: EditText = view.findViewById(R.id.inputPassword)

        val inputEmail = email.text.toString()
        val inputPass = password.text.toString()

        if (inputEmail.isEmpty() || !inputEmail.contains("@")) {
            showError(email, "Enter a valid email")
        } else if (inputPass.isEmpty() || inputPass.length < 7) {
            showError(password, "Password must be 7 character")

        } else {
            progressDialog.show()

            mAuth.signInWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (!task.isSuccessful) {
                        dismissDialog()
                        Toast.makeText(
                            requireContext(), task.exception?.localizedMessage, Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val userId = mAuth.currentUser?.uid
                    userId?.apply {

                        AppClass.databaseReference.child(Constants.USER)
                            .child(userId)
                            .get().addOnCompleteListener(OnCompleteListener { task ->
                                dismissDialog()
                                if (!task.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        task.exception?.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@OnCompleteListener
                                }

                                val user: User? = task.result.getValue(User::class.java)
                                Log.e("LoginFragment", user.toString())

                                SharedPreferenceUtils.saveUserDetails(user)
                                if (user!!.isProvider) {
                                    //Start detailer flow
                                    if (!user.isSetupCompleted || !user.haveCleaningKit || !user.isCleaningKitReceive || !Permission.hasLocationPermission(
                                            requireContext()
                                        )
                                    ) {
                                        //Initital setup is not completed yet. Move to DetailerSetupActivity
                                        startActivity(
                                            Intent(
                                                requireActivity(),
                                                DetailerSetupActivity::class.java
                                            )
                                        )
                                    } else {
                                        //Initial Setup is done.
                                        startActivity(
                                            Intent(
                                                requireActivity(),
                                                DetailerActivity::class.java
                                            )
                                        )
                                    }
                                    requireActivity().finish()
                                } else {
                                    //Start customer flow
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            CustomerActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                }
                            })
                    }
                }
        }

    }

    private fun dismissDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun showError(input: EditText, s: String) {
        input.setError(s)
        input.requestFocus()
    }

    // signInGoogle() function
    private fun signInGoogle() {

        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // onActivityResult() function : this is where we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    // handleResult() function -  this is where we update the UI after Google signin takes place
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // UpdateUI() function - this is where we specify what UI updation are needed after google signin has taken place.
    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //SavedPreference.setEmail(requireActivity(), account.email.toString())
                //SavedPreference.setUsername(requireActivity(), account.displayName.toString())
                /* val intent = Intent(requireActivity(), MainActivity::class.java)
                 startActivity(intent)
                 finish()*/
            }
        }
    }

    override fun onStart() {
        super.onStart()
        /* if(GoogleSignIn.getLastSignedInAccount(this)!=null){
             startActivity(Intent(this, MainActivity::class.java))
             finish()
         }*/
    }

    private fun onPostExecute(s: String?) {
        // binding.progressBar.visibility = View.GONE
        var loc = Location("")
        try {
            val jsonObject = JSONObject(s)
            val lat =
                (jsonObject["results"] as JSONArray).getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location")["lat"].toString()
            val lng =
                (jsonObject["results"] as JSONArray).getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location")["lng"].toString()
            // binding.txtCoordinates.setText(String.format("Coordinates : %s / %s ", lat, lng))

            //Gettign the
            loc.latitude = lat.toDouble()
            loc.longitude = lng.toDouble()
            val latloc = loc.latitude
            val lngLoc = loc.longitude
            loc = createNewLocation(latloc, lngLoc)
            /*findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToMapsFragment(
                    loc.latitude.toFloat(), loc.longitude.toFloat()
                )
            )*/

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createNewLocation(latitude: Double, longitude: Double): Location {
        val location = Location("")
        location.longitude = longitude
        location.latitude = latitude
        return location
    }


}