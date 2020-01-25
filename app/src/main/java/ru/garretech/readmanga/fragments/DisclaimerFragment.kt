package ru.garretech.readmanga.fragments


import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import ru.garretech.readmanga.R


class DisclaimerFragment : androidx.fragment.app.DialogFragment() {

    var message: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_disclaimer, container, false)
        val disclaimerMessage = view.findViewById<TextView>(R.id.disclaimerMessage)
        val button = view.findViewById<Button>(R.id.dissmissDisclaimerButton)

        if (message != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                disclaimerMessage.text = Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
            } else
                disclaimerMessage.text = Html.fromHtml(message)
        }

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        button.setOnClickListener { dismiss() }
        return view
    }

    companion object {


        @JvmStatic
        fun newInstance(message: String) = DisclaimerFragment().also {
            it.message = message
        }
    }
}
