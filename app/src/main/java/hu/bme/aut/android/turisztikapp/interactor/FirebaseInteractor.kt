package hu.bme.aut.android.turisztikapp.interactor

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

open class FirebaseInteractor() {

    companion object {
        const val SUCCESS = "Successful"
        const val FAILURE = "Failure"
        const val TAG = "FirebaseInteractor"
    }

    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val firebaseUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    fun userLoggedIn(): Boolean {
        if (firebaseUser?.email != null) {
            Log.d(TAG, "User already logged in")
            return true
        }
        Log.d(TAG, "No user logged in")
        return false

    }

    fun sendResetPasswordEmail(email: String, fv: (String, String?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    fv(SUCCESS, null)
                    Log.d(TAG, "Email sent to reset password")
                } else {
                    fv(FAILURE, it.exception?.localizedMessage)
                    Log.d(TAG, "Failed to send email")
                }
            }
    }

    fun register(email: String, password: String, fv: (String, String?) -> Unit) {
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fv(SUCCESS, null)
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(firebaseUser?.email?.substringBefore('@'))
                    .build()
                firebaseUser?.updateProfile(profileChangeRequest)
                Log.d(TAG, "User successfully registered")
            }
            .addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
                Log.d(TAG, "Register failed")
            }
    }

    fun login(email: String, password: String, fv: (String, String?) -> Unit) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fv(SUCCESS, null)
                Log.d(TAG, "User successfully logged in")
            }
            .addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
                Log.d(TAG, "Login failed")
            }
    }

    fun reAuth(password: String, fv: (String, String?) -> Unit) {
        firebaseUser?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, password)
            it.reauthenticate(credential)
                .addOnSuccessListener {
                    fv(SUCCESS, null)
                    Log.d(TAG, "The re-authentication was successful")
                }
                .addOnFailureListener { e ->
                    fv(FAILURE, e.localizedMessage)
                    Log.d(TAG, "Failed to re-authenticate")
                }
        }
    }

    fun sendVerifyEmail(fv: (String, String?) -> Unit) {
        firebaseUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    fv(SUCCESS, null)
                    Log.d(TAG, "The email sent to verify")
                } else {
                    fv(FAILURE, null)
                    Log.d(TAG, "Failed to send email")
                }
            }
    }

    fun updateProfile(name: String, imageUri: Uri?, fv: (String, String?) -> Unit) {
        val updates = UserProfileChangeRequest.Builder()
        if (!name.isNullOrEmpty()) {
            updates.displayName = name
        }
        if (imageUri != null) {
            updates.photoUri = imageUri
        }
        firebaseUser?.updateProfile(updates.build())
            ?.addOnSuccessListener {
                fv(SUCCESS, null)
                Log.d(TAG, "User successfully updated the profile")
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
                Log.d(TAG, "Failed to update the profile")
            }
    }

    fun updateEmail(email: String, fv: (String, String?) -> Unit) {
        firebaseUser?.updateEmail(email)
            ?.addOnSuccessListener {
                fv(SUCCESS, null)
                Log.d(TAG, "User successfully updated the email")
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
                Log.d(TAG, "Failed to update the email")
            }
    }

    fun updatePassword(password: String, fv: (String, String?) -> Unit) {
        firebaseUser?.updatePassword(password)
            ?.addOnSuccessListener {
                fv(SUCCESS, null)
                Log.d(TAG, "User successfully updated the password")
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
                Log.d(TAG, "Failed to update the password")
            }
    }
}