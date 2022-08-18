package project.sheridancollege.wash2goproject.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.User


class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var mAuth: FirebaseAuth;
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private var rootDatabaseref: DatabaseReference? = null

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        val googleButton: Button = view.findViewById(R.id.btnGoogle)
        val btnLogin: Button = view.findViewById(R.id.btnLogin)
        val btn: TextView = view.findViewById(R.id.textViewSignup)

        rootDatabaseref = FirebaseDatabase.getInstance().reference.child("email")


        btn.setOnClickListener {
            val directions =
                LoginFragmentDirections.actionLoginFragmentToRegisterActivity()
            findNavController().navigate(directions)
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


        viewModel.coOrdinates.observe(viewLifecycleOwner) { response->
            if(response!=null) {
                onPostExecute(response)
                viewModel.resetResponse();
            }
        }
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
            mAuth.signInWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {

                        val userId = mAuth.currentUser?.uid
                        userId?.apply {
                            val userRefs = FirebaseDatabase.getInstance().getReference("USERS").child(userId)
                            userRefs.addValueEventListener(object:ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val user:User? = snapshot.getValue(User::class.java)
                                    Log.d("LoginFragment",user.toString())


                                    viewModel.getCoOrdinates(user?.streetNum+"+"+user?.streetName)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })

                        }










                    } else {
                        Toast.makeText(
                            requireContext(), "sorry something went wrong!", Toast.LENGTH_LONG
                        ).show()
                    }

                }
        }

    }

    private fun showError(input: EditText, s: String) {
        input.setError(s)
        input.requestFocus()
    }

    object SavedPreference {

        const val EMAIL = "email"
        const val USERNAME = "username"

        private fun getSharedPreference(ctx: Context?): SharedPreferences? {
            return PreferenceManager.getDefaultSharedPreferences(ctx)
        }

        private fun editor(context: Context, const: String, string: String) {
            getSharedPreference(
                context
            )?.edit()?.putString(const, string)?.apply()
        }

        fun getEmail(context: Context) = getSharedPreference(
            context
        )?.getString(EMAIL, "")

        fun setEmail(context: Context, email: String) {
            editor(
                context,
                EMAIL,
                email
            )
        }

        fun setUsername(context: Context, username: String) {
            editor(
                context,
                USERNAME,
                username
            )
        }

        fun getUsername(context: Context) = getSharedPreference(
            context
        )?.getString(USERNAME, "")

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
                SavedPreference.setEmail(requireActivity(), account.email.toString())
                SavedPreference.setUsername(requireActivity(), account.displayName.toString())
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
        findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToMapsFragment(
            loc.latitude.toFloat(), loc.longitude.toFloat()
        ))

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