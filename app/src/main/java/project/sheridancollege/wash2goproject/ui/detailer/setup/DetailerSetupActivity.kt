package project.sheridancollege.wash2goproject.ui.detailer.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.ActivityDetailerSetupBinding
import project.sheridancollege.wash2goproject.ui.authentication.MainActivity
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils

class DetailerSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailerSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailerSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.logoutBtn.setOnClickListener(View.OnClickListener {
            SharedPreferenceUtils.saveUserDetails(User())
            startActivity(Intent(this@DetailerSetupActivity, MainActivity::class.java))
            finish()
        })
    }

    override fun onBackPressed() {
        findNavController(R.id.setupNavHostFragment).currentDestination?.let {
            if (it.id == R.id.cleaningSuppliesFragment || it.id == R.id.cleaningKitReceivedFragment) {
                //Do nothing
                finish()
            } else
                super.onBackPressed()
        }
    }

    fun showBackBtn() {
        binding.backBtn.visibility = View.VISIBLE
    }

    fun hideBackBtn() {
        binding.backBtn.visibility = View.INVISIBLE
    }

    fun setToolbarProgress(progress: Int) {
        binding.toolbarProgress.progress = progress
    }

    fun setTitle(title: String) {
        binding.setupTitleTv.text = title
    }

    fun setBody(body: String) {
        binding.setupBodyTv.text = body
    }
    fun hideBody(){
        binding.setupBodyTv.visibility= View.GONE
    }
}