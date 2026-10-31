package com.kharochkhata.app;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * Screen 5: Add / Edit expense (one screen, two modes).
 *
 * - No extra in the Intent         -> "Add" mode
 * - Intent has EXTRA_EXPENSE_ID    -> "Edit" mode, the old values are loaded into the form
 *
 * Also handles: input validation, date picker, and the receipt photo
 * (take with the camera OR pick from the gallery).
 */
public class AddEditExpenseActivity extends AppCompatActivity {

    private TextInputEditText etTitle;
    private TextInputEditText etAmount;
    private TextInputEditText etNote;
    private Spinner spinnerCategory;
    private Button btnDate;
    private Button btnSave;
    private Button btnRemovePhoto;
    private ImageView ivReceipt;
    private View rootView;

    private String expenseId;                 // null = adding a new expense
    private long dateMillis = System.currentTimeMillis();
    private String photoBase64 = "";          // "" = no photo

    // ---- Camera: returns a small preview picture, so no CAMERA permission is needed ----
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    setPhoto(bitmap);
                }
            });

    // ---- Gallery: the system picker returns the Uri of the chosen picture ----
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri == null) {
                    return; // user cancelled
                }
                try {
                    setPhoto(ImageUtils.decodeUri(getContentResolver(), uri));
                } catch (Exception e) {
                    Snackbar.make(rootView, R.string.msg_photo_failed, Snackbar.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        rootView = findViewById(R.id.addEditRoot);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnDate = findViewById(R.id.btnDate);
        btnSave = findViewById(R.id.btnSave);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        ivReceipt = findViewById(R.id.ivReceipt);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnGallery = findViewById(R.id.btnGallery);

        // Category drop-down filled from res/values/strings.xml (string-array "categories")
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        updateDateButton();
        btnDate.setOnClickListener(v -> showDatePicker());
        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnRemovePhoto.setOnClickListener(v -> clearPhoto());
        btnSave.setOnClickListener(v -> saveExpense());

        // Add or Edit?
        expenseId = getIntent().getStringExtra(AppUtils.EXTRA_EXPENSE_ID);
        if (expenseId != null) {
            toolbar.setTitle(R.string.edit_title);
            loadExpense();
        } else {
            toolbar.setTitle(R.string.add_title);
        }
    }

    /** Edit mode: read the existing expense once and fill the form. */
    private void loadExpense() {
        DatabaseReference ref = AppUtils.expensesRef();
        if (ref == null) {
            AppUtils.goToLogin(this);
            return;
        }
        ref.child(expenseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Expense expense = snapshot.getValue(Expense.class);
                if (expense == null) {
                    finish();
                    return;
                }
                etTitle.setText(expense.getTitle());
                etAmount.setText(String.valueOf(expense.getAmount()));
                etNote.setText(expense.getNote());
                selectCategory(expense.getCategory());
                dateMillis = expense.getDateMillis();
                updateDateButton();

                if (expense.getPhoto() != null && !expense.getPhoto().isEmpty()) {
                    photoBase64 = expense.getPhoto();
                    showPhoto(ImageUtils.fromBase64(photoBase64));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(rootView, getString(R.string.msg_load_failed, error.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void selectCategory(String category) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinnerCategory.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(category)) {
                spinnerCategory.setSelection(i);
                return;
            }
        }
    }

    // ---------------------------------------------------------------------------------
    // Date
    // ---------------------------------------------------------------------------------

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day);
            dateMillis = chosen.getTimeInMillis();
            updateDateButton();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButton() {
        btnDate.setText(AppUtils.formatDate(dateMillis));
    }

    // ---------------------------------------------------------------------------------
    // Photo
    // ---------------------------------------------------------------------------------

    private void openCamera() {
        try {
            cameraLauncher.launch(null);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(rootView, R.string.msg_no_camera, Snackbar.LENGTH_LONG).show();
        }
    }

    /** Shrink the picture, remember it as text, and show it on the screen. */
    private void setPhoto(Bitmap bitmap) {
        photoBase64 = ImageUtils.toBase64(bitmap);
        showPhoto(bitmap);
    }

    private void showPhoto(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        ivReceipt.setImageBitmap(bitmap);
        ivReceipt.setVisibility(View.VISIBLE);
        btnRemovePhoto.setVisibility(View.VISIBLE);
    }

    private void clearPhoto() {
        photoBase64 = "";
        ivReceipt.setImageDrawable(null);
        ivReceipt.setVisibility(View.GONE);
        btnRemovePhoto.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------------
    // Save (create or update)
    // ---------------------------------------------------------------------------------

    private void saveExpense() {
        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        String amountText = etAmount.getText() == null ? ""
                : etAmount.getText().toString().trim().replace(',', '.');
        String note = etNote.getText() == null ? "" : etNote.getText().toString().trim();

        // ---- validation: nothing may crash on empty or wrong input ----
        boolean valid = true;
        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_title_required));
            valid = false;
        }

        double amount = 0;
        if (amountText.isEmpty()) {
            etAmount.setError(getString(R.string.error_amount_required));
            valid = false;
        } else {
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                amount = -1;
            }
            if (amount <= 0) {
                etAmount.setError(getString(R.string.error_amount_invalid));
                valid = false;
            } else if (amount > 1_000_000_000d) {
                etAmount.setError(getString(R.string.error_amount_big));
                valid = false;
            }
        }
        if (!valid) {
            return;
        }

        DatabaseReference ref = AppUtils.expensesRef();
        if (ref == null) {
            Toast.makeText(this, R.string.msg_not_logged_in, Toast.LENGTH_LONG).show();
            AppUtils.goToLogin(this);
            return;
        }

        boolean isEdit = expenseId != null;
        String id = isEdit ? expenseId : ref.push().getKey();   // push() makes a new unique key
        String category = spinnerCategory.getSelectedItem().toString();

        Expense expense = new Expense(id, title, amount, category, note, photoBase64, dateMillis);

        btnSave.setEnabled(false);
        // The local list updates at once; this listener reports a failure from the server.
        ref.child(id).setValue(expense).addOnFailureListener(e ->
                Toast.makeText(getApplicationContext(),
                        getString(R.string.msg_save_failed, e.getMessage()),
                        Toast.LENGTH_LONG).show());

        Toast.makeText(this, isEdit ? R.string.msg_updated : R.string.msg_saved,
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
