package com.kharochkhata.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Screen 4: Home.
 *
 * - Shows all expenses in a RecyclerView. The list updates in real time because a
 *   ValueEventListener is attached to the Firebase Realtime Database.
 * - FAB opens the Add screen. Tap a row to view it. The row's trash button deletes it
 *   after an AlertDialog confirmation.
 * - Accelerometer sensor: shake the phone to open the Add screen.
 * - Menu: Log out, and Help (implicit Intent that opens a web link).
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // A shake is a total acceleration above 2.7 times gravity, at most once per second.
    private static final float SHAKE_THRESHOLD_G = 2.7f;
    private static final long SHAKE_GAP_MS = 1000;

    private final List<Expense> expenses = new ArrayList<>();
    private ExpenseAdapter adapter;

    private TextView tvTotal;
    private TextView tvEmpty;
    private View rootView;

    private DatabaseReference expensesRef;
    private ValueEventListener expensesListener;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        expensesRef = AppUtils.expensesRef();
        if (expensesRef == null) {            // nobody is logged in
            AppUtils.goToLogin(this);
            return;
        }

        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.mainRoot);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        tvGreeting.setText(getString(R.string.msg_welcome, displayName()));

        // RecyclerView = layout manager + adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expenses, new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                // Pass the id to the detail screen with putExtra()
                Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
                intent.putExtra(AppUtils.EXTRA_EXPENSE_ID, expense.getId());
                startActivity(intent);
            }

            @Override
            public void onExpenseDelete(Expense expense) {
                confirmDelete(expense);
            }
        });
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> openAddScreen());

        // Sensor setup (the accelerometer may be missing on some devices)
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    // ---------------------------------------------------------------------------------
    // Real-time data: start listening when the screen is visible, stop when it is not.
    // ---------------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        if (expensesRef == null) {
            return;
        }
        expensesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenses.clear();
                double total = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Expense expense = child.getValue(Expense.class);
                    if (expense != null) {
                        expense.setId(child.getKey());
                        expenses.add(expense);
                        total += expense.getAmount();
                    }
                }
                // Newest expense first
                Collections.sort(expenses, (a, b) -> Long.compare(b.getDateMillis(), a.getDateMillis()));
                adapter.notifyDataSetChanged();

                tvTotal.setText(AppUtils.formatMoney(MainActivity.this, total));
                tvEmpty.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(rootView, getString(R.string.msg_load_failed, error.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        };
        expensesRef.addValueEventListener(expensesListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (expensesRef != null && expensesListener != null) {
            expensesRef.removeEventListener(expensesListener);
            expensesListener = null;
        }
    }

    // ---------------------------------------------------------------------------------
    // Sensor: shake to add
    // ---------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Convert the three axis values (m/s²) to "g-force" (1 g = normal gravity).
        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_G) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > SHAKE_GAP_MS) {
                lastShakeTime = now;
                openAddScreen();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for shake detection.
    }

    // ---------------------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------------------

    private void openAddScreen() {
        startActivity(new Intent(this, AddEditExpenseActivity.class));
    }

    /** AlertDialog asking "are you sure?" before deleting. */
    private void confirmDelete(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, expense.getTitle()))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> deleteExpense(expense))
                .show();
    }

    private void deleteExpense(Expense expense) {
        expensesRef.child(expense.getId()).removeValue()
                .addOnSuccessListener(unused ->
                        Snackbar.make(rootView, R.string.msg_deleted, Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Snackbar.make(rootView, getString(R.string.msg_delete_failed, e.getMessage()),
                                Snackbar.LENGTH_LONG).show());
    }

    private String displayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return "";
        }
        String name = user.getDisplayName();
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return user.getEmail() == null ? "" : user.getEmail();
    }

    // ---------------------------------------------------------------------------------
    // Toolbar menu
    // ---------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        } else if (id == R.id.action_help) {
            openHelpLink();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    AppUtils.goToLogin(this);
                })
                .show();
    }

    /** Implicit Intent: ACTION_VIEW lets the phone's browser open the link. */
    private void openHelpLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_url)));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(rootView, R.string.msg_no_app, Snackbar.LENGTH_SHORT).show();
        }
    }
}
