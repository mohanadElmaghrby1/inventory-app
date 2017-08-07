package android.example.com.inventoryapp.data;

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

/**
 * Created by mohanad on 05/08/17.
 */

public class ProductProvider extends ContentProvider {


    //Uri matcher code for the content Uri for the products table
    public static final int PRODUCTS = 100;

    //Uri matcher code for the content of single row of the table
    public static final int PRODUCTS_ID = 101;

    /**
     * UriMatcher object is to match the content of Uri corresponding to the int code
     * the input passed into constructor represent the code and return the root of Uri
     * No_match is the most common in Uri Matcher
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //static initializer this run the first time if anything is called from this class
    static {

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
    }


    public static final String LOG_TAG = ProductProvider.class.getName();

    private ProductDbHelper mProductDbHelper;

    @Override
    public boolean onCreate() {
        //crete object
        mProductDbHelper = new ProductDbHelper(getContext());
        return  true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //get the readable database
        SQLiteDatabase database = mProductDbHelper.getReadableDatabase();

        //This cursor will hold the result of the query
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            //Query the table directly with projection... content multi rows
            case PRODUCTS:
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //perform a query return a cursor contain row of table
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri" + uri);
        }

        //set notification Uri on the Cursor
        //to know the data content Uri the Cursor created
        //data in Uri changes, let us know to update Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri + "with match");
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return saveProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for" + uri);
        }

    }

    /**
     * Insert new pet using the given Uri and content Value
     *
     * @param uri
     * @param contentValues
     * @return
     */
    private Uri saveProduct(Uri uri, ContentValues contentValues) {

        //check the name of the product is not null
        String name = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("product required a name");
        }

        //check the name of the product is not null
        String supplier_phone = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (supplier_phone == null) {
            throw new IllegalArgumentException("product required a valid supplier phone");
        }

        Integer price = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price<0) {
            throw new IllegalArgumentException("product required valid price");
        }

        Integer quantity = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("product need valid quantity info");
        }

        //get writable database
        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();

        //Insert new product with given value
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert new product to table");
            return null;
        }

        String image = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("product need valid image");
        }
        //notify all listener that data has change for the pet content Uri
        getContext().getContentResolver().notifyChange(uri, null);

        //return with the new Uri with the id that append at the end
        //like content://com.example.android.pets/pet/#id
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase sqLiteDBWritable = mProductDbHelper.getWritableDatabase();
        int rowsDeleted =  0;
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = sqLiteDBWritable.delete(ProductContract.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDBWritable.delete(ProductContract.ProductEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Delete : unknown uri");
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);

            case PRODUCTS_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException ("update :unkown uri");
        }
    }

    /**
     *
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("product requires a name");
            }
        }


        if (contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {

            Integer Quantity= contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (Quantity != null && Quantity< 0) {
                throw new IllegalArgumentException("product requires valid weight");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }


        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);


        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }


        return rowsUpdated;
        }
    }
