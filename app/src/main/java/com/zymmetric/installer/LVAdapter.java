package com.zymmetric.installer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by chris on 2/23/16.
 */
public class LVAdapter extends ArrayAdapter<AppItem> {

    Context mContext;
    int layoutResourceId;
    AppItem data[] = null;

    public LVAdapter(Context mContext, int layoutResourceId, AppItem[] data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }


    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public AppItem getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            // inflate the layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(layoutResourceId, viewGroup, false);
        }
        // object item based on the position
        AppItem objectItem = data[i];

        TextView titleItem = (TextView) view.findViewById(R.id.lititle);
        titleItem.setText(objectItem.displayName);
        TextView descriptionItem = (TextView) view.findViewById(R.id.info_view);
        descriptionItem.setText(objectItem.introduction);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AppDetailsActivity.class);
                mContext.startActivity(intent);
            }
        });
        //textViewItem.setTag(objectItem.itemId);
        return view;

    }



}
