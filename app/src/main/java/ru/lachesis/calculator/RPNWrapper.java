package ru.lachesis.calculator;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RPNWrapper implements Parcelable {
    private final StringBuilder mCurrentNumber = new StringBuilder();
    private List<String> mOutList = new ArrayList<>();
    private Context mContext;
    private EditText mOutputString;
    private TextView mResultString;
    private RPN mRpn;

    public RPNWrapper(Context context, EditText outputString, TextView resultString) {
        mContext = context;
        mOutputString = outputString;
        mResultString = resultString;
    }

    public String getOutputStr() {
        return mOutputString.getText().toString();
    }

    public String getResultStr() {
        return mResultString.getText().toString();
    }

    protected RPNWrapper(Parcel in) {
        mOutList = in.createStringArrayList();
    }

    public static final Creator<RPNWrapper> CREATOR = new Creator<RPNWrapper>() {
        @Override
        public RPNWrapper createFromParcel(Parcel in) {
            return new RPNWrapper(in);
        }

        @Override
        public RPNWrapper[] newArray(int size) {
            return new RPNWrapper[size];
        }
    };

    public void buildRPNString(List<String> inputList) {
        mRpn = new RPN(mContext);
        mOutList = inputList;
        mRpn.makeRPN(mOutList);
    }

    public Double calculateRPN() {
        Double result = 0D;
        if (mRpn != null)
            result = mRpn.calculateRPN();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mOutList);
    }

    public void calculate() {
        buildOutputFromString(mOutputString.getText().toString());
        buildOutputStringFromOutList();
        buildRPNString(mOutList);
        mResultString.setText(getResult());
    }

    public void clear() {
        mOutputString.setText("");
        mResultString.setText("");
        mOutList.clear();
        mCurrentNumber.setLength(0);
    }

    public void clearLastElement() {
        String text = mOutputString.getText().toString();
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            mOutputString.setText(text);
        }
        calculate();
    }

    public void buildOutputStringFromOutList() {
        mOutputString.setText("");
        for (String s : mOutList)
            if (!s.contentEquals(mContext.getText(R.string.buttonTextEqual)))
                mOutputString.append(s);
            else {
                mOutList.add(s);
//                break;
            }
    }

    public void buildOutputFromString(String str) {
        clear();
        for (int i = 0; i < str.length(); i++) {
            String s = str.substring(i, i + 1);
            buildOutList(s);
        }
    }

    public void buildOutputString(String symbol) {
        mOutputString.append(symbol);
    }

    public void buildOutList(String symbol) {
        Double number;
        if (isNumber(symbol) || isPoint(symbol)) {
            mCurrentNumber.append(symbol);
            String lastElement = mOutList.size() != 0 ? mOutList.get(mOutList.size() - 1) : null;
            if (mOutList.size() == 0 || !isNumber(lastElement))
                mOutList.add(mCurrentNumber.toString());
            else mOutList.set(mOutList.size() - 1, mCurrentNumber.toString());
//            mOutList.add("=");
            return;
        } else {
            if (!mContext.getText(R.string.buttonLeftBracket).equals(symbol)) {
                mCurrentNumber.setLength(0);
            }
            if (symbol.contentEquals(mContext.getText(R.string.buttonTextPlusMinus))) {
                try {
                    if (mOutList.size() > 0) {
                        number = -1 * Double.parseDouble(mOutList.get(mOutList.size() - 1));
                        mOutList.set(mOutList.size() - 1, number.toString());
                    } else
                        mOutList.add(symbol);
                } catch (NumberFormatException ignored) {
                } finally {
                    //                   calculate(mOutputString.getText().toString());
                    getResult();
                }
            } else
                mOutList.add(symbol);

        }
    }

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

    public String getResult() {
        try {
            Double result = calculateRPN();
            String format = "%." + BigDecimal.valueOf(result).scale() + "f\n";
            return String.format(Locale.getDefault(), format, result);
        } catch (RuntimeException e) {
            if (e.getMessage() != null) {
                switch (e.getMessage()) {
                    case "Div0":
                        return (String) mContext.getText(R.string.excDiv0);
                    case "Infinite":
                        return (String) mContext.getText(R.string.infinite);
                    case "WrongInput":
                        return (String) mContext.getText(R.string.wrongInput);
                }
            }
            return (String) mContext.getText(R.string.exc);
        }
    }

    private boolean isPoint(String symbol) {
        return mContext.getText(R.string.buttonTextPt).equals(symbol);
    }


}
