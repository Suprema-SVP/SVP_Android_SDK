package com.suprema.svpsample;

import android.app.Application;

import com.supremainc.sdk.SvpManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SVP extends Application {
    public static SvpManager manager = new SvpManager();
    public static Map<String, User> userMap = new HashMap<>();
    public static ArrayList<User> userArray = new ArrayList<>();
    public static int sdkUserCount = 0;
}



