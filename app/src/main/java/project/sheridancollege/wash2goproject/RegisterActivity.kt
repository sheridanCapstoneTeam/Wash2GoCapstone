package project.sheridancollege.wash2goproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import project.sheridancollege.wash2goproject.databinding.ActivityRegisterBinding
import project.sheridancollege.wash2goproject.ui.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private var database: DatabaseReference? = null
    var currentUserId: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference.child("USERS")




        val btnRegister:Button = findViewById(R.id.btnRegister)
        mAuth = FirebaseAuth.getInstance()
         currentUserId = mAuth.currentUser?.uid

        btnRegister.setOnClickListener {
            checkCredentials()
        }

    }

    @SuppressLint("ResourceType")
    private fun checkCredentials(){
        val firstName: EditText = findViewById(R.id.userName)
        val lastName: EditText = findViewById(R.id.lastName)
        val email:EditText = findViewById(R.id.inputEmail)
        val password: EditText = findViewById(R.id.inputPassword)
        val conformPassword:EditText = findViewById(R.id.inputConfirmPassword)
        val streetNumber: EditText = findViewById(R.id.streetNumber)
        val streetName: EditText = findViewById(R.id.streetName)
        val phone: EditText = findViewById(R.id.phone)
        val city: EditText = findViewById(R.id.city)

        val inputFirstName = firstName.text.toString()
        val inputLastName = lastName.text.toString()
        val inputEmail = email.text.toString()
        val inputPass = password.text.toString()
        val inputConformPass = conformPassword.text.toString()
        val inputStreetNumber = streetNumber.text.toString()
        val inputStreetName = streetName.text.toString()
        val inputPhone = phone.text.toString()
        val inputCity = city.text.toString()




        if(inputFirstName.isEmpty() || inputFirstName.length > 30) {
            showError(firstName,"your First Name is not valid")
        } else if (inputLastName.isEmpty() || inputLastName.length>30){
            showError(lastName,"your Last Name is not valid")
        } else if (inputEmail.isEmpty() || !inputEmail.contains("@")){
            showError(email,"Enter a valid email")
        }else if (inputPass.isEmpty() || inputPass.length < 7){
            showError(password,"Password must be 7 character")
        }else if (inputConformPass.isEmpty() || inputConformPass != inputPass){
            showError(conformPassword ,"password not Match")
        } else if (inputStreetNumber.isEmpty()){
            showError(streetNumber,"Please enter a street number")
        } else if (inputStreetName.isEmpty()){
            showError(streetName,"Please enter a street name")
        } else if (inputPhone.isEmpty()){
            showError(phone,"Please enter your phone")
        } else if (inputCity.isEmpty()){
            showError(city,"Please enter your city")
        }else {
            //Add the user to the authentication list000
            mAuth.createUserWithEmailAndPassword(inputEmail, inputPass)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if(task.isSuccessful){
                        Toast.makeText(
                            this, "Your are successfully registered ", Toast.LENGTH_LONG
                        ).show()

                        //add user to the realtime database
                        var currentUserId = mAuth.currentUser?.uid.toString()
                        val user = User(currentUserId, inputFirstName,inputLastName,inputEmail,inputStreetNumber, inputStreetName, inputCity, inputPhone, true)
                        database?.child(currentUserId)?.setValue(user)


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
        input.error = s
        input.requestFocus()
    }
}