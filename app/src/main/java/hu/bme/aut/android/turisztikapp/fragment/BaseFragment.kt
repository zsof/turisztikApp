package hu.bme.aut.android.turisztikapp.fragment

import android.app.ProgressDialog
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class BaseFragment : Fragment() {
    private var progressDialog: ProgressDialog? = null

    private val firebaseUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    protected val uid: String?
        get() = firebaseUser?.uid

    protected val userName: String?
        get() = firebaseUser?.displayName

    protected val userEmail: String?
        get() = firebaseUser?.email

    fun showProgressDialog() {
        if (progressDialog != null) {
            return
        }

        progressDialog = ProgressDialog(context).apply {
            setCancelable(false)
            setMessage("Loading...")
            show()
        }
    }

    protected fun hideProgressDialog() {
        progressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        progressDialog = null
    }

     protected fun toast(message: String?) {
         Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
     }

    open fun onBackPressed() = true
}