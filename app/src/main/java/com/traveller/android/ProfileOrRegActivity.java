package com.traveller.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.traveller.android.db.Person;
import com.traveller.android.db.PersonStore;
import com.traveller.android.db.TripDatabase;
import com.traveller.android.utils.EmailValidation;
import com.traveller.android.utils.LoaderClass;
import com.traveller.android.utils.LoginSharedPref;

import static com.traveller.android.utils.LoaderClass.stopAnimation;

public class ProfileOrRegActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "TAG";
    private LinearLayout llCom;
    private Person pojo;
    private EditText addET, mobnoET, emailET, nameET, passET;
    private GetProfileTask getProfileTask;
    private RadioGroup rgVeg, rgDrink;
    private RadioButton rbV, rbNV, rbD, rbND;
    private boolean isReg;
    private String cuisines;
    private MaterialCheckBox cb1, cb2, cb3, cb4, cb5;
    private ImageButton submit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        initUI();
        if (getIntent() != null && getIntent().getBooleanExtra("REG", false)) {
            getSupportActionBar().setTitle("Register");
            isReg = true;
        } else {
            getSupportActionBar().setTitle("Profile");
            emailET.setEnabled(false);
            findViewById(R.id.pass_til).setVisibility(View.GONE);
            getProfile();
        }
    }

    private void initUI() {
        nameET = findViewById(R.id.et_firstname);
        passET = findViewById(R.id.et_pass);
        addET = findViewById(R.id.et_add);
        mobnoET = findViewById(R.id.et_mobno);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(this);
        emailET = findViewById(R.id.et_emailaddress);
        rgVeg = findViewById(R.id.eatarian);
        rgVeg.setOnCheckedChangeListener(this);
        rgDrink = findViewById(R.id.drinking);
        rgDrink.setOnCheckedChangeListener(this);
        rbD = findViewById(R.id.rb_d);
        rbND = findViewById(R.id.rb_nd);
        rbV = findViewById(R.id.rb_v);
        rbNV = findViewById(R.id.rb_nv);
        cb1 = findViewById(R.id.c1);
        cb2 = findViewById(R.id.c2);
        cb3 = findViewById(R.id.c3);
        cb4 = findViewById(R.id.c4);
        cb5 = findViewById(R.id.c5);
        llCom = findViewById(R.id.ll_willing);
    }

    private void getProfile() {
        getProfileTask = new GetProfileTask(ProfileOrRegActivity.this);
        getProfileTask.execute(LoginSharedPref.getEmailIDKey(ProfileOrRegActivity.this));
    }

    private void setData() {
        if (pojo != null) {
            nameET.setText(pojo.fullname);
            emailET.setText(pojo.emailid);
            mobnoET.setText(pojo.phone);
            addET.setText(pojo.address);
            //rb, cboxes set
            if (pojo.isDrinker) {
                rbD.setChecked(true);
            } else rbND.setChecked(true);
            if (pojo.isNonveg) {
                rbNV.setChecked(true);
            } else rbV.setChecked(true);
            if (!pojo.toString().isEmpty()) {
              //Log.i(TAG, "setData: " + pojo.toString());
                String[] cui = pojo.toString().split(",");
                for (int i = 0; i < cui.length; i++) {
                    if (!cui[i].isEmpty())
                        markCheckbox(cui[i].toLowerCase());
                  //Log.i(TAG, "setData: " + cui[i]);
                }
            }
        }
    }

    private void markCheckbox(String whichCui) {
        switch (whichCui) {
            case "indian":
                cb1.setChecked(true);
                break;
            case "italian":
                cb2.setChecked(true);
                break;
            case "thai":
                cb3.setChecked(true);
                break;
            case "continental":
                cb4.setChecked(true);
                break;
            case "chinese":
                cb5.setChecked(true);
                break;

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submit) {
            updateDb();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // if ()
    }


    private class GetProfileTask extends AsyncTask<String, Void, Person> {
        private Context context;

        public GetProfileTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoaderClass.startAnimation(ProfileOrRegActivity.this);
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
            stopAnimation();
            if (person == null) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ProfileOrRegActivity.this);
                ad.setTitle("Login expired");
                ad.setMessage("relogin to continue");
                ad.setNeutralButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        gotoLogin();
                    }
                });
                ad.show();
            } else {
                pojo = new Person(person.emailid, person.phone, person.fullname, person.address, person.pass, person.isNonveg, person.isDrinker, person.cuisines);
                setData();
            }
        }

    }

    private class UpdateorRegProfileTask extends AsyncTask<Void, Boolean, Boolean> {
        private Context context;

        public UpdateorRegProfileTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoaderClass.startAnimation(ProfileOrRegActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            PersonStore store = TripDatabase.get(context).personStore();
            long val;
            if (isReg) {
                val = store.insertCheck(pojo, emailET.getText().toString());   //long id inserted
            } else
                val = store.update(pojo);   //int no of rows updated
          //Log.i(TAG, "doInBackground: " + val + isReg);
            return val != -1 && val != 0 && val != -2;
        }

        @Override
        protected void onPostExecute(Boolean reply) {
            super.onPostExecute(reply);
            stopAnimation();
            if (!reply) {
                Toast.makeText(context, (isReg ? "Registration failed, this email is already registered" : "Update failed"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, (isReg ? "Registration success, Login to continue" : "Profile Updated"), Toast.LENGTH_LONG).show();
                if (isReg) gotoLogin();
                else gotoDash();
            }
        }
    }

    private void gotoLogin() {
        Intent i = new Intent(ProfileOrRegActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void gotoDash() {
        Intent i = new Intent(ProfileOrRegActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private boolean valid() {
        if (nameET.getText().toString().trim().isEmpty()) {
            Snackbar.make(llCom, "Please Enter name", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (addET.getText().toString().trim().isEmpty()) {
            Snackbar.make(llCom, "Please Enter Address", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (mobnoET.getText().toString().trim().isEmpty()) {
            Snackbar.make(llCom, "Please Enter Mobile Number", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (emailET.getText().toString().trim().isEmpty()) {
            Snackbar.make(llCom, "Please Enter Email", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        EmailValidation validation = new EmailValidation();
        if (!validation.validateEmail(emailET.getText().toString().trim())) {
            Snackbar.make(llCom, "Please Enter a valid Email Address for Contact Person", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (addET.getText().toString().trim().isEmpty()) {
            Snackbar.make(llCom, "Please Enter postal address", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (passET.getText().toString().trim().isEmpty() && isReg) {
            Snackbar.make(llCom, "Please Enter password", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!rbNV.isChecked() && !rbV.isChecked()) {
            Snackbar.make(llCom, "Please Select preferences(Veg/Non Veg)", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!rbD.isChecked() && !rbND.isChecked()) {
            Snackbar.make(llCom, "Please Select preferences(Drinker/Non Drinker)", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!cb5.isChecked() && !cb4.isChecked() && !cb3.isChecked() && !cb2.isChecked() && !cb1.isChecked()) {
            Snackbar.make(llCom, "Please Select at least one favorite Cuisine", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void updateDb() {
        if (valid()) {
            cuisines = "";
            if (cb1.isChecked()) {
                cuisines = cuisines + "," + cb1.getText().toString();
            }
            if (cb2.isChecked()) {
                cuisines = cuisines + "," + cb2.getText().toString();
            }
            if (cb3.isChecked()) {
                cuisines = cuisines + "," + cb3.getText().toString();
            }
            if (cb4.isChecked()) {
                cuisines = cuisines + "," + cb4.getText().toString();
            }
            if (cb5.isChecked()) {
                cuisines = cuisines + "," + cb5.getText().toString();
            }
            pojo = new Person(emailET.getText().toString(), mobnoET.getText().toString(),
                    nameET.getText().toString(), addET.getText().toString(),
                    isReg ? passET.getText().toString() : pojo.pass, rbNV.isChecked(), rbD.isChecked(), cuisines
            );
            UpdateorRegProfileTask task = new UpdateorRegProfileTask(ProfileOrRegActivity.this);
            task.execute();
        }
    }

    @Override
    public void onDestroy() {
        if (getProfileTask != null && !getProfileTask.isCancelled())
            getProfileTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
