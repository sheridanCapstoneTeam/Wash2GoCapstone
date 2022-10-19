package project.sheridancollege.wash2goproject.ui.customer

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import project.sheridancollege.wash2goproject.AppClass
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.common.UserStatus
import project.sheridancollege.wash2goproject.databinding.ActivityCustomerBinding
import project.sheridancollege.wash2goproject.ui.authentication.MainActivity
import project.sheridancollege.wash2goproject.ui.detailer.DetailerActivity
import project.sheridancollege.wash2goproject.util.Constants
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class CustomerActivity : AppCompatActivity() {

    companion object {
        val TAG: String = CustomerActivity::class.java.simpleName
    }

    private var user: User? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityCustomerBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarCustomer.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_customer)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_customer_home, R.id.nav_customer_permission
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        progressDialog = ProgressDialog(this@CustomerActivity)
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
                R.id.nav_customer_logout -> {
                    Log.e(DetailerActivity.TAG, "Logout clicked")
                    if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
                        binding.drawerLayout.closeDrawers()
                    }
                    doLogoutWithFCMUpdate()
                }

            }

            result
        }
    }


    private fun doLogoutWithFCMUpdate() {

        progressDialog.show()

        user?.fcmToken = "N/A"
        AppClass.databaseReference.child(Constants.USER).child(user?.userId.toString())
            .setValue(user)
            .addOnCompleteListener(OnCompleteListener { task ->
                progressDialog.dismiss()
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@CustomerActivity,
                        task.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }

                doSignOutUser()
            })
    }

    private fun doSignOutUser() {
        AuthUI.getInstance().signOut(this@CustomerActivity)
            .addOnCompleteListener(OnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(
                        this,
                        it.exception?.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnCompleteListener
                }
                SharedPreferenceUtils.saveUserDetails(User())
                startActivity(Intent(this@CustomerActivity, MainActivity::class.java))
                finish()
            })
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawers()
            return
        }
        super.onBackPressed()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_customer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}