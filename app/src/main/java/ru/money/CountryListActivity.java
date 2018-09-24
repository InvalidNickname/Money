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

public class CountryListActivity extends AppCompatActivity implements NewCountryDialogFragment.OnAddListener, CountryRVAdapter.OnDeleteListener {

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private List<Country> cardList;
    private String continent;
    private TextView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getPackageName(), "CountryListActivity is created");
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        continent = getIntent().getStringExtra("continent");
        toolbar.setTitle(continent);
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

    public void openAddDialog(View view) {
        Log.i(getPackageName(), "Opening NewCountryDialog");
        DialogFragment newFragment = new NewCountryDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_country");
    }

    private void updateList() {
        Log.i(getPackageName(), "Getting data from database...");
        cardList = new ArrayList<>();
        Cursor c = database.query("countries", null, "continent = '" + continent + "'", null, null, null, null);
        if (c.getCount() == 0) {
            view.setText(getString(R.string.no_countries));
            view.setVisibility(View.VISIBLE);
        } else if (c.moveToFirst()) {
            view.setVisibility(View.GONE);
            do {
                String name = c.getString(c.getColumnIndex("name"));
                String flagPath = c.getString(c.getColumnIndex("flag"));
                Cursor c2 = database.query("banknotes", null, "country = '" + name + "'", null, null, null, null);
                cardList.add(new Country(name, flagPath, c2.getCount()));
                c2.close();
            } while (c.moveToNext());
        }
        c.close();
        Log.i(getPackageName(), "Updating list...");
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setAdapter(new CountryRVAdapter(cardList, this));
        Log.i(getPackageName(), "List updated");
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
    public void addNewCountry(String name, String flagPath) {
        Log.i(getPackageName(), "Adding new country");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("flag", flagPath);
        cv.put("continent", continent);
        database.insert("countries", null, cv);
        Log.i(getPackageName(), "Country was added");
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(getPackageName(), "Closing dbHelper");
        dbHelper.close();
    }

    @Override
    public void deleteCountry(int id) {
        Log.i(getPackageName(), "Deleting country");
        database.delete("countries", "name='" + cardList.get(id).country + "'", null);
        database.delete("banknotes", "country='" + cardList.get(id).country + "'", null);
        Log.i(getPackageName(), "Country was deleted");
        updateList();
    }
}
