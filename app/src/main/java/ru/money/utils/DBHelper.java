package ru.money.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ru.money.App.USES_DB_VERSION;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TABLE_BANKNOTES = "banknotes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_REVERSE = "reverse";
    public static final String COLUMN_OBVERSE = "obverse";
    public static final String COLUMN_CIRCULATION = "circulation";
    public static final String DATABASE_NAME = "mainDB";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PARENT = "parent";
    public static final String COLUMN_IMAGE = "image";
    public static final String TABLE_CATEGORIES = "categories";
    private static DBHelper instance = null;
    private static SQLiteDatabase database;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, USES_DB_VERSION);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
            database = instance.getWritableDatabase();
        }
        return instance;
    }

    public static String updateCategoryType(int id, String newType) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TYPE, newType);
        database.update(TABLE_CATEGORIES, cv, COLUMN_ID + " = " + id, null);
        Cursor c = database.query(TABLE_CATEGORIES, null, COLUMN_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        c.close();
        return newType;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_BANKNOTES + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + COLUMN_COUNTRY + " text,"
                + COLUMN_NAME + " text,"
                + COLUMN_CIRCULATION + " text,"
                + COLUMN_OBVERSE + " text,"
                + COLUMN_REVERSE + " text,"
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

    // создание главной категории, если её ещё нет
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
