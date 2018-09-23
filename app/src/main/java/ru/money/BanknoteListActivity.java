package ru.money;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BanknoteListActivity extends AppCompatActivity implements NewBanknoteDialogFragment.OnAddListener {

    DBHelper dbHelper;
    SQLiteDatabase database;
    private String country;
    private TextView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getPackageName(), "BanknoteListActivity is created");
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        country = getIntent().getStringExtra("country");
        toolbar.setTitle(country);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        view = findViewById(R.id.noItemsText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    void updateList() {
        Log.i(getPackageName(), "Getting data from database...");
        List<Banknote> banknoteList = new ArrayList<>();
        Cursor c = database.query("banknotes", null, "country = '" + country + "'", null, null, null, null);
        if (c.getCount() == 0) {
            view.setText(getString(R.string.no_banknotes));
            view.setVisibility(View.VISIBLE);
        } else if (c.moveToFirst()) {
            view.setVisibility(View.GONE);
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                String name = c.getString(c.getColumnIndex("name"));
                String circulationTime = c.getString(c.getColumnIndex("circulation"));
                String obversePath = c.getString(c.getColumnIndex("obverse"));
                String description = c.getString(c.getColumnIndex("description"));
                banknoteList.add(new Banknote(id, country, name, circulationTime, obversePath, description));
            } while (c.moveToNext());
        }
        c.close();
        Log.i(getPackageName(), "Data is loaded, updating list...");
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setAdapter(new BanknoteRVAdapter(banknoteList, this));
        Log.i(getPackageName(), "List updated");
    }

    public void openAddDialog(View view) {
        Log.i(getPackageName(), "Opening NewBanknoteDialog");
        DialogFragment newFragment = new NewBanknoteDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_banknote");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Log.i(getPackageName(), "Back button on toolbar selected, finishing");
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description) {
        Log.i(getPackageName(), "Adding new banknote");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("circulation", circulationTime);
        cv.put("country", country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put("description", description);
        database.insert("banknotes", null, cv);
        Log.i(getPackageName(), "Banknote added");
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(getPackageName(), "Closing dbHelper");
        dbHelper.close();
    }
}
