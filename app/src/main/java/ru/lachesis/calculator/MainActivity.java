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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //    private final static String TRACE_TAG = "Trace";
    public static final int CODE_THEME = 1;
    public static final String KEY_THEME = "my.extra.keys.isNight";
    private static final String prefs = "prefs.xml";
    private static final String pref_name = "theme";
    private final static String PARCEL_KEY = "rpnCalculator";
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

        catchOuterIntent();

        rpnWrapper = new RPNWrapper(this);

        setListeners(intent, rootLayout);
    }

    private void catchOuterIntent() {
        Intent outIntent = getIntent();
        Bundle bundle = outIntent.getExtras();
        if (bundle != null) {
            Log.e("INPUT", bundle.getString("inputString", ""));
            String s = bundle.getString("inputString", "");
//            mOutputString.setText(s);
            s = s.replace("-", "\u2212");
            buildOutputFromString(s);
            buildOutputStringFromOutList();
        }
    }

    private void setListeners(Intent intent, ConstraintLayout rootLayout) {
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

    private void calculate(String str) {
        buildOutputFromString(str);
        buildOutputStringFromOutList();
        rpnWrapper.buildRPNString(mOutList);
        mResultString.setText(getResult());
    }


    private boolean clear() {
        mOutputString.setText("");
        mResultString.setText("");
        mOutList.clear();
        if (rpnWrapper != null)
            rpnWrapper.clearStacks();
        mCurrentNumber.setLength(0);
        return true;
    }

    private void clearLastElement() {
        String text = mOutputString.getText().toString();
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
//            mOutputString.setText(text);
        }

        calculate(text);
    }

    private void setAction(View b) {
        Button button = (Button) b;
        String symbol = (String) button.getText();
        buildOutputString(symbol);
        calculate(mOutputString.getText().toString());

    }

    private void buildOutputStringFromOutList() {
        mOutputString.setText("");
        for (String s : mOutList)
            if (!s.contentEquals(getText(R.string.buttonTextEqual)))
                mOutputString.append(s);
            else mOutList.add(s);
    }

    private void buildOutputFromString(String str) {
        clear();
        for (int i = 0; i < str.length(); i++) {
            String s = str.substring(i, i + 1);
            buildOutList(s);
        }
    }

    private void buildOutputString(String symbol) {
        mOutputString.append(symbol);
    }

    private void buildOutList(String symbol) {
        Double number;
        if (isNumber(symbol) || isPoint(symbol)) {
            mCurrentNumber.append(symbol);
            String lastElement = mOutList.size() != 0 ? mOutList.get(mOutList.size() - 1) : null;
            if (mOutList.size() == 0 || !isNumber(lastElement))
                mOutList.add(mCurrentNumber.toString());
            else mOutList.set(mOutList.size() - 1, mCurrentNumber.toString());
            return;
        } else {
            if (!getText(R.string.buttonLeftBracket).equals(symbol)) {
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
                } finally {
                    getResult();
                }
            } else mOutList.add(symbol);

        }
    }

/*
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
            } else //if (!DEL.equals(symbol))//&&!getText(R.string.buttonTextEqual).equals(symbol))
                mOutList.add(symbol);
            mOutputString.setText("");
            for (String s : mOutList)
                if (!s.contentEquals(getText(R.string.buttonTextEqual)))
                    mOutputString.append(s);
        }
    }
*/

    private boolean isNumber(String str) {
        boolean isNumber;
        if (isPoint(str)) return true;//str = "0.";
        if ("-".equals(str)) return true;
        // if (getText(R.string.buttonTextPlusMinus).equals(str)) return true;
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
            Double result = rpnWrapper.calculate();
            String format = "%." + BigDecimal.valueOf(result).scale() + "f\n";
            return String.format(Locale.getDefault(), format, rpnWrapper.calculate());
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


    private boolean isPoint(String symbol) {
        return getText(R.string.buttonTextPt).equals(symbol);
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


}