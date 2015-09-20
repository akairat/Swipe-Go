package com.team_scream_mit.scream;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import java.io.InputStream;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class SigninActivity extends AppCompatActivity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "Signin";

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 400;

    // Google client to interact with Google API

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    protected ImageView imgProfilePic;
    private LinearLayout llProfileLayout;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();

        // Initializing google plus api client
        app.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        if(app.getUserName(this).length() != 0)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            setContentView(R.layout.activity_signin);

            btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
            btnSignOut = (Button) findViewById(R.id.btn_sign_out);
            btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
            imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
            llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);

            // Button click listeners
            btnSignIn.setOnClickListener(this);
            btnSignOut.setOnClickListener(this);
            btnRevokeAccess.setOnClickListener(this);
        }
    }

    protected void onStart() {
        Log.e(TAG, "Name: " + 2);
        super.onStart();
        app.mGoogleApiClient.connect();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (app.mGoogleApiClient.isConnected()) {
            app.mGoogleApiClient.disconnect();
        }
    }

    protected void onStop() {
        super.onStop();
    }

    /**
     * Button on click listener
     * */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signInWithGplus();

                break;
            case R.id.btn_sign_out:
                signOutFromGplus();

                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!app.mGoogleApiClient.isConnecting()) {
                app.mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (app.mGooglePlusLogoutClicked) {
            signOutFromGplus();
            app.mGooglePlusLogoutClicked = false;
        } else {
            mSignInClicked = false;

            // Get user's information
            getProfileInformation();

            // Update the UI after signin
            updateUI(true);
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(app.mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(app.mGoogleApiClient);

                app.userName = currentPerson.getDisplayName();
                app.setUserName(this, app.userName);
                String personPhotoUrl = currentPerson.getImage().getUrl();
                app.userEmail = currentPerson.getUrl();

                app.addNewUser(app.userName, app.userEmail);

                Log.e(TAG, "Name: " + app.userName + ", plusProfile: " + app.userEmail
                        + ", Image: " + personPhotoUrl);

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionSuspended(int arg0) {
        app.mGoogleApiClient.connect();
        updateUI(false);
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!app.mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                app.mGoogleApiClient.connect();
            }
        }
    }

    public void signOutFromGplus() {
        if (app.mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(app.mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(app.mGoogleApiClient);
            app.mGoogleApiClient.disconnect();
            app.mGoogleApiClient.connect();
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (app.mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(app.mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(app.mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            app.mGoogleApiClient.connect();
                            updateUI(false);
                        }

                    });
        }
    }
}
