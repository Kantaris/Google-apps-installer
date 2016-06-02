package com.zymmetric.installer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements TaskCompleted, NavigationView.OnNavigationItemSelectedListener {

    AppList alist;
    //private ListView rv;
    private ScrollView sv;
    public ArrayList<View> categoryViews = new ArrayList<View>();
    Map<Integer, View> itemViews = new HashMap<Integer, View>();
    Map<Integer, AppItem> appsMap;



    BroadcastReceiver installrec = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = 0;
            Uri data = intent.getData();
            String pkgName = data.getEncodedSchemeSpecificPart();
            if(appsMap != null) {
                for (Map.Entry<Integer, AppItem> entry : appsMap.entrySet()) {
                    if (pkgName.equals(entry.getValue().packageName)) {
                        entry.getValue().status = AppStatus.INSTALLED;
                        id = entry.getValue().id;
                        break;
                    }
                }
                if(DataHolder.getInstance().pendingList.size() > 0 && DataHolder.getInstance().pendingList.get(0).id == id){
                    DataHolder.getInstance().pendingList.remove(0);
                    Helper.startDownload(context);
                }

                if(itemViews != null && itemViews.get(id) != null) {
                    TextView statusItem = (TextView) itemViews.get(id).findViewById(R.id.status_text);
                    statusItem.setText("Installed");
                }


            }
        }
    };

    BroadcastReceiver pendingrec = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("id", 0);
            ArrayList<PendingItem> pendingList = DataHolder.getInstance().pendingList;
            if(appsMap != null) {
                for (Map.Entry<Integer, AppItem> entry : appsMap.get(id).depMap.entrySet()) {
                    boolean found = false;
                    for(int i = 0; i < pendingList.size(); i++){
                        if(pendingList.get(i).id == entry.getKey()){
                            found = true;
                            break;
                        }
                    }
                    if(!found){
                        PackageManager pm = getPackageManager();
                        entry.getValue().setStatus(pm);
                        if(entry.getValue().status != AppStatus.INSTALLED){
                            pendingList.add(new PendingItem(entry.getKey(), id));
                            entry.getValue().status = AppStatus.PENDING;
                            if(itemViews != null && itemViews.get(entry.getKey()) != null) {
                                TextView statusItem = (TextView) itemViews.get(id).findViewById(R.id.status_text);
                                statusItem.setText("Pending");
                            }
                        }
                    }
                }

                boolean found = false;
                for(int i = 0; i < pendingList.size(); i++){
                    if(pendingList.get(i).id == id){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    PackageManager pm = getPackageManager();
                    appsMap.get(id).setStatus(pm);
                    if(appsMap.get(id).status != AppStatus.INSTALLED){
                        pendingList.add(new PendingItem(id, id));
                        appsMap.get(id).status = AppStatus.PENDING;
                        if(itemViews != null && itemViews.get(id) != null) {
                            TextView statusItem = (TextView) itemViews.get(id).findViewById(R.id.status_text);
                            statusItem.setText("Pending");
                        }
                    }
                }
                DataHolder.getInstance().pendingList = pendingList;

                Helper.startDownload(context);
            }

        }
    };



    BroadcastReceiver rec = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            int ix = intent.getIntExtra("id", 0);
            int main = intent.getIntExtra("main", 0);
            boolean finish = intent.getBooleanExtra("finished", false);
            boolean canceled = intent.getBooleanExtra("canceled", false);
            String path = intent.getStringExtra("path");
            if(itemViews != null && itemViews.get(ix) != null){
                TextView statusItem = (TextView) itemViews.get(ix).findViewById(R.id.status_text);
                statusItem.setText("Downloading...(" +  progress + "%)" );
            }
            if(finish && DataHolder.getInstance().currentActivity == ActivityStatus.MAIN){
                Helper.startInstall(context, ix, path);
            }
            else if(canceled){
                if(DataHolder.getInstance().pendingList.size() > 0 && DataHolder.getInstance().pendingList.get(0).id == ix){
                    DataHolder.getInstance().pendingList.remove(0);
                    Helper.startDownload(context);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("GOOGLEINSTALLER");
        this.registerReceiver(rec, filter);
        IntentFilter pendingfilter = new IntentFilter("PENDINGSADDED");
        this.registerReceiver(pendingrec, pendingfilter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        registerReceiver(installrec, intentFilter);
        setContentView(R.layout.activity_main);
        //rv=(ListView)findViewById(R.id.rv);
        sv=(ScrollView)findViewById(R.id.sv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
       // rv.setLayoutManager(llm);
       // rv.setHasFixedSize(true);

        initializeData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    private void initializeData(){
        PackageManager pm = getPackageManager();
        alist = new AppList(this, pm);
        alist.downloadJson();
    }

    private void initializeAdapter(){
        appsMap = DataHolder.getInstance().appsMap;
        ArrayList<String>  categories = DataHolder.getInstance().categories;
        RelativeLayout ll = (RelativeLayout) sv.findViewById(R.id.listlayout);
        for(int index = 0; index < categories.size(); index++) {
            View itemView = getLayoutInflater().inflate(R.layout.item, null);
            itemView.setId(index + 1000);
            if(index > 0){
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                llp.addRule(RelativeLayout.BELOW, index + 999);
                llp.bottomMargin = 36;
                itemView.setLayoutParams(llp);
            }
            else {
                RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                llp.bottomMargin = 36;
                llp.topMargin = 36;
                itemView.setLayoutParams(llp);
            }
            CardView cv;
            Context mContext;
            TextView title;
            LayoutInflater inflater;
            RelativeLayout rl;
            int counter = 1;
            boolean first = true;
            categoryViews.add(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            rl = (RelativeLayout) itemView.findViewById(R.id.cardlayout);
            inflater = LayoutInflater.from(rl.getContext());
            View header = inflater.inflate(R.layout.card_header, rl, false);
            title = (TextView) header.findViewById(R.id.title2);
            header.setId(counter);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), AppListActivity.class);
                    view.getContext().startActivity(intent);
                }
            });
            rl.addView(header);
            title.setText(categories.get(index));
            for (Map.Entry<Integer, AppItem> entry : appsMap.entrySet()) {
                if (entry.getValue().groupId == index) {
                    RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    llp.addRule(RelativeLayout.BELOW, counter);
                    counter++;
                    View view = inflater.inflate(R.layout.app_item, rl, false);
                    itemViews.put(entry.getValue().id, view);
                    view.setId(counter);
                    view.setTag(entry.getValue().id);
                    TextView titleItem = (TextView) view.findViewById(R.id.title);
                    titleItem.setText(entry.getValue().displayName);
                    TextView statusItem = (TextView) view.findViewById(R.id.status_text);
                    ImageView indInstalled = (ImageView) view.findViewById(R.id.indicator_installed);
                    if (entry.getValue().status == AppStatus.INSTALLED) {
                        indInstalled.setVisibility(View.VISIBLE);
                        statusItem.setText("Installed");
                    }
                    else if (entry.getValue().status == AppStatus.UPDATEAVAILABLE) {
                        statusItem.setText("Update Available");
                    }
                    else {
                        statusItem.setText("Not Installed");
                    }
                    TextView descriptionItem = (TextView) view.findViewById(R.id.description);
                    descriptionItem.setText(entry.getValue().introduction);
                    new DownloadImageTask((ImageView) view.findViewById(R.id.thumbnail))
                            .execute(entry.getValue());
                    //imageItem.setImageBitmap(alist.apps.get(i).iconBitmap);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(view.getContext(), AppDetailsActivity.class);
                            intent.putExtra("id", (int) view.getTag());
                            startActivityForResult(intent, Helper.APP_DETAILS);

                        }
                    });
                    view.setLayoutParams(llp);
                    rl.addView(view);
                }
            }
            ll.addView(itemView);
        }

        //addItem(0, ll);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == Helper.APP_DETAILS) {
            appsMap = DataHolder.getInstance().appsMap;
            if (appsMap != null) {
                for (Map.Entry<Integer, AppItem> entry : appsMap.entrySet()) {
                    if(itemViews.get(entry.getValue().id) != null) {
                        TextView statusItem = (TextView) itemViews.get(entry.getValue().id).findViewById(R.id.status_text);
                        if (entry.getValue().status == AppStatus.NOTINSTALLED) {
                            statusItem.setText("Not Installed");
                        }
                        if (entry.getValue().status == AppStatus.PENDING) {
                            statusItem.setText("Pending");
                        }
                        if (entry.getValue().status == AppStatus.DOWNLOADING) {
                            statusItem.setText("Downloading");
                        }
                    }
                }
            }
        }
        else if(requestCode == Helper.APP_INSTALL){
            if(DataHolder.getInstance().pendingList.size() > 0 && DataHolder.getInstance().pendingList.get(0).id == data.getIntExtra("appid", 0)){
                DataHolder.getInstance().pendingList.remove(0);
                Helper.startDownload(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(rec);
        unregisterReceiver(installrec);
        unregisterReceiver(pendingrec);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void downloadJsonCompleted(String json) {

        DataHolder.getInstance().appsMap = alist.appsMap;
        DataHolder.getInstance().categories = alist.categories;
        initializeAdapter();
    }
}
