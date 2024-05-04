package intech.co.starbug.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import intech.co.starbug.R

class LoadingDialog(val activity: Activity){
    private var dialog: AlertDialog = AlertDialog.Builder(activity).create()

    fun startLoadingDialog()
    {
        val dialogBuilder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        dialogBuilder.setView(inflater.inflate(R.layout.loading_dialog, null))
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()

    }

    fun dismissDialog()
    {
        if(dialog.isShowing)
            dialog.dismiss()
    }

}