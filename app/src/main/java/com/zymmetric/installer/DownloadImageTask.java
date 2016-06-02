package com.zymmetric.installer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<AppItem, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(AppItem... items) {
        String urldisplay = "http://file.market.xiaomi.com/mfc/thumbnail/png/w164q80/" + items[0].iconUrl;
        Bitmap mIcon11 = null;
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            bitmap = Bitmap.createBitmap(mIcon11.getHeight() + (int) (mIcon11.getHeight() / 2), mIcon11.getHeight() + (int) (mIcon11.getHeight() / 2), Bitmap.Config.ARGB_8888);
            // bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            Canvas canvas = new Canvas(bitmap);
            //bmImage.setImageResource(android.R.color.transparent);

            Paint myPaint = new Paint();
           /* if(items[0].installed){
                if(items[0].updateAvailable){
                    myPaint.setColor(Color.parseColor("#ffdfd1d1"));
                }
                else{
                    myPaint.setColor(Color.parseColor("#208420"));
                }
            }
            else {*/
                myPaint.setColor(Color.parseColor("#ffcfd8dc")); //Color.rgb(190, 190, 190));
            //}
            canvas.drawRect(0, 0, mIcon11.getHeight() + (int) (mIcon11.getHeight() / 2), mIcon11.getHeight() + (int) (mIcon11.getHeight() / 2), myPaint);

            canvas.drawBitmap(mIcon11, (int) (mIcon11.getHeight() / 4), (int) (mIcon11.getHeight() / 4), myPaint);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        items[0].iconBitmap = bitmap;
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {

        //bmImage.draw(canvas);
        bmImage.setImageBitmap(result);
    }
}