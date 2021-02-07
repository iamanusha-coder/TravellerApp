package com.traveller.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.traveller.android.db.Person;
import com.traveller.android.db.PersonStore;
import com.traveller.android.db.Placee;
import com.traveller.android.db.TripDatabase;
import com.traveller.android.retrofit.APIClient;
import com.traveller.android.retrofit.APIInterface;
import com.traveller.android.retrofit.PlacesN;
import com.traveller.android.retrofit.PlacesT;
import com.traveller.android.utils.LoginSharedPref;
import com.traveller.android.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.traveller.android.utils.LoaderClass.stopAnimation;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TAG";
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatPojo> pojos;
    private String placelatlongSelected;
    private Location location;
    private EditText inputET;
    private ImageButton sendBtn;
    private boolean isNearbySearch;
    private String typeSelected;
    private int RADIUSKm = 100_000;
    private String textSearch;
    private Person personObj;
    private List<Object> placesN;
    private String DEF_ = "I can help you search for places like historical places, zoo, garden, amusement park, ancient temples, restaurant near you";
    private Place place1, place2, place3;
    private ArrayList<Placee> placees;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields;
    private String placeName;
    private String area;
    private int count;
    private FetchPlaceRequest request;
    private Dialog dialogSearchResult;
    private View dview;
    private TextView tvDetail2, tvDetail1, tvDetail3, tvCall1, tvCall2, tvCall3;
    private RelativeLayout rl1, rl2, rl3;
    private ImageView nav1, nav2, nav3;
    private Button closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.rv);
        inputET = findViewById(R.id.et_userinput);
        sendBtn = findViewById(R.id.button);
        sendBtn.setOnClickListener(this);
        placelatlongSelected = getIntent().getStringExtra("location");
        area = getIntent().getStringExtra("area");
        placeName = getIntent().getStringExtra("placeName");
        String[] ltlng;
        if (placelatlongSelected == null || placelatlongSelected.isEmpty()) {
            placelatlongSelected = LoginSharedPref.getLATKey(ChatActivity.this);
        }
        ltlng = placelatlongSelected.split(",");
        location = new android.location.Location("");
        location.setLatitude(Double.parseDouble(ltlng[0]));
        location.setLongitude(Double.parseDouble(ltlng[1]));

        addToChat("Hi, " + DEF_, true);
        Places.initialize(getApplicationContext(), Util.KEY);
        placesClient = Places.createClient(this);
        placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.BUSINESS_STATUS, Place.Field.OPENING_HOURS);
    }

    private void addToChat(String chatText, boolean robot) {
        if (pojos == null)
            pojos = new ArrayList<>();
        pojos.add(new ChatPojo(chatText, robot));
        if (chatAdapter == null) {
            chatAdapter = new ChatAdapter(pojos, ChatActivity.this);
            recyclerView.setAdapter(chatAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        AddtoChatRView();
        if (robot) {
            System.out.println("Support: " + chatText);
        } else {
            System.out.println("You: " + chatText);
        }
    }

    private void AddtoChatRView() {
      //Log.i(TAG, "AddtoChatRView: " + pojos.size());
        chatAdapter.notifyDataSetChanged();
    }

    private void callApi() {
      //Log.i(TAG, "callApi: " + textSearch + typeSelected + isNearbySearch);
        if (isNearbySearch) {
            APIInterface apiClient = APIClient.getClient().create(APIInterface.class);
            Call<PlacesN> callN = apiClient.getNearby(placelatlongSelected, String.valueOf(RADIUSKm), typeSelected, Util.KEY);
            callN.enqueue(new Callback<PlacesN>() {
                @Override
                public void onResponse(Call<PlacesN> call, Response<PlacesN> response) {
                  //Log.i(TAG, "onResponse: " + response.raw() + response.toString());
                    if (response.body() != null && response.body().status != null && response.body().status.equals("OK")
                            && response.body().results != null && response.body().results.size() > 0) {
                        addToList(response.body().results);
                    } else failedSearching();
                }

                @Override
                public void onFailure(Call<PlacesN> call, Throwable t) {
                  //Log.i(TAG, "onFailure: " + t.getMessage());
                    failedSearching();
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
                            && response.body().results != null && response.body().results.size() > 0) {
                        addToList(response.body().results);
                    } else failedSearching();
                }

                @Override
                public void onFailure(Call<PlacesT> call, Throwable t) {
                  //Log.i(TAG, "onFailure: " + t.getMessage());
                    failedSearching();
                }
            });
        }
    }

    private void whichApi() {
        if (typeSelected == null) typeSelected = "";
        if (typeSelected.contains("historical place")) {
            typeSelected = "historical place";
            isNearbySearch = false;
            textSearch = "historical place" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("ancient temples")) {
            isNearbySearch = false;
            typeSelected = "ancient temples";
            textSearch = "ancient temples" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("garden")) {
            isNearbySearch = false;
            typeSelected = "garden";
            textSearch = "garden" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("amusement park")) {
            isNearbySearch = false;
            typeSelected = "amusement_park";
            textSearch = "amusement park" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("zoo")) {
            isNearbySearch = false;
            typeSelected = "zoo";
            textSearch = "zoo" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("shopping mall")) {
            typeSelected = "shopping_mall";
            isNearbySearch = false;
            textSearch = "shopping mall" + (placeName == null ? "" : " in " + placeName);
        } else if (typeSelected.contains("food") || typeSelected.contains("restaurant") || typeSelected.contains("hotel")) {
            typeSelected = "food";
            isNearbySearch = false;
            GetProfileTask getProfileTask = new GetProfileTask(ChatActivity.this);
            getProfileTask.execute(LoginSharedPref.getEmailIDKey(ChatActivity.this));
            return;
        } else {
            cantUndersatnd();
            return;
        }
        callApi();
    }

    private void cantUndersatnd() {
        addToChat("I don't understand your query." + DEF_, true);
    }

    private void failedSearching() {
        addToChat("I can't find that for you. Search with different keywords." + DEF_, true);
    }

    private void addToList(List<?> results) {
        int topN = 10;
        if (results.size() < topN) {
            topN = results.size();
        }
        if (placesN == null) {
            placesN = new ArrayList<>();
        } else placesN.clear();
        for (int i = 0; i < topN; i++) {
            if (withinRadius(((PlacesT.ResultsEntity) results.get(i)).geometry.location.lat, ((PlacesT.ResultsEntity) results.get(i)).geometry.location.lng, ((PlacesT.ResultsEntity) results.get(i)).place_id)) {
                placesN.add(results.get(i));
              //Log.i(TAG, "addToList: " + ((PlacesT.ResultsEntity) results.get(i)).place_id + ((PlacesT.ResultsEntity) results.get(i)).name);
            }
        }
        typeSelected = "";
        try {
            convertIntoPojo();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        place1 = null;
        place2 = null;
        place3 = null;
        count = 0;
        getPlace();
    }

    private void convertIntoPojo() throws ClassNotFoundException {
        placees = new ArrayList<>();
        if (placesN != null) {
            for (int i = 0; i < placesN.size(); i++) {
                if (Class.forName("com.traveller.android.retrofit.PlacesN$ResultsEntity").isInstance(placesN.get(i))) {
                    PlacesN.ResultsEntity entity = ((PlacesN.ResultsEntity) placesN.get(i));
                    placees.add(new Placee(entity.place_id, 0, entity.name, entity.vicinity, entity.rating, new com.traveller.android.db.Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                } else if (Class.forName("com.traveller.android.retrofit.PlacesT$ResultsEntity").isInstance(placesN.get(i))) {
                    PlacesT.ResultsEntity entity = ((PlacesT.ResultsEntity) placesN.get(i));
                    placees.add(new Placee(entity.place_id, 0, entity.name, entity.formatted_address, entity.rating, new com.traveller.android.db.Location(entity.geometry.location.lat, entity.geometry.location.lng), entity.types.get(0)));
                }
            }
        }
        if (placees == null || placees.size() <= 0)
            return;
        Collections.sort(placees, new CompareDist());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            typeSelected = inputET.getText().toString();
            addToChat(typeSelected, false);
            hideKeyBoard(sendBtn);
            whichApi();
            inputET.setText("");
        } else if (v.getId() == R.id.nav1) {
            if (place1 != null)
                intentForMapBtnStartNavigation(place1.getLatLng().latitude + "," + place1.getLatLng().longitude);
        } else if (v.getId() == R.id.nav2) {
            if (place2 != null)
                intentForMapBtnStartNavigation(place2.getLatLng().latitude + "," + place2.getLatLng().longitude);
        } else if (v.getId() == R.id.nav3) {
            if (place3 != null)
                intentForMapBtnStartNavigation(place3.getLatLng().latitude + "," + place3.getLatLng().longitude);
        } else if (v.getId() == R.id.tv_call1) {
            if (place1 != null) call(place1.getPhoneNumber());
        } else if (v.getId() == R.id.tv_call2) {
            if (place2 != null) call(place2.getPhoneNumber());
        } else if (v.getId() == R.id.tv_call3) {
            if (place3 != null) call(place3.getPhoneNumber());
        } else if (v.getId() == R.id.btn_close) {
            if (dialogSearchResult.isShowing()) dialogSearchResult.dismiss();
        }
    }

    private void call(String phoneNumber) {
      //Log.i(TAG, "call: " + phoneNumber);
        if (phoneNumber == null || phoneNumber.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager manager = getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
        if (infos.size() > 0) {
            startActivity(intent);
        } else
            copyToClip(phoneNumber);
    }

    private void copyToClip(String number) {
        String label = number;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, number);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied number " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean withinRadius(double lat, double lng, String place_id) {
        for (int i = 0; i < placesN.size(); i++) {
            if (((PlacesT.ResultsEntity) placesN.get(i)).place_id.contentEquals(place_id)) {
              //Log.i(TAG, "placid match: " + ((PlacesT.ResultsEntity) placesN.get(i)).name + ((PlacesT.ResultsEntity) placesN.get(i)).place_id + place_id);
                return false;
            }
        }

        android.location.Location location2 = new android.location.Location("");
        location2.setLatitude(lat);
        location2.setLongitude(lng);
        float l = location.distanceTo(location2);
      //Log.i(TAG, "withinRadius: " + l);
        return l <= RADIUSKm;
    }

    private void hideKeyBoard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this);
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
                isNearbySearch = false;
                if (personObj.isNonveg) {
                    textSearch = textSearch + " nonveg";
                }
                if (!personObj.toString().isEmpty()) {
                    textSearch = textSearch + personObj.toString().replace(",", " ");
                }
                if (personObj.isDrinker) {
                    textSearch = textSearch + " bar";
                }
                textSearch = textSearch + " food restaurant";
                callApi();
            }
        }

        private void gotoLogin() {
            Intent i = new Intent(ChatActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    private static class CompareRating implements Comparator<ChatActivity.Placee> {
        @Override
        public int compare(ChatActivity.Placee o1, ChatActivity.Placee o2) {
            if (o2.rating == null || o1.rating == null)
                return -1;
            return Float.compare(Float.parseFloat(o2.rating), Float.parseFloat(o1.rating));
        }
    }

    private class CompareDist implements Comparator<ChatActivity.Placee> {
        @Override
        public int compare(ChatActivity.Placee o1, ChatActivity.Placee o2) {
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

    public void getPlace() {
        String placeid = "";
        if (placees != null && placees.size() > 0) {
            if (count <= placees.size() - 1) {
                placeid = placees.get(count).placeId;
              //Log.i(TAG, "getPlace: " + placeid);
                count++;
            } else {
                fillPlaces();
                return;
            }
        } else {
            failedSearching();
            return;
        }
        if (count >= 3) {
            fillPlaces();
            return;
        }
        request = FetchPlaceRequest.builder(placeid, placeFields)
                .build();
        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
                if (place1 == null) {
                    place1 = response.getPlace();
                    getPlace();
                } else if (place2 == null) {
                    place2 = response.getPlace();
                    getPlace();
                } else if (place3 == null) {
                    place3 = response.getPlace();
                    fillPlaces();
                } else fillPlaces();
              //Log.i(TAG, "Placee found: " + place1.getName());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                  //Log.e(TAG, "Placee not found: " + exception.getMessage());
                    failedSearching();
                }
            }
        });
    }

    private void fillPlaces() {
        count = 0;
        //setText
        String places =
                (place1 != null ? place1.getName() : "") + "\n" +
                        (place2 != null ? ", " + place2.getName() : "") + "\n" +
                        (place3 != null ? ", " + place3.getName() : "");
        if (places.trim().isEmpty()) {
            failedSearching();
        } else {
            addToChat("Places that I suggest are " + places, true);
            showDialogSearchResult();
        }
    }

    private class Placee {
        public String placeId;
        public int tripId;
        public String placName;
        public String address;
        public String rating;
        public String type;
        public com.traveller.android.db.Location location;

        public Placee(String placeId, int tripId, String placName, String address, String rating, com.traveller.android.db.Location location, String type) {
            this.placeId = placeId;
            this.tripId = tripId;
            this.placName = placName;
            this.address = address;
            this.rating = rating;
            this.type = type;
            this.location = location;
        }
    }

    private void showDialogSearchResult() {
        if (dialogSearchResult == null) {
            dialogSearchResult = new Dialog(this);
            dview = getLayoutInflater().inflate(R.layout.search_result_chat, null);
            rl1 = dview.findViewById(R.id.rl_1);
            rl2 = dview.findViewById(R.id.rl_2);
            rl3 = dview.findViewById(R.id.rl_3);
            tvDetail1 = dview.findViewById(R.id.tv_detail1);
            tvDetail2 = dview.findViewById(R.id.tv_detail2);
            tvDetail3 = dview.findViewById(R.id.tv_detail3);
            tvCall1 = dview.findViewById(R.id.tv_call1);
            tvCall2 = dview.findViewById(R.id.tv_call2);
            tvCall3 = dview.findViewById(R.id.tv_call3);
            nav1 = dview.findViewById(R.id.nav1);
            nav2 = dview.findViewById(R.id.nav2);
            nav3 = dview.findViewById(R.id.nav3);
            closeBtn = dview.findViewById(R.id.btn_close);
            closeBtn.setOnClickListener(this);
            nav1.setOnClickListener(this);
            nav2.setOnClickListener(this);
            nav3.setOnClickListener(this);
            tvCall1.setOnClickListener(this);
            tvCall2.setOnClickListener(this);
            tvCall3.setOnClickListener(this);
            if (place1 == null) {
                rl1.setVisibility(View.GONE);
            }
            if (place2 == null) {
                rl2.setVisibility(View.GONE);
            }
            if (place3 == null) {
                rl3.setVisibility(View.GONE);
            }
            dialogSearchResult.setCancelable(false);
            dialogSearchResult.setContentView(dview);
        }
        setAllGONE();
        if (place1 != null) {
            tvDetail1.setText(place1.getName().toUpperCase() + "\n" + place1.getAddress()+(place1.getRating()!=null?"\nrating: "+place1.getRating():""));
            rl1.setVisibility(View.VISIBLE);
            tvCall1.setText(place1.getPhoneNumber() != null && !place1.getPhoneNumber().isEmpty() ? place1.getPhoneNumber() : "No Phone Number");
        }
        if (place2 != null) {
            tvDetail2.setText(place2.getName().toUpperCase() + "\n" + place2.getAddress()+(place2.getRating()!=null?"\nrating: "+place2.getRating():""));
            rl2.setVisibility(View.VISIBLE);
            tvCall2.setText(place2.getPhoneNumber() != null && !place2.getPhoneNumber().isEmpty() ? place2.getPhoneNumber() : "No Phone Number");
        }
        if (place3 != null) {
            tvDetail3.setText(place3.getName().toUpperCase() + "\n" + place3.getAddress()+(place3.getRating()!=null?"\nrating: "+place3.getRating():""));
            rl3.setVisibility(View.VISIBLE);
            tvCall3.setText(place3.getPhoneNumber() != null && !place3.getPhoneNumber().isEmpty() ? place3.getPhoneNumber() : "No Phone Number");
        }
        if (dialogSearchResult.isShowing()) dialogSearchResult.dismiss();
        dialogSearchResult.show();
    }

    private void setAllGONE() {
        rl1.setVisibility(View.GONE);
        rl2.setVisibility(View.GONE);
        rl3.setVisibility(View.GONE);
        tvDetail1.setText("");
        tvDetail2.setText("");
        tvDetail3.setText("");
        tvCall1.setText("No Phone Number");
        tvCall2.setText("No Phone Number");
        tvCall3.setText("No Phone Number");
    }

    private void intentForMapBtnStartNavigation(String coord) {
        if (coord.isEmpty()) return;
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + coord);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}
