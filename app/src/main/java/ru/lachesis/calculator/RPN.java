package ru.lachesis.calculator;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RPN {

    private final List<String> mOutputList = new ArrayList<>();
    private final List<String> mOperationStack = new ArrayList<>();
    private final String mOperationPlus;
    private final String mOperationMinus;
    private final String mOperationMultiply;
    private final String mOperationDivide;
    private final String mOperationNegative;
    private final String mOperationEquals;
    private final String mOperationPrc;
    private final String mOperationDegree;
    private final String mOperationSqrt;
    private final String mOperationLn;
    private final String mOperationLog;


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
        mOperationEquals = context.getString(R.string.buttonTextEqual);
        mOperationPrc = context.getString(R.string.buttonTextPrc);
        mOperationSqrt = context.getString(R.string.buttonTextSqrt);
        mOperationDegree = context.getString(R.string.buttonTextDeg);
        mOperationLn = context.getString(R.string.buttonTextLn);
        mOperationLog = context.getString(R.string.buttonTextLog);
    }

    public List<String> makeRPN(List<String> inputList) {
        List<String> result = new ArrayList<>();
        for (String symbol : inputList) {
            result = addSymbol(symbol);
        }
        return result;
    }

    private List<String> addSymbol(String symbol) {
        Top top = getStackTop();
        try {
            Double.parseDouble(symbol);
            mOutputList.add(symbol);
        } catch (Exception e) {
            if (getPriority(symbol) > 0 || "=".equals(symbol)) {
                //операции
                while (top != null && getPriority(symbol) <= getPriority(top.mValue)) {
                    if (isBrackets(top.mValue))
                        break;
                    mOutputList.add(top.mValue);
                    top = removeStackTop(top);
                }
                top = addStackTop(top, symbol);
            } else if (symbol.equals(")")) {
                while (top != null) {
                    if (top.mValue.equals("(")) {
                        top = removeStackTop(top);
                        break;
                    } else {
                        mOutputList.add(top.mValue);
                        top = removeStackTop(top);
                    }
                }
            } else if (top != null)
                addStackTop(top, symbol);
//            mCurrentNumber.setLength(0);
        }
        return mOutputList;
    }

    private Top removeStackTop(Top top) {
        try {
            mOperationStack.remove(mOperationStack.size() - 1);
            top.mIndex = mOperationStack.size() - 1;
            top.mValue = mOperationStack.get(mOperationStack.size() - 1);
            if (top.mIndex < 0) return null;
            else return top;
        } catch (Exception e) {
            return null;
        }
    }

    private Top addStackTop(Top top, String symbol) {
        try {
            mOperationStack.add(symbol);
            top.mIndex = mOperationStack.size() - 1;
            top.mValue = mOperationStack.get(mOperationStack.size() - 1);
            return top;
        } catch (Exception e) {
            return null;
        }
    }

    private Top getStackTop() {
        if (mOperationStack.size() == 0) return null;
        else {
            return new Top(mOperationStack.size() - 1, mOperationStack.get(mOperationStack.size() - 1));
        }
    }


    private boolean isBrackets(String symbol) {
        return "()".contains(symbol);
    }

    private int getPriority(String symbol) {
        if ( mOperationLn.equals(symbol) || mOperationPrc.equals(symbol) || mOperationLog.equals(symbol) || mOperationSqrt.equals(symbol))
            return 5;
        else if (mOperationDegree.equals(symbol))
            return 4;
        else if (mOperationNegative.equals(symbol) )
            return 3;
        else if (mOperationMultiply.equals(symbol) || mOperationDivide.equals(symbol))
            return 2;
        else if (mOperationMinus.equals(symbol) || mOperationPlus.equals(symbol))
            return 1;
        else return 0;
    }

    public strictfp Double calculateRPN() throws RuntimeException {
        Double result = 0D;
        boolean isMyException = false;
        List<Double> stack = new ArrayList<>();
        for (int i = 0; i < mOutputList.size(); i++) {
            String value = mOutputList.get(i);
            try {
                stack.add(Double.parseDouble(value));
//                mOutputList.add(mOperationEquals);
                result = stack.get(stack.size() - 1);
            } catch (Exception e) {
                try {
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
                        if (stack.get(stack.size() - 1) == 0) {
                            isMyException = true;
                            throw new RuntimeException("Div0");
                        }
                        result = stack.get(stack.size() - 2) / stack.get(stack.size() - 1);
                        afterBinaryOperation(stack, result);
                    } else if (value.equals(mOperationNegative)) {
                        result = -1 * stack.get(stack.size() - 1);
                        afterUnaryOperation(stack, result);
                    } else if (value.equals(mOperationSqrt)) {
                        result = Math.sqrt(stack.get(stack.size() - 1));
                        afterUnaryOperation(stack, result);
                    } else if (value.equals(mOperationDegree)) {
                        result = Math.pow(stack.get(stack.size() - 2), stack.get(stack.size() - 1));
                        afterBinaryOperation(stack, result);
                    } else if (value.equals(mOperationLn)) {
                        result = Math.log(stack.get(stack.size() - 1));
                        afterUnaryOperation(stack, result);
                    } else if (value.equals(mOperationLog)) {
                        result = Math.log10(stack.get(stack.size() - 1));
                        afterUnaryOperation(stack, result);
                    }
                    if (value.equals(mOperationPrc)) {
                        result = stack.get(stack.size() - 1) * stack.get(stack.size() - 2) / 100;
                        afterBinaryOperation(stack, result);
                    }
                } catch (RuntimeException ie) {
                    throw new RuntimeException(!isMyException ? "WrongInput" : ie.getMessage());
                }
            }
        }
        Log.i("RESULT", result.toString());
        if (Double.isInfinite(result)) throw new ArithmeticException("Infinite");
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
