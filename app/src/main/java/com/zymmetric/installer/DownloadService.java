package com.zymmetric.installer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadService extends IntentService {
    Integer notificationID = 100;

    public DownloadService() {
        super("DownloadService");
    }
    boolean stop = false;
    public class StopReceiver extends BroadcastReceiver {

        public static final String ACTION_STOP = "STOPDOWNLOAD";

        @Override
        public void onReceive(Context context, Intent intent) {
            stop = true;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DataHolder.getInstance().serviceIsRunning = true;
        IntentFilter filter = new IntentFilter(StopReceiver.ACTION_STOP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        StopReceiver receiver = new StopReceiver();
        registerReceiver(receiver, filter);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
//Set notification information:
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setContentTitle("Google Installer")
                .setContentText("Downloading...")
                .setProgress(100, 0, false);


//Send the notification:
        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationID, notification);
        //Update notification information:
        int id = intent.getIntExtra("id", 0);
        String name = intent.getStringExtra("name");
        String path = intent.getStringExtra("path");
        String link = intent.getStringExtra("link");
        String main = intent.getStringExtra("main");
        //ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");

        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(path + ".apk");

            byte data[] = new byte[1024 * 8];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1 && !stop) {
                total += count;
                // publishing the progress....
                Intent in = new Intent("GOOGLEINSTALLER");
                in.putExtra("progress", (int) (total * 100 / fileLength));
                in.putExtra("id", id);
                in.putExtra("main", main);
                in.putExtra("finished", false);
                in.putExtra("canceled", false);
                in.putExtra("path", path + ".apk");
                sendBroadcast(in);
                notificationBuilder.setProgress(100, (int) (total * 100 / fileLength), false);
                notificationBuilder.setContentTitle(name);
                notificationBuilder.setContentText("Downloading...(" + (int) (total * 100 / fileLength) + "%)");
                //Send the notification:
                notification = notificationBuilder.build();
                notificationManager.notify(notificationID, notification);

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

      /*  if(!stop) {
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setDataAndType(Uri.fromFile(new File(paths.get(i) + ".apk")), "application/vnd.android.package-archive");
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
        }*/
        Intent in = new Intent("GOOGLEINSTALLER");
        in.putExtra("progress", 100);
        in.putExtra("id", id);
        in.putExtra("main", main);
        in.putExtra("finished", !stop);
        in.putExtra("canceled", stop);
        in.putExtra("path", path + ".apk");
        sendBroadcast(in);
        notificationManager.cancel(notificationID);

        unregisterReceiver(receiver);
        DataHolder.getInstance().serviceIsRunning = false;
        //receiver.send(UPDATE_PROGRESS, resultData);
    }
}

