package ru.money;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import ru.money.settings.SettingsActivity;
import ru.money.utils.Utils;

import static ru.money.App.LOG_TAG;
import static ru.money.App.height;
import static ru.money.DBHelper.COLUMN_CIRCULATION;
import static ru.money.DBHelper.COLUMN_COUNTRY;
import static ru.money.DBHelper.COLUMN_DESCRIPTION;
import static ru.money.DBHelper.COLUMN_ID;
import static ru.money.DBHelper.COLUMN_IMAGE;
import static ru.money.DBHelper.COLUMN_NAME;
import static ru.money.DBHelper.COLUMN_OBVERSE;
import static ru.money.DBHelper.COLUMN_PARENT;
import static ru.money.DBHelper.COLUMN_POSITION;
import static ru.money.DBHelper.COLUMN_REVERSE;
import static ru.money.DBHelper.COLUMN_TYPE;
import static ru.money.DBHelper.TABLE_BANKNOTES;
import static ru.money.DBHelper.TABLE_CATEGORIES;

public class ListActivity extends AppCompatActivity
        implements NewCategoryDialogFragment.OnAddListener, CategoryRVAdapter.OnDeleteListener, BanknoteDialogFragment.OnAddListener {

    static String mode = "normal";
    private SQLiteDatabase database;
    private int currID;
    private String type;
    private Menu menu;
    private float fabY;
    private RecyclerView main;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "ListActivity is created");
        // установка выбранного размера шрифта
        Utils.updateFontScale(this);
        setContentView(R.layout.activity_list);
        // создание или открытие БД
        database = DBHelper.getInstance(this).getDatabase();
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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (currID != 1) {
            getSupportActionBar().setTitle(parentName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // поиск RecyclerView
        main = findViewById(R.id.main);
        main.setLayoutManager(new LinearLayoutManager(this));
        setDragListener();
        // поиск FloatingActionButton
        floatingActionButton = findViewById(R.id.fab);
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
                    (new BanknoteDialogFragment()).show(getSupportFragmentManager(), "add_banknote");
                    break;
                default:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.select_add)
                            .setItems(new CharSequence[]{getString(R.string.categoryName), getString(R.string.banknote)}, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        Log.i(LOG_TAG, "Opening NewCategoryDialog");
                                        (new NewCategoryDialogFragment()).show(getSupportFragmentManager(), "add_category");
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

    private void updateList() {
        Log.i(LOG_TAG, "Getting data from database...");
        ListUpdater updater = new ListUpdater(type, currID, this);
        updater.setOnLoadListener(newType -> type = newType);
        updater.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
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
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.vertical_slide);
        animator.setFloatValues(floatingActionButton.getY(), fabY);
        animator.setTarget(floatingActionButton);
        animator.start();
        updateList();
    }

    private void goToEditMode() {
        mode = "edit";
        menu.clear();
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        toolbar.setBackgroundColor(getResources().getColor(R.color.editMode));
        ObjectAnimator animator = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.vertical_slide);
        fabY = floatingActionButton.getY();
        animator.setFloatValues(floatingActionButton.getY(), height);
        animator.setTarget(floatingActionButton);
        animator.start();
    }

    private void setDragListener() {
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                RecyclerView.Adapter adapter = main.getAdapter();
                if (type.equals("category"))
                    Collections.swap(((CategoryRVAdapter) adapter).getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                else if (type.equals("banknotes"))
                    Collections.swap(((BanknoteRVAdapter) adapter).getList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return mode.equals("edit");
            }
        }).attachToRecyclerView(main);
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
        this.type = DBHelper.updateCategoryType(currID, "category");
        updateList();
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
        cv.put(COLUMN_POSITION, main.getAdapter() == null ? 1 : main.getAdapter().getItemCount() + 1);
        database.insert(TABLE_BANKNOTES, null, cv);
        Log.i(LOG_TAG, "Banknote added");
        type = DBHelper.updateCategoryType(currID, "banknotes");
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
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex(COLUMN_OBVERSE)), this);
                    Utils.deleteFromFiles(query.getString(query.getColumnIndex(COLUMN_REVERSE)), this);
                } while (query.moveToNext());
            query.close();
        }
    }
}
