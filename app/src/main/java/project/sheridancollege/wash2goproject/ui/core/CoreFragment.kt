package project.sheridancollege.wash2goproject.ui.core

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.databinding.CoreFragmentBinding

class CoreFragment : Fragment() {

    companion object {
        fun newInstance() = CoreFragment()
    }


    val EXTRA_MESSAGELot = "cusLot"
    val EXTRA_MESSAGELng = "cusLng"
    private lateinit var viewModel: CoreViewModel
    private lateinit var binding: CoreFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.core_fragment, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CoreViewModel::class.java)


        binding.btnShowCoor.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.getCoOrdinates(binding.edtAddress.getText().toString().replace(" ", "+"))
        }


        viewModel.coOrdinates.observe(viewLifecycleOwner) { response->
            if(response!=null) {
                onPostExecute(response)
                viewModel.resetResponse();
            }
        }
    }

     private fun onPostExecute(s: String?) {
         binding.progressBar.visibility = View.GONE
        var loc = Location("")
        try {
            val jsonObject = JSONObject(s)
            val lat =
                (jsonObject["results"] as JSONArray).getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location")["lat"].toString()
            val lng =
                (jsonObject["results"] as JSONArray).getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location")["lng"].toString()
            binding.txtCoordinates.setText(String.format("Coordinates : %s / %s ", lat, lng))

            //Gettign the
            loc.latitude = lat.toDouble()
            loc.longitude = lng.toDouble()
            val latloc = loc.latitude
            val lngLoc = loc.longitude
            loc = createNewLocation(latloc, lngLoc)
          /*  findNavController().navigate(CoreFragmentDirections.actionCoreFragmentToMapsFragment(
                loc.latitude.toFloat(), loc.longitude.toFloat()
            ))*/

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