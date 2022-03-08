package project.sheridancollege.wash2goproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
// pass the same server client ID used while implementing the LogIn feature earlier.
        val btnLogout : Button = findViewById(R.id.btnlogout)
        mAuth = FirebaseAuth.getInstance()
        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@MainActivity,
                LoginAcivity::class.java)
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent= Intent(this, LoginAcivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}