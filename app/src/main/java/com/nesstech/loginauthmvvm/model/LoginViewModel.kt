package com.nesstech.loginauthmvvm.model

import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import com.nesstech.loginauthmvvm.App
import com.nesstech.loginauthmvvm.liveobserver.LiveMessageEvent
import com.nesstech.loginauthmvvm.interfaces.LoginActivityCommand
import com.nesstech.loginauthmvvm.repository.LoginRepository
import com.nesstech.loginauthmvvm.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

const val INPUT_EMAIL = "EMAIL"
const val INPUT_PASS = "PASS"
const val G_SIGN_IN = 100


class LoginViewModel(private val mRepository: LoginRepository) : ViewModel() {

    /**The disposable object is basically a return object from the RxJava 2.0 that tracks the fetching activity.
     *  In the case of your Activity has been destroyed, we could dispose this object, so that your fetching event would stop,
     *  if it has not completed. This would prevent unintended crashes,
     *  in the event of your Activity has been destroyed before your fetching result return.
     */
    private var disposable: Disposable? = null
    private var navigationEvent: LiveMessageEvent<LoginActivityCommand> = LiveMessageEvent()
    var progress: ObservableInt = ObservableInt(View.GONE)
    var rememberme: ObservableBoolean = ObservableBoolean(false)
    var username: ObservableField<String> = ObservableField("myloginmvvm@gmail.com")
    var password: ObservableField<String> = ObservableField("123456789")

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }


    fun getNavigationEvent(): LiveMessageEvent<LoginActivityCommand> {
      return navigationEvent
    }

    fun onEmailTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (text.isNotEmpty()) {
            if (isValidEmail(text) /*|| isNotValidMobile(text)*/) {
                setError(false, INPUT_EMAIL)
            } else {
                setError(true, INPUT_EMAIL)
            }
        }
    }

    private fun setError(isError: Boolean, inputType: String) {
        navigationEvent.sendEvent { setError(isError, inputType) }
    }


    fun onPasswordTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (text.isNotEmpty()) {
            if (isPassValid(text)) {
                setError(false, INPUT_PASS)
            } else {
                setError(true, INPUT_PASS)
            }
        }
    }

    private fun isValidEmail(email: CharSequence): Boolean {
        /*Min a@b.cc*/
        var isValid = false
        if (email.isEmpty()) {
            navigationEvent.sendEvent { showToast("Enter an email!") }
        } else if (Utils.isEmailValid(email)) {
            isValid = true
        }
        return isValid
    }

    private fun isPassValid(password: CharSequence): Boolean {
        var isValid = false
        if (password.isEmpty()) {
            navigationEvent.sendEvent { showToast("Enter a password!") }
        } else if (password.length > 7) {
            isValid = true
        }
        return isValid
    }


    private fun notifyProgressVisibility(visibility: Int) {
        navigationEvent.sendEvent { pgVisibility(visibility) }
    }

    private fun sendLoginData(data: AuthResponse) {
        navigationEvent.sendEvent { loginData(data) }
    }

    fun remember() {
        if (rememberme.get()) {
            navigationEvent.sendEvent { setRememberMe(false) }
        } else {
            navigationEvent.sendEvent { setRememberMe(true) }
        }
    }

    fun updateRemember(isChecked: Boolean) {
        rememberme.set(isChecked)
    }

    fun login() {
        notifyProgressVisibility(View.VISIBLE)
        if (App.getInstance().isConnected()) {
            if (isValidEmail(username.get()!!) && isPassValid(password.get()!!)) {
                disposable?.dispose()//dispose  pre call
                disposable = mRepository.login(username.get()!!, password.get()!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        sendLoginData(result)
                        mRepository.setLoggedInUser(result)
                        notifyProgressVisibility(View.GONE)
                    }, { error ->
                        notifyProgressVisibility(View.GONE)
                        navigationEvent.sendEvent { showToast(error.message!!) }
                    })
            } else {
                notifyProgressVisibility(View.GONE)
            }
        } else {
            notifyProgressVisibility(View.GONE)
            navigationEvent.sendEvent { showToast("Something went wrong internet or data!") }
        }
    }


    fun forget() {
        //TODO for forget password
        navigationEvent.sendEvent { showToast("Forget Password click") }
    }

    fun fbLogin() {
        navigationEvent.sendEvent { loginFB() }
    }

    fun googleLogin() {
        navigationEvent.sendEvent { loginGoogle() }
    }

    fun signUp() {
        //TODO for sign up form
        navigationEvent.sendEvent { showToast("SignUp  click") }
    }

}



