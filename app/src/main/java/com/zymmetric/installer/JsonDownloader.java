package com.zymmetric.installer;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by chris on 3/7/16.
 */
public class JsonDownloader extends AsyncTask<String, Integer, String> {

    TaskCompleted tc;
    public JsonDownloader(TaskCompleted tc){
        this.tc = tc;
    }

    protected String doInBackground(String... urls) {
        try{
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setRequestProperty("Content-type", "application/json");
            InputStream in = null;
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                return sb.toString();
            }
            finally {
                urlConnection.disconnect();
                if(in != null)in.close();
            }
        } catch (Exception e) {
            // Oops
        }
        finally {
            try{}catch(Exception squish){}
        }
        return "";
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(String result) {
        tc.downloadJsonCompleted(result);
    }
}
