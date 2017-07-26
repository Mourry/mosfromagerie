package com.example.android.mosfromagerie;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.mosfromagerie.R;

import data.CheeseContract.CheeseEntry;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_CHEESE_LOADER = 0;
    private static final int IMAGE_REQUEST_CODE = 1;
    private Uri mCurrentUri;

    private Bitmap mBitmap;
    private boolean mHasImage;

    // image view
    private ImageView mImageView;

    // edit texts
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;

    // spinner
    private Spinner mSupplierSpinner;
    private int mSupplier = CheeseEntry.SUPPLIER_UNKNOWN;

    private boolean mCheeseHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCheeseHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        Button orderButton = (Button) findViewById(R.id.editor_order_button);

        if (mCurrentUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_cheese));
            invalidateOptionsMenu();
            orderButton.setEnabled(false);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_cheese));
            getLoaderManager().initLoader(EXISTING_CHEESE_LOADER, null, this);
        }

        mImageView = (ImageView) findViewById(R.id.edit_cheese_image);
        Button imageButton = (Button) findViewById(R.id.editor_add_image_button);
        mNameEditText = (EditText) findViewById(R.id.edit_cheese_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_cheese_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_cheese_quantity);
        Button minusButton = (Button) findViewById(R.id.minus_button);
        Button plusButton = (Button) findViewById(R.id.plus_button);
        mSupplierSpinner = (Spinner) findViewById(R.id.edit_cheese_supplier);
        mHasImage = false;
        mBitmap = null;

        mImageView.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierSpinner.setOnTouchListener(mTouchListener);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(pictureIntent, IMAGE_REQUEST_CODE);
                }
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                if (quantity == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_negative_quantity),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity--;
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                quantity++;
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameEmail = mNameEditText.getText().toString().trim();

                String emailMessage = getString(R.string.email_message) + "\n" + nameEmail;

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:mo@mosfromagerie.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, emailMessage);

                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });

        setupSpinner();
    }

    private void saveCheese() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.name_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.price_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.quantity_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues editorValues = new ContentValues();
        if (mHasImage) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageByte = byteArrayOutputStream.toByteArray();
            editorValues.put(CheeseEntry.COLUMN_CHEESE_PHOTO, imageByte);
        } else {
            Toast.makeText(this, getString(R.string.photo_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        editorValues.put(CheeseEntry.COLUMN_CHEESE_NAME, nameString);
        editorValues.put(CheeseEntry.COLUMN_CHEESE_PRICE, Integer.parseInt(priceString));
        editorValues.put(CheeseEntry.COLUMN_CHEESE_QUANTITY, Integer.parseInt(quantityString));
        editorValues.put(CheeseEntry.COLUMN_CHEESE_SUPPLIER, mSupplier);


        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(CheeseEntry.CONTENT_URI, editorValues);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_cheese_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_cheese_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentUri, editorValues, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_cheese_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_cheese_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    // setup the dropdown spinner that allows the user to select a supplier
    private void setupSpinner() {
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_farm))) {
                        mSupplier = CheeseEntry.SUPPLIER_FARM; // ROY G
                    } else {
                        mSupplier = CheeseEntry.SUPPLIER_UNKNOWN; // unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplier = 0; // unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveCheese();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mCheeseHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mCheeseHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (mCurrentUri == null) {
            return null;
        }

        String[] projection = {
                CheeseEntry._ID,
                CheeseEntry.COLUMN_CHEESE_NAME,
                CheeseEntry.COLUMN_CHEESE_PHOTO,
                CheeseEntry.COLUMN_CHEESE_PRICE,
                CheeseEntry.COLUMN_CHEESE_QUANTITY,
                CheeseEntry.COLUMN_CHEESE_SUPPLIER
        };

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // find the columns of the Cheese attributes we want
            int nameColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_NAME);
            int photoColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_PHOTO);
            int priceColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(CheeseEntry.COLUMN_CHEESE_SUPPLIER);

            // extract the values from the cursor for the given column indices
            String name = cursor.getString(nameColumnIndex);
            byte[] image = cursor.getBlob(photoColumnIndex);
            if (image != null) {
                mHasImage = true;
                mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);

            // update views
            mNameEditText.setText(name);
            mImageView.setImageBitmap(mBitmap);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            switch (supplier) {
                case CheeseEntry.SUPPLIER_FARM:
                    mSupplierSpinner.setSelection(1);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }

        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteCheese();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteCheese() {
        getLoaderManager().destroyLoader(0);
        if (mCurrentUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_cheese_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_cheese_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmap = (Bitmap) extras.get("data");
            mHasImage = true;
            mImageView.setImageBitmap(mBitmap);
        }
    }

}
