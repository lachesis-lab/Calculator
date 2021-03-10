package ru.lachesis.calculator;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class RPNWrapper implements Parcelable {
    List<String> mInputList = new ArrayList<>();
    //    List<String> mOutputList = new ArrayList<>();
    Context mContext;
    RPN rpn;

    public RPNWrapper(Context context) {
        mContext = context;
    }

    public List<String> getInputList() {
        return mInputList;
    }

    public void setInputList(List<String> inputList) {
        mInputList = inputList;
    }

    protected RPNWrapper(Parcel in) {
        mInputList = in.createStringArrayList();
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

    public List<String> buildRPNString(List<String> inputList) {
        rpn = new RPN(mContext);
        mInputList = inputList;
        return rpn.makeRPN(mInputList);
    }


    public Double calculate() {
        Double result = 0D;
        if (rpn != null)
            result = rpn.calculateRPN();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mInputList);
    }

    public void clearStacks() {
        mInputList.clear();
    }
}
