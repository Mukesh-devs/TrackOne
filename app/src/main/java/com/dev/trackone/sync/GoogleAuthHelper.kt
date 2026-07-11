package com.dev.trackone.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GoogleAuthHelper(private val context: Context) {

    private val signInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
    }

    private val signInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, signInOptions)
    }

    /**
     * Gets the currently signed-in Google account.
     * @return GoogleSignInAccount if signed in and granted scopes, null otherwise.
     */
    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))) {
            account
        } else {
            null
        }
    }

    /**
     * Gets the Intent to start the Google Sign-In flow.
     */
    fun getSignInIntent(): Intent {
        return signInClient.signInIntent
    }

    /**
     * Signs out the current user.
     */
    fun signOut(onComplete: () -> Unit) {
        signInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }
}
