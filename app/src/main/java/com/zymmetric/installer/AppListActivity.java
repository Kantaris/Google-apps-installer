package com.zymmetric.installer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

/**
 * Created by chris on 3/2/16.
 */
public class AppListActivity extends AppCompatActivity {

    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.d_activity);
        lv=(ListView)findViewById(R.id.listView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppItem[] appItems = new AppItem[2];
      /*  Bitmap emb = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.emma);
        appItems[0] = new AppItem("Google Play", "Google is Awesome", emb);
        Bitmap lav = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.lavery);
        appItems[1] = new AppItem("Google Services", "Google is Awesome", lav);*/
        LVAdapter adapter = new LVAdapter(this, R.layout.list_item, appItems);
        lv.setAdapter(adapter);
    }
}
