package project.sheridancollege.wash2goproject.ui.customer.ui.order

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.Config
import project.sheridancollege.wash2goproject.common.DetailerServicesPrice
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.common.User
import project.sheridancollege.wash2goproject.databinding.FragmentScheduleAppointmentBinding
import project.sheridancollege.wash2goproject.util.SharedPreferenceUtils
import java.text.SimpleDateFormat
import java.util.*


class ScheduleAppointmentFragment : Fragment() {

    private lateinit var binding: FragmentScheduleAppointmentBinding
    private lateinit var detailerId: String
    private lateinit var detailerServicesPrice: DetailerServicesPrice
    private var user: User? = null
    private var price: Int = 0
    private lateinit var customerLocation: Location
    private var config: Config? = null
    private var carConditionImages: List<String>? = null

    companion object {
        val TAG: String = ScheduleAppointmentFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            detailerId = it.getString("detailerId") as String
            detailerServicesPrice = Gson().fromJson(
                it.getString("detailerServicePrice"),
                DetailerServicesPrice::class.java
            )
            customerLocation = it.getParcelable<Location>("customerLocation")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_schedule_appointment,
                container,
                false
            )


        config = SharedPreferenceUtils.getAppConfig()

        carConditionImages = config?.carConditionImagesList?.split(",")
        user = SharedPreferenceUtils.getUserDetails()

        val serviceArray = detailerServicesPrice.serviceAndPriceList.map {
            it.type
        }.toTypedArray()

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            serviceArray
        )
        binding.dropDownMenu.setAdapter(spinnerAdapter)


        binding.changeDateTimeBtn.setOnClickListener(View.OnClickListener {
            showDateTimePickerDialog()
        })

        (binding.textInputLayout.editText as AutoCompleteTextView).onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                price = detailerServicesPrice.serviceAndPriceList.get(position).price
                Log.e(TAG, "Price : $price")
            }


        binding.seekBar.progress = 1
        binding.carConditionText.text = "Car's Condition(Level ${binding.seekBar.progress})"


        Glide.with(requireContext())
            .load(carConditionImages?.get(0))
            .placeholder(R.drawable.loading_placeholder)
            .into(binding.carConditionImg)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.carConditionText.text = "Car's Condition(Level $p1)"

                Glide.with(requireContext())
                    .load(carConditionImages?.get(p1 - 1))
                    .placeholder(R.drawable.loading_placeholder)
                    .into(binding.carConditionImg)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        binding.nextBtn.setOnClickListener(View.OnClickListener {
            val validateResult = validateFielads()
            if (validateResult.isNotEmpty()) {
                Toast.makeText(requireContext(), validateResult, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }


            //Proceed with order
            val order = Order(
                detailerId = detailerId,
                customerId = user?.userId.toString(),
                orderDateTime = binding.orderDateTimeTv.text.toString(),
                addOnsInclude = binding.addsonCheckBox.isChecked,
                carType = binding.dropDownMenu.text.toString(),
                totalPrice = price + if (binding.addsonCheckBox.isChecked) detailerServicesPrice.addsOnCharges else 0,
                customerLat = customerLocation.latitude,
                customerLong = customerLocation.longitude,
                orderId = "TXN${System.currentTimeMillis()}",
                carCondition = binding.seekBar.progress,
                servicePrice = price,
                addOnsPrice = if (binding.addsonCheckBox.isChecked) detailerServicesPrice.addsOnCharges else 0
            )


            findNavController().navigate(
                R.id.action_go_to_order_review,
                bundleOf("order" to order)
            )


        })
        return binding.root
    }

    private fun showDateTimePickerDialog() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        val datetimePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, year, month, day ->

                TimePickerDialog(
                    requireContext(),
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        val pickedDateTime = Calendar.getInstance()
                        pickedDateTime.set(year, month, day, hour, minute)

                        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
                        val currentDate = sdf.format(pickedDateTime.time)
                        Log.e(TAG, "DateTime : $currentDate")
                        binding.orderDateTimeTv.text = currentDate

                    },
                    startHour,
                    startMinute,
                    false
                ).show()
            },
            startYear,
            startMonth,
            startDay
        )
        datetimePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datetimePickerDialog.show()
    }


    private fun validateFielads(): String {
        var result = ""
        if ((binding.orderDateTimeTv.text == "Set Date and Time") &&
            (binding.dropDownMenu.text.toString() == "Select Vehicle Type")
        ) {
            result = "Please fill order details first"
            return result
        }

        if (binding.orderDateTimeTv.text == "Set Date and Time") {
            result = "Please set appointment date and time"
            return result
        }

        if (binding.dropDownMenu.text.toString() == "Select Vehicle Type") {
            result = "Please select vehicle type"
            return result
        }
        return result
    }

}
