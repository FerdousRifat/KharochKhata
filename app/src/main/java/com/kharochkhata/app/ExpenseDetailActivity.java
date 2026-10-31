package com.kharochkhata.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Screen 6: Expense details (the "View" of CRUD).
 *
 * - Shows one expense, and updates by itself if the expense is edited.
 * - Edit button  -> opens the Add/Edit screen in edit mode (putExtra with the id)
 * - Delete button-> AlertDialog, then removes the record
 * - Share button -> implicit Intent (ACTION_SEND): any app that handles text can receive it
 */
public class ExpenseDetailActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvAmount;
    private TextView tvCategory;
    private TextView tvDate;
    private TextView tvNote;
    private ImageView ivReceipt;
    private View rootView;

    private String expenseId;
    private Expense currentExpense;
    private DatabaseReference expenseRef;
    private ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        rootView = findViewById(R.id.detailRoot);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAmount = findViewById(R.id.tvDetailAmount);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvDate = findViewById(R.id.tvDetailDate);
        tvNote = findViewById(R.id.tvDetailNote);
        ivReceipt = findViewById(R.id.ivDetailReceipt);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnDelete = findViewById(R.id.btnDelete);

        // Read the id that the Home screen sent with putExtra()
        expenseId = getIntent().getStringExtra(AppUtils.EXTRA_EXPENSE_ID);
        DatabaseReference allExpenses = AppUtils.expensesRef();
        if (expenseId == null || allExpenses == null) {
            finish();
            return;
        }
        expenseRef = allExpenses.child(expenseId);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditExpenseActivity.class);
            intent.putExtra(AppUtils.EXTRA_EXPENSE_ID, expenseId);
            startActivity(intent);
        });
        btnShare.setOnClickListener(v -> shareExpense());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (expenseRef == null) {
            return;
        }
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Expense expense = snapshot.getValue(Expense.class);
                if (expense == null) {      // deleted
                    finish();
                    return;
                }
                currentExpense = expense;
                showExpense(expense);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(rootView, getString(R.string.msg_load_failed, error.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        };
        expenseRef.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (expenseRef != null && listener != null) {
            expenseRef.removeEventListener(listener);
            listener = null;
        }
    }

    private void showExpense(Expense expense) {
        tvTitle.setText(expense.getTitle());
        tvAmount.setText(AppUtils.formatMoney(this, expense.getAmount()));
        tvCategory.setText(expense.getCategory());
        tvDate.setText(AppUtils.formatDate(expense.getDateMillis()));

        String note = expense.getNote();
        tvNote.setText(note == null || note.isEmpty() ? getString(R.string.no_note) : note);

        Bitmap photo = ImageUtils.fromBase64(expense.getPhoto());
        if (photo != null) {
            ivReceipt.setImageBitmap(photo);
            ivReceipt.setVisibility(View.VISIBLE);
        } else {
            ivReceipt.setVisibility(View.GONE);
        }
    }

    /** Implicit Intent: we do not name an app, Android shows the apps that can handle ACTION_SEND. */
    private void shareExpense() {
        if (currentExpense == null) {
            return;
        }
        String note = currentExpense.getNote();
        String body = getString(R.string.share_body,
                currentExpense.getTitle(),
                AppUtils.formatMoney(this, currentExpense.getAmount()),
                currentExpense.getCategory(),
                AppUtils.formatDate(currentExpense.getDateMillis()),
                note == null || note.isEmpty() ? getString(R.string.no_note) : note);

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        send.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
        } catch (ActivityNotFoundException e) {
            Snackbar.make(rootView, R.string.msg_no_app, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        if (currentExpense == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, currentExpense.getTitle()))
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete, (dialog, which) ->
                        expenseRef.removeValue().addOnFailureListener(e ->
                                Snackbar.make(rootView,
                                        getString(R.string.msg_delete_failed, e.getMessage()),
                                        Snackbar.LENGTH_LONG).show()))
                .show();
        // When the record is removed, the listener above sees "null" and closes this screen.
    }
}
