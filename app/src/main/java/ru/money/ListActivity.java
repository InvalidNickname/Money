package ru.money;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ru.money.DBHelper.TABLE_BANKNOTES;
import static ru.money.DBHelper.TABLE_CATEGORIES;

public class ListActivity extends AppCompatActivity implements NewCategoryDialogFragment.OnAddListener, CategoryRVAdapter.OnDeleteListener, NewBanknoteDialogFragment.OnAddListener {

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private TextView view;
    private int currID;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getPackageName(), "ListActivity is created");
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        currID = getIntent().getIntExtra("parent", 1);
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        createID1();
        Cursor c = database.query(TABLE_CATEGORIES, null, "_id = ?", new String[]{String.valueOf(currID)}, null, null, null);
        String parentName = "NaN";
        if (c.moveToFirst()) {
            type = c.getString(c.getColumnIndex("type"));
            int parentID = c.getInt(c.getColumnIndex("parent"));
            Cursor c2 = database.query(TABLE_CATEGORIES, null, "_id = ?", new String[]{String.valueOf(parentID)}, null, null, null);
            if (c2.moveToFirst()) {
                parentName = c.getString(c.getColumnIndex("name"));
            }
            c2.close();
        }
        c.close();
        if (currID != 1) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(parentName);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        } else {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
        view = findViewById(R.id.noItemsText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void createID1() {
        Cursor c = database.query(TABLE_CATEGORIES, null, "_id = ?", new String[]{String.valueOf(1)}, null, null, null);
        if (c.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put("name", "main");
            cv.put("image", "nothing");
            cv.put("type", "no category");
            cv.put("parent", 0);
            database.insert("categories", null, cv);
        }
        c.close();
    }

    public void openAddDialog(View view) {
        switch (type) {
            case "category":
                Log.i(getPackageName(), "Opening NewCountryDialog");
                DialogFragment categoryDialogFragment = new NewCategoryDialogFragment();
                categoryDialogFragment.show(getSupportFragmentManager(), "add_country");
                break;
            case "banknotes":
                Log.i(getPackageName(), "Opening NewBanknoteDialog");
                DialogFragment banknoteDialogFragment = new NewBanknoteDialogFragment();
                banknoteDialogFragment.show(getSupportFragmentManager(), "add_banknote");
                break;
            default:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.select_add)
                        .setItems(new CharSequence[]{getString(R.string.categoryName), getString(R.string.banknote)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Log.i(getPackageName(), "Opening NewCategoryDialog");
                                        DialogFragment categoryDialogFragment = new NewCategoryDialogFragment();
                                        categoryDialogFragment.show(getSupportFragmentManager(), "add_country");
                                        break;
                                    case 1:
                                        Log.i(getPackageName(), "Opening NewBanknoteDialog");
                                        DialogFragment banknoteDialogFragment = new NewBanknoteDialogFragment();
                                        banknoteDialogFragment.show(getSupportFragmentManager(), "add_banknote");
                                        break;
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
        }
    }

    private void updateList() {
        Log.i(getPackageName(), "Getting data from database...");
        Cursor c;
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        switch (type) {
            case "category":
                List<Category> categoryList = new ArrayList<>();
                c = database.query(TABLE_CATEGORIES, null, "parent = '" + currID + "'", null, null, null, "name");
                if (c.getCount() == 0) {
                    view.setText(getString(R.string.no_items));
                    view.setVisibility(View.VISIBLE);
                    cleanCategory();
                } else if (c.moveToFirst()) {
                    view.setVisibility(View.GONE);
                    do {
                        String name = c.getString(c.getColumnIndex("name"));
                        String image = c.getString(c.getColumnIndex("image"));
                        int id = c.getInt(c.getColumnIndex("_id"));
                        categoryList.add(new Category(name, image, 0, id));
                    } while (c.moveToNext());
                }
                c.close();
                Log.i(getPackageName(), "Data is loaded, updating list...");
                mainView.setAdapter(new CategoryRVAdapter(categoryList, this));
                Log.i(getPackageName(), "List updated");
                break;
            case "banknotes":
                List<Banknote> banknoteList = new ArrayList<>();
                c = database.query(TABLE_BANKNOTES, null, "parent = '" + currID + "'", null, null, null, null);
                if (c.getCount() == 0) {
                    view.setText(getString(R.string.no_items));
                    view.setVisibility(View.VISIBLE);
                    cleanCategory();
                } else if (c.moveToFirst()) {
                    view.setVisibility(View.GONE);
                    do {
                        int id = c.getInt(c.getColumnIndex("_id"));
                        String name = c.getString(c.getColumnIndex("name"));
                        String circulationTime = c.getString(c.getColumnIndex("circulation"));
                        String obversePath = c.getString(c.getColumnIndex("obverse"));
                        String country = c.getString(c.getColumnIndex("country"));
                        banknoteList.add(new Banknote(id, country, name, circulationTime, obversePath));
                    } while (c.moveToNext());
                }
                c.close();
                Log.i(getPackageName(), "Data is loaded, updating list...");
                mainView.setAdapter(new BanknoteRVAdapter(banknoteList, this));
                Log.i(getPackageName(), "List updated");
                break;
            default:
                view.setText(getString(R.string.no_items));
                view.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void cleanCategory() {
        ContentValues cv = new ContentValues();
        cv.put("type", "no category");
        type = "no category";
        database.update(TABLE_CATEGORIES, cv, "_id = ?", new String[]{String.valueOf(currID)});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                Log.i(getPackageName(), "Settings button clicked");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                Log.i(getPackageName(), "Back button on toolbar selected, finishing");
                finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void addNewCategory(String name, String flagPath, String type) {
        Log.i(getPackageName(), "Adding new category");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("image", flagPath);
        cv.put("type", type);
        cv.put("parent", currID);
        database.insert(TABLE_CATEGORIES, null, cv);
        Log.i(getPackageName(), "Category was added");
        cv = new ContentValues();
        cv.put("type", "category");
        this.type = "category";
        database.update(TABLE_CATEGORIES, cv, "_id = ?", new String[]{String.valueOf(currID)});
        updateList();
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {
        Log.i(getPackageName(), "Adding new banknote");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("circulation", circulationTime);
        cv.put("country", country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put("description", description);
        cv.put("parent", currID);
        database.insert(TABLE_BANKNOTES, null, cv);
        Log.i(getPackageName(), "Banknote added");
        cv = new ContentValues();
        cv.put("type", "banknotes");
        type = "banknotes";
        database.update(TABLE_CATEGORIES, cv, "_id = ?", new String[]{String.valueOf(currID)});
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(getPackageName(), "Closing dbHelper");
        dbHelper.close();
    }

    @Override
    public void deleteCategory(int id) {
        Log.i(getPackageName(), "Deleting category");
        database.delete(TABLE_CATEGORIES, "_id = '" + id + "'", null);
        database.delete(TABLE_BANKNOTES, "parent = '" + id + "'", null);
        Log.i(getPackageName(), "Category was deleted");
        updateList();
    }
}
