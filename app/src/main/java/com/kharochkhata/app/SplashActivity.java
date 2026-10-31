package com.kharochkhata.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Screen 1: Splash.
 *
 * Shows the logo for a moment, then decides where to go:
 * - user already logged in  -> Home (this is the "stay logged in" feature,
 *   Firebase keeps the login on the phone until the user logs out)
 * - nobody logged in        -> Login
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable goNext = () -> {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            AppUtils.goToHome(this);
        } else {
            AppUtils.goToLogin(this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goNext, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If the screen closes early, cancel the timer so nothing runs on a dead screen.
        handler.removeCallbacks(goNext);
    }
}
