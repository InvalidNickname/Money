package ru.money;

import android.content.Context;
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

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, USES_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_BANKNOTES + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + COLUMN_COUNTRY + " text,"
                + "name text,"
                + "circulation text,"
                + "obverse text,"
                + "reverse text,"
                + "description text,"
                + "parent integer,"
                + COLUMN_POSITION + " integer" + ");");
        db.execSQL("create table " + TABLE_CATEGORIES + " ("
                + COLUMN_ID + " integer primary key autoincrement,"
                + "name text,"
                + "image text,"
                + "parent integer,"
                + "type text,"
                + COLUMN_POSITION + " integer" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
