package com.example.cameraxlibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import com.example.cameraxlibrary.databinding.DialogueNumberSelectBinding


class DialogueInfo(
    private val it: CallBack
) : AppCompatDialogFragment() {

    var dataBinding: DialogueNumberSelectBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialogue_number_select, container, false)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dataBinding?.txtTitle?.text = "Enter Photo Number"
        return dataBinding?.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog);

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataBinding?.btnSubmit?.setOnClickListener {
            val notes = dataBinding?.edtNumber?.text.toString()
            if (notes.isBlank()) {
                Toast.makeText(requireContext(), "Enter a value", Toast.LENGTH_SHORT).show()
            } else {
                this.it.setData(notes)
                dismiss()
            }

        }
        dataBinding?.btnCancel?.setOnClickListener {
            dismiss()
        }

    }

    interface CallBack {
        fun setData(it: String)
    }
}
