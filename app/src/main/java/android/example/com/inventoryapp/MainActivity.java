package android.example.com.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.example.com.inventoryapp.data.ProductContract;
import android.example.com.inventoryapp.data.ProductContract.ProductEntry;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by mohanad on 05/08/17.
 */

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * the product loader id
     */
    private static final int PRODUCT_LOADERS = 0;

    /**
     * the cursor adapter
     */
    private ProductCursorAdapter mCursorAdapter;

    private ListView ProductListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ProductListView = (ListView) findViewById(R.id.products_listview);

        View emptyView = findViewById(R.id.empty_view);
        ProductListView.setEmptyView(emptyView);

        mCursorAdapter = new ProductCursorAdapter(this, null);
        ProductListView.setAdapter(mCursorAdapter);

        ProductListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        //kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADERS, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define the projection that specifies the column that we need
        String[] projection = {ProductEntry._ID
                , ProductEntry.COLUMN_PRODUCT_NAME
                , ProductEntry.COLUMN_PRODUCT_PRICE
                , ProductEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI
                , projection
                , null
                , null
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update new Cursor that contain the updated data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data need to be deleted
        mCursorAdapter.swapCursor(null);
    }

    private void dummydata() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "ToTo");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 50);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 100);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        Log.v("CatalogActivity", "New row ID: " + newUri);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_all_products:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * delete all products from data base
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from Product database");
    }

}