package com.example.android.mosfromagerie;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import data.CheeseContract;
import data.CheeseContract.CheeseEntry;

import java.io.ByteArrayOutputStream;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int CHEESE_LOADER = 0;

    CheeseCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fabIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(fabIntent);
            }
        });

        ListView cheeseListView = (ListView) findViewById(R.id.list_view);

        View emptyView = findViewById(R.id.empty_view);
        cheeseListView.setEmptyView(emptyView);

        mCursorAdapter = new CheeseCursorAdapter(this, null);
        cheeseListView.setAdapter(mCursorAdapter);

        cheeseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentCheeseUri = ContentUris.withAppendedId(CheeseContract.CheeseEntry.CONTENT_URI, id);
                intent.setData(currentCheeseUri);
                startActivity(intent);
            }
        });

        // start loader
        getLoaderManager().initLoader(CHEESE_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void insertCheese() {
        insertIntoDatabase("Camembert", convertToByte(getDrawable(R.drawable.camembert)), 2, 20, CheeseEntry.SUPPLIER_FARM);
    }

    private byte[] convertToByte(Drawable drawable) {
        // convert to bitmap
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();

        // convert to byte to store
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void insertIntoDatabase(
            String name,
            byte[] photo,
            int price,
            int quantity,
            int supplier) {

        ContentValues values = new ContentValues();
        values.put(CheeseEntry.COLUMN_CHEESE_NAME, name);
        values.put(CheeseEntry.COLUMN_CHEESE_PHOTO, photo);
        values.put(CheeseEntry.COLUMN_CHEESE_PRICE, price);
        values.put(CheeseEntry.COLUMN_CHEESE_QUANTITY, quantity);
        values.put(CheeseEntry.COLUMN_CHEESE_SUPPLIER, supplier);
        getContentResolver().insert(CheeseEntry.CONTENT_URI, values);
    }

    private void deleteAllCheese() {
        int rowsDeleted = getContentResolver().delete(CheeseEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from cheese database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertCheese();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllCheese();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                CheeseEntry._ID,
                CheeseEntry.COLUMN_CHEESE_NAME,
                CheeseEntry.COLUMN_CHEESE_PHOTO,
                CheeseEntry.COLUMN_CHEESE_PRICE,
                CheeseEntry.COLUMN_CHEESE_QUANTITY};

        return new CursorLoader(
                this,
                CheeseEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}