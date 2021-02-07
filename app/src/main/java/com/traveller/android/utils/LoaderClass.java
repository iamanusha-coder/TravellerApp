package com.traveller.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.traveller.android.R;


public class LoaderClass {
    public static String getImageEncoded() {
        return imageEncoded;
    }

    public static void setImageEncoded(String imageEncoded) {
        LoaderClass.imageEncoded = imageEncoded;
    }

    public static String imageEncoded;
    private static Dialog dialogLoader;
    private static Context context;
    public static void stopAnimation() {
        if (dialogLoader != null&& dialogLoader.isShowing())
            dialogLoader.cancel();
    }

    public static void startAnimation(Context context) {
        LoaderClass.context = context;
        if (dialogLoader!=null && dialogLoader.isShowing())dialogLoader.cancel();
        dialogLoader = new Dialog(LoaderClass.context, R.style.Theme_AppCompat_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = ((Activity) LoaderClass.context).getLayoutInflater().inflate(R.layout.custom_dialog_loader, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        dialogLoader.setCancelable(false);
        try {
            dialogLoader.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void startAnimation_(Context context) {
        LoaderClass.context = context;
        if (dialogLoader!=null && dialogLoader.isShowing())dialogLoader.cancel();
        dialogLoader = new Dialog(LoaderClass.context, R.style.Theme_AppCompat_NoActionBar);
        dialogLoader.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#8D000000")));
        final View view = ((Activity) LoaderClass.context).getLayoutInflater().inflate(R.layout.custom_dialog_loader_, null);
        LottieAnimationView animationView = view.findViewById(R.id.loader);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAnimation();
            }
        });
        animationView.playAnimation();
        dialogLoader.setContentView(view);
        try {
            dialogLoader.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}