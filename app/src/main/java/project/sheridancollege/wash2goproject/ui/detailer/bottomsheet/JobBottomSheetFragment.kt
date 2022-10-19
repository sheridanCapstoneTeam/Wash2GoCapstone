package project.sheridancollege.wash2goproject.ui.detailer.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import project.sheridancollege.wash2goproject.R
import project.sheridancollege.wash2goproject.common.AppEnum
import project.sheridancollege.wash2goproject.common.Order
import project.sheridancollege.wash2goproject.databinding.LayoutJobBottomSheetBinding
import project.sheridancollege.wash2goproject.ui.detailer.adapter.OrderAdapter
import project.sheridancollege.wash2goproject.ui.detailer.ui.home.BottomSheetClickListener

class JobBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        val TAG: String = JobBottomSheetFragment::class.java.simpleName

        fun newInstance(
            jobList: ArrayList<Order>,
            jobType: String,
            bottomSheetClickListener: BottomSheetClickListener
        ): JobBottomSheetFragment {
            return JobBottomSheetFragment().apply {
                this.bottomSheetClickListener = bottomSheetClickListener
                this.arguments = Bundle().apply {
                    putString("jobType", jobType)
                    putSerializable("jobList", jobList)
                }
            }
        }

    }

    private var _binding: LayoutJobBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var jobType: String
    private lateinit var jobList: ArrayList<Order>
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var bottomSheetClickListener: BottomSheetClickListener

    private var itemClickListener = object : ItemClickListener {
        override fun OrderViewBtnClick(order: Order) {
            bottomSheetClickListener.OrderViewBtnClick(order)
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetShowStyle)

        arguments.let {
            jobType = it?.getString("jobType")!!
            jobList = (it?.getSerializable("jobList") as ArrayList<Order>?)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutJobBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (jobList.isNullOrEmpty()) {
            binding.notFoundGroup.visibility = View.VISIBLE
            binding.jobRv.visibility = View.GONE

            when (jobType) {
                AppEnum.ACTIVE.toString() -> {
                    binding.notFoundTv.text = "No Active Jobs"
                }
                AppEnum.NEW.toString() -> {
                    binding.notFoundTv.text = "No New Jobs"
                }
                AppEnum.COMPLETED.toString() -> {
                    binding.notFoundTv.text = "No Completed Jobs"
                }
                AppEnum.DECLINED.toString() -> {
                    binding.notFoundTv.text = "No Declined Jobs"
                }
            }

        } else {
            binding.notFoundGroup.visibility = View.GONE
            binding.jobRv.visibility = View.VISIBLE

            orderAdapter = OrderAdapter(jobList, itemClickListener)
            binding.jobRv.adapter = orderAdapter


        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismiss()
    }

}

interface ItemClickListener {
    fun OrderViewBtnClick(order: Order)
}