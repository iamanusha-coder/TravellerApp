package com.traveller.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.traveller.android.db.Location;
import com.traveller.android.db.Person;
import com.traveller.android.db.PersonStore;
import com.traveller.android.db.Placee;
import com.traveller.android.db.Trip;
import com.traveller.android.db.TripDatabase;
import com.traveller.android.db.TripStore;
import com.traveller.android.retrofit.APIClient;
import com.traveller.android.retrofit.APIInterface;
import com.traveller.android.retrofit.PlacesN;
import com.traveller.android.retrofit.PlacesT;
import com.traveller.android.utils.LoaderClass;
import com.traveller.android.utils.LoginSharedPref;
import com.traveller.android.utils.Util;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.traveller.android.utils.LoaderClass.stopAnimation;

public class PlanDetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private static final String TAG = "TAG";
    private static final float ZOOM_ = 12;
    private boolean isFood, isNearbySearch, isRecursive, isFoodFINAL, makePlan;
    private String placelatlongSelected, typesComma;
    private int hours, recurcount, planCount;
    private int RADIUSKm;
    private String[] type;
    private List<Object> placesN;
    private List<Placee> placesFood, placesAmuse;
    private List<Placee> plan1, plan2, plan3;
    private boolean isPlan1Saved, isPlan2Saved, isPlan3Saved;
    private List<PlacesT.ResultsEntity> placesTFood;
    private List<PlacesT.ResultsEntity> placesTAmuse;
    private String typeSelected, from, to, area, placeName;
    private int TopN;

    //save to db
    private Trip pojo;
    private List<Placee> placees;
    private android.location.Location location;
    private String textSearch;
    private Person personObj;
    private int maxIndexA, maxIndexF, maxIndexP;
    private int countFood;
    private int planAmuse;
    private List<Placee> selectedPlan;
    private Intent backIntent;
    private final int FOOD_INTERVAL_HR = 5;
    private GoogleMap mMap;
    private MenuItem viewPlanMI;
    private Dialog dialogPlans;
    private View dview;
    private TextView tvPlan2, tvPlan1, tvPlan3;
    private Button btnSave1, btnSave2, btnSave3;
    private TextView placeTitle;
    private int move;
    private ImageButton nextPlaceIB, prevPlaceIB;
    private ArrayList<Marker> markerList;
    private boolean onlyDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);
        initUI();
        if (getIntent().getSerializableExtra("trip") != null) {
            pojo = (Trip) getIntent().getSerializableExtra("trip");
            selectedPlan = Parcels.unwrap(getIntent().getParcelableExtra("POJO"));
            onlyDisplay = true;
        } else {
            isFood = getIntent().getBooleanExtra("isFood", false);
            isFoodFINAL = isFood;
            isRecursive = true;
            placelatlongSelected = getIntent().getStringExtra("location");
            String[] ltlng = placelatlongSelected.split(",");
            location = new android.location.Location("");
            location.setLatitude(Double.parseDouble(ltlng[0]));
            location.setLongitude(Double.parseDouble(ltlng[1]));
            from = getIntent().getStringExtra("from");
            to = getIntent().getStringExtra("to");
            area = getIntent().getStringExtra("area");
            placeName = getIntent().getStringExtra("placeName");
            typesComma = getIntent().getStringExtra("types");
            if (typesComma != null) {
                type = typesComma.split(",");
            }
            hours = getIntent().getIntExtra("hours", 6);
            RADIUSKm = getIntent().getIntExtra("RADIUS", 100_000);
        }
        if (!onlyDisplay) {
            whichApi();
            LoaderClass.startAnimation_(PlanDetailActivity.this);
        }
    }

    private void initUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        placeTitle = findViewById(R.id.title_place);
        prevPlaceIB = findViewById(R.id.ibtn_prev);
        nextPlaceIB = findViewById(R.id.ibtn_next);
    }

    private void savePlan() {
        SavedTrips trips = new SavedTrips(this);
        trips.execute();
    }

    private void convertIntoPojo() throws ClassNotFoundException {
        placees = new ArrayList<>();
        if (placesN != null) {
            for (int i = 0; i < placesN.size(); i++) {
                if (Class.forName("com.traveller.android.retrofit.PlacesN$ResultsEntity").isInstance(placesN.get(i))) {
                    PlacesN.ResultsEntity entity = ((PlacesN.ResultsEntity) placesN.get(i));
                    placees.add(new Placee(0, entity.name, entity.vicinity, entity.rating, new Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                } else if (Class.forName("com.traveller.android.retrofit.PlacesT$ResultsEntity").isInstance(placesN.get(i))) {
                    PlacesT.ResultsEntity entity = ((PlacesT.ResultsEntity) placesN.get(i));
                    placees.add(new Placee(0, entity.name, entity.formatted_address, entity.rating, new Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                }
            }
        }
        placesFood = new ArrayList<>();
        if (placesTFood != null)
            for (int i = 0; i < placesTFood.size(); i++) {
                if (Class.forName("com.traveller.android.retrofit.PlacesT$ResultsEntity").isInstance(placesTFood.get(i))) {
                    PlacesT.ResultsEntity entity = ((PlacesT.ResultsEntity) placesTFood.get(i));
                    placesFood.add(new Placee(0, entity.name, entity.formatted_address, entity.rating, new Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                }
            }
        placesAmuse = new ArrayList<>();
        if (placesTAmuse != null)
            for (int i = 0; i < placesTAmuse.size(); i++) {
                if (Class.forName("com.traveller.android.retrofit.PlacesT$ResultsEntity").isInstance(placesTAmuse.get(i))) {
                    PlacesT.ResultsEntity entity = ((PlacesT.ResultsEntity) placesTAmuse.get(i));
                    placesAmuse.add(new Placee(0, entity.name, entity.formatted_address, entity.rating, new Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                }
            }
//      //Log.i(TAG, "convertIntoPojo: " + placees.size() + "," + placesTAmuse.size());
    }

    private void callApi() {
        if (isNearbySearch) {
            APIInterface apiClient = APIClient.getClient().create(APIInterface.class);
            Call<PlacesN> callN = apiClient.getNearby(placelatlongSelected, String.valueOf(RADIUSKm), typeSelected, Util.KEY);
            callN.enqueue(new Callback<PlacesN>() {
                @Override
                public void onResponse(Call<PlacesN> call, Response<PlacesN> response) {
                  //Log.i(TAG, "onResponse: " + response.raw() + response.toString());
                    if (response.body() != null && response.body().status != null && response.body().status.equals("OK")
                            && response.body().results != null && response.body().results.size() > 0)
                        addToList(response.body().results);
                  //Log.i(TAG, "onResponse: makePlan" + isRecursive + makePlan);
                    if (isRecursive) {
                        recurcount++;
                        whichApi();
                    } else {
                        makePlans();
                    }
                }

                @Override
                public void onFailure(Call<PlacesN> call, Throwable t) {
                  //Log.i(TAG, "onFailure: " + t.getMessage());
                    if (isRecursive) {
                        recurcount++;
                        whichApi();
                    } else {
                        makePlans();
                    }
                }
            });
        } else {
            APIInterface apiClient = APIClient.getClient().create(APIInterface.class);
            Call<PlacesT> callT = apiClient.getTextSearch(textSearch, placelatlongSelected, String.valueOf(RADIUSKm), typeSelected, Util.KEY);
            callT.enqueue(new Callback<PlacesT>() {
                @Override
                public void onResponse(Call<PlacesT> call, Response<PlacesT> response) {
                  //Log.i(TAG, "onResponse: " + response.raw() + response.toString());
                    if (response.body() != null && response.body().status != null && response.body().status.equals("OK")
                            && response.body().results != null && response.body().results.size() > 0)
                        addToList(response.body().results);
                    if (isRecursive) {
                        recurcount++;
                        whichApi();
                    } else {
                        makePlans();
                    }
                }

                @Override
                public void onFailure(Call<PlacesT> call, Throwable t) {
                  //Log.i(TAG, "onFailure: " + t.getMessage());
                    if (isRecursive) {
                        recurcount++;
                        whichApi();
                    } else {
                        makePlans();
                    }
                }
            });
        }
    }

    private void makePlans() {
        try {
            convertIntoPojo();
        } catch (ClassNotFoundException e) {
            stopAnimation();
            e.printStackTrace();
        }
        if (makePlan) {
            if (placees == null || placees.size() <= 0)
                return;
            //rating sort all
            if (placees != null && placees.size() > 0) {
                Collections.sort(placees, new CompareRating());
            }
            HashSet<Placee> hashSet = new HashSet<>(placees);      // create has set. Set will contains only unique objects
            placees = new ArrayList<>(hashSet);
            if (placesFood != null && placesFood.size() > 0) {
                Collections.sort(placesFood, new CompareRating());
            }
            if (placesAmuse != null && placesAmuse.size() > 0) {
                Collections.sort(placesAmuse, new CompareRating());
            }
          //Log.i(TAG, "makePlans: " + placees.size() + ": " + placees.toString());
          //Log.i(TAG, "makePlans: " + placesFood.size() + ": " + placesFood.toString());
          //Log.i(TAG, "makePlans: " + placesAmuse.size() + ": " + placesAmuse.toString());
            calculateTopNCount();
            if (placesAmuse != null && placesAmuse.size() > 0) {
                maxIndexA = placesAmuse.size() - 1;
            }
            if (placesFood != null && placesFood.size() > 0) {
                maxIndexF = placesFood.size() - 1;
            }
            if (placees != null && placees.size() > 0) {
                maxIndexP = placees.size() - 1;
            }
            //distribute in diff plan lists
            int hourLeft = hours;
            boolean isAmuse = false;
            if (hours >= Util.AMUSEPARK_THRESHOLD && placesAmuse != null && maxIndexA > 0) {
                isAmuse = true;
            }
            int startFoodHourIndex;
            if (maxIndexF > 0) {
                startFoodHourIndex = Util.getStartInterval(from, to);
            } else startFoodHourIndex = -1;
            int numberOfPlans = 3;  //making 3 plans option
            boolean isAmusetemp, nomoreLeft = false;
            while (numberOfPlans > 0) {
                hourLeft = hours;
                isAmusetemp = isAmuse;
                if (numberOfPlans == 3) {
                    plan1 = new ArrayList<>();
                } else if (numberOfPlans == 2) {
                    plan2 = new ArrayList<>();
                } else if (numberOfPlans == 1) {
                    plan3 = new ArrayList<>();
                }
                while (hourLeft >= 0) {
                    if (maxIndexP > planCount) {
                        addToPlans(numberOfPlans);
                        hourLeft--;
                    } else {
                        nomoreLeft = true;
                        break;
                    }
                    if (hourLeft <= 4 && isAmusetemp) {
                        if (maxIndexA > planAmuse) {
                            addToPlansAmuse(numberOfPlans);
                            hourLeft -= 4;
                          //Log.i(TAG, "isAmusetemp: " + isAmusetemp);
                            isAmusetemp = false;
                        }
                    }
                }
                sortByDist(numberOfPlans);
                if (startFoodHourIndex != -1)
                    addToPlanFood(numberOfPlans, startFoodHourIndex);
                if (nomoreLeft)
                    break;
                numberOfPlans--;
            }
            //as per current location selected: sort by distance
            if (plan1 != null) Log.i(TAG, "madePlan1: " + plan1.size() + ": " + plan1.toString());
            if (plan2 != null) Log.i(TAG, "madePlan2: " + plan2.size() + ": " + plan2.toString());
            if (plan3 != null) Log.i(TAG, "madePlan3: " + plan3.size() + ": " + plan3.toString());
            stopAnimation();
            if (plan1 != null && plan1.size() > 0) {
                showDialogPlans();
                viewPlanMI.setVisible(true);
            }
        }
    }

    private void drawPolyline(List<Placee> mapPlacees) {
        if (mMap == null)
            return;
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions().color(Color.LTGRAY).width(8);
        polylineOptions.endCap(new SquareCap());
        polylineOptions.startCap(new RoundCap());
        polylineOptions.jointType(JointType.ROUND);
        ArrayList<LatLng> latlongList = new ArrayList<LatLng>();
        MarkerOptions moStart;
        if (markerList == null)
            markerList = new ArrayList<>();
        else markerList.clear();
        LatLng latln;
        for (int i = 0; i < mapPlacees.size(); i++) {
            latln = new LatLng(mapPlacees.get(i).location.lat, mapPlacees.get(i).location.longi);
            latlongList.add(latln);
            moStart = new MarkerOptions();
            moStart.position(latln);
            moStart.title(mapPlacees.get(i).placName + ((mapPlacees.get(i).rating != null &&
                    !mapPlacees.get(i).rating.contentEquals("0") && !mapPlacees.get(i).rating.isEmpty()) ? "(" + mapPlacees.get(i).rating + ")" : ""));
            moStart.draggable(false);
            moStart.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            Marker marker = mMap.addMarker(moStart);
            markerList.add(marker);
        }
        polylineOptions.addAll(latlongList);
        mMap.addPolyline(polylineOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapPlacees.get(0).location.lat, mapPlacees.get(0).location.longi),
                ZOOM_));

    }

    private void sortByDist(int plan) {
        if (plan == 3 && plan1 != null && plan1.size() > 0)
            Collections.sort(plan1, new CompareDist());
        if (plan == 2 && plan2 != null && plan2.size() > 0)
            Collections.sort(plan2, new CompareDist());
        if (plan == 1 && plan3 != null && plan3.size() > 0)
            Collections.sort(plan3, new CompareDist());
    }

    private void addToPlansAmuse(int numberOfPlans) {
        if (placesAmuse == null || placesAmuse.size() <= 0) {
            return;
        }
        Placee placee = placesAmuse.get(planAmuse);
        if (numberOfPlans == 3) {
            plan1.add(placee);
        } else if (numberOfPlans == 2) {
            plan2.add(placee);
        } else if (numberOfPlans == 1) {
            plan3.add(placee);
        }
        planAmuse++;
    }

    private void addToPlans(int numberOfPlans) {
        if (placees == null || placees.size() <= 0) {
            return;
        }
        Placee placee = placees.get(planCount);
        if (numberOfPlans == 3) {
            plan1.add(placee);
        } else if (numberOfPlans == 2) {
            plan2.add(placee);
        } else if (numberOfPlans == 1) {
            plan3.add(placee);
        }
        planCount++;
    }

    private void addToPlanFood(int numberOfPlans, int indexFood) {
        int index5hr = indexFood;
        if (placesFood == null || placesFood.size() <= 0) {
            return;
        }
        //every 5 hrs foodstops
        if (numberOfPlans == 3) {
            if (countFood > maxIndexF) {
                countFood = 0;
            }
            while (index5hr < plan1.size() - 1) {
                Placee placee = placesFood.get(countFood);
                plan1.add(index5hr, placee);
                index5hr += FOOD_INTERVAL_HR;
                countFood++;
            }
        } else if (numberOfPlans == 2) {
            if (countFood > maxIndexF) {
                countFood = 0;
            }
            while (index5hr < plan2.size() - 1) {
                Placee placee = placesFood.get(countFood);
                plan2.add(index5hr, placee);
                index5hr += FOOD_INTERVAL_HR;
                countFood++;
            }
        } else if (numberOfPlans == 1) {
            if (countFood > maxIndexF) {
                countFood = 0;
            }
            while (index5hr < plan3.size() - 1) {
                Placee placee = placesFood.get(countFood);
                plan3.add(index5hr, placee);
                index5hr += FOOD_INTERVAL_HR;
                countFood++;
            }
        }
        countFood = 0;
    }

    //not used
    private void calculateTopNCount() {
        //not applicable to theme park
        if (hours == 0)
            TopN = 1;
        if (hours < 6) {
            TopN = 2;
        } else if (hours < 12) {
            TopN = 3;
        } else {
            TopN = 3;
        }
    }

    private void addToList(List<?> results) {
        int topN = 14;
        if (results.size() < topN) {
            topN = results.size();
        }
        if (typeSelected.equalsIgnoreCase("food") && isFoodFINAL) {
            if (placesTFood == null) {
                placesTFood = new ArrayList<>();
            }
            for (int i = 0; i < topN; i++) {
                if (withinRadius(((PlacesT.ResultsEntity) results.get(i)).geometry.location.lat, ((PlacesT.ResultsEntity) results.get(i)).geometry.location.lng, ((PlacesT.ResultsEntity) results.get(i)).place_id, 3)) {
                    placesTFood.add((PlacesT.ResultsEntity) results.get(i));
                  //Log.i(TAG, "addToListFood: " + placesTFood.size());
                }
            }
            typeSelected = "";
            return;
        }
        if (typeSelected.equalsIgnoreCase("amusement_park")) {
            if (placesTAmuse == null) {
                placesTAmuse = new ArrayList<>();
            }
            for (int i = 0; i < topN; i++) {
                if (withinRadius(((PlacesT.ResultsEntity) results.get(i)).geometry.location.lat, ((PlacesT.ResultsEntity) results.get(i)).geometry.location.lng, ((PlacesT.ResultsEntity) results.get(i)).place_id, 2)) {
                    placesTAmuse.add((PlacesT.ResultsEntity) results.get(i));
                  //Log.i(TAG, "placesTAmuse: " +  ((PlacesT.ResultsEntity) results.get(i)).name);
                }
            }
            typeSelected = "";
            return;
        }

        if (placesN == null) {
            placesN = new ArrayList<>();
        }
        for (int i = 0; i < topN; i++) {
            if (withinRadius(((PlacesT.ResultsEntity) results.get(i)).geometry.location.lat, ((PlacesT.ResultsEntity) results.get(i)).geometry.location.lng, ((PlacesT.ResultsEntity) results.get(i)).place_id, 1)) {
                placesN.add(results.get(i));
              //Log.i(TAG, "addToList: " + ((PlacesT.ResultsEntity) results.get(i)).name);
            }
        }
        typeSelected = "";
    }

    private boolean withinRadius(double lat, double lng, String place_id, int which) {
        if (which == 1) {
            for (int i = 0; i < placesN.size(); i++) {
                if (((PlacesT.ResultsEntity) placesN.get(i)).place_id.contentEquals(place_id)) {
                  //Log.i(TAG, "placid match: " + ((PlacesT.ResultsEntity) placesN.get(i)).name + ((PlacesT.ResultsEntity) placesN.get(i)).place_id + place_id);
                    return false;
                }
            }
        } else if (which == 2) {
            for (int i = 0; i < placesTAmuse.size(); i++) {
                if ((placesTAmuse.get(i)).place_id.contentEquals(place_id)) {
                  //Log.i(TAG, "placid match: " + ((PlacesT.ResultsEntity) placesN.get(i)).name + (placesTAmuse.get(i)).place_id + place_id);
                    return false;
                }
            }
        } else if (which == 3) {
            for (int i = 0; i < placesTFood.size(); i++) {
                if ((placesTFood.get(i)).place_id.contentEquals(place_id)) {
                  //Log.i(TAG, "placid match: " + ((PlacesT.ResultsEntity) placesN.get(i)).name + (placesTFood.get(i)).place_id + place_id);
                    return false;
                }
            }
        }
        android.location.Location location2 = new android.location.Location("");
        location2.setLatitude(lat);
        location2.setLongitude(lng);
        float l = location.distanceTo(location2);
      //Log.i(TAG, "withinRadius: " + l);
        return l <= RADIUSKm;
    }

    private void whichApi() {
        //will use only TextSearch as Nearby api radius distance search is too small
        if (typeSelected == null) typeSelected = "";
        if (isRecursive) {
            if (recurcount > type.length - 1)
                isRecursive = false;
            else typeSelected = type[recurcount];
        }
        switch (typeSelected) {
            case "historical place":
                isNearbySearch = false;
                textSearch = "historical place" + (placeName == null ? "" : " in " + placeName);
                break;
            case "ancient temples":
                isNearbySearch = false;
                textSearch = "ancient temples" + (placeName == null ? "" : " in " + placeName);
                break;
            case "garden":
                isNearbySearch = false;
                textSearch = "garden" + (placeName == null ? "" : " in " + placeName);
                break;
            case "amusement_park":
                isNearbySearch = false;
                textSearch = "amusement park" + (placeName == null ? "" : " in " + placeName);
                break;
            case "zoo":
                isNearbySearch = false;
                textSearch = "zoo" + (placeName == null ? "" : " in " + placeName);
                break;
            case "shopping_mall":
                isNearbySearch = false;
                textSearch = "shopping mall" + (placeName == null ? "" : " in " + placeName);
                break;
        }
        if (isRecursive) callApi();
        else if (isFood) {
            //adjust food as per time selected in list
            typeSelected = "food";
            isNearbySearch = false;
            GetProfileTask getProfileTask = new GetProfileTask(PlanDetailActivity.this);
            getProfileTask.execute(LoginSharedPref.getEmailIDKey(PlanDetailActivity.this));
        } else {
          //Log.i(TAG, "whichApi: listfilled, call algo");
        }
      //Log.i(TAG, "whichApi: isRecursive " + isRecursive + " ,isFood " + isFood + ",typeSelected " + typeSelected + "," + textSearch + ",isNearbySearch " + isNearbySearch);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
      //Log.i(TAG, "onMapReady: " + googleMap);
        if (googleMap != null)
            mMap = googleMap;
        if (onlyDisplay) {
            if (selectedPlan != null && selectedPlan.size() > 0) {
                showArrows();
                drawPolyline(selectedPlan);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save1) {
            selectedPlan = plan1;
            savePlan();
            isPlan1Saved = true;
        } else if (v.getId() == R.id.btn_save2) {
            selectedPlan = plan2;
            savePlan();
            isPlan2Saved = true;
        } else if (v.getId() == R.id.btn_save3) {
            selectedPlan = plan3;
            savePlan();
            isPlan3Saved = true;
        } else if (v.getId() == R.id.plan1) {
            selectedPlan = plan1;
            showArrows();
            drawPolyline(selectedPlan);
        } else if (v.getId() == R.id.plan2) {
            selectedPlan = plan2;
            showArrows();
            drawPolyline(selectedPlan);
        } else if (v.getId() == R.id.plan3) {
            selectedPlan = plan3;
            showArrows();
            drawPolyline(selectedPlan);
        }
    }

    private void showArrows() {
        move = 0;
        if (placeTitle.getVisibility() == View.GONE) {
            placeTitle.setVisibility(View.VISIBLE);
        }
        if (nextPlaceIB.getVisibility() == View.GONE) {
            nextPlaceIB.setVisibility(View.VISIBLE);
        }
        if (prevPlaceIB.getVisibility() == View.GONE) {
            prevPlaceIB.setVisibility(View.VISIBLE);
        }
        if (selectedPlan == null)
            return;
        setArrow();
        if (dialogPlans != null && dialogPlans.isShowing())
            dialogPlans.dismiss();
    }

    public void onPrevClicked(View view) {
        if (selectedPlan != null && selectedPlan.size() > 0) {
            --move;
            setArrow();
        }
    }

    private void setArrow() {
        if (move == 0) {
            prevPlaceIB.setVisibility(View.GONE);
        } else {
            prevPlaceIB.setVisibility(View.VISIBLE);
        }
        if (move == selectedPlan.size() - 1) {
            nextPlaceIB.setVisibility(View.GONE);
        } else {
            nextPlaceIB.setVisibility(View.VISIBLE);
        }
        if (move >= 0 && move < selectedPlan.size()) {
            placeTitle.setText(selectedPlan.get(move).placName);
            moveMarker();
        }
    }

    private void moveMarker() {
        if (mMap == null)
            return;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selectedPlan.get(move).location.lat, selectedPlan.get(move).location.longi),
                ZOOM_));
        if (markerList != null && markerList.size() == selectedPlan.size())
            markerList.get(move).showInfoWindow();
    }

    public void onNextClicked(View view) {
        if (selectedPlan != null && selectedPlan.size() > 0) {
            ++move;
            setArrow();
        }
    }

    //SAVE
    private class SavedTrips extends AsyncTask<Void, Boolean, Boolean> {
        private Context context;

        public SavedTrips(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TripStore store = TripDatabase.get(context).tripStore();
            pojo = new Trip(LoginSharedPref.getEmailIDKey(PlanDetailActivity.this), selectedPlan.get(0).address, from, to, selectedPlan.get(0).placName + ">..>" + selectedPlan.get(selectedPlan.size() - 1).placName, RADIUSKm);
            long inserted = -1;
            inserted = store.insertPlan(pojo, LoginSharedPref.getEmailIDKey(PlanDetailActivity.this), selectedPlan);
            return (inserted != -1);
        }

        @Override
        protected void onPostExecute(Boolean reply) {
            super.onPostExecute(reply);
          //Log.d("TAG", "onPostExecute() called with: reply = [" + reply + "]");
            if (!reply) {
                Toast.makeText(context, "Not saved", Toast.LENGTH_SHORT).show();
            } else {
                if (dialogPlans != null && dialogPlans.isShowing())
                    dialogPlans.dismiss();
                if (backIntent == null) {
                    backIntent = new Intent();
                    setResult(RESULT_OK);
                }
                Toast.makeText(context, "Saved plan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class CompareRating implements Comparator<Placee> {
        @Override
        public int compare(Placee o1, Placee o2) {
            if (o2.rating == null || o1.rating == null)
                return -1;
            return Float.compare(Float.parseFloat(o2.rating), Float.parseFloat(o1.rating));
        }
    }

    private class CompareDist implements Comparator<Placee> {
        @Override
        public int compare(Placee o1, Placee o2) {
            android.location.Location location1 = new android.location.Location("");
            location1.setLatitude(o1.location.lat);
            location1.setLongitude(o1.location.longi);
            android.location.Location location2 = new android.location.Location("");
            location2.setLatitude(o2.location.lat);
            location2.setLongitude(o2.location.longi);
            float distE = location.distanceTo(location2);
            float distS = location.distanceTo(location1);
            if (Float.compare(distS, distE) == 0) {
                if (o2.rating == null || o1.rating == null)
                    return Float.compare(distS, distE);
                return Float.compare(Float.parseFloat(o2.rating), Float.parseFloat(o1.rating));
            }
            return Float.compare(distS, distE);
        }
    }

    private class GetProfileTask extends AsyncTask<String, Void, Person> {
        private Context context;

        public GetProfileTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Person doInBackground(String... strings) {
            PersonStore store = TripDatabase.get(context).personStore();
            Person person = store.findProfileById(strings[0]);
            return person;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);
            if (person == null) {
                stopAnimation();
                AlertDialog.Builder ad = new AlertDialog.Builder(PlanDetailActivity.this);
                ad.setTitle("Login expired");
                ad.setMessage("relogin to continue");
                ad.setNeutralButton("register now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        gotoLogin();
                    }
                });
                ad.show();
            } else {
                personObj = new Person(person.emailid, person.phone, person.fullname, person.address, person.pass, person.isNonveg, person.isDrinker, person.cuisines);
                textSearch = "";
                if (personObj.isNonveg) {
                    textSearch = textSearch + " nonveg";
                }
                if (!personObj.toString().isEmpty()) {
                    textSearch = textSearch + personObj.toString().replace(",", " ");
                }
                if (personObj.isDrinker) {
                    textSearch = textSearch + " bar";
                }
                textSearch = textSearch + " food";
                callApi();
                isFood = false;
                makePlan = true;
            }
        }

        private void gotoLogin() {
            Intent i = new Intent(PlanDetailActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_plan, menu);
        viewPlanMI = menu.findItem(R.id.mi_plans);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_plans) {
            showDialogPlans();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogPlans() {
        if (dialogPlans == null) {
            dialogPlans = new Dialog(this);
            dview = getLayoutInflater().inflate(R.layout.dialog_plans, null);
            tvPlan1 = dview.findViewById(R.id.plan1);
            tvPlan2 = dview.findViewById(R.id.plan2);
            tvPlan3 = dview.findViewById(R.id.plan3);
            btnSave1 = dview.findViewById(R.id.btn_save1);
            btnSave2 = dview.findViewById(R.id.btn_save2);
            btnSave3 = dview.findViewById(R.id.btn_save3);
            btnSave1.setOnClickListener(this);
            btnSave2.setOnClickListener(this);
            btnSave3.setOnClickListener(this);
            if (plan1 == null) {
                tvPlan1.setVisibility(View.GONE);
                btnSave1.setVisibility(View.GONE);
            }
            if (plan2 == null) {
                tvPlan2.setVisibility(View.GONE);
                btnSave2.setVisibility(View.GONE);
            }
            if (plan3 == null) {
                tvPlan3.setVisibility(View.GONE);
                btnSave3.setVisibility(View.GONE);
            }
            dialogPlans.setContentView(dview);
        }
        if (plan1 != null && plan1.size() > 0) {
            tvPlan1.setText(plan1.toString().replace(",", ""));
            tvPlan1.setOnClickListener(this);
        }
        if (plan2 != null && plan2.size() > 0) {
            tvPlan2.setText(plan2.toString().replace(",", ""));
            tvPlan2.setOnClickListener(this);
        }
        if (plan3 != null && plan3.size() > 0) {
            tvPlan3.setText(plan3.toString().replace(",", ""));
            tvPlan3.setOnClickListener(this);
        }
        if (isPlan1Saved) {
            btnSave1.setVisibility(View.GONE);
        }
        if (isPlan2Saved) {
            btnSave2.setVisibility(View.GONE);
        }
        if (isPlan3Saved) {
            btnSave3.setVisibility(View.GONE);
        }
        if (dialogPlans.isShowing()) dialogPlans.dismiss();
        dialogPlans.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (dialogPlans == null || plan1 == null) {
            viewPlanMI.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        placesFood = null;
        placesTAmuse = null;
        placesN = null;
    }
}