package intech.co.starbug.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import intech.co.starbug.R

class ConfirmDialog: DialogFragment() {


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            // Verify that the host activity implements the callback interface
//            // Instantiate the NoticeDialogListener so we can send events to the host
//            listener = context as ConfirmListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException((context.toString() +
//                    " must implement dialog Listener"))
//        }
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =  AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val message =  arguments?.getString("message")
            val commentId = arguments?.getString("comment_id")
            val viewDialog = inflater.inflate(R.layout.confirm_dialog, null)
            builder
                .setView(viewDialog)
            viewDialog.findViewById<TextView>(R.id.confirm_dialog_text).text = message
            viewDialog.findViewById<Button>(R.id.yes_btn).setOnClickListener {
                if(commentId == null)
                {
                    this.dismiss()
                    return@setOnClickListener
                }
                deleteComment(commentId)
            }
            viewDialog.findViewById<Button>(R.id.no_btn).setOnClickListener {
                this.dismiss()
            }



            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    private fun deleteComment(commentId: String) {
        // Delete comment from database
        val db = FirebaseDatabase.getInstance().getReference("Comment")
        db.child(commentId).removeValue().addOnCompleteListener {
            this.dismiss()
        }


    }

}