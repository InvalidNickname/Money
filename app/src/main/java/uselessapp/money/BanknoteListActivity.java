package uselessapp.money;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class BanknoteListActivity extends AppCompatActivity implements NewBanknoteDialogFragment.OnAddListener {

    DBHelper dbHelper;
    SQLiteDatabase database;
    private List<Banknote> banknoteList = new ArrayList<>();
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Intent i = getIntent();
        country = i.getStringExtra("country");
        toolbar.setTitle(country);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        Cursor c = database.query("banknotes", null, "country = '" + country + "'", null, null, null, null);
        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex("name"));
                String circulationTime = c.getString(c.getColumnIndex("circulation"));
                String obversePath = c.getString(c.getColumnIndex("obverse"));
                String reversePath = c.getString(c.getColumnIndex("reverse"));
                String description = c.getString(c.getColumnIndex("description"));
                System.out.println(obversePath);
                banknoteList.add(new Banknote(country, name, circulationTime, obversePath, reversePath, description));
            } while (c.moveToNext());
        }
        c.close();
        updateList();
    }

    void updateList() {
        BanknoteRVAdapter banknoteRVAdapter = new BanknoteRVAdapter(banknoteList);
        RecyclerView mainView = findViewById(R.id.main);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainView.setLayoutManager(layoutManager);
        mainView.setAdapter(banknoteRVAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void add(View view) {
        DialogFragment newFragment = new NewBanknoteDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_banknote");
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("circulation", circulationTime);
        cv.put("country", country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put("description", description);
        database.insert("banknotes", null, cv);
        Cursor c = database.query("banknotes", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                System.out.println(c.getString(c.getColumnIndex("obverse")));
            } while (c.moveToNext());
        }
        c.close();
        banknoteList.add(new Banknote(country, name, circulationTime, obversePath, reversePath, description));
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
