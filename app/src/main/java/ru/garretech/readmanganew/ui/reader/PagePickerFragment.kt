package ru.garretech.readmanganew.ui.reader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shawnlin.numberpicker.NumberPicker
import ru.garretech.readmanganew.R


class PagePickerFragment : BottomSheetDialogFragment() {
    private var currentPage: Int? = null
    private var maxPages: Int? = null
    private var listener: OnNumberPickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPage = it.getInt(ARG_PARAM1)
            maxPages = it.getInt(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_page_picker, container, false)
        //view.setBackgroundResource(android.R.color.transparent)
        //val back = activity!!.window.decorView.background
        //back.setAlpha(180); // если нужно менять прозрачность
        //view.alpha = 0f

        val pagePicker = view.findViewById<NumberPicker>(R.id.pagePicker)

        pagePicker.minValue = 1
        pagePicker.maxValue = maxPages ?: 99
        pagePicker.value = currentPage ?: 1
        pagePicker.wrapSelectorWheel = false

        pagePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            listener?.onNumberPicked(newVal)
        }

        /*pagePicker.setOnClickListener {
            listener?.onNumberPicked(currentPage ?: 1)
            dismiss()
        }*/


        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNumberPickedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnNumberPickedListener {
        // TODO: Update argument type and name
        fun onNumberPicked(pageIndex: Int)
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "currentPage"
        private const val ARG_PARAM2 = "maxPages"


        @JvmStatic
        fun newInstance(currentPage: Int, maxPages: Int) =
            PagePickerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, currentPage)
                    putInt(ARG_PARAM2, maxPages)
                }
            }
    }
}
