package ru.money;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context) {
        super(context, "mainDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table banknotes ("
                + "_id integer primary key autoincrement,"
                + "country text,"
                + "name text,"
                + "circulation text,"
                + "obverse text,"
                + "reverse text,"
                + "description text,"
                + "parent integer" + ");");
        db.execSQL("create table categories ("
                + "_id integer primary key autoincrement,"
                + "name text,"
                + "image text,"
                + "parent integer,"
                + "type text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
