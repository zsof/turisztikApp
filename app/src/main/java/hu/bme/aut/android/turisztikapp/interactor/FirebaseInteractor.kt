package hu.bme.aut.android.turisztikapp.interactor

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

open class FirebaseInteractor() {

    companion object {
        const val SUCCESS = "Successful"
        const val FAILURE = "Failure"
    }

    private val firebaseAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val firebaseUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    fun userLoggedIn(): Boolean {
        if (firebaseUser?.email != null) {
            return true
        }
        return false
    }

    fun sendResetPasswordEmail(email: String, fv: (String, String?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    fv(SUCCESS, null)
                } else {
                    fv(FAILURE, it.exception?.localizedMessage)
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
            }
            .addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
            }
    }

    fun login(email: String, password: String, fv: (String, String?) -> Unit) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fv(SUCCESS, null)
            }
            .addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
            }
    }

    fun reAuth(password: String, fv: (String, String?) -> Unit) {
        firebaseUser?.let {
            val credential = EmailAuthProvider.getCredential(it.email!!, password)
            it.reauthenticate(credential)
                .addOnSuccessListener {
                    fv(SUCCESS, null)
                }
                .addOnFailureListener { e ->
                    fv(FAILURE, e.localizedMessage)
                }
        }
    }

    fun sendVerifyEmail(fv: (String, String?) -> Unit) {
        firebaseUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    fv(SUCCESS, null)
                } else {
                    fv(FAILURE, null)
                }
            }
    }

    fun saveProfile(name: String, imageUri: Uri?, fv: (String, String?) -> Unit) {
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
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
            }
    }

    fun updateEmail(email: String, fv: (String, String?) -> Unit) {
        firebaseUser?.updateEmail(email)
            ?.addOnSuccessListener {
                fv(SUCCESS, null)
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
            }
    }

    fun updatePassword(password: String, fv: (String, String?) -> Unit) {
        firebaseUser?.updatePassword(password)
            ?.addOnSuccessListener {
                fv(SUCCESS, null)
            }
            ?.addOnFailureListener { e ->
                fv(FAILURE, e.localizedMessage)
            }
    }
}