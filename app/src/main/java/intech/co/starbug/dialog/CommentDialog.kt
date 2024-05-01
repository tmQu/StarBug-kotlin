package intech.co.starbug.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import intech.co.starbug.R


class CommentDialog: DialogFragment() {
    internal lateinit var listener: DialogListener

    private lateinit var commentBox: TextInputLayout
    interface DialogListener {
        fun onSendCommentClick(commentIndex: Int, comment: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            // Verify that the host activity implements the callback interface
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogListener
        } catch (e: ClassCastException) {

            throw ClassCastException((context.toString() +
                    " must implement Dialog Listener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =  AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val index =  arguments?.getInt("comment_index")
            val comment = arguments?.getString("reply")
            if(index == null)
            {
                Log.e("CommentDialog", "Index is null")
                this.dismiss()
            }
            val viewDialog = inflater.inflate(R.layout.comment_dialog, null)
            builder
                .setView(viewDialog)

            val commentBox = viewDialog.findViewById<TextInputLayout>(R.id.comment_box)

            commentBox.editText?.setText(comment)
            viewDialog.findViewById<Button>(R.id.send_now).setOnClickListener {
                if(commentBox.editText?.text.toString().isEmpty())
                {
                    commentBox.error = "Comment can't be empty"
                    return@setOnClickListener
                }
                if(index != null)
                {
                    listener.onSendCommentClick(index, commentBox.editText?.text.toString())
                }
                this.dismiss()
            }



            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }
}

