package ru.lachesis.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        boolean isNight = getIntent().getBooleanExtra(MainActivity.KEY_THEME, false);
        MaterialButton saveButton = findViewById(R.id.saveSettingsButton);
        SwitchMaterial dayNightSwitch = findViewById(R.id.dayNightSwitch);
        dayNightSwitch.setChecked(isNight);

        saveButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra(MainActivity.KEY_THEME, dayNightSwitch.isChecked());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

}