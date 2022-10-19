package project.sheridancollege.wash2goproject.ui.detailer

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.databinding.ActivityDetailerBinding
import project.sheridancollege.wash2goproject.ui.authentication.MainActivity
import project.sheridancollege.wash2goproject.ui.detailer.ui.home.DetailerHomeViewModel
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class DetailerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDetailerBinding
    private var user: User? = null
    private lateinit var progressDialog: ProgressDialog
    companion object {
        val TAG: String = DetailerActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityDetailerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDetailer.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_detailer)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_detailer_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        progressDialog = ProgressDialog(this@DetailerActivity)
        progressDialog.setMessage("Please wait...")

        user = SharedPreferenceUtils.getUserDetails()

        setupNavMenu(navView)

    }


    private fun setupNavMenu(navView: NavigationView) {
        val navViewHeader = navView.getHeaderView(0)
        val userNameNav = navViewHeader.findViewById<TextView>(R.id.userNameNav)
        val userEmailNav = navViewHeader.findViewById<TextView>(R.id.userEmailNav)
        userNameNav.text = user?.firstName + " " + user?.lastName
        userEmailNav.text = user?.email


        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val result = true

            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    Log.e(TAG, "Logout clicked")
                    if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
                        binding.drawerLayout.closeDrawers()
                    }
                    user = SharedPreferenceUtils.getUserDetails()

                    doLogoutWithOffline()

                }

            }

            result
        }
    }

    private fun doLogoutWithOffline() {

        progressDialog.show()

        user?.status = UserStatus.OFFLINE
        user?.fcmToken = "N/A"
        AppClass.databaseReference.child(Constants.USER).child(user?.userId.toString())
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->
                progressDialog.dismiss()
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@DetailerActivity,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }

                doSignOutUser()
            })
    }

    private fun doSignOutUser() {
        AuthUI.getInstance().signOut(this@DetailerActivity)
            .addOnCompleteListener(OnCompleteListener {
                if(!it.isSuccessful){
                    Toast.makeText(
                        this@DetailerActivity,
                        it.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }
                SharedPreferenceUtils.saveUserDetails(User())
                startActivity(Intent(this@DetailerActivity, MainActivity::class.java))
                finish()
            })
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_detailer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawers()
            return
        }
        super.onBackPressed()

    }
}