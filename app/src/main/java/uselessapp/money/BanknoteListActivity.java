package uselessapp.money;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class BanknoteListActivity extends AppCompatActivity implements NewBanknoteDialogFragment.OnAddListener {

    DBHelper dbHelper;
    SQLiteDatabase database;
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    void updateList() {
        List<Banknote> banknoteList = new ArrayList<>();
        Cursor c = database.query("banknotes", null, "country = '" + country + "'", null, null, null, null);
        if (c.moveToFirst()) {
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
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        mainView.setAdapter(new BanknoteRVAdapter(banknoteList, this));
    }

    public void openAddDialog(View view) {
        DialogFragment newFragment = new NewBanknoteDialogFragment();
        newFragment.show(getSupportFragmentManager(), "add_banknote");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
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
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
