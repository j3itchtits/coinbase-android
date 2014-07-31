package com.coinbase.android;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.coinbase.android.pin.PINManager;
import com.coinbase.android.pin.PINPromptActivity;
import com.coinbase.api.LoginManager;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

public class CoinbaseActivity extends RoboSherlockFragmentActivity {

  /** This activity requires authentication */
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface RequiresAuthentication { }

  /** This activity requires PIN entry (if PIN is enabled) */
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface RequiresPIN { }

  @Inject
  protected LoginManager mLoginManager;

  @Inject
  protected PINManager mPinManager;

  @Override
  public void onResume() {

    super.onResume();

    if(getClass().isAnnotationPresent(RequiresAuthentication.class)) {
      // Check authentication status
      if(!mLoginManager.isSignedIn(this)) {

        // Not signed in.
        // First check if there are any accounts available to sign in to:
        boolean success = mLoginManager.switchActiveAccount(this, 0);

        if(!success) {
          // Not signed in - open login activity.
          Intent intent = new Intent(this, LoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);

          finish();
        } else {
          // Now signed in, continue with Activity initialization
        }
      }
    }

    if(getClass().isAnnotationPresent(RequiresPIN.class)) {
      // Check PIN status
      if(!mPinManager.shouldGrantAccess(this)) {
        // Check if user wants to quit PIN lock
        if(mPinManager.isQuitPINLock()){
          mPinManager.setQuitPINLock(false);
          finish();
        } else {
          // PIN reprompt required.
          Intent intent = new Intent(this, PINPromptActivity.class);
          intent.setAction(PINPromptActivity.ACTION_PROMPT);
          startActivity(intent);
        }
      }
    }
  }
}
