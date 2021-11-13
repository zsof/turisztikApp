package hu.bme.aut.android.turisztikapp.extension

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import hu.bme.aut.android.turisztikapp.R

fun EditText.validateNonEmpty(): Boolean {
    if (text.isEmpty()) {
        error = context.getString(R.string.required)
        return false
    }
    return true
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}