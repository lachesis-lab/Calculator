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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //    private final static String TRACE_TAG = "Trace";
    public static final int CODE_THEME = 1;
    public static final String KEY_THEME = "my.extra.keys.isNight";
    private static final String prefs = "prefs.xml";
    private static final String pref_name = "theme";
    private final static String PARCEL_KEY = "rpnCalculator";
    private final static String DEL = "DEL";
    private EditText mOutputString;
    private TextView mResultString;
    StringBuilder mCurrentNumber = new StringBuilder();
    List<String> mOutList = new ArrayList<>();
    private RPNWrapper rpnWrapper;

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

        rpnWrapper = new RPNWrapper(this);
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View element = rootLayout.getChildAt(i);
            if (element instanceof MaterialButton) {
                if (element.getId() == R.id.buttonDel) {
                    element.setOnLongClickListener(b -> clear());
                    element.setOnClickListener(b -> clearLastElement());
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

/*
    private void calculate() {
        buildOutputString(getText(R.string.buttonTextEqual).toString());
        rpnWrapper.buildRPNString(mOutList);
        getResult();
    }
*/

    private void clearLastElement() {
        if (mOutList.size() > 0)
            mOutList.remove(mOutList.size() - 1);
        buildOutputString(DEL);
        rpnWrapper.buildRPNString(mOutList);
        getResult();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);
        rpnWrapper = instanceState.getParcelable(PARCEL_KEY);
        mResultString.setText(getResult());
        mOutList = rpnWrapper.getInputList();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putParcelable(PARCEL_KEY, rpnWrapper);
    }

    private boolean clear() {
        mOutputString.setText("");
        mResultString.setText("");
        mOutList.clear();
        rpnWrapper.clearStacks();
        return true;
    }

    private void setAction(View b) {
        Button button = (Button) b;
        String symbol = (String) button.getText();
        buildOutputString(symbol);
        rpnWrapper.buildRPNString(mOutList);
        mResultString.setText(getResult());
    }

    private void buildOutputString(String symbol) {
        Double number;
        if (isNumber(symbol) || isPoint(symbol)) {
            mCurrentNumber.append(symbol);
            mOutputString.append(symbol);
            String lastElement = mOutList.size() != 0 ? mOutList.get(mOutList.size() - 1) : null;
            if (mOutList.size() == 0 || !isNumber(lastElement))
                mOutList.add(mCurrentNumber.toString());
            else mOutList.set(mOutList.size() - 1, mCurrentNumber.toString());
            return;
        } else {
            if (!getText(R.string.buttonLeftBracket).equals(symbol)) { //&& mCurrentNumber.length() != 0) {
                mCurrentNumber.setLength(0);
            }
            if (symbol.contentEquals(getText(R.string.buttonTextPlusMinus))) {
                try {
                    if (mOutList.size() > 0) {
                        number = -1 * Double.parseDouble(mOutList.get(mOutList.size() - 1));
                        mOutList.set(mOutList.size() - 1, number.toString());
                    } else
                        mOutList.add(symbol);
                } catch (NumberFormatException e) {
//                    mResultString.setText(getText(R.string.wrongInput));
                } finally {
                    getResult();
                }
            } else if (!DEL.equals(symbol))//&&!getText(R.string.buttonTextEqual).equals(symbol))
                mOutList.add(symbol);
            mOutputString.setText("");
            for (String s : mOutList)
                if (!s.contentEquals(getText(R.string.buttonTextEqual)))
                    mOutputString.append(s);
        }
    }

    private boolean isNumber(String str) {
        boolean isNumber;
        try {
            Double.parseDouble(str);
            isNumber = true;
        } catch (Exception e) {
            isNumber = false;
        }
        return isNumber;
    }

    private String getResult() {
        try {
            return rpnWrapper.calculate().toString();
        } catch (RuntimeException e) {
            if (e.getMessage() != null) {
                switch (e.getMessage()) {
                    case "Div0":
                        return (String) getText(R.string.excDiv0);
                    case "Infinite":
                        return (String) getText(R.string.infinite);
                    case "WrongInput":
                        return (String) getText(R.string.wrongInput);
                }
            }
            return (String) getText(R.string.exc);
        }
    }

    /*   private boolean isDigit(String symbol) {
           return "0123456789".contains(symbol);
       }
   */
    private boolean isPoint(String symbol) {
        return getText(R.string.buttonTextPt).equals(symbol);
    }


}