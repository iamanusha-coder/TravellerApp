package com.traveller.android.adap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.traveller.android.HistActivity;
import com.traveller.android.R;
import com.traveller.android.db.Trip;

import java.util.ArrayList;
import java.util.List;

public class HistAdap extends BaseAdapter {
    private Context context;
    private HistActivity activity;
    private List<Trip> pojos;
    private TextView name;

    public HistAdap(Context context, HistActivity activity, List<Trip> pojos) {
        this.context = context;
        this.activity = activity;
        this.pojos = pojos;
    }

    @Override
    public int getCount() {
        return pojos.size();
    }

    @Override
    public Object getItem(int position) {
        return pojos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_lv, parent, false);
            name = convertView.findViewById(R.id.tv_name);
        }
        final Trip pojo = pojos.get(position);
        name.setText(pojo.toString());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    activity.onTripClicked(pojo);
                }
            }
        });
        return convertView;
    }
}
