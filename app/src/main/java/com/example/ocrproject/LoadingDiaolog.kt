import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.example.ocrproject.R

class dialog  // constructor of dialog class
// with parameter activity
internal constructor(
    private val activity: Activity?
) {
    private var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    fun startLoadingdialog() {
        activity ?: return
        // adding ALERT Dialog builder object and passing activity as parameter
        val builder = AlertDialog.Builder(activity)

        // layoutinflater object and use activity to get layout inflater
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading, null))
        builder.setCancelable(true)
        dialog = builder.create()
        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.requestFeature(Window.FEATURE_NO_TITLE)
        }
        dialog?.show()

    }

    // dismiss method
    fun dismissdialog() {
        dialog?.dismiss()
    }
}