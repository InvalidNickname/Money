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
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class CountryListActivity extends AppCompatActivity implements NewCountryDialogFragment.OnAddListener {

    DBHelper dbHelper;
    SQLiteDatabase database;
    private List<Country> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        Cursor c = database.query("countries", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex("name"));
                String flagPath = c.getString(c.getColumnIndex("flag"));
                cardList.add(new Country(name, flagPath, 0));
            } while (c.moveToNext());
        }
        c.close();
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void add(View view) {
        DialogFragment newFragment = new NewCountryDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_country");
    }

    void updateList() {
        CountryRVAdapter countryRVAdapter = new CountryRVAdapter(cardList, this);
        RecyclerView mainView = findViewById(R.id.main);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainView.setLayoutManager(layoutManager);
        mainView.setAdapter(countryRVAdapter);
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
}
