package ru.money;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ru.money.DBHelper.COLUMN_COUNTRY;
import static ru.money.DBHelper.COLUMN_ID;
import static ru.money.DBHelper.COLUMN_POSITION;
import static ru.money.DBHelper.TABLE_BANKNOTES;
import static ru.money.DBHelper.TABLE_CATEGORIES;

public class ListActivity extends AppCompatActivity implements NewCategoryDialogFragment.OnAddListener, CategoryRVAdapter.OnDeleteListener, BanknoteDialogFragment.OnAddListener {

    static final int USES_DB_VERSION = 2;
    static final String LOG_TAG = "ru.money";
    static String mode = "normal";
    private BanknoteRVAdapter banknoteRVAdapter;
    private CategoryRVAdapter categoryRVAdapter;
    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private int currID;
    private String type;
    private Menu menu;
    private float fabY;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "ListActivity is created");
        setContentView(R.layout.activity_list);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        // добавление в БД записи о главной категорииЮ если её ещё нет
        createID1();
        // получеие ID текущей категории, если это - первая, то ID = 1
        currID = getIntent().getIntExtra("parent", 1);
        // получение типа и названия открытой категории
        String parentName = "NaN";
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + currID, null, null, null, null);
        if (c.moveToFirst()) {
            type = c.getString(c.getColumnIndex("type"));
            int parentID = c.getInt(c.getColumnIndex("parent"));
            Cursor c2 = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + parentID, null, null, null, null);
            if (c2.moveToFirst())
                parentName = c.getString(c.getColumnIndex("name"));
            c2.close();
        }
        c.close();
        Toolbar toolbar = findViewById(R.id.toolbar);
        // если это не главная категория, добавить кнопку "назад"
        setSupportActionBar(toolbar);
        if (currID != 1) {
            getSupportActionBar().setTitle(parentName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // определение высоты
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        height = metrics.heightPixels;
        setDragListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void createID1() {
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = 1", null, null, null, null);
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
        if (mode.equals("normal"))
            switch (type) {
                case "category":
                    Log.i(LOG_TAG, "Opening NewCategoryDialog");
                    NewCategoryDialogFragment categoryDialogFragment = new NewCategoryDialogFragment();
                    categoryDialogFragment.show(getSupportFragmentManager(), "add_category");
                    break;
                case "banknotes":
                    Log.i(LOG_TAG, "Opening NewBanknoteDialog");
                    BanknoteDialogFragment banknoteDialogFragment = new BanknoteDialogFragment();
                    banknoteDialogFragment.setOnAddListener(this);
                    banknoteDialogFragment.show(getSupportFragmentManager(), "add_banknote");
                    break;
                default:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final Context context = this;
                    builder.setTitle(R.string.select_add)
                            .setItems(new CharSequence[]{getString(R.string.categoryName), getString(R.string.banknote)}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            Log.i(LOG_TAG, "Opening NewCategoryDialog");
                                            NewCategoryDialogFragment categoryDialogFragment = new NewCategoryDialogFragment();
                                            categoryDialogFragment.show(getSupportFragmentManager(), "add_category");
                                            break;
                                        case 1:
                                            Log.i(LOG_TAG, "Opening NewBanknoteDialog");
                                            BanknoteDialogFragment banknoteDialogFragment = new BanknoteDialogFragment();
                                            banknoteDialogFragment.setOnAddListener(context);
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
        Log.i(LOG_TAG, "Getting data from database...");
        Cursor c;
        RecyclerView mainView = findViewById(R.id.main);
        mainView.setLayoutManager(new LinearLayoutManager(this));
        TextView view = findViewById(R.id.noItemsText);
        switch (type) {
            case "category":
                List<Category> categoryList = new ArrayList<>();
                c = database.query(TABLE_CATEGORIES, null, "parent = '" + currID + "'", null, null, null, "position");
                if (c.getCount() == 0) {
                    view.setText(getString(R.string.no_items));
                    view.setVisibility(View.VISIBLE);
                    cleanCategory();
                } else if (c.moveToFirst()) {
                    view.setVisibility(View.GONE);
                    do {
                        String name = c.getString(c.getColumnIndex("name"));
                        String image = c.getString(c.getColumnIndex("image"));
                        int id = c.getInt(c.getColumnIndex(COLUMN_ID));
                        categoryList.add(new Category(name, image, 0, id));
                    } while (c.moveToNext());
                }
                c.close();
                Log.i(LOG_TAG, "Data is loaded, updating list...");
                categoryRVAdapter = new CategoryRVAdapter(categoryList, this);
                mainView.setAdapter(categoryRVAdapter);
                Log.i(LOG_TAG, "List updated");
                break;
            case "banknotes":
                List<Banknote> banknoteList = new ArrayList<>();
                c = database.query(TABLE_BANKNOTES, null, "parent = '" + currID + "'", null, null, null, "position");
                if (c.getCount() == 0) {
                    view.setText(getString(R.string.no_items));
                    view.setVisibility(View.VISIBLE);
                    cleanCategory();
                } else if (c.moveToFirst()) {
                    view.setVisibility(View.GONE);
                    do {
                        int id = c.getInt(c.getColumnIndex(COLUMN_ID));
                        String name = c.getString(c.getColumnIndex("name"));
                        String circulationTime = c.getString(c.getColumnIndex("circulation"));
                        String obversePath = c.getString(c.getColumnIndex("obverse"));
                        String country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
                        banknoteList.add(new Banknote(id, country, name, circulationTime, obversePath));
                    } while (c.moveToNext());
                }
                c.close();
                Log.i(LOG_TAG, "Data is loaded, updating list...");
                banknoteRVAdapter = new BanknoteRVAdapter(banknoteList, this);
                mainView.setAdapter(banknoteRVAdapter);
                Log.i(LOG_TAG, "List updated");
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
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + currID, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                Log.i(LOG_TAG, "Settings button clicked");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                Log.i(LOG_TAG, "Back button on toolbar selected, finishing");
                onBackPressed();
                break;
            case R.id.edit:
                goToEditMode();
                break;
            case R.id.done:
                goToNormalMode();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        if (mode.equals("edit"))
            goToNormalMode();
        else
            super.onBackPressed();
    }

    private void goToNormalMode() {
        mode = "normal";
        menu.clear();
        getMenuInflater().inflate(R.menu.main_menu, menu);
        findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.vertical_slide);
        animator.setFloatValues(findViewById(R.id.fab).getY(), fabY);
        animator.setTarget(findViewById(R.id.fab));
        animator.start();
        updateList();
    }

    void goToEditMode() {
        mode = "edit";
        menu.clear();
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.editMode));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.vertical_slide);
        fabY = findViewById(R.id.fab).getY();
        animator.setFloatValues(findViewById(R.id.fab).getY(), height);
        animator.setTarget(findViewById(R.id.fab));
        animator.start();
    }

    void setDragListener() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (type.equals("category")) {
                    updateCategoryPosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    Collections.swap(categoryRVAdapter.getCardList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    categoryRVAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                } else if (type.equals("banknotes")) {
                    updateBanknotePosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    Collections.swap(banknoteRVAdapter.getCardList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    banknoteRVAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return mode.equals("edit");
            }
        });
        itemTouchHelper.attachToRecyclerView((RecyclerView) findViewById(R.id.main));
    }

    void updateCategoryPosition(int oldPos, int newPos) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_POSITION, oldPos);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + categoryRVAdapter.getCardList().get(newPos).id, null);
        cv = new ContentValues();
        cv.put(COLUMN_POSITION, newPos);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + categoryRVAdapter.getCardList().get(oldPos).id, null);
    }

    void updateBanknotePosition(int oldPos, int newPos) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_POSITION, oldPos);
        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + banknoteRVAdapter.getCardList().get(newPos).id, null);
        cv = new ContentValues();
        cv.put(COLUMN_POSITION, newPos);
        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + banknoteRVAdapter.getCardList().get(oldPos).id, null);
    }

    @Override
    public void addNewCategory(String name, String flagPath, String type) {
        Log.i(LOG_TAG, "Adding new category");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("image", flagPath);
        cv.put("type", type);
        cv.put("parent", currID);
        Cursor c = database.query(TABLE_CATEGORIES, null, "parent = '" + currID + "'", null, null, null, "position");
        cv.put(COLUMN_POSITION, c.getCount() + 1);
        c.close();
        database.insert(TABLE_CATEGORIES, null, cv);
        Log.i(LOG_TAG, "Category was added");
        cv = new ContentValues();
        cv.put("type", "category");
        this.type = "category";
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + currID, null);
        updateList();
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {
        Log.i(LOG_TAG, "Adding new banknote");
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("circulation", circulationTime);
        cv.put(COLUMN_COUNTRY, country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put("description", description);
        cv.put("parent", currID);
        Cursor c = database.query(TABLE_BANKNOTES, null, "parent = '" + currID + "'", null, null, null, "position");
        cv.put(COLUMN_POSITION, c.getCount() + 1);
        c.close();
        database.insert(TABLE_BANKNOTES, null, cv);
        Log.i(LOG_TAG, "Banknote added");
        cv = new ContentValues();
        cv.put("type", "banknotes");
        type = "banknotes";
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + currID, null);
        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Closing dbHelper");
        dbHelper.close();
    }

    @Override
    public void deleteCategory(int id) {
        Log.i(LOG_TAG, "Deleting category");
        database.delete(TABLE_CATEGORIES, COLUMN_ID + " = " + id, null);
        database.delete(TABLE_CATEGORIES, "parent = '" + id + "'", null);
        database.delete(TABLE_BANKNOTES, "parent = '" + id + "'", null);
        Log.i(LOG_TAG, "Category was deleted");
        updateList();
    }
}
