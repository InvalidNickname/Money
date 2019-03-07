package ru.mycollection.list;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import ru.mycollection.R;
import ru.mycollection.utils.DBHelper;
import ru.mycollection.utils.Utils;

import static ru.mycollection.App.LOG_TAG;
import static ru.mycollection.utils.DBHelper.COLUMN_CIRCULATION;
import static ru.mycollection.utils.DBHelper.COLUMN_COUNTRY;
import static ru.mycollection.utils.DBHelper.COLUMN_ID;
import static ru.mycollection.utils.DBHelper.COLUMN_IMAGE;
import static ru.mycollection.utils.DBHelper.COLUMN_NAME;
import static ru.mycollection.utils.DBHelper.COLUMN_OBVERSE;
import static ru.mycollection.utils.DBHelper.COLUMN_PARENT;
import static ru.mycollection.utils.DBHelper.COLUMN_POSITION;
import static ru.mycollection.utils.DBHelper.COLUMN_TYPE;
import static ru.mycollection.utils.DBHelper.TABLE_BANKNOTES;
import static ru.mycollection.utils.DBHelper.TABLE_CATEGORIES;

class ListUpdater extends AsyncTask<Void, Void, Void> {

    private final WeakReference<AppCompatActivity> activity;
    private final SQLiteDatabase database;
    private final List<Banknote> banknoteList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private final boolean animationNeeded;
    private int currID;
    private String type;
    private OnLoadListener onLoadListener;
    private String newType;
    private int parent;
    private String parentName;
    private boolean searchMode;
    private String searchString;

    ListUpdater(String type, int currID, boolean animationNeeded, AppCompatActivity activity) {
        this.type = type;
        this.currID = currID;
        this.animationNeeded = animationNeeded;
        this.activity = new WeakReference<>(activity);
        searchMode = false;
        database = DBHelper.getInstance(activity).getDatabase();
    }

    ListUpdater(String searchString, boolean animationNeeded, AppCompatActivity activity) {
        this.searchString = searchString;
        this.animationNeeded = animationNeeded;
        this.activity = new WeakReference<>(activity);
        searchMode = true;
        newType = "banknotes";
        database = DBHelper.getInstance(activity).getDatabase();
    }

    @Nullable
    @Override
    protected Void doInBackground(Void... voids) {
        getData();
        if (!searchMode) updatePositions();
        setData();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // скрытие RecyclerView до загрузки нового списка
        ((RecyclerView) activity.get().findViewById(R.id.main)).setAdapter(null);
        // показ прогресса
        activity.get().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        // установка заголовка
        ActionBar actionBar = activity.get().getSupportActionBar();
        if (searchMode) {
            actionBar.setTitle(activity.get().getString(R.string.search));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        } else if (currID == 1) {
            actionBar.setTitle(activity.get().getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        } else {
            actionBar.setTitle(parentName);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onPostExecute(Void object) {
        Log.i(LOG_TAG, "Data is loaded, updating list...");
        // скрытие прогресса
        activity.get().findViewById(R.id.progressBar).setVisibility(View.GONE);
        // установка адаптера
        RecyclerView main = activity.get().findViewById(R.id.main);
        main.setAdapter(newType.equals("category") ? new CategoryRVAdapter(categoryList) : new BanknoteRVAdapter(banknoteList));
        Log.i(LOG_TAG, "List updated");
        // выводится надпись об отсутствии объектов в категории, тип категории сбрасывается
        TextView noItemsText = activity.get().findViewById(R.id.noItemsText);
        if (main.getAdapter() == null || main.getAdapter().getItemCount() == 0) {
            noItemsText.setVisibility(View.VISIBLE);
            newType = DBHelper.updateCategoryType(currID, "no category");
        } else noItemsText.setVisibility(View.GONE);
        if (animationNeeded) Utils.runLayoutAnimation(main);
        onLoadListener.loadFinished(newType, parent, main.getAdapter());
        super.onPostExecute(object);
    }

    private void getData() {
        if (!searchMode) {
            Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + currID, null, null, null, null);
            if (c.moveToFirst()) {
                newType = c.getString(c.getColumnIndex(COLUMN_TYPE));
                parent = c.getInt(c.getColumnIndex(COLUMN_PARENT));
                Cursor c2 = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + parent, null, null, null, null);
                if (c2.moveToFirst()) parentName = c.getString(c.getColumnIndex(COLUMN_NAME));
                c2.close();
            }
            c.close();
        }
        publishProgress();
    }

    private int countBanknotes(int id, int count) {
        // определение типа проверяемой категории
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        String type = c.getString(c.getColumnIndex(COLUMN_TYPE));
        c.close();
        // если тип - категории, рекурсивно проверяются следующие
        if (type.equals("category")) {
            Cursor query = database.query(TABLE_CATEGORIES, null, COLUMN_PARENT + " = " + id, null, null, null, null);
            if (query.moveToFirst())
                do {
                    count = countBanknotes(query.getInt(query.getColumnIndex(COLUMN_ID)), count);
                } while (query.moveToNext());
            query.close();
        } // если банкноты - просто подсчет
        else if (type.equals("banknotes")) {
            Cursor query = database.query(TABLE_BANKNOTES, null, COLUMN_PARENT + " = " + id, null, null, null, null);
            if (query.moveToFirst())
                do {
                    count++;
                } while (query.moveToNext());
            query.close();
        }
        return count;
    }

    private void updatePositions() {
        RecyclerView.Adapter adapter = ((RecyclerView) activity.get().findViewById(R.id.main)).getAdapter();
        if (adapter != null)
            switch (type) {
                case "category":
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        ContentValues cv = new ContentValues();
                        cv.put(COLUMN_POSITION, i + 1);
                        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + ((CategoryRVAdapter) adapter).getList().get(i).getId(), null);
                    }
                    break;
                case "banknotes":
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        ContentValues cv = new ContentValues();
                        cv.put(COLUMN_POSITION, i + 1);
                        database.update(TABLE_BANKNOTES, cv, COLUMN_ID + " = " + ((BanknoteRVAdapter) adapter).getList().get(i).getId(), null);
                    }
                    break;
            }
    }

    private void setData() {
        if (searchMode) {
            Cursor c;
            c = database.query(TABLE_BANKNOTES, null, null, null, null, null, "position");
            if (c.moveToFirst())
                do {
                    int id = c.getInt(c.getColumnIndex(COLUMN_ID));
                    String name = c.getString(c.getColumnIndex(COLUMN_NAME));
                    if (name.toLowerCase().contains(searchString.toLowerCase())) {
                        String circulationTime = c.getString(c.getColumnIndex(COLUMN_CIRCULATION));
                        String obversePath = c.getString(c.getColumnIndex(COLUMN_OBVERSE));
                        String country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
                        banknoteList.add(new Banknote(id, country, name, circulationTime, obversePath));
                    }
                } while (c.moveToNext());
            c.close();
        } else {
            Cursor c = null;
            switch (newType) {
                case "category":
                    c = database.query(TABLE_CATEGORIES, null, COLUMN_PARENT + " = " + currID, null, null, null, "position");
                    if (c.moveToFirst())
                        do {
                            String name = c.getString(c.getColumnIndex(COLUMN_NAME));
                            String image = c.getString(c.getColumnIndex(COLUMN_IMAGE));
                            int id = c.getInt(c.getColumnIndex(COLUMN_ID));
                            int count = countBanknotes(id, 0);
                            categoryList.add(new Category(name, image, count, id));
                        } while (c.moveToNext());
                    break;
                case "banknotes":
                    c = database.query(TABLE_BANKNOTES, null, COLUMN_PARENT + " = " + currID, null, null, null, "position");
                    if (c.moveToFirst())
                        do {
                            int id = c.getInt(c.getColumnIndex(COLUMN_ID));
                            String name = c.getString(c.getColumnIndex(COLUMN_NAME));
                            String circulationTime = c.getString(c.getColumnIndex(COLUMN_CIRCULATION));
                            String obversePath = c.getString(c.getColumnIndex(COLUMN_OBVERSE));
                            String country = c.getString(c.getColumnIndex(COLUMN_COUNTRY));
                            banknoteList.add(new Banknote(id, country, name, circulationTime, obversePath));
                        } while (c.moveToNext());
                    break;
            }
            if (c != null) c.close();
        }
    }

    void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public interface OnLoadListener {
        void loadFinished(String type, int parent, RecyclerView.Adapter adapter);
    }
}
