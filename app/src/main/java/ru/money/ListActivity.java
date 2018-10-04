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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.money.dialog.BanknoteDialogFragment;
import ru.money.dialog.NewCategoryDialogFragment;

import static ru.money.App.LOG_TAG;
import static ru.money.App.height;
import static ru.money.DBHelper.COLUMN_COUNTRY;
import static ru.money.DBHelper.COLUMN_DESCRIPTION;
import static ru.money.DBHelper.COLUMN_ID;
import static ru.money.DBHelper.COLUMN_IMAGE;
import static ru.money.DBHelper.COLUMN_NAME;
import static ru.money.DBHelper.COLUMN_PARENT;
import static ru.money.DBHelper.COLUMN_POSITION;
import static ru.money.DBHelper.COLUMN_TYPE;
import static ru.money.DBHelper.TABLE_BANKNOTES;
import static ru.money.DBHelper.TABLE_CATEGORIES;

public class ListActivity extends AppCompatActivity
        implements NewCategoryDialogFragment.OnAddListener, CategoryRVAdapter.OnDeleteListener, BanknoteDialogFragment.OnAddListener {

    static final int USES_DB_VERSION = 2;
    static String mode = "normal";
    private SQLiteDatabase database;
    private int currID;
    private String type;
    private Menu menu;
    private float fabY;
    private RecyclerView main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "ListActivity is created");
        // установка выбранного размера шрифта
        Utils.updateFontScale(this);
        setContentView(R.layout.activity_list);
        // создание или открытие БД
        database = DBHelper.getInstance(this).getWritableDatabase();
        // получение ID текущей категории, если это - первая, то ID = 1
        currID = getIntent().getIntExtra("parent", 1);
        // получение типа и названия открытой категории
        String parentName = "NaN";
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + currID, null, null, null, null);
        if (c.moveToFirst()) {
            type = c.getString(c.getColumnIndex(COLUMN_TYPE));
            int parentID = c.getInt(c.getColumnIndex(COLUMN_PARENT));
            Cursor c2 = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + parentID, null, null, null, null);
            if (c2.moveToFirst())
                parentName = c.getString(c.getColumnIndex(COLUMN_NAME));
            c2.close();
        }
        c.close();
        // если это не главная категория, добавить кнопку "назад" и заголовок
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (currID != 1) {
            getSupportActionBar().setTitle(parentName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // поиск RecyclerView
        main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
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

    public void openAddDialog(View view) {
        if (mode.equals("normal"))
            switch (type) {
                case "category":
                    Log.i(LOG_TAG, "Opening NewCategoryDialog");
                    (new NewCategoryDialogFragment()).show(getSupportFragmentManager(), "add_category");
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
                                            (new NewCategoryDialogFragment()).show(getSupportFragmentManager(), "add_category");
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
        ListUpdater updater = new ListUpdater(type, currID, this);
        updater.execute();
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
            case R.id.swap:
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

    private void goToEditMode() {
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

    private void setDragListener() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (type.equals("category")) {
                    updateCategoryPosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    Collections.swap(((CategoryRVAdapter) main.getAdapter()).getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    main.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                } else if (type.equals("banknotes")) {
                    updateBanknotePosition(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    Collections.swap(((BanknoteRVAdapter) main.getAdapter()).getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    main.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
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
        itemTouchHelper.attachToRecyclerView(main);
    }

    private void updateCategoryPosition(int oldPos, int newPos) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_POSITION, oldPos);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + ((CategoryRVAdapter) main.getAdapter()).getList().get(newPos).getId(), null);
        cv = new ContentValues();
        cv.put(COLUMN_POSITION, newPos);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + ((CategoryRVAdapter) main.getAdapter()).getList().get(oldPos).getId(), null);
    }

    private void updateBanknotePosition(int oldPos, int newPos) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_POSITION, oldPos);
        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + ((BanknoteRVAdapter) main.getAdapter()).getList().get(newPos).id, null);
        cv = new ContentValues();
        cv.put(COLUMN_POSITION, newPos);
        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + ((BanknoteRVAdapter) main.getAdapter()).getList().get(oldPos).id, null);
    }

    @Override
    public void addNewCategory(String name, String flagPath, String type) {
        Log.i(LOG_TAG, "Adding new category");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_IMAGE, flagPath);
        cv.put(COLUMN_TYPE, type);
        cv.put(COLUMN_PARENT, currID);
        cv.put(COLUMN_POSITION, main.getAdapter() == null ? 1 : main.getAdapter().getItemCount() + 1);
        database.insert(TABLE_CATEGORIES, null, cv);
        Log.i(LOG_TAG, "Category was added");
        this.type = DBHelper.updateCategoryType(database, currID, "category");
        updateList();
    }

    @Override
    public void addNewBanknote(String name, String circulationTime, String obversePath, String reversePath, String description, String country) {
        Log.i(LOG_TAG, "Adding new banknote");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put("circulation", circulationTime);
        cv.put(COLUMN_COUNTRY, country);
        cv.put("obverse", obversePath);
        cv.put("reverse", reversePath);
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_PARENT, currID);
        cv.put(COLUMN_POSITION, main.getAdapter() == null ? 1 : main.getAdapter().getItemCount() + 1);
        database.insert(TABLE_BANKNOTES, null, cv);
        Log.i(LOG_TAG, "Banknote added");
        type = DBHelper.updateCategoryType(database, currID, "banknotes");
        updateList();
    }

    @Override
    public void deleteCategory(int id) {
        Log.i(LOG_TAG, "Deleting category...");
        deleteChildren(id);
        Log.i(LOG_TAG, "Category was deleted");
        updateList();
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
                    database.delete(TABLE_CATEGORIES, COLUMN_ID + " = " + query.getInt(query.getColumnIndex(COLUMN_ID)), null);
                    // удаление изображений
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex("obverse")), this);
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex("reverse")), this);
                } while (query.moveToNext());
            query.close();
        }
    }
}
