package com.traveller.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.traveller.android.db.Person;
import com.traveller.android.db.PersonStore;
import com.traveller.android.db.TripDatabase;
import com.traveller.android.utils.EmailValidation;
import com.traveller.android.utils.LoaderClass;
import com.traveller.android.utils.LoginSharedPref;

import static com.traveller.android.utils.LoaderClass.stopAnimation;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RCODE_PERM = 19;
    private TextInputEditText emailIdET, passET;
    private FrameLayout frameLayout;
    private Button loginBtn;
    private EmailValidation emailValidation;
    private LinearLayout llLogin;
    private TextInputLayout tilEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check for logged in or not
        if (!LoginSharedPref.getEmailIDKey(LoginActivity.this).isEmpty()) {
            //already loggedin
            Intent regIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(regIntent);
            finish();
        }else {
            setContentView(R.layout.activity_login);
            initUI();
        }
    }


    private void initUI() {
        emailIdET = findViewById(R.id.et_email_log);
        tilEmail = findViewById(R.id.til_email);
        passET = findViewById(R.id.et_pass_log);
        loginBtn = findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
        frameLayout = findViewById(R.id.fl_login);
        llLogin = findViewById(R.id.ll_login_ui);

        emailValidation = new EmailValidation();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            validationAndLogin();
        }
        hideKeyboard(v);
    }

    private void validationAndLogin() {
        EmailValidation validation = new EmailValidation();
        if (emailIdET.getText().toString().isEmpty()) {
            Snackbar.make(frameLayout, "username cannot be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }
         if (!validation.validateEmail(emailIdET.getText().toString())) {
            Snackbar.make(frameLayout, "Enter a valid Email id", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (passET.getText().toString().isEmpty()) {
            Snackbar.make(frameLayout, "Password field cannot be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }
        loginSuccess();
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void loginSuccess() {
        //LoginAPi succes->then
        LoginTask task = new LoginTask(LoginActivity.this);
      //Log.i("TAG", "loginSuccess: " + emailIdET.getText().toString() + passET.getText().toString()+ "type selected ");
        task.execute(emailIdET.getText().toString(), passET.getText().toString());
    }

    public void onRegClicked(View view) {
        gotoReg();
    }

    private void gotoReg() {
        Intent i = new Intent(LoginActivity.this, ProfileOrRegActivity.class);
        i.putExtra("REG", true);
        startActivity(i);
    }

    private class LoginTask extends AsyncTask<String, Void, Person> {
        private Context context;

        public LoginTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoaderClass.startAnimation(LoginActivity.this);
        }

        @Override
        protected Person doInBackground(String... strings) {
            PersonStore store = TripDatabase.get(context).personStore();
            Person person = store.findById(strings[0], strings[1]);
            return person;
        }

        @Override
        protected void onPostExecute(Person person) {
            super.onPostExecute(person);
            stopAnimation();
            if (person == null) {
                AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
                ad.setTitle("Login failed");
                ad.setMessage("This User is not registered");
                ad.setNeutralButton("register now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        gotoReg();
                    }
                });
                ad.show();
            } else {
                LoginSharedPref.setEmailIDKEY(LoginActivity.this, person.emailid);
                LoginSharedPref.setNameKey(LoginActivity.this, person.fullname);
                Intent intentLogin = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intentLogin);
                finish();
            }
        }

    }
}

