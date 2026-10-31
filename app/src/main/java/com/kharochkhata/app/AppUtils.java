package com.kharochkhata.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Patterns;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper methods and constants used by more than one screen.
 * Keeping them here avoids copy-pasting the same code in every Activity.
 */
public final class AppUtils {

    /** Key used with putExtra()/getStringExtra() to pass an expense id between screens. */
    public static final String EXTRA_EXPENSE_ID = "expense_id";

    /** SharedPreferences file and keys for the "Remember me" setting. */
    public static final String PREFS_NAME = "kharoch_prefs";
    public static final String PREF_REMEMBER = "remember_me";
    public static final String PREF_EMAIL = "saved_email";

    private AppUtils() {
        // Utility class, no objects needed.
    }

    /**
     * Database location of the logged-in user's expenses:
     * users / {uid} / expenses
     * Returns null when nobody is logged in.
     */
    public static DatabaseReference expensesRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("expenses");
    }

    /** Example result: ৳1,250.00 */
    public static String formatMoney(Context context, double amount) {
        return context.getString(R.string.currency_symbol)
                + String.format(Locale.getDefault(), "%,.2f", amount);
    }

    /** Example result: 25 Oct 2026 */
    public static String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /** Opens the Login screen and removes all other screens from the back stack. */
    public static void goToLogin(Activity from) {
        Intent intent = new Intent(from, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }

    /** Opens the Home screen and removes all other screens from the back stack. */
    public static void goToHome(Activity from) {
        Intent intent = new Intent(from, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }
}
