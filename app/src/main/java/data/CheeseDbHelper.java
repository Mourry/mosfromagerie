package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import data.CheeseContract.CheeseEntry;

public class CheeseDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = CheeseDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "fromagerie.db";
    private static final int DATABASE_VERSION = 1;

    public CheeseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_CHEESE_TABLE =
                "CREATE TABLE " + CheeseEntry.TABLE_NAME + "( " +
                        CheeseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        CheeseEntry.COLUMN_CHEESE_NAME + " TEXT NOT NULL, " +
                        CheeseEntry.COLUMN_CHEESE_PHOTO + " BLOB, " +
                        CheeseEntry.COLUMN_CHEESE_PRICE + " INTEGER NOT NULL, " +
                        CheeseEntry.COLUMN_CHEESE_QUANTITY + " INTEGER NOT NULL, " +
                        CheeseEntry.COLUMN_CHEESE_SUPPLIER + " INTEGER NOT NULL DEFAULT 0 );";

        Log.i(LOG_TAG, SQL_CREATE_CHEESE_TABLE);
        db.execSQL(SQL_CREATE_CHEESE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + CheeseEntry.TABLE_NAME);
        onCreate(db);
    }
}