package ru.garretech.readmanga.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.garretech.readmanga.R


class ConfirmationFragment : DialogFragment() {
    private var titleText: String? = null
    private var contentText: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            titleText = it.getString(ARG_PARAM1)
            contentText = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirmation, container, false)
        val cancelButton = view.findViewById<Button>(R.id.confirmationCancelButton)
        val acceptButton = view.findViewById<Button>(R.id.confirmationAcceptButton)
        val messageContentText = view.findViewById<TextView>(R.id.messageContent)

        dialog?.setTitle(titleText)
        messageContentText.text = contentText

        acceptButton.setOnClickListener {
            listener?.onAcceptPressed()
            dismiss()
        }

        cancelButton.setOnClickListener {
            listener?.onCancelPressed()
            dismiss()
        }

        return view
    }


    fun setConfirmationListener(listener: OnFragmentInteractionListener) {
        this.listener = listener
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onAcceptPressed()

        fun onCancelPressed()
    }

    companion object {

        private const val ARG_PARAM1 = "title"
        private const val ARG_PARAM2 = "content"

        @JvmStatic
        fun newInstance(title: String, content: String) =
            ConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, title)
                    putString(ARG_PARAM2, content)
                }
            }
    }
}
