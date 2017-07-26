package data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import data.CheeseContract.CheeseEntry;

public class CheeseProvider extends ContentProvider {

    private static final String LOG_TAG = CheeseProvider.class.getName();

    // URI matcher code for the entire table
    private static final int CHEESE = 100;
    // URI matcher code for a single entry
    private static final int CHEESE_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CheeseContract.CONTENT_AUTHORITY,
                CheeseContract.PATH_CHEESE, CHEESE);

        sUriMatcher.addURI(CheeseContract.CONTENT_AUTHORITY,
                CheeseContract.PATH_CHEESE + "/#", CHEESE_ID);
    }

    private CheeseDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new CheeseDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case CHEESE:
                cursor = db.query(CheeseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case CHEESE_ID:
                selection = CheeseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(CheeseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHEESE:
                return CheeseEntry.CONTENT_LIST_TYPE;
            case CHEESE_ID:
                return CheeseEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHEESE:
                return insertCheese(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertCheese(Uri uri, ContentValues values) {
        String name = values.getAsString(CheeseEntry.COLUMN_CHEESE_NAME);
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Cheese requires a name");
        }

        Integer price = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Cheese price must be greater than 0");
        }

        Integer quantity = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Cheese quantity must be greater than 0");
        }

        Integer supplier = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_SUPPLIER);
        if (supplier == null || !CheeseEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException("Cheese requires valid supplier");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(CheeseEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHEESE:
                rowsDeleted = db.delete(CheeseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHEESE_ID:
                selection = CheeseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(CheeseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHEESE:
                return updateCheese(uri, values, selection, selectionArgs);
            case CHEESE_ID:
                selection = CheeseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCheese(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateCheese(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(CheeseEntry.COLUMN_CHEESE_NAME)) {
            String name = values.getAsString(CheeseEntry.COLUMN_CHEESE_NAME);
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Cheese requires a name");
            }
        }

        if (values.containsKey(CheeseEntry.COLUMN_CHEESE_PRICE)) {
            Integer price = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Cheese price must be greater than 0");
            }
        }

        if (values.containsKey(CheeseEntry.COLUMN_CHEESE_QUANTITY)) {
            Integer quantity = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Cheese quantity must be greater than 0");
            }
        }

        if (values.containsKey(CheeseEntry.COLUMN_CHEESE_SUPPLIER)) {
            Integer supplier = values.getAsInteger(CheeseEntry.COLUMN_CHEESE_SUPPLIER);
            if (supplier == null || !CheeseEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException("Cheese requires valid supplier");
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(CheeseEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}