package ru.lachesis.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String TRACE_TAG = "Trace";
    private final static String PARCEL_KEY = "rpnCalculator";
    private EditText mOutputString;
    private TextView mResultString;
    private RPN rpn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_constraint);
        ConstraintLayout rootLayout = (ConstraintLayout) findViewById(R.id.rootLayout);
        mOutputString = (EditText) findViewById(R.id.inputView);
        mOutputString.setInputType(InputType.TYPE_NULL);
        mResultString = (TextView) findViewById(R.id.resultView);

        rpn = new RPN(this);
        for (int i = 0; i < rootLayout.getChildCount(); i++) {
            View element = rootLayout.getChildAt(i);
            if (element instanceof Button) {
                if (element.getId() == R.id.buttonDel)
                    element.setOnLongClickListener(b -> clear());
//                    element.setOnClickListener();
                else
                    element.setOnClickListener(b -> setAction(b));
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);
        rpn=instanceState.getParcelable(PARCEL_KEY);
        mResultString.setText( rpn.calculateRPN().toString());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putParcelable(PARCEL_KEY,rpn);
    }

    private boolean clear() {
        mOutputString.setText("");
        rpn.clearStacks();
        return true;
    }

    private void setAction(View b) {
        Button button = (Button) b;
        String symbol = (String) button.getText();
        buildOutputString(symbol);
        mOutputString.append(symbol);
        mResultString.setText(rpn.calculateRPN().toString());
    }



    private void buildOutputString(String symbol) {
        Log.i(TRACE_TAG, rpn.makeRPN(symbol).toString());
    }

}