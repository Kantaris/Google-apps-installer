package com.zymmetric.installer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chris on 4/19/16.
 */
public class Helper {

    static final int APP_DETAILS = 1;
    static final int APP_INSTALL = 2;

    public static void startDownload(Context context) {
        String cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Map<Integer, AppItem> appsMap = DataHolder.getInstance().appsMap;
        ArrayList<PendingItem> pendingList = DataHolder.getInstance().pendingList;
        if(!DataHolder.getInstance().serviceIsRunning && pendingList.size() > 0){
            Intent intent2 = new Intent(context, DownloadService.class);
            intent2.putExtra("link", "http://f3.market.xiaomi.com/download/" + appsMap.get(pendingList.get(0).id).apkUrl);
            intent2.putExtra("path", cacheDir + "/" + appsMap.get(pendingList.get(0).id).packageName);
            intent2.putExtra("id", pendingList.get(0).id);
            intent2.putExtra("main", pendingList.get(0).main);
            intent2.putExtra("name", appsMap.get(pendingList.get(0).id).publisherName);
            context.startService(intent2);
        }
    }
    public static void startInstall(Context context, int id, String path){
        Intent installApp = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installApp.setData(Uri.fromFile(new File(path)));
        //installApp.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        installApp.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        //installApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installApp.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);
        installApp.putExtra("appid", id);
        ((Activity) context).startActivityForResult(installApp, APP_INSTALL);
    }
}
