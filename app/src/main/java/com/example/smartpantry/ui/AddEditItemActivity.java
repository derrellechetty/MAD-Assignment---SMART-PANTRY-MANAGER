package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryDbHelper;
import com.example.smartpantry.logic.IngredientNormalizer;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Add / Edit ingredient form (CREATE and UPDATE). The same screen is used for both:
 * if the launching Intent carries EXTRA_ITEM_ID we are editing, otherwise adding.
 * All fields are validated before anything is written to the database.
 */
public class AddEditItemActivity extends AppCompatActivity {

    private static final double MAX_QTY = 100000;

    private PantryDbHelper db;
    private PantryItem editing;            // null in add mode
    private long expiryMillis = 0;         // 0 = none

    private TextInputLayout tilName, tilQuantity;
    private TextInputEditText etName, etQuantity, etExpiry;
    private Spinner spinnerUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        db = PantryDbHelper.getInstance(this);

        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnClearDate = findViewById(R.id.btnClearDate);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UnitConverter.UI_UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        // Edit mode? Read the id passed by MainActivity.
        long id = getIntent().getLongExtra(MainActivity.EXTRA_ITEM_ID, -1);
        if (id != -1) editing = db.getItem(id);

        if (editing != null) {
            toolbar.setTitle(R.string.title_edit_item);
            etName.setText(editing.getName());
            etQuantity.setText(PantryAdapter.formatQty(editing.getQuantity()));
            int pos = Arrays.asList(UnitConverter.UI_UNITS).indexOf(editing.getUnit());
            spinnerUnit.setSelection(Math.max(pos, 0));
            expiryMillis = editing.getExpiryMillis();
            btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            toolbar.setTitle(R.string.title_add_item);
            String def = AppSettings.defaultUnit(this);
            spinnerUnit.setSelection(Math.max(Arrays.asList(UnitConverter.UI_UNITS).indexOf(def), 0));
            btnDelete.setVisibility(android.view.View.GONE);
        }
        showExpiry();

        etExpiry.setOnClickListener(v -> pickDate());
        btnClearDate.setOnClickListener(v -> { expiryMillis = 0; showExpiry(); });
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showExpiry() {
        etExpiry.setText(expiryMillis > 0
                ? DateFormat.getDateInstance().format(new Date(expiryMillis)) : "");
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        if (expiryMillis > 0) c.setTimeInMillis(expiryMillis);
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar picked = Calendar.getInstance();
            picked.clear();
            picked.set(y, m, d);
            expiryMillis = picked.getTimeInMillis();   // midnight of the chosen day
            showExpiry();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form; returns true only if every field is acceptable. */
    private boolean validate() {
        boolean ok = true;
        tilName.setError(null);
        tilQuantity.setError(null);

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.err_name_required));
            ok = false;
        } else if (!name.matches("[\\p{L}][\\p{L} '\\-]{1,39}")
                || IngredientNormalizer.normalize(name).isEmpty()) {
            tilName.setError(getString(R.string.err_name_invalid));
            ok = false;
        }

        String q = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        if (q.isEmpty()) {
            tilQuantity.setError(getString(R.string.err_qty_required));
            ok = false;
        } else {
            try {
                double v = Double.parseDouble(q);
                if (Double.isNaN(v) || v <= 0 || v > MAX_QTY) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.err_qty_invalid));
                ok = false;
            }
        }
        return ok;
    }

    private void save() {
        if (!validate()) return;

        String name = etName.getText().toString().trim();
        double qty = Double.parseDouble(etQuantity.getText().toString().trim());
        String unit = (String) spinnerUnit.getSelectedItem();
        long expiry = expiryMillis > 0 ? RecipeMatcher.startOfDay(expiryMillis) : 0;

        if (editing == null) {
            db.insertItem(new PantryItem(name, qty, unit, expiry));          // CREATE
        } else {
            editing.setName(name);
            editing.setQuantity(qty);
            editing.setUnit(unit);
            editing.setExpiryMillis(expiry);
            db.updateItem(editing);                                          // UPDATE
        }
        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
        finish();    // MainActivity.onResume() reloads the list
    }


    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(editing.getName())
                .setPositiveButton(R.string.yes, (d, w) -> {
                    db.deleteItem(editing.getId());                          // DELETE
                    Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
