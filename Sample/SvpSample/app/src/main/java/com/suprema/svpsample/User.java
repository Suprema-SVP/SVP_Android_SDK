package com.suprema.svpsample;

import com.supremainc.sdk.model.Finger;
import com.supremainc.sdk.model.FingerList;

public class User
{
    private String mUserID;
    private FingerList mFingerList;

    public User() {
        mUserID = "";
        mFingerList = new FingerList();
    }

    public User(final String userID) {
        mUserID = userID;
        mFingerList = new FingerList();
    }

    public void addFinger(Finger finger) {
        if (finger != null)
            mFingerList.addFinger(finger);
    }

    public void updateFinger(Finger finger) {
        if (finger != null)
            mFingerList.updateFinger(finger);
    }

    public void deleteFinger(Finger finger) {
        if (finger != null)
            mFingerList.deleteFinger(finger);
    }

    public int getFingerID(int index) {
        return mFingerList.getFinger(index).id;
    }

    public int getNumOfFinger() {
        return mFingerList.fingers.size();
    }

    public String getUserID() {
        return mUserID;
    }

    public FingerList getFingerList() {
        return mFingerList;
    }

    public Finger getFinger(int index) {
        return mFingerList.getFinger(index);
    }
}