package com.traveller.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.traveller.android.utils.LoaderClass;
import com.traveller.android.utils.LoginSharedPref;
import com.traveller.android.utils.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, CompoundButton.OnCheckedChangeListener {
    private static final int RCODE_LOC = 10;
    private static final long INTERVAL_ = 10_000;
    private static final long FAST_INTERVAL_ = 5_000;
    private static final int RCODE_RES_CURRENTLOC = 100;
    private static final int RCODE_REFRESH = 102;
    private static final String TAG = "TAG";
    private static final int KM = 1000;
    private TextView toTV, fromTV;
    private int from, to;
    private Calendar dateCal, timeCal, stimeCal, etimeCal;
    private SimpleDateFormat simpleFormat, timesimpleFormat;
    private MaterialCheckBox cb1, cb2, cb3, cb4, cb5, cb6;
    private ScrollView sview;
    private SwitchMaterial switchFood;
    private static String placelatlongSelected;
    private GoogleMap gMaps;
    private SearchView searchView;
    private SearchManager searchMgr;

    private static double RADIUSkm = 50;
    private static final int ZOOM_DEFAULT_LEVEL_ = 8;
    private int hours;
    private LatLng latLng;
    private FusedLocationProviderClient fusedLocationClient;
    private Dialog dView;
    private View dview;
    private Button btnClose;
    private boolean isFood;
    private String types;
    private String[] areas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoaderClass.startAnimation_(this);
        initUI();
        initDataObj();
        initDialog();
      //Log.i(TAG, "onPlanClicked: " + types);
    }

    private void initUI() {
        fromTV = findViewById(R.id.tv_from);
        toTV = findViewById(R.id.tv_to);
        fromTV.setOnClickListener(this);
        toTV.setOnClickListener(this);
        sview = findViewById(R.id.sv);
        switchFood = findViewById(R.id.foodplaces);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.searchview);
        searchMgr = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchMgr.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //String query = searchView.getQuery().toString();
              //Log.i(TAG, "onClick: query input" + s);
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);
                List<Address> address;
                try {
                    address = (List<Address>) geocoder.getFromLocationName(s, 1);
                    if (address != null && address.size() > 0) {
                        setMap(new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude()));
                      //Log.i(TAG, "onClick: " + address.get(0).getLocality());
                    } else
                        Snackbar.make(sview, "Location not found", Snackbar.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e.getMessage().equals("grpc failed"))
                        Snackbar.make(sview, "Make sure you have active internet connection", Snackbar.LENGTH_LONG).show();
                }
                hideKeyBoard(searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void hideKeyBoard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_hist) {
            Intent i = new Intent(MainActivity.this, HistActivity.class);
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.mi_prof) {
            gotoReg();
            return true;
        } else if (item.getItemId() == R.id.mi_chat) {
            gotoChatBot();
            return true;
        } else if (item.getItemId() == R.id.mi_logout) {
            LoginSharedPref.clear(MainActivity.this);
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotoReg() {
        Intent i = new Intent(MainActivity.this, ProfileOrRegActivity.class);
        i.putExtra("REG", false);
        startActivity(i);
    }

    private void gotoPlanDetail() {
        if (placelatlongSelected == null || placelatlongSelected.isEmpty()) {
            Snackbar.make(sview, "Please select a location on map to plan trip", Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(MainActivity.this, PlanDetailActivity.class);
        i.putExtra("isFood", isFood);
        i.putExtra("location", placelatlongSelected);
        i.putExtra("types", types);
        i.putExtra("hours", hours);
        i.putExtra("from", fromTV.getText().toString());
        i.putExtra("to", toTV.getText().toString());
        i.putExtra("RADIUS", (int) RADIUSkm);
        if (areas != null && areas.length > 1) {
            i.putExtra("area", areas[1]);
        }
        if (areas != null && areas.length > 0) {
            i.putExtra("placeName", areas[0]);
        }
        startActivityForResult(i, RCODE_REFRESH);
    }

    private void gotoChatBot() {
        if (LoginSharedPref.getLATKey(MainActivity.this).isEmpty()) {
            Snackbar.make(sview, "Make sure your location is turned on, detecting your current location", Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(MainActivity.this, ChatActivity.class);
        i.putExtra("location", placelatlongSelected);
        if (areas != null && areas.length > 1) {
            i.putExtra("area", areas[1]);
        }
        if (areas != null && areas.length > 0) {
            i.putExtra("placeName", areas[0]);
        }
        startActivity(i);
    }

    private String[] getArea() {
        if (latLng == null)
            return null;
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> address;
        try {
            address = (List<Address>) geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (address != null && address.size() > 0) {
                areas = new String[2];
                areas[0] = address.get(0).getLocality();
                areas[1] = address.get(0).getAddressLine(0);
              //Log.i(TAG, "onClick: " + address.get(0).getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return areas;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_to) {
            etimePicker();
        } else if (id == R.id.tv_from) {
            stimePicker();
        } else if (id == R.id.btn_close) {
            if (dView != null && dView.isShowing()) dView.dismiss();
            hideKeyBoard(sview);
        }
    }

    public void onPlanClicked(View view) {
        if (valid()) {
          //Log.i(TAG, "onPlanClicked: " + (areas != null ? areas[0] : ""));
            gotoPlanDetail();
        }
    }

    private boolean valid() {
        if (from == 0) {
            Snackbar.make(sview, "Please select starting time", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (to == 0) {
            Snackbar.make(sview, "Please select end time", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        hours = (int) Util.tripHour(stimeCal.getTime(), etimeCal.getTime());
      //Log.i(TAG, "valid: " + hours);
        if (hours == -1) {
            Snackbar.make(sview, "Please select valid time", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (hours > 23) {
            Snackbar.make(sview, "Please select time less than a day", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (hours < 4) {
            Snackbar.make(sview, "Please select time at least of 4 hours to plan trip", Snackbar.LENGTH_LONG).show();
            return false;
        }
        radiusCalcu();
        if (placelatlongSelected == null || placelatlongSelected.isEmpty()) {
            Snackbar.make(sview, "Please select place for planning trip", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (switchFood.isChecked()) {
            isFood = true;
        }
        if (cb6 != null) {
            if (!cb6.isChecked() && !cb5.isChecked() && !cb4.isChecked() && !cb3.isChecked() && !cb2.isChecked() && !cb1.isChecked()) {
                Snackbar.make(sview, "Please Select at least one place to visit", Snackbar.LENGTH_SHORT).show();
                return false;
            }
            types = "";
            if (cb1.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb1.getText().toString());
                } else
                    types = types + "," + formatType(cb1.getText().toString());
            }
            if (cb2.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb2.getText().toString());
                } else
                    types = types + "," + formatType(cb2.getText().toString());
            }
            if (cb3.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb3.getText().toString());
                } else
                    types = types + "," + formatType(cb3.getText().toString());
            }
            if (cb4.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb4.getText().toString());
                } else
                    types = types + "," + formatType(cb4.getText().toString());
            }
            if (cb5.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb5.getText().toString());
                } else
                    types = types + "," + formatType(cb5.getText().toString());
            }
            if (cb6.isChecked()) {
                if (types.isEmpty()) {
                    types = formatType(cb6.getText().toString());
                } else
                    types = types + "," + formatType(cb6.getText().toString());
            }

        }
        return true;
    }

    private String formatType(String type) {
        switch (type) {
            case "historical place":
                return "historical place";
            case "ancient temple":
                return "ancient temple";
            case "garden":
                return "garden";
            case "theme park":
                return "amusement_park";
            case "zoo":
                return "zoo";
            case "mall":
                return "shopping_mall";
        }
        return type;
    }

    private void radiusCalcu() {
        if (hours < 6) {
            RADIUSkm = 100 * KM;
        } else if (hours < 10) {
            RADIUSkm = 300 * KM;
        } else if (hours < 15) {
            RADIUSkm = 600 * KM;
        } else {
            RADIUSkm = 1000 * KM;
        }
    }

    private void stimePicker() {
        TimePickerDialog pickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                stimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                from = hourOfDay;
                stimeCal.set(Calendar.MINUTE, minute);
                fromTV.setText(timesimpleFormat.format(stimeCal.getTime()));
                radiusCalcu();
            }
        }, stimeCal.get(Calendar.HOUR_OF_DAY), stimeCal.get(Calendar.MINUTE), true);
        pickerDialog.show();
    }

    private void etimePicker() {
        TimePickerDialog pickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                etimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                etimeCal.set(Calendar.MINUTE, minute);
                to = hourOfDay;
                radiusCalcu();
                toTV.setText(timesimpleFormat.format(etimeCal.getTime()));
            }
        }, etimeCal.get(Calendar.HOUR_OF_DAY), etimeCal.get(Calendar.MINUTE), true);
        pickerDialog.show();
    }

    private void initDataObj() {
        timeCal = Calendar.getInstance();
        stimeCal = Calendar.getInstance();
        etimeCal = Calendar.getInstance();
        dateCal = Calendar.getInstance();
        simpleFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        timesimpleFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gMaps = googleMap;
      //Log.i(TAG, "onMapReady: " + googleMap);
        LoaderClass.stopAnimation();
        if (gMaps != null) {
          //Log.d(TAG, "onMapReady: " + gMaps + "," + latLng);
            setMap(latLng);
            //gMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_DEFAULT_LEVEL_));
        }
        gMaps.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
              //Log.d(TAG, "onMapLongClick: " + latLng.latitude + " , " + latLng.longitude);
                setMap(latLng);
            }
        });
        gMaps.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng = marker.getPosition();
              //Log.d(TAG, "onMarkerDragEnd: " + latLng.latitude + " , " + latLng.longitude);
                setMap(latLng);
            }
        });
    }

    private void setMap(LatLng latLng) {
        if (latLng == null) return;
        gMaps.clear();
        //for edit
        this.latLng = latLng;
        placelatlongSelected = latLng.latitude + "," + latLng.longitude;
        areas = getArea();
        MarkerOptions mo = new MarkerOptions();
        mo.position(latLng);
        if (areas != null && areas.length > 0)
            mo.title(areas[0]);
        mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mo.draggable(true);
        Marker marker = gMaps.addMarker(mo);
        marker.showInfoWindow();
      //Log.d(TAG, "onMapLongClick: " + latLng.latitude + " , " + latLng.longitude);
/*
        gMaps.addCircle(new CircleOptions()
                .center(latLng)
                .radius(RADIUSkm)
                .fillColor(Color.TRANSPARENT)
                .strokeWidth(2).strokeColor(Color.LTGRAY)
        );
*/
        gMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_DEFAULT_LEVEL_));
    }

    private void permissionInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //dynamic location permission
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //ask for permission since not granted

                requestPerm();
              //Log.d(TAG, "onCreate: shouldShowReq else");

            } else {
              //Log.d(TAG, "onCreate> persmissions granted");
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                requestLocUpdates();
            }
        } else {
            //call directly for lessthan Marsh
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            requestLocUpdates();
        }
    }

    private void requestLocUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Log.i(TAG, "requestLocUpdates: " + fusedLocationClient.getLastLocation());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            latLng = new LatLng(lat, lng);
                            LoginSharedPref.setLATKey(MainActivity.this, lat+","+lng);
                          //Log.d(TAG, "onSuccess() called with: location = [" + location + "]");
                            if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                                LoaderClass.stopAnimation();
                                setMap(latLng);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RCODE_RES_CURRENTLOC) {
            if (resultCode == RESULT_OK) {
                callLocationReq();
            } else if (resultCode == RESULT_CANCELED) {
              //Log.i(TAG, "onActivityResult: GPS denied");
                requestPerm();
            }
        } else if (requestCode == RCODE_REFRESH) {
            if (resultCode == RESULT_OK) {
                initRefresh();
            }
        }
    }

    private void initRefresh() {
        initDataObj();
        initDialog();
        fromTV.setText("FROM");
        toTV.setText("TO");
    }

    private void callLocationReq() {
        requestLocUpdates();
    }


    private void requestPerm() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION}, RCODE_LOC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionInit();
    }

    public void onDialogClicked(View view) {
        if (from == 0 || to == 0) {
            Snackbar.make(sview, "Please select time", Snackbar.LENGTH_LONG).show();
            return;
        }
        initDialog();
        if (dView.isShowing()) dView.dismiss();
        hours = (int) Util.tripHour(stimeCal.getTime(), etimeCal.getTime());
      //Log.i(TAG, "onDialogClicked: " + hours);
        if (hours != 0 && hours < Util.AMUSEPARK_THRESHOLD) cb4.setChecked(false);
        else cb4.setChecked(true);
        dView.show();
    }

    private void initDialog() {
        if (dView == null) {
            dView = new Dialog(this);
            dview = getLayoutInflater().inflate(R.layout.includeplaces, null);
            cb1 = dview.findViewById(R.id.c1);
            cb2 = dview.findViewById(R.id.c2);
            cb3 = dview.findViewById(R.id.c3);
            cb4 = dview.findViewById(R.id.c4);
            cb5 = dview.findViewById(R.id.c5);
            cb6 = dview.findViewById(R.id.c6);
            cb6.setOnCheckedChangeListener(this);
            btnClose = dview.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(this);
            dView.setContentView(dview);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.c6 && isChecked) {
            if (hours != 0 && hours < Util.AMUSEPARK_THRESHOLD) {
                buttonView.setChecked(false);
            }
        }
    }
}