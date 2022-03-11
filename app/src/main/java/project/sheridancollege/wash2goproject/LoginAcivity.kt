package project.sheridancollege.wash2goproject


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginAcivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code:Int=123
    private var rootDatabaseref: DatabaseReference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_acivity)
        val googleButton : Button = findViewById(R.id.btnGoogle)
        val btnLogin : Button = findViewById(R.id.btnLogin)
        val btn : TextView = findViewById(R.id.textViewSignup)

        rootDatabaseref = FirebaseDatabase.getInstance().reference.child("email")


        btn.setOnClickListener {
            val intent = Intent(this@LoginAcivity,
                RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener{
            checkCredentials() }
        mAuth = FirebaseAuth.getInstance()


        // Configure Google Sign In
        // Configure Google Sign In inside onCreate mentod
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
// getting the value of gso inside the GoogleSigninClient
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso)



        googleButton.setOnClickListener {
            signInGoogle()
        }

    }

    private fun checkCredentials(){
        val email: EditText = findViewById(R.id.inputEmail)
        val password: EditText = findViewById(R.id.inputPassword)

        val inputEmail = email.text.toString()
        val inputPass = password.text.toString()

        if (inputEmail.isEmpty() || !inputEmail.contains("@")){
            showError(email,"Enter a valid email")
        }else if (inputPass.isEmpty() || inputPass.length < 7){
            showError(password,"Password must be 7 character")

        }else {
            mAuth.signInWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if(task.isSuccessful){
                        Toast.makeText(
                            this, "Your are successfully registered ", Toast.LENGTH_LONG
                        ).show()

                        val firebaseUser = this.mAuth.currentUser!!
                        val intent = Intent(this@LoginAcivity,
                            MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                or Intent.FLAG_ACTIVITY_NEW_TASK)
                        var currentUserId = mAuth.currentUser?.uid
                        intent.putExtra("currentUserId" , currentUserId)
                        startActivity(intent)
                    }else {
                        Toast.makeText(
                            this, "sorry something went wrong!", Toast.LENGTH_LONG
                        ).show()
                    }

                }
        }

    }

    private  fun showError(input: EditText, s: String){
        input.setError(s)
        input.requestFocus()
    }

    object SavedPreference {

        const val EMAIL= "email"
        const val USERNAME="username"

        private  fun getSharedPreference(ctx: Context?): SharedPreferences? {
            return PreferenceManager.getDefaultSharedPreferences(ctx)
        }

        private fun  editor(context: Context, const:String, string: String){
            getSharedPreference(
                context
            )?.edit()?.putString(const,string)?.apply()
        }

        fun getEmail(context: Context)= getSharedPreference(
            context
        )?.getString(EMAIL,"")

        fun setEmail(context: Context, email: String){
            editor(
                context,
                EMAIL,
                email
            )
        }

        fun setUsername(context: Context, username:String){
            editor(
                context,
                USERNAME,
                username
            )
        }

        fun getUsername(context: Context) = getSharedPreference(
            context
        )?.getString(USERNAME,"")

    }
    // signInGoogle() function
    private  fun signInGoogle(){

        val signInIntent:Intent=mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,Req_Code)
    }
    // onActivityResult() function : this is where we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==Req_Code){
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }
    // handleResult() function -  this is where we update the UI after Google signin takes place
    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e:ApiException){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    // UpdateUI() function - this is where we specify what UI updation are needed after google signin has taken place.
    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        mAuth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                SavedPreference.setEmail(this,account.email.toString())
                SavedPreference.setUsername(this,account.displayName.toString())
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(GoogleSignIn.getLastSignedInAccount(this)!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}