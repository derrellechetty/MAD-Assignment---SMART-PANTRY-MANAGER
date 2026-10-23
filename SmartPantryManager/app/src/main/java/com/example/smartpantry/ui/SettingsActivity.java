package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.UnitConverter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Arrays;

/** Settings screen: preferences are saved immediately in SharedPreferences. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchMaterial alerts = findViewById(R.id.switchAlerts);
        alerts.setChecked(AppSettings.expiringAlertsEnabled(this));
        alerts.setOnCheckedChangeListener((b, on) -> AppSettings.setExpiringAlerts(this, on));

        SwitchMaterial ignore = findViewById(R.id.switchIgnoreExpired);
        ignore.setChecked(AppSettings.ignoreExpired(this));
        ignore.setOnCheckedChangeListener((b, on) -> AppSettings.setIgnoreExpired(this, on));

        Spinner unit = findViewById(R.id.spinnerDefaultUnit);
        unit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UnitConverter.UI_UNITS));
        unit.setSelection(Math.max(0,
                Arrays.asList(UnitConverter.UI_UNITS).indexOf(AppSettings.defaultUnit(this))));
        unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AppSettings.setDefaultUnit(SettingsActivity.this, UnitConverter.UI_UNITS[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        NavHelper.setup(this, R.id.nav_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((BottomNavigationView) findViewById(R.id.bottomNav))
                .getMenu().findItem(R.id.nav_settings).setChecked(true);
    }
}
