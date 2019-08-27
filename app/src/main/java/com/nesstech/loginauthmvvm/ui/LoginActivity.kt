package com.nesstech.loginauthmvvm.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.nesstech.loginauthmvvm.R
import com.nesstech.loginauthmvvm.databinding.ActivityLoginBinding
import com.nesstech.loginauthmvvm.interfaces.LoginActivityCommand
import com.nesstech.loginauthmvvm.model.*
import com.nesstech.loginauthmvvm.utils.Utils

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity(), LoginActivityCommand,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewmodel: LoginViewModel
    private var callbackManager: CallbackManager? = null
    private var mGoogleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings(savedInstanceState)
    }

    private fun setupBindings(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
            viewmodel = ViewModelProviders.of(this, ViewModelFactory()).get(LoginViewModel::class.java)
            binding.model = viewmodel
            registerObservables()
        }

    }

    /**With this No observer needed in activity*/
    private fun registerObservables() {
        viewmodel.getNavigationEvent().setEventReceiver(this, this)
    }


    override fun setError(isError: Boolean, inputType: String) {
        if (isError) {
            if (inputType == INPUT_EMAIL) setError(binding.etEmail)
            else if (inputType == INPUT_PASS) setError(binding.etPass)
        } else {
            if (inputType == INPUT_EMAIL) removeError(binding.etEmail)
            else if (inputType == INPUT_PASS) removeError(binding.etPass)
        }
    }


    private fun removeError(text: TextView) {
        text.setTextColor(Color.BLACK)
        text.setHintTextColor(Color.BLACK)
    }

    private fun setError(text: TextView) {
        text.setTextColor(Color.RED)
        text.setHintTextColor(Color.RED)
    }

    override fun setRememberMe(isRemember: Boolean) {
        binding.checkRemember.isChecked = isRemember
        viewmodel.updateRemember(isRemember)
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    override fun loginData(data: AuthResponse) {
        try {
            Toast.makeText(this, "${data.message}", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "${data.user.toString()}", Toast.LENGTH_LONG).show()
            //startHomeActivity()
        } catch (e: Exception) {
            Toast.makeText(this, "${data.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun pgVisibility(visibility: Int) {
        binding.progress.visibility = visibility
    }


    override fun loginFB() {
        callbackManager = CallbackManager.Factory.create()//FB callback initiation
        LoginManager.getInstance().logInWithReadPermissions(this, mutableListOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val params = Bundle()
                params.putString("fields", "id,name,email,link,picture.type(large)")
                GraphRequest(AccessToken.getCurrentAccessToken(), "me", params, HttpMethod.GET,
                    GraphRequest.Callback { response ->
                        if (response != null) {
                            try {
                                val data = response.jsonObject
                                /**TODO your own logic*/
                                showToast(data.toString())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }).executeAsync()

            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Cancel", Toast.LENGTH_SHORT).show()
            }

            override fun onError(e: FacebookException) {
                Toast.makeText(this@LoginActivity, e.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun loginGoogle() {
        if (mGoogleApiClient == null || !mGoogleApiClient?.isConnected!!) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this@LoginActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        }

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, G_SIGN_IN)
    }


    override fun onConnectionFailed(result: ConnectionResult) {
        Toast.makeText(this@LoginActivity, "" + result, Toast.LENGTH_SHORT).show()
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.errorCode, this, 0).show()
            return
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == G_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val acct = result.signInAccount
                if (acct != null) {
                    val name: String? = acct.displayName
                    val email: String? = acct.email
                    val id: String? = acct.id
                    val profileUri: Uri? = acct.photoUrl
                    /**TODO your own logic*/
                    showToast(name + "\n" + email + "\n" + id + "\n" + profileUri)
                }
            }
        } else {
            if (callbackManager != null) {
                callbackManager?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppEventsLogger.activateApp(this)//FB event logger activate

        if (mGoogleApiClient != null && !mGoogleApiClient?.isConnected!! && !mGoogleApiClient?.isConnecting!!) {
            mGoogleApiClient?.connect()
        }//Google

        Utils.makeImmersive(window.decorView)//for full screen window
    }

    override fun onPause() {
        super.onPause()
        AppEventsLogger.deactivateApp(this)//FB event logger deactivate

        if (mGoogleApiClient != null && mGoogleApiClient?.isConnected!!) {
            mGoogleApiClient?.stopAutoManage(this)
            mGoogleApiClient?.disconnect()
        }//Google
    }


    /*btnLogout.setOnClickListener(View.OnClickListener {
        // Logout
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, GraphRequest.Callback {
                AccessToken.setCurrentAccessToken(null)
                LoginManager.getInstance().logOut()

                finish()
            }).executeAsync()
        }
    })*/

}