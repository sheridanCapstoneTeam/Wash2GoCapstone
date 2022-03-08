package project.sheridancollege.wash2goproject

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.internal.util.HalfSerializer.onComplete

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btn:TextView = findViewById(R.id.alreadyHaveAccount)
        val btnRegister:Button = findViewById(R.id.btnRegister)
          mAuth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            checkCredentials();
        }
        btn.setOnClickListener {

                val intent = Intent(this@RegisterActivity,
                    LoginAcivity::class.java)

                startActivity(intent)


        }
    }

    private fun checkCredentials(){
        val username:EditText = findViewById(R.id.userName)
        val email:EditText = findViewById(R.id.inputEmail)
        val password: EditText = findViewById(R.id.inputPassword)
        val conformPassword:EditText = findViewById(R.id.inputConfirmPassword)
        val userName = username.text.toString()
        val inputEmail = email.text.toString()
        val inputPass = password.text.toString()
        val inputConformPass = conformPassword.text.toString()

        if(userName.isEmpty() || userName.length > 30) {
           showError(username,"your Name is not valid")
        } else if (inputEmail.isEmpty() || !inputEmail.contains("@")){
            showError(email,"Enter a valid email")
        }else if (inputPass.isEmpty() || inputPass.length < 7){
          showError(password,"Password must be 7 character")
        }else if (inputConformPass.isEmpty() || !inputConformPass.equals(inputPass)){
            showError(conformPassword ,"password not Match")
        }else {
            mAuth.createUserWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if(task.isSuccessful){
                        Toast.makeText(
                            this, "Your are successfully registered ", Toast.LENGTH_LONG
                        ).show()
                        val firebaseUser = this.mAuth.currentUser!!
                        val intent = Intent(this@RegisterActivity,
                            MainActivity::class.java)

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
}