package ru.lachesis.calculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final int CODE_THEME = 1;
    public static final String KEY_THEME = "my.extra.keys.isNight";
    private static final String prefs = "prefs.xml";
    private static final String pref_name = "theme";
    private final static String PARCEL_KEY = "rpnCalculator";
    private EditText mOutputString;
    private TextView mResultString;
    private RPNWrapper mRpnWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SettingsActivity.class);

        boolean isNight = getSharedPreferences(prefs, MODE_PRIVATE).getBoolean(pref_name, false);
        switchTheme(isNight);
        intent.putExtra(KEY_THEME, isNight);

        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        mOutputString = findViewById(R.id.inputView);
        mOutputString.setInputType(InputType.TYPE_NULL);
        mResultString = findViewById(R.id.resultView);

        mRpnWrapper = new RPNWrapper(this, mOutputString, mResultString);

        setListeners(intent, rootLayout);

        catchOuterIntent();
    }

    private void catchOuterIntent() {
        Intent outIntent = getIntent();
        Bundle bundle = outIntent.getExtras();
        if (bundle != null) {
            Log.e("INPUT", bundle.getString("inputString", ""));
            String s = bundle.getString("inputString", "");
            s = s.replace("-", "\u2212");
            mRpnWrapper.buildOutputFromString(s);
            mRpnWrapper.buildOutputStringFromOutList();
        }
    }

    private void setListeners(Intent intent, ConstraintLayout rootLayout) {
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View element = rootLayout.getChildAt(i);
            if (element instanceof MaterialButton) {
                if (element.getId() == R.id.buttonDel) {
                    element.setOnLongClickListener(b -> {
                        mRpnWrapper.clear();
                        updateState();
                        return true;
                    });
                    element.setOnClickListener(b -> {
                        mRpnWrapper.clearLastElement();
                        updateState();
                    });
//                } else if (element.getId() == R.id.buttonEqual) { element.setOnClickListener(b->calculate());
                } else if (element.getId() == R.id.settingsButton) {
                    element.setOnClickListener(b -> {
                        startActivityForResult(intent, CODE_THEME);
                    });
                } else
                    element.setOnClickListener(b -> setAction(b));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            boolean isNight = data.getBooleanExtra(KEY_THEME, false);
            getSharedPreferences(prefs, MODE_PRIVATE).edit().putBoolean(pref_name, isNight).apply();
            recreate();
        }
    }

    public static void switchTheme(boolean isChecked) {
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setAction(View b) {
        Button button = (Button) b;
        String symbol = (String) button.getText();
        mRpnWrapper.buildOutputString(symbol);
        mRpnWrapper.calculate();
        updateState();
    }

    private void updateState() {
        mOutputString.setText(mRpnWrapper.getOutputStr());
        mResultString.setText(mRpnWrapper.getResultStr());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);
        mRpnWrapper = instanceState.getParcelable(PARCEL_KEY);
        mResultString.setText(mRpnWrapper.getResult());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putParcelable(PARCEL_KEY, mRpnWrapper);
    }


}