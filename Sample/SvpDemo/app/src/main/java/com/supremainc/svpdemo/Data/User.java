package com.supremainc.svpdemo.Data;

import android.support.annotation.Nullable;

import com.supremainc.sdk.model.Finger;
import com.supremainc.sdk.model.FingerList;

public class User {
    private String      mUserID;
    private String      mUserName;
    private String      mCardNumber;
    private FingerList  mFingerList;

    public User() {
        mUserID = "";
        mUserName = "";
        mFingerList = new FingerList();
    }

    public User(final String userID) {
        this.mUserID = userID;
        mFingerList = new FingerList();
    }

    public User(final String userID, String userName, String cardNumber) {
        this.mUserID = userID;
        this.mUserName = userName;
        this.mCardNumber = cardNumber;
        mFingerList = new FingerList();
    }

    public void setUserID(final String userID) {
        this.mUserID = userID;
    }

    public String getUserID() {
        return this.mUserID;
    }

    public void setUserName(final String userName) {
        this.mUserName = userName;
    }

    public String getUserName() {
        return this.mUserName;
    }

    public void setCardNumber(final String cardNumber) {
        this.mCardNumber = cardNumber;
    }

    public String getCardNumber() {
        return this.mCardNumber;
    }

    public void addFinger(Finger finger1, @Nullable Finger finger2) {
        if (finger1 != null)
            this.mFingerList.addFinger(finger1);

        if (finger2 != null)
            this.mFingerList.addFinger(finger2);
    }

    public void updateFinger(Finger finger1, @Nullable Finger finger2) {
        if (finger1 != null)
            this.mFingerList.updateFinger(finger1);

        if (finger2 != null)
            this.mFingerList.updateFinger(finger2);
    }

    public void deleteFinger(Finger finger1, @Nullable Finger finger2) {
        if (finger1 != null)
            this.mFingerList.deleteFinger(finger1);

        if (finger2 != null)
            this.mFingerList.deleteFinger(finger2);
    }

    public int getFingerID(int index) {
        return this.mFingerList.getFinger(index).id;
    }

    public int getNumOfFinger() {
        return this.mFingerList.fingers.size();
    }

    public Finger getFinger(int index) {
        return this.mFingerList.getFinger(index);
    }

}