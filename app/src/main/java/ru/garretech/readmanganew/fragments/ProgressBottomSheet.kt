package ru.garretech.readmanganew.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.garretech.readmanganew.R

class ProgressBottomSheet : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_progress, container, false)
    }


    override fun dismissAllowingStateLoss() {
        if (fragmentManager != null) super.dismissAllowingStateLoss()
    }
}
