package com.zymmetric.installer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {
    public boolean isDownloading;
    Map<Integer, AppItem> appsMap = new HashMap<Integer, AppItem>();
    public ArrayList<String>  categories = new ArrayList<String>();
    public ArrayList<PendingItem> pendingList = new ArrayList<PendingItem>();
    boolean serviceIsRunning = false;
    public ActivityStatus currentActivity = ActivityStatus.MAIN;

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
