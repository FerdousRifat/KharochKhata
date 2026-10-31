package com.kharochkhata.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Screen 3: Register.
 *
 * Creates a new Firebase account (name, email, password) and goes to Home.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirm;
    private Button btnRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish()); // back to the Login screen
    }

    private void attemptRegister() {
        String name = text(etName);
        String email = text(etEmail);
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String confirm = etConfirm.getText() == null ? "" : etConfirm.getText().toString();

        // ---- validation ----
        boolean valid = true;
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_name_required));
            valid = false;
        }
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
        if (!confirm.equals(password)) {
            etConfirm.setError(getString(R.string.error_confirm_mismatch));
            valid = false;
        }
        if (!valid) {
            return;
        }

        // ---- Firebase register ----
        btnRegister.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        AppUtils.goToHome(this);
                        return;
                    }
                    // Save the display name, then continue whether it worked or not.
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    user.updateProfile(request)
                            .addOnCompleteListener(task -> AppUtils.goToHome(this));
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    View root = findViewById(android.R.id.content);
                    Snackbar.make(root, getString(R.string.msg_register_failed, e.getMessage()),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
