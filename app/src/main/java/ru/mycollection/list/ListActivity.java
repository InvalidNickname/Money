package ru.mycollection.list;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Collections;
import java.util.List;

import ru.mycollection.BaseActivity;
import ru.mycollection.R;
import ru.mycollection.dialog.BanknoteDialogFragment;
import ru.mycollection.dialog.CategoryDialogFragment;
import ru.mycollection.dialog.ItemNameDialogFragment;
import ru.mycollection.dialog.SearchDialogFragment;
import ru.mycollection.list.modemanager.ModeManager;
import ru.mycollection.settings.SettingsActivity;
import ru.mycollection.utils.DBHelper;
import ru.mycollection.utils.Utils;

import static ru.mycollection.App.LOG_TAG;
import static ru.mycollection.list.modemanager.Mode.Edit;
import static ru.mycollection.list.modemanager.Mode.Move;
import static ru.mycollection.list.modemanager.Mode.Normal;
import static ru.mycollection.list.modemanager.Mode.Search;
import static ru.mycollection.utils.DBHelper.COLUMN_CIRCULATION;
import static ru.mycollection.utils.DBHelper.COLUMN_COUNTRY;
import static ru.mycollection.utils.DBHelper.COLUMN_DESCRIPTION;
import static ru.mycollection.utils.DBHelper.COLUMN_ID;
import static ru.mycollection.utils.DBHelper.COLUMN_IMAGE;
import static ru.mycollection.utils.DBHelper.COLUMN_NAME;
import static ru.mycollection.utils.DBHelper.COLUMN_OBVERSE;
import static ru.mycollection.utils.DBHelper.COLUMN_PARENT;
import static ru.mycollection.utils.DBHelper.COLUMN_POSITION;
import static ru.mycollection.utils.DBHelper.COLUMN_REVERSE;
import static ru.mycollection.utils.DBHelper.COLUMN_TYPE;
import static ru.mycollection.utils.DBHelper.TABLE_BANKNOTES;
import static ru.mycollection.utils.DBHelper.TABLE_CATEGORIES;

public class ListActivity extends BaseActivity
        implements CategoryDialogFragment.OnChangeListener, CategoryRVAdapter.OnDeleteListener, BanknoteDialogFragment.OnChangeListener,
        CategoryRVAdapter.OnAddListener, SearchDialogFragment.OnSearchListener {

    private ModeManager modeManager;
    private SQLiteDatabase database;
    private int currID;
    private int parentID;
    private String type;
    private Adapter adapter;

    private int moveID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "ListActivity is created");
        setContentView(R.layout.activity_list);
        // сохранение резервной копии БД
        Utils.backupDB(this);
        // создание или открытие БД
        database = DBHelper.getInstance(this).getDatabase();
        // запуск идёт с главной категории, поэтому ID = 1
        currID = 1;
        // получение типа главной категории
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + currID, null, null, null, null);
        if (c.moveToFirst()) type = c.getString(c.getColumnIndex(COLUMN_TYPE));
        c.close();
        // если это не главная категория, добавить кнопку "назад" и заголовок
        setSupportActionBar(findViewById(R.id.toolbar));
        // инициализация ModeManager
        modeManager = new ModeManager(this);
        // поиск RecyclerView и установка слушателей
        RecyclerView main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
        setDragListener(main);
        updateList(true);
        initializeAd();
        checkIfItemNameSet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        modeManager.setMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void checkIfItemNameSet() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("item_name", "").equals("")) {
            ItemNameDialogFragment dialogFragment = new ItemNameDialogFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "item_name");
        }
    }

    private void initializeAd() {
        AdView adView = findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("update", false)) updateList(true);
        if (intent.getIntExtra("id", -1) != -1) {
            moveID = intent.getIntExtra("id", -1);
            modeManager.setMode(Move);
        }
    }

    public void openAddDialog(View view) {
        if (ModeManager.getMode() == Normal)
            switch (type) {
                case "category":
                    Log.i(LOG_TAG, "Opening NewCategoryDialog");
                    (new CategoryDialogFragment()).show(getSupportFragmentManager(), "add_category");
                    break;
                case "banknotes":
                    Log.i(LOG_TAG, "Opening NewBanknoteDialog");
                    (new BanknoteDialogFragment()).show(getSupportFragmentManager(), "add_banknote");
                    break;
                default:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String namedItem = String.format(getString(R.string.banknote), PreferenceManager.getDefaultSharedPreferences(this).getString("item_name", ""));
                    builder.setTitle(R.string.select_add)
                            .setItems(new CharSequence[]{getString(R.string.categoryName), namedItem}, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        Log.i(LOG_TAG, "Opening NewCategoryDialog");
                                        (new CategoryDialogFragment()).show(getSupportFragmentManager(), "add_category");
                                        break;
                                    case 1:
                                        Log.i(LOG_TAG, "Opening NewBanknoteDialog");
                                        (new BanknoteDialogFragment()).show(getSupportFragmentManager(), "add_banknote");
                                        break;
                                }
                            });
                    builder.create().show();
            }
    }

    private void updateList(boolean animationNeeded) {
        // обновление списка
        Log.i(LOG_TAG, "Getting data from database...");
        ListUpdater updater = new ListUpdater(type, currID, animationNeeded, this);
        updater.setOnLoadListener((newType, parent, newAdapter) -> {
            type = newType;
            parentID = parent;
            adapter = newAdapter;
        });
        updater.execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                Log.i(LOG_TAG, "Settings button clicked");
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case android.R.id.home:
                Log.i(LOG_TAG, "Back button on toolbar selected");
                onBackPressed();
                break;
            case R.id.swap:
                modeManager.setMode(Edit);
                break;
            case R.id.done:
                if (ModeManager.getMode() == Move && type.equals("banknotes")) {
                    ContentValues cv = new ContentValues();
                    cv.put(COLUMN_PARENT, currID);
                    database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + moveID, null);
                    modeManager.setMode(Normal);
                    updateList(false);
                }
                if (ModeManager.getMode() == Edit) {
                    modeManager.setMode(Normal);
                    updateList(false);
                }
                break;
            case R.id.cancel:
                modeManager.setMode(Normal);
                break;
            case R.id.search:
                Log.i(LOG_TAG, "Opening search dialog");
                (new SearchDialogFragment()).show(getSupportFragmentManager(), "search");
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void goBack() {
        if (currID == 1) finish();
        else {
            currID = parentID;
            updateList(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!(ModeManager.getMode() == Normal) && !(ModeManager.getMode() == Move)) {
            modeManager.setMode(Normal);
            updateList(false);
        } else goBack();
    }

    private void setDragListener(RecyclerView recyclerView) {
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                List list = type.equals("category") ? ((CategoryRVAdapter) adapter).getList() : ((BanknoteRVAdapter) adapter).getList();
                Collections.swap(list, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return ModeManager.getMode() == Edit;
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public void addNewCategory(String name, String flagPath, String type) {
        Log.i(LOG_TAG, "Adding new category");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_IMAGE, flagPath);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_PARENT, currID);
        cv.put(COLUMN_POSITION, adapter == null ? 1 : adapter.getItemCount() + 1);
        database.insert(TABLE_CATEGORIES, null, cv);
        Log.i(LOG_TAG, "Category was added");
        this.type = DBHelper.updateCategoryType(currID, "category");
        updateList(false);
    }

    @Override
    public void updateCategory(String name, String flagPath, int id) {
        Log.i(LOG_TAG, "Updating category...");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_IMAGE, flagPath);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + id, null);
        Log.i(LOG_TAG, "Category updated");
        updateList(false);
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {
        Log.i(LOG_TAG, "Adding new banknote");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_CIRCULATION, circulationTime);
        cv.put(COLUMN_COUNTRY, country);
        cv.put(COLUMN_OBVERSE, obversePath);
        cv.put(COLUMN_REVERSE, reversePath);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_PARENT, currID);
        cv.put(COLUMN_POSITION, adapter == null ? 1 : adapter.getItemCount() + 1);
        database.insert(TABLE_BANKNOTES, null, cv);
        Log.i(LOG_TAG, "Banknote added");
        type = DBHelper.updateCategoryType(currID, "banknotes");
        updateList(false);
    }

    @Override
    public void updateBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {

    }

    @Override
    public void deleteCategory(int id) {
        Log.i(LOG_TAG, "Deleting category...");
        deleteChildren(id);
        Log.i(LOG_TAG, "Category was deleted");
        updateList(false);
    }

    private void deleteChildren(int id) {
        // определение типа удаляемой категории
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        String type = c.getString(c.getColumnIndex(COLUMN_TYPE));
        String image = c.getString(c.getColumnIndex(COLUMN_IMAGE));
        c.close();
        // удаление категории и иконки категории
        database.delete(TABLE_CATEGORIES, COLUMN_ID + " = " + id, null);
        Utils.deleteFromFiles(image, this);
        // если тип - категории, рекурсивно удаляются следующие
        if (type.equals("category")) {
            Cursor query = database.query(TABLE_CATEGORIES, null, COLUMN_PARENT + " = " + id, null, null, null, null);
            if (query.moveToFirst())
                do {
                    deleteChildren(query.getInt(query.getColumnIndex(COLUMN_ID)));
                } while (query.moveToNext());
            query.close();
        } // если банкноты - просто очистка категории
        else if (type.equals("banknotes")) {
            Cursor query = database.query(TABLE_BANKNOTES, null, COLUMN_PARENT + " = " + id, null, null, null, null);
            if (query.moveToFirst())
                do {
                    database.delete(TABLE_BANKNOTES, COLUMN_ID + " = " + query.getInt(query.getColumnIndex(COLUMN_ID)), null);
                    // удаление изображений
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex(COLUMN_OBVERSE)), this);
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex(COLUMN_REVERSE)), this);
                } while (query.moveToNext());
            query.close();
        }
    }

    @Override
    public void loadNewCategory(int id) {
        currID = id;
        updateList(true);
    }

    @Override
    public void searchForBanknote(String name, int search) {
        modeManager.setMode(Search);
        // обновление списка
        Log.i(LOG_TAG, "Getting data from database...");
        ListUpdater updater = new ListUpdater(name, true, search, this);
        updater.setOnLoadListener((newType, parent, newAdapter) -> {
            type = newType;
            parentID = parent;
            adapter = newAdapter;
        });
        updater.execute();
    }
}
