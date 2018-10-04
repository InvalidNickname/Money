package ru.money;

import android.app.Application;
import android.util.DisplayMetrics;

public class App extends Application {

    static final String LOG_TAG = "ru.money";
    static public int width;
    static public int height;

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
    }
}
