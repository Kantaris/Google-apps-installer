package com.zymmetric.installer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 2/23/16.
 */


public class AppItem{
    public String apkUrl;
    public String apkHash;
    public int apkSize;
    public String changeLog;
    public String dependencies = "";
    public String displayName ;
    public String iconUrl;
    public int id;
    public String introduction;
    public String packageName;
    public String packageNameHash;
    public String publisherName;
    public ArrayList<String> screenshot = new ArrayList<String>();
    public String subjectGroup;
    public int groupId = -1;
    public long updateTime;
    public Date updateDate;
    public int versionCode;
    public String versionName;
    public Bitmap iconBitmap;
    Map<Integer, AppItem> depMap = new HashMap<Integer, AppItem>();
    public AppStatus status = AppStatus.NOTINSTALLED;


    public AppItem()  {

    }

    public boolean setStatus(PackageManager pm){
        try {
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
            status = AppStatus.INSTALLED;
            if (!pInfo.versionName.equals(versionName)) {
                if (versionCode > pInfo.versionCode) {
                    status = AppStatus.UPDATEAVAILABLE;
                }
            }
            return true;
        } catch (Exception ee) {
        }
        return false;
    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("apk", apkUrl);
            jsonObject.put("apkHash", apkHash);
            jsonObject.put("apkSize", apkSize);
            jsonObject.put("changeLog", changeLog);
            jsonObject.put("dependencies", dependencies);
            jsonObject.put("displayName", displayName);

            jsonObject.put("introduction", introduction);
            jsonObject.put("icon", iconUrl);
            jsonObject.put("id", id);
            jsonObject.put("packageName", packageName);
            jsonObject.put("packageNameHash", packageNameHash);
            jsonObject.put("publisherName", publisherName);

            jsonObject.put("versionCode", versionCode);
            jsonObject.put("versionName", versionName);
            jsonObject.put("subjectGroup", subjectGroup);
            jsonObject.put("screenshot", screenshot);

        } catch (JSONException e){}

        return jsonObject;

    }
}
