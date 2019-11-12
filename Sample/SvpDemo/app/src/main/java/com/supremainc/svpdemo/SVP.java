package com.supremainc.svpdemo;

import android.app.Application;

import com.supremainc.svpdemo.Data.User;
import com.supremainc.sdk.SvpManager;
import com.supremainc.sdk.model.Finger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SVP extends Application  {
    public static Map<String, User> userMap = new HashMap<>();
    public static ArrayList<User> userArray;

    public static boolean firstScan = false;
    public static Finger newFinger = null;

    public static SvpManager manager = new SvpManager();
}
