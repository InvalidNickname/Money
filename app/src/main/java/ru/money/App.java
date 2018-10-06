package ru.money;

import android.app.Application;
import android.util.DisplayMetrics;

public class App extends Application {

    public static final String LOG_TAG = "ru.money";
    public static final int USES_DB_VERSION = 2;
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
