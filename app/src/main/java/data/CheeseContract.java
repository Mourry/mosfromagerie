package data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class CheeseContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.mosfromagerie";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CHEESE = "cheese";

    // to prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private CheeseContract() {
    }

    // inner class to define the table contents
    public static class CheeseEntry implements BaseColumns {

        // CONTENT_URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CHEESE);

        // CONTENT_LIST_TYPE
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHEESE;

        // CONTENT_ITEM_TYPE
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHEESE;

        // table name
        public static final String TABLE_NAME = "cheese";
        // column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CHEESE_NAME = "name";
        public static final String COLUMN_CHEESE_PHOTO = "photo";
        public static final String COLUMN_CHEESE_PRICE = "price";
        public static final String COLUMN_CHEESE_QUANTITY = "quantity";
        public static final String COLUMN_CHEESE_SUPPLIER = "supplier";

        // possible cheese suppliers
        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_FARM = 1;

        public static boolean isValidSupplier(int supplier) {
            return supplier == SUPPLIER_UNKNOWN || supplier == SUPPLIER_FARM;
        }
    }
}