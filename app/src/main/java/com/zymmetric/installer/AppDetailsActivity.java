package com.zymmetric.installer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Map;

/**
 * Created by chris on 2/24/16.
 */
public class AppDetailsActivity  extends AppCompatActivity implements TaskCompleted, ObservableScrollView.Callbacks{

    ViewPager viewPager;
    //ImageView mPhotoView;
    private int mPhotoHeightPixels;
    private View mHeaderBox;
    private int mHeaderHeightPixels;
    private boolean mHasPhoto;
    private ObservableScrollView mScrollView;
    private View mPhotoViewContainer;
    private View mDetailsContainer;
    private float mMaxHeaderElevation;

    ProgressBar pBar;
    TextView statusText;
    RelativeLayout downloadLayout;
    ImageButton cancelButton;
    MenuItem mi;
    int oldProgress = 0;
    Map<Integer, AppItem> appsMap;
    int id;
    String cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    static final int APP_INSTALL = 2;
    private static final float PHOTO_ASPECT_RATIO = 0.5f;

    BroadcastReceiver installrec = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            String pkgName = data.getEncodedSchemeSpecificPart();

            if (pkgName.equals(appsMap.get(id).packageName)) {
                downloadLayout.setVisibility(View.GONE);
                if(mi != null) {
                    mi.setTitle("Launch");
                }
            }
        }
    };

    BroadcastReceiver rec = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(appsMap == null){
                appsMap = DataHolder.getInstance().appsMap;
            }
            int progress = intent.getIntExtra("progress", 0);
            int ix = intent.getIntExtra("id", 0);
            int main = intent.getIntExtra("main", 0);
            boolean finish = intent.getBooleanExtra("finished", false);
            boolean canceled = intent.getBooleanExtra("canceled", false);

            if(oldProgress != progress) {
                pBar.setProgress(progress);
                //if main = ix
                statusText.setText("Downloading " + appsMap.get(ix).displayName + "... (" + progress + "%)");
                if (mi != null) {
                    if(!mi.getTitle().toString().equals("Downloading..."))
                        mi.setTitle("Downloading..."); //(" + progress + "%)");
                }
            }
            oldProgress = progress;
            if(canceled){
                downloadLayout.setVisibility(View.GONE);
                if(mi != null) {
                    mi.setTitle("Install");
                }
            }
            else if(finish && DataHolder.getInstance().currentActivity == ActivityStatus.DETAILS){

                if(mi != null) {
                    mi.setTitle("Installing...");
                }


            }
            else {
                if(downloadLayout.getVisibility() == View.GONE) {
                    downloadLayout.setVisibility(View.VISIBLE);
                }
            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.details_activity);
       // PackageManager pm = getPackageManager();

        /*AppDetailsFragment details = new AppDetailsFragment();
        //details.setArguments(getIntent().getExtras());

        //There is no layout xml for DetailActivity, we add the fragment programmatically to the activity.
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();
       // fragTransaction.add(R.layout.app_details, details);
       // fragTransaction.addToBackStack(null);
        fragTransaction.commit();
       /* setContentView(R.layout.app_details);
        ImageView imageItem = (ImageView) findViewById(R.id.session_photo);
        Bitmap emb = BitmapFactory.decodeResource(getResources(),
                R.drawable.emma);
        imageItem.setImageBitmap(emb);*/

        PackageManager pm = getPackageManager();
        IntentFilter filter = new IntentFilter("GOOGLEINSTALLER");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        registerReceiver(installrec, intentFilter);
        registerReceiver(rec, filter);
        if(appsMap == null){
            appsMap = DataHolder.getInstance().appsMap;
        }
        id = getIntent().getIntExtra("id", 0);
        setContentView(R.layout.app_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        statusText = (TextView) findViewById(R.id.download_text);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        //setHasOptionsMenu(true);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.addCallbacks(this);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        statusText = (TextView) findViewById(R.id.download_text);
        downloadLayout = (RelativeLayout) findViewById(R.id.download_layout);
        cancelButton = (ImageButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent("STOPDOWNLOAD");
                v.getContext().sendBroadcast(in);

            }
        });
        ViewTreeObserver vto = mScrollView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        // mPhotoView = (ImageView) appd.findViewById(R.id.session_photo);
        mHeaderBox = findViewById(R.id.header_session);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mDetailsContainer = findViewById(R.id.details_container);
        mPhotoViewContainer = findViewById(R.id.session_photo_container);
        TextView title = (TextView) findViewById(R.id.session_title);
        TextView summary = (TextView) findViewById(R.id.session_abstract);
        title.setText(appsMap.get(id).displayName);
        summary.setText(appsMap.get(id).introduction);
        mMaxHeaderElevation = getResources().getDimensionPixelSize(
                R.dimen.session_detail_max_header_elevation);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        ImageAdapter adapter = new ImageAdapter(this);
        viewPager.setAdapter(adapter);
        //mScrollView.addCallbacks(this);
        // Bitmap emb = BitmapFactory.decodeResource(getResources(),
        //       R.drawable.emma);
        // new DownloadImageTask(mPhotoView).execute(aList.apps.get(index));
        mHasPhoto = true;
        //mHeaderBox.setTranslationY(100);
        recomputePhotoAndScrollingMetrics();

    }

    public void installApp(MenuItem item){
        item.setTitle("Connecting...");
        String cacheDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(); //appd.getContext().getCacheDir().getAbsolutePath();

        statusText.setText("Connecting...");
        pBar.setProgress(0);

        Intent intent = new Intent("PENDINGSADDED");
        intent.putExtra("id", id);
        sendBroadcast(intent);

    }

    public void updateApp(MenuItem item){
        item.setTitle("Connecting...)");
    }
    public void launchApp(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.session_detail, menu);
        mi = (MenuItem)menu.findItem(R.id.action_install);
        //SpannableString s = new SpannableString("My red MenuItem");
        //s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        //mi.setTitle(s);
        mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals("Install")) {
                    installApp(item);
                } else if (item.getTitle().equals("Update")) {
                    updateApp(item);
                } else if (item.getTitle().equals("Launch")) {
                    launchApp();
                }

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
        //tryExecuteDeferredUiOperations();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // ((AppCompatActivity) getActivity()).unregisterReceiver(rec);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener
            = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            //mAddScheduleButtonContainerHeightPixels = mAddScheduleButtonContainer.getHeight();
            recomputePhotoAndScrollingMetrics();
        }
    };

    private void recomputePhotoAndScrollingMetrics() {
        mHeaderHeightPixels = mHeaderBox.getHeight();

        mPhotoHeightPixels = 0;
        if (mHasPhoto) {
            mPhotoHeightPixels = (int) (viewPager.getWidth() / PHOTO_ASPECT_RATIO);
            mPhotoHeightPixels = Math.min(mPhotoHeightPixels, mScrollView.getHeight() * 2 / 3);
        }

        ViewGroup.LayoutParams lp;
        lp = mPhotoViewContainer.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            mPhotoViewContainer.setLayoutParams(lp);
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mDetailsContainer.getLayoutParams();
        if (mlp.topMargin != mHeaderHeightPixels + mPhotoHeightPixels) {
            mlp.topMargin = mHeaderHeightPixels  + mPhotoHeightPixels;
            mDetailsContainer.setLayoutParams(mlp);
            mScrollView.setScrollY((mPhotoHeightPixels / 2));
        }

        onScrollChanged(0, 0); // trigger scroll handling
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        // Reposition the header bar -- it's normally anchored to the top of the content,
        // but locks to the top of the screen on scroll
        int scrollY = mScrollView.getScrollY();

        float newTop = Math.max(mPhotoHeightPixels, scrollY);
        mHeaderBox.setTranslationY(newTop);
        // mAddScheduleButtonContainer.setTranslationY(newTop + mHeaderHeightPixels
        //       - mAddScheduleButtonContainerHeightPixels / 2);

        float gapFillProgress = 1;
        if (mPhotoHeightPixels != 0) {
            gapFillProgress = Math.min(Math.max(UIUtils.getProgress(scrollY,
                    0,
                    mPhotoHeightPixels), 0), 1);
        }

        ViewCompat.setElevation(mHeaderBox, gapFillProgress * mMaxHeaderElevation);
        //ViewCompat.setElevation(mAddScheduleButtonContainer, gapFillProgress * mMaxHeaderElevation
        //      + mFABElevation);
        //ViewCompat.setElevation(mAddScheduleButton, gapFillProgress * mMaxHeaderElevation
        //      + mFABElevation);

        // Move background photo (parallax effect)
        mPhotoViewContainer.setTranslationY(scrollY * 0.5f);
    }

    @Override
    public void downloadJsonCompleted(String json) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == APP_INSTALL){
           // if(DataHolder.getInstance().pendingList.size() > 0 && DataHolder.getInstance().pendingList.get(0).id == installId){
             //   DataHolder.getInstance().pendingList.remove(0);
               //startDownload();
            //}
        }
    }


}