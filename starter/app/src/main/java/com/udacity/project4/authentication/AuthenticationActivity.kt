package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.RemindersViewModel
import org.koin.android.ext.android.bind

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel: AuthenticationViewModel by lazy {
        AuthenticationViewModel(application)
    }

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        binding.authButton.setOnClickListener {
            launchSignInFlow()
        }

        // TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        // TODO: If the user was authenticated, send him to RemindersActivity
        // TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    println("do something")
                }
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    Intent(this, RemindersActivity::class.java).apply {
                        startActivity(this)
                    }
                }
                else -> {

                }
            }

        })




    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.drawable.map)
                .setAvailableProviders(providers).build(), SIGN_IN_RESULT_CODE
        )
    }
}
