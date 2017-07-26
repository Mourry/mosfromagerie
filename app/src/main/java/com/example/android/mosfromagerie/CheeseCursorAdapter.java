package com.example.android.mosfromagerie;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import data.CheeseContract.CheeseEntry;


public class CheeseCursorAdapter extends CursorAdapter {

    public CheeseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // find individual views
        ImageView cheeseImage = (ImageView) view.findViewById(R.id.list_item_cheese_image_view);
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        ImageView saleImage = (ImageView) view.findViewById(R.id.list_item_sale_image_view);

        // find columns of cheese attributes needed
        int idColumnIndex = cursor.getColumnIndex(CheeseEntry._ID);
        int photoColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_PHOTO);
        int nameColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_NAME);
        int priceColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_QUANTITY);

        // read cheese attributes from the cursor for current cheese
        final int cheeseId = cursor.getInt(idColumnIndex);
        byte[] cheesePhoto = cursor.getBlob(photoColumnIndex);
        String cheeseName = cursor.getString(nameColumnIndex);
        int cheesePrice = cursor.getInt(priceColumnIndex);
        final int cheeseQuantity = cursor.getInt(quantityColumnIndex);

        // update views with the attributes for the current cheese
        Bitmap cheeseBitmap = BitmapFactory.decodeByteArray(cheesePhoto, 0, cheesePhoto.length);
        cheeseImage.setImageBitmap(cheeseBitmap);
        nameTextView.setText(cheeseName);
        priceTextView.setText("€" + Integer.toString(cheesePrice));
        quantityTextView.setText(Integer.toString(cheeseQuantity));

        saleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentCheeseUri = ContentUris.withAppendedId(CheeseEntry.CONTENT_URI, cheeseId);
                makeSale(context, cheeseQuantity, currentCheeseUri);
            }
        });
    }

    private void makeSale(Context context, int cheeseQuantity, Uri uriCheese) {
        if (cheeseQuantity == 0) {
            Toast.makeText(context, R.string.no_cheese_to_sell, Toast.LENGTH_SHORT).show();
        } else {
            int newQuantity = cheeseQuantity - 1;
            ContentValues values = new ContentValues();
            values.put(CheeseEntry.COLUMN_CHEESE_QUANTITY, newQuantity);
            context.getContentResolver().update(uriCheese, values, null, null);
        }
    }
}