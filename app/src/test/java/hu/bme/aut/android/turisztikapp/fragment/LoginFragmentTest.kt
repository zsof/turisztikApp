package hu.bme.aut.android.turisztikapp.fragment

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.turisztikapp.interactor.FirebaseInteractor
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.lang.Exception
import java.util.concurrent.Executor

interface LoginListener {
    fun loginSuccess(email: String?, password: String?)
    fun loginFailure(exception: Exception?, email: String?, password: String?)
}

class LoginFragmentTest : LoginListener {
    private lateinit var successTask: Task<AuthResult>
    private lateinit var failureTask: Task<AuthResult>
    private var loginResult = UNDEF
    private val firebaseInteractor = FirebaseInteractor()

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth


    companion object {
        private const val SUCCESS = 1
        private const val FAILURE = -1
        private const val UNDEF = 0
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        object : Task<AuthResult>() {
            override fun isComplete(): Boolean = true

            override fun isSuccessful(): Boolean = true

            override fun addOnCompleteListener(
                executor: Executor,
                onCompleteListener: OnCompleteListener<AuthResult>
            ): Task<AuthResult> {
                onCompleteListener.onComplete(successTask)
                return successTask
            }

            override fun isCanceled(): Boolean {
                TODO("Not yet implemented")
            }

            override fun getResult(): AuthResult {
                TODO("Not yet implemented")
            }

            override fun <X : Throwable?> getResult(p0: Class<X>): AuthResult {
                TODO("Not yet implemented")
            }

            override fun getException(): Exception? {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(p0: OnSuccessListener<in AuthResult>): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(p0: OnFailureListener): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

        }.also { successTask = it }

        failureTask = object : Task<AuthResult>() {
            override fun isComplete(): Boolean = true

            override fun isSuccessful(): Boolean = false

            // ...
            override fun addOnCompleteListener(
                executor: Executor,
                onCompleteListener: OnCompleteListener<AuthResult>
            ): Task<AuthResult> {
                onCompleteListener.onComplete(failureTask)
                return failureTask
            }

            override fun isCanceled(): Boolean {
                TODO("Not yet implemented")
            }

            override fun getResult(): AuthResult {
                TODO("Not yet implemented")
            }

            override fun <X : Throwable?> getResult(p0: Class<X>): AuthResult {
                TODO("Not yet implemented")
            }

            override fun getException(): Exception? {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(p0: OnSuccessListener<in AuthResult>): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(p0: OnFailureListener): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

        }
    }

    @Test
    fun logInSuccess_test() {
        val email = "test@test.com"
        val password = "123456"
        Mockito.`when`(firebaseAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(successTask)
        firebaseInteractor.login(email, password) { _, _ -> }
        assert(loginResult == SUCCESS)
    }

    /* @Test
     fun logInFailure_test() {
         val email = "cool@cool.com"
         val password = "123_456"
         Mockito.`when`(firebaseAuth.signInWithEmailAndPassword(email, password))
             .thenReturn(failureTask)
         accountModel!!.logIn(email, password)
         assert(loginResult == FAILURE)
     }*/

    override fun loginSuccess(email: String?, password: String?) {
        loginResult = SUCCESS
    }

    override fun loginFailure(exception: Exception?, email: String?, password: String?) {
        loginResult = FAILURE
    }

}





