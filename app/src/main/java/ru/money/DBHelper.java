package ru.money;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ru.money.ListActivity.USES_DB_VERSION;

class DBHelper extends SQLiteOpenHelper {

    static final String TABLE_BANKNOTES = "banknotes";
    static final String TABLE_CATEGORIES = "categories";
    static final String DATABASE_NAME = "mainDB";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_POSITION = "position";
    static final String COLUMN_COUNTRY = "country";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_TYPE = "type";
    static final String COLUMN_PARENT = "parent";
    static final String COLUMN_IMAGE = "image";
    static final String COLUMN_DESCRIPTION = "description";

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, USES_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_BANKNOTES + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + COLUMN_COUNTRY + " text,"
                + COLUMN_NAME + " text,"
                + "circulation text,"
                + "obverse text,"
                + "reverse text,"
                + COLUMN_DESCRIPTION + " text,"
                + COLUMN_PARENT + " integer,"
                + COLUMN_POSITION + " integer" + ");");
        db.execSQL("create table " + TABLE_CATEGORIES + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + COLUMN_NAME + " text,"
                + COLUMN_IMAGE + " text,"
                + COLUMN_PARENT + " integer,"
                + COLUMN_TYPE + " text,"
                + COLUMN_POSITION + " integer" + ");");
        createMainCategory(db);
    }

    private void createMainCategory(SQLiteDatabase database) {
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = 1", null, null, null, null);
        if (c.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, "main");
            cv.put(COLUMN_IMAGE, "nothing");
            cv.put(COLUMN_TYPE, "no category");
            cv.put(COLUMN_PARENT, 0);
            database.insert(TABLE_CATEGORIES, null, cv);
        }
        c.close();
    }

    String updateCategoryType(SQLiteDatabase database, int id, String newType) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TYPE, newType);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + id, null);
        return newType;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
