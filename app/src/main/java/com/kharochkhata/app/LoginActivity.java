package com.kharochkhata.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Screen 2: Login.
 *
 * - Validates the input (setError, no crash on empty or wrong input)
 * - Logs in with Firebase Authentication (email + password)
 * - "Remember me" saves the email on the phone with SharedPreferences
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private SharedPreferences prefs;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(AppUtils.PREFS_NAME, Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        loadRememberedEmail();

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    /** Reads the saved setting and fills the email box if "Remember me" was ticked before. */
    private void loadRememberedEmail() {
        boolean remember = prefs.getBoolean(AppUtils.PREF_REMEMBER, false);
        cbRemember.setChecked(remember);
        if (remember) {
            etEmail.setText(prefs.getString(AppUtils.PREF_EMAIL, ""));
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        // ---- validation ----
        boolean valid = true;
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!AppUtils.isValidEmail(email)) {
            etEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.error_password_required));
            valid = false;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!valid) {
            return;
        }

        // ---- Firebase login ----
        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    saveRememberMe(email);
                    AppUtils.goToHome(this);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError(getString(R.string.msg_login_failed, e.getMessage()));
                });
    }

    /** Stores the "Remember me" choice (and the email) in SharedPreferences. */
    private void saveRememberMe(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AppUtils.PREF_REMEMBER, cbRemember.isChecked());
        if (cbRemember.isChecked()) {
            editor.putString(AppUtils.PREF_EMAIL, email);
        } else {
            editor.remove(AppUtils.PREF_EMAIL);
        }
        editor.apply();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        View root = findViewById(android.R.id.content);
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }
}
