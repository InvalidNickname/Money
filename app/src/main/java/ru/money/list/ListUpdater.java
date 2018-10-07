package ru.money.list;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import ru.money.R;
import ru.money.utils.DBHelper;

import static ru.money.App.LOG_TAG;
import static ru.money.utils.DBHelper.COLUMN_CIRCULATION;
import static ru.money.utils.DBHelper.COLUMN_COUNTRY;
import static ru.money.utils.DBHelper.COLUMN_ID;
import static ru.money.utils.DBHelper.COLUMN_IMAGE;
import static ru.money.utils.DBHelper.COLUMN_NAME;
import static ru.money.utils.DBHelper.COLUMN_OBVERSE;
import static ru.money.utils.DBHelper.COLUMN_PARENT;
import static ru.money.utils.DBHelper.COLUMN_POSITION;
import static ru.money.utils.DBHelper.COLUMN_TYPE;
import static ru.money.utils.DBHelper.TABLE_BANKNOTES;
import static ru.money.utils.DBHelper.TABLE_CATEGORIES;

class ListUpdater extends AsyncTask<Void, Void, Void> {

    private final int currID;
    private final WeakReference<AppCompatActivity> activity;
    private final SQLiteDatabase database;
    private final List<Banknote> banknoteList = new ArrayList<>();
    private final List<Category> categoryList = new ArrayList<>();
    private OnLoadListener onLoadListener;
    private String type;

    ListUpdater(String type, int currID, AppCompatActivity activity) {
        this.type = type;
        this.currID = currID;
        this.activity = new WeakReference<>(activity);
        database = DBHelper.getInstance(activity).getDatabase();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        updatePositions();
        setData();
        return null;
    }

    @Override
    protected void onPostExecute(Void object) {
        Log.i(LOG_TAG, "Data is loaded, updating list...");
        RecyclerView main = activity.get().findViewById(R.id.main);
        TextView noItemsText = activity.get().findViewById(R.id.noItemsText);
        // установка адаптера
        if (type.equals("category"))
            main.setAdapter(new CategoryRVAdapter(categoryList));
        else
            main.setAdapter(new BanknoteRVAdapter(banknoteList));
        Log.i(LOG_TAG, "List updated");
        // выводится надпись об отсутствии объектов в категории, тип категории сбрасывается
        if (main.getAdapter() == null || main.getAdapter().getItemCount() == 0) {
            noItemsText.setVisibility(View.VISIBLE);
            type = DBHelper.updateCategoryType(currID, "no category");
        } else
            noItemsText.setVisibility(View.GONE);
        onLoadListener.loadFinished(type, main.getAdapter());
        super.onPostExecute(object);
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
        Cursor c = null;
        Cursor c2 = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + currID, null, null, null, null);
        if (c2.moveToFirst())
            type = c2.getString(c2.getColumnIndex(COLUMN_TYPE));
        c2.close();
        switch (type) {
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
        if (c != null)
            c.close();
    }

    void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public interface OnLoadListener {
        void loadFinished(String type, RecyclerView.Adapter adapter);
    }
}
