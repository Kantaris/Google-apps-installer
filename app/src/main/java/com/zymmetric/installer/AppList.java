package com.zymmetric.installer;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chris on 3/7/16.
 */
public class AppList implements TaskCompleted {

    String jsonUrl= "https://raw.githubusercontent.com/Kantaris/installer-app/master/json/gapp.json";
    public ArrayList<String>  categories = new ArrayList<String>();
    Map<Integer, AppItem> appsMap = new HashMap<Integer, AppItem>();
    TaskCompleted tc;
    PackageManager pm;
    public String json;
    public AppList(TaskCompleted tc, PackageManager pm){
        this.tc = tc;
        this.pm = pm;


    }
    public void downloadJson(){
        JsonDownloader jd = new JsonDownloader(this);
        jd.execute(jsonUrl);
    }



    @Override
    public void downloadJsonCompleted(String json) {
        this.json = json;
        try {
            JSONObject response = new JSONObject(json);
            String[] sa = {"googleApps","googleSFWApps"};
            for(int qq = 0; qq < sa.length; qq++) {
                JSONArray ja = response.getJSONArray(sa[qq]);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jjn = ja.getJSONObject(i);
                    AppItem ai = new AppItem();
                    ai.apkUrl = jjn.getString("apk");
                    ai.apkHash = jjn.getString("apkHash");
                    ai.apkSize = jjn.getInt("apkSize");
                    ai.changeLog = jjn.getString("changeLog");
                    if(jjn.has("dependencies")) {
                        ai.dependencies = jjn.getString("dependencies");
                    }
                    ai.displayName = jjn.getString("displayName");
                    ai.introduction = jjn.getString("introduction");
                    ai.iconUrl = jjn.getString("icon");
                    ai.id = jjn.getInt("id");
                    ai.packageName = jjn.getString("packageName");
                    ai.packageNameHash = jjn.getString("packageNameHash");
                    ai.publisherName = jjn.getString("publisherName");
                    ai.versionCode = jjn.getInt("versionCode");
                    ai.versionName = jjn.getString("versionName");
                    if(jjn.has("subjectGroup")) {
                        ai.subjectGroup = jjn.getString("subjectGroup");

                        boolean found = false;
                        for (int j = 0; j < categories.size(); j++) {
                            if (categories.get(j).equals(ai.subjectGroup)) {
                                ai.groupId = j;
                                found = true;
                            }
                        }
                        if (!found) {
                            ai.groupId = categories.size();
                            categories.add(ai.subjectGroup);
                        }
                    }
                    String jas = jjn.getString("screenshot");
                    ai.screenshot.add(jas);
                    ai.setStatus(pm);
                    appsMap.put(ai.id, ai);

                }
            }
            for (Map.Entry<Integer, AppItem> entry : appsMap.entrySet()) {
                String deps = entry.getValue().dependencies;
                if(deps.length() > 0) {
                    String[] dd = deps.split(",");
                    for (int k = 0; k < dd.length; k++) {
                        int id = Integer.parseInt(dd[k]);
                        AppItem ai = appsMap.get(id);
                        entry.getValue().depMap.put(id, ai);
                    }
                }
            }

            tc.downloadJsonCompleted("");
        }catch (JSONException ex){
            String blah ="";

        }
    }
}
