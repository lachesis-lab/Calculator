package ru.lachesis.calculator;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RPN implements Parcelable {

    private List<String> outputList = new ArrayList<>();
    private List<String> operationStack = new ArrayList<>();
    private StringBuilder currentNumber = new StringBuilder();
    private String mOperationPlus, mOperationMinus, mOperationMultiply, mOperationDivide, mOperationNegative;

    protected RPN(Parcel in) {
        outputList = in.createStringArrayList();
        operationStack = in.createStringArrayList();
        mOperationPlus = in.readString();
        mOperationMinus = in.readString();
        mOperationMultiply = in.readString();
        mOperationDivide = in.readString();
        mOperationNegative = in.readString();
    }

    public static final Creator<RPN> CREATOR = new Creator<RPN>() {
        @Override
        public RPN createFromParcel(Parcel in) {
            return new RPN(in);
        }

        @Override
        public RPN[] newArray(int size) {
            return new RPN[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(outputList);
        dest.writeStringList(operationStack);
        dest.writeString(mOperationPlus);
        dest.writeString(mOperationMinus);
        dest.writeString(mOperationMultiply);
        dest.writeString(mOperationDivide);
        dest.writeString(mOperationNegative);
    }

    private static class Top {
        private int mIndex;
        private String mValue;

        private Top(int index, String value) {
            mIndex = index;
            mValue = value;
        }
    }

    public RPN(Context context) {
        mOperationPlus = context.getString(R.string.buttonTextPlus);
        mOperationMinus = context.getString(R.string.buttonTextMinus);
        mOperationMultiply = context.getString(R.string.buttonTextMultiply);
        mOperationDivide = context.getString(R.string.buttonTextDivide);
        mOperationNegative = context.getString(R.string.buttonTextPlusMinus);
    }

    public List<String> makeRPN(String symbol) {

        Top top = getStackTop();
        if (isDigit(symbol) || isPoint(symbol)) {
            currentNumber.append(symbol);
        } else {
            if (!"(".contains(symbol) && currentNumber.length() != 0)
                outputList.add(currentNumber.toString());
            if (getPriority(symbol) > 0 || "=".equals(symbol)) {
                //операции
                while (top != null && getPriority(symbol) <= getPriority(top.mValue)) {
                    if (isBrackets(top.mValue))
                        break;
                    outputList.add(top.mValue);
                    top = removeStackTop(top);
                }
                top = addStackTop(top, symbol);
            } else if (symbol.equals(")")) {
                while (top != null) { //&& !top.mValue.contains("(")) {
                    if (top.mValue.equals("(")) {
                        top = removeStackTop(top);
                        break;
                    } else {
                        outputList.add(top.mValue);
                        top = removeStackTop(top);
                    }
                }
            } else if (top != null)
                addStackTop(top, symbol);
            currentNumber.setLength(0);
        }
        return outputList;
    }

    private Top removeStackTop(Top top) {
        try {
            operationStack.remove(operationStack.size() - 1);
            top.mIndex = operationStack.size() - 1;
            top.mValue = operationStack.get(operationStack.size() - 1);
            if (top.mIndex < 0) return null;
            else return top;
        } catch (Exception e) {
            return null;
        }
    }

    private Top addStackTop(Top top, String symbol) {
        try {
            operationStack.add(symbol);
            top.mIndex = operationStack.size() - 1;
            top.mValue = operationStack.get(operationStack.size() - 1);
            return top;
        } catch (Exception e) {
            return null;
        }
    }

    private Top getStackTop() {
        if (operationStack.size() == 0) return null;
        else {
            return new Top(operationStack.size() - 1, operationStack.get(operationStack.size() - 1));
        }
    }

    public void clearStacks() {
        operationStack.clear();
        outputList.clear();
        currentNumber.setLength(0);
    }

    private boolean isDigit(String symbol) {
        return "0123456789".contains(symbol);
    }

    private boolean isPoint(String symbol) {
        return ".".equals(symbol);
    }

    private boolean isBrackets(String symbol) {
        return "()".contains(symbol);
    }

    private int getPriority(String symbol) {
        if (mOperationNegative.equals(symbol))
            return 3;
        if (mOperationMultiply.equals(symbol) || mOperationDivide.equals(symbol))
            return 2;
        else if (mOperationMinus.equals(symbol) || mOperationPlus.equals(symbol))
            return 1;
        else return 0;
    }

    public Double calculateRPN() {
        Double result = 0D;
        List<Double> stack = new ArrayList<>();
        for (int i = 0; i < outputList.size(); i++) {
            String value = outputList.get(i);
            try {
                stack.add(Double.parseDouble(value));
            } catch (Exception e) {
                if (value.equals(mOperationPlus)) {
                    result = stack.get(stack.size() - 2) + stack.get(stack.size() - 1);
                    afterBinaryOperation(stack, result);
                } else if (value.equals(mOperationMinus)) {
                    result = stack.get(stack.size() - 2) - stack.get(stack.size() - 1);
                    afterBinaryOperation(stack, result);
                } else if (value.equals(mOperationMultiply)) {
                    result = stack.get(stack.size() - 2) * stack.get(stack.size() - 1);
                    afterBinaryOperation(stack, result);
                } else if (value.equals(mOperationDivide)) {
                    result = stack.get(stack.size() - 2) / stack.get(stack.size() - 1);
                    afterBinaryOperation(stack, result);
                } else if (value.equals(mOperationNegative)) {
                    result = -1 * stack.get(stack.size() - 1);
                    afterUnaryOperation(stack, result);
                }
            }
        }
        Log.i("RESULT", result.toString());
        return result;
    }

    private void afterBinaryOperation(List<Double> stack, Double result) {
        stack.remove(stack.size() - 1);
        stack.set(stack.size() - 1, result);
    }

    private void afterUnaryOperation(List<Double> stack, Double result) {
        stack.set(stack.size() - 1, result);
    }
}
