package com.hack.faceml;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class FaceDetection extends Application {
    public static final String result_text ="RESULT_TEXT";
    public static final String result_dialog ="RESULT_DIALOG";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
