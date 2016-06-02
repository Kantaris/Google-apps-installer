package com.zymmetric.installer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chris on 3/21/16.
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {

    DownloadCompleted dc;
    int index;
    String title;
    TextView statusView;
    ProgressBar pBar;
    String packageName;
    String cacheDir;
    MenuItem item;

    public DownloadTask(DownloadCompleted dc, int index, String title, String packageName, String cacheDir, TextView statusView, ProgressBar pBar, MenuItem menuItem) {
        this.dc = dc;
        this.index = index;
        this.title = title;
        this.cacheDir = cacheDir;
        this.packageName = packageName;
        this.statusView = statusView;
        this.pBar = pBar;
        this.item = item;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(cacheDir + "/" + packageName);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                /*if (isCancelled()) {
                    input.close();
                    return null;
                }*/
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        pBar.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        dc.downloadCompleted(index, result);
    }
}