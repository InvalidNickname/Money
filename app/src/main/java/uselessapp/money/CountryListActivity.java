package uselessapp.money;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class CountryListActivity extends AppCompatActivity implements NewCountryDialogFragment.OnAddListener, CountryRVAdapter.OnDeleteListener {

    DBHelper dbHelper;
    SQLiteDatabase database;
    private List<Country> cardList;
    static int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardList = new ArrayList<>();
        Cursor c = database.query("countries", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex("name"));
                String flagPath = c.getString(c.getColumnIndex("flag"));
                Cursor c2 = database.query("banknotes", null, "country = '" + name + "'", null, null, null, null);
                cardList.add(new Country(name, flagPath, c2.getCount()));
                c2.close();
            } while (c.moveToNext());
        }
        c.close();
        updateList();
    }

    public void openAddDialog(View view) {
        DialogFragment newFragment = new NewCountryDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_country");
    }

    void updateList() {
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setAdapter(new CountryRVAdapter(cardList, this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void addNewCountry(String name, String flagPath) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("flag", flagPath);
        database.insert("countries", null, cv);
        cardList.add(new Country(name, flagPath, 0));
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public void deleteCountry(int id) {
        database.delete("countries", "name='" + cardList.get(id).country + "'", null);
        database.delete("banknotes", "country='" + cardList.get(id).country + "'", null);
        cardList.remove(id);
        updateList();
    }
}
