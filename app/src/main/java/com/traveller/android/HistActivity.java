package com.traveller.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.traveller.android.adap.HistAdap;
import com.traveller.android.db.Placee;
import com.traveller.android.db.Trip;
import com.traveller.android.db.TripDatabase;
import com.traveller.android.db.TripStore;
import com.traveller.android.utils.LoginSharedPref;

import org.parceler.Parcels;

import java.util.List;

public class HistActivity extends AppCompatActivity {

    private ListView listView;

    private Trip pojo;
    private List<Trip> trips;
    private List<Placee> placees;
    private HistAdap adap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hist);
        listView = findViewById(R.id.lv);
        getSavedTrips trips = new getSavedTrips(this);
        trips.execute();
    }

    public void onTripClicked(Trip trip) {
        LoadClickedTripTask task = new LoadClickedTripTask(this, trip);
        task.execute();
    }

    //todo save/view saved trip plan
    private class LoadClickedTripTask extends AsyncTask<Void, Boolean, Boolean> {
        private Context context;
        private Trip trip;

        public LoadClickedTripTask(Context context, Trip trip) {
            this.context = context;
            this.trip = trip;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // LoaderClass.startAnimation(MainActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TripStore store = TripDatabase.get(context).tripStore();
            int id = trip.id;
            placees = store.selectAllPlaces(id);   //int no of rows updated
           // Log.i("TAG", "doInBackground: for trip id:" + id + ";" + (placees != null ? placees.size() : "null placees") + "trip:" + store.findById(id).areaName);
            return (placees != null || placees.size() != 0);
        }

        @Override
        protected void onPostExecute(Boolean reply) {
            super.onPostExecute(reply);
            //  stopAnimation();
            if (!reply) {
                Toast.makeText(context, ("No placees in saved trip"), Toast.LENGTH_SHORT).show();
            } else {
               gotoPlanDetail(trip);
            }
        }
    }

    private void gotoPlanDetail(Trip trip) {
        Intent i = new Intent(HistActivity.this, PlanDetailActivity.class);
        i.putExtra("POJO", Parcels.wrap(placees));
        i.putExtra("trip", trip);
        startActivity(i);
    }

    private class getSavedTrips extends AsyncTask<Void, Boolean, Boolean> {
        private Context context;

        public getSavedTrips(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // LoaderClass.startAnimation(MainActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TripStore store = TripDatabase.get(context).tripStore();
            trips = store.selectAll(LoginSharedPref.getEmailIDKey(HistActivity.this));
          //Log.i("TAG", "doInBackground: "+trips.size());
            return (trips != null && trips.size() > 0);
        }

        @Override
        protected void onPostExecute(Boolean reply) {
            super.onPostExecute(reply);
            //  stopAnimation();
          //Log.d("TAG", "onPostExecute() called with: reply = [" + reply + "]");
            if (!reply) {
                Toast.makeText(context, "No saved trips", Toast.LENGTH_SHORT).show();
                findViewById(R.id.tv_nodata).setVisibility(View.VISIBLE);
                listView.setAdapter(null);
                listView.setVisibility(View.GONE);
            } else {
                setAdap();
            }
        }
    }

    private void setAdap() {
        adap = new HistAdap(HistActivity.this, HistActivity.this, trips);
        listView.setAdapter(adap);
        listView.setVisibility(View.VISIBLE);
        findViewById(R.id.tv_nodata).setVisibility(View.GONE);
    }
}