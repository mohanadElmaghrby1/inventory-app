package android.example.com.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.example.com.inventoryapp.data.ProductContract;
import android.example.com.inventoryapp.data.ProductContract.ProductEntry;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mohanad on 05/08/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     *
     * @param context
     * @param cursor
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }


    /**
     * This make a new blank of list view, no data is set to view yet
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    /**
     * Method to bind the product data current pointed by cursor to given list item view
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //first need to find list item
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        final TextView quantity = (TextView) view.findViewById(R.id.quantity);
        Button sale=(Button)view.findViewById(R.id.sale);

        //use the Cursor to find column in the table that needed
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        //read the value of the column name from Cursor for current pet
        String nameCursor = cursor.getString(nameColumnIndex);
        final int[] quantityCursor = {cursor.getInt(quantityColumnIndex)};
        int PriceCursor = cursor.getInt(priceColumnIndex);

        int iDColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        final String rowId = cursor.getString(iDColumnIndex);


        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver contentResolver = v.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantityCursor[0] > 0) {
                    Toast.makeText(context,"salled",Toast.LENGTH_SHORT).show();
                    quantityCursor[0] -=1;
                    Integer itemId = Integer.parseInt(rowId);
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,quantityCursor[0] );
                    Uri currentItemUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, itemId);
                    contentResolver.update(currentItemUri, values, null, null);
                }
            }
        });


        name.setText(nameCursor);
        quantity.setText(quantityCursor[0] +"");
        price.setText(PriceCursor+" $");



    }
}

