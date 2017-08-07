/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.example.com.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.example.com.inventoryapp.data.ProductContract;
import android.example.com.inventoryapp.data.ProductContract.ProductEntry;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by mohanad on 06/08/17.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** the current product uri*/
    private Uri mCurrentProductUri;

    /** the current product supplier phone*/
    private EditText mEtxtSuppllierPhone;

    /** the current product image*/
    private ImageView mImage;

    /** the current product min quanityt*/
    private Button mSubtractOneProduct;

    /** the current product add quanityt by one*/
    private Button mAddOneProduct;

    /** the current product uri*/
    private Button btn_order;
    
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** the current product name*/
    private EditText mEtxtName;

    /** the current product quanityt*/
    private EditText mEtxtQuantity;

    /** the current product price*/
    private EditText mEtxtPrice;
    
    
    private static final int IMG_REQUEST = 88;


    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_add_Product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_Product));

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mEtxtName = (EditText) findViewById(R.id.etxt_product_name);
        mEtxtQuantity = (EditText) findViewById(R.id.etxt_product_quantity);
        mEtxtPrice = (EditText) findViewById(R.id.etxt_product_price);
        mImage = (ImageView) findViewById(R.id.img_product);
        mAddOneProduct = (Button) findViewById(R.id.btn_subtract);
        mSubtractOneProduct = (Button) findViewById(R.id.btn_add);
        mEtxtSuppllierPhone = (EditText) findViewById(R.id.etxt_supplier_phone);
        
        mEtxtName.setOnTouchListener(mTouchListener);
        mEtxtQuantity.setOnTouchListener(mTouchListener);
        mEtxtPrice.setOnTouchListener(mTouchListener);
        mEtxtSuppllierPhone.setOnTouchListener(mTouchListener);
        btn_order = (Button) findViewById(R.id.btn_order);


        mEtxtName.setOnTouchListener(mTouchListener);
        mEtxtQuantity.setOnTouchListener(mTouchListener);
        mEtxtPrice.setOnTouchListener(mTouchListener);


        mSubtractOneProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantityByOne();
                mProductHasChanged = true;
            }
        });
        Button addImage = (Button) findViewById(R.id.btn_select_image);
        btn_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mEtxtSuppllierPhone.getText().toString()));
                startActivity(intent);
            }
        });
        mAddOneProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseQuantityByeOne();
                mProductHasChanged = true;
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromDevice();
            }
        });

    }

    private Uri mImageUri;
    private String mImageUriString = "";


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mImageUriString = mImageUri.toString();
                mImage.setImageBitmap(getBitmapFromUri(mImageUri));
                Log.v("editor", "Uri: " + mImageUriString);
            }
        }
    }


    /**
     * save product to data base
     */
    private void saveProduct() {
        String nameString = mEtxtName.getText().toString().trim();
        String QuantityString = mEtxtQuantity.getText().toString().trim();
        String PriceString = mEtxtPrice.getText().toString().trim();
        String SupplierString_phone = mEtxtSuppllierPhone.getText().toString().trim();

        if (nameString.isEmpty() || PriceString.isEmpty() || QuantityString.isEmpty() || SupplierString_phone.isEmpty()
                || mImageUriString.isEmpty()) {
            Toast.makeText(this," you should insert all data", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, QuantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, PriceString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, SupplierString_phone);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUriString);


        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();

                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierColumnIndex_phone = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            int imageColumnIndex_image = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier_phone = cursor.getString(supplierColumnIndex_phone);
            String image = cursor.getString(imageColumnIndex_image);


            if (image != null) {
                Uri imgUri = Uri.parse(image);
                mImage.setImageURI(imgUri);
                mImageUriString = image;
            }
            Log.v("editor image", image);
            mEtxtName.setText(name);
            mEtxtSuppllierPhone.setText(supplier_phone);
            mEtxtQuantity.setText(Integer.toString(quantity));
            mEtxtPrice.setText(Integer.toString(price));


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEtxtName.setText("");
        mEtxtQuantity.setText("");
        mEtxtPrice.setText("");

    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e("editor", "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e("editor", "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void selectImageFromDevice() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMG_REQUEST);
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_Product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_Product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }



    /**
     * decrease quantity by one
     */
    private void decreaseQuantityByeOne() {
        String previousValueString = mEtxtQuantity.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        if (previousValue > 0) {
            mEtxtQuantity.setText(String.valueOf(previousValue - 1));
        }
    }

    /**
     * increase quantity by one
     */
    private void increaseQuantityByOne() {
        String previousValueString = mEtxtQuantity.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mEtxtQuantity.setText(String.valueOf(previousValue + 1));
    }


}