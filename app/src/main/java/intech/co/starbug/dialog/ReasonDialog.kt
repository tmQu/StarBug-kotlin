package intech.co.starbug.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.R

class ReasonDialog: DialogFragment() {
    internal lateinit var listener: ConfirmListener

    internal interface ConfirmListener {
        fun onYesButton(reason: String)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Verify that the host activity implements the callback interface
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ConfirmListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement dialog Listener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =  AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;

            val viewDialog = inflater.inflate(R.layout.cancel_dialog, null)
            builder
                .setView(viewDialog)
            val reason = viewDialog.findViewById<TextInputLayout>(R.id.reason_input_layout)
            viewDialog.findViewById<Button>(R.id.yes_btn).setOnClickListener {
                if(reason.editText?.text.toString().isNotEmpty())
                {
                    listener.onYesButton(reason.editText?.text.toString())
                    this.dismiss()
                }
                else
                {
                    reason.error = "Please enter reason"
                }

            }
            viewDialog.findViewById<Button>(R.id.no_btn).setOnClickListener {
                this.dismiss()
            }



            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }
}