package com.traveller.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginSharedPref {
    private static final String SPREF_FILE = "SPREF_LOGIN";
    private static final String NGOID_LOGIN_KEY = "email";
    private static final String NAME_KEY = "name";
    private static final String LAT_KEY = "latilong";

    public static String getEmailIDKey(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sprefLogin.getString(NGOID_LOGIN_KEY, "");
    }
    public static void clear(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.clear();
        editor.apply();
    }

    public static void setEmailIDKEY(Context context, String uid) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(NGOID_LOGIN_KEY, uid);
        editor.apply();
    }
    public static String getLATKey(Context context) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sprefLogin.getString(LAT_KEY, "");
    }

    public static void setLATKey(Context context, String uid) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(LAT_KEY, uid);
        editor.apply();
    }

    public static void setNameKey(Context context, String name) {
        SharedPreferences sprefLogin = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefLogin.edit();
        editor.putString(NAME_KEY, name);
        editor.apply();
    }

    public static String getNameKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SPREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(NAME_KEY, "");
    }
}
