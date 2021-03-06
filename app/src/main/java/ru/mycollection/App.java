package ru.mycollection;

import android.app.Application;
import android.util.DisplayMetrics;

import com.google.android.gms.ads.MobileAds;

import ru.mycollection.utils.Utils;

public class App extends Application {

    public static final String LOG_TAG = "ru.mycollection";
    public static final int USES_DB_VERSION = 2;
    static public int width;
    static public int height;

    @Override
    public void onCreate() {
        super.onCreate();
        // нахождение высоты и ширины экрана
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        MobileAds.initialize(this, "ca-app-pub-2853509457699224~5628614596");
        Utils.updateFontScale(this);
    }
}
