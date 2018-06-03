package com.udacityprojs.lucas.invapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.udacityprojs.lucas.invapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    public static final int GET_FROM_GALLERY = 3;
    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private ImageButton mQuantityDecButton;
    private ImageButton mQuantityIncButton;
    private EditText mPriceEditText;
    private ImageView mIvItem;
    private Bitmap ivItemBitmap;
    private Button mAddImgBtn;

    private String nameString;
    private String quantityString;
    private String priceString;
    private String orderEmailString;

    private int RETURN_TO_CATALOG = 0;
    private int EMPTY_FIELD = 1;
    private int SAVE = 2;

    private boolean mItemHasChanged = false;
    private boolean mImgHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            infoItemLayout(true);
        } else {
            infoItemLayout(false);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mQuantityDecButton = (ImageButton) findViewById(R.id.quantity_dec_imgbtn);
        mQuantityIncButton = (ImageButton) findViewById(R.id.quantity_inc_imgbtn);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mAddImgBtn = (Button) findViewById(R.id.add_img_btn);
        mIvItem = (ImageView) findViewById(R.id.iv_item);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mQuantityDecButton.setOnClickListener(mOnClickListenerDec);
        mQuantityIncButton.setOnClickListener(mOnClickListenerInc);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mAddImgBtn.setOnClickListener(mOnClickListenerImgBtn);
    }

    private void saveItem() {
        ContentValues values = putContentValues();

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add a menu to the app bar by inflating the menu options from the following XML file.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    // Actions to be executed when the user clicks on a respective menu option in the app bar overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                switch (checkAnyFieldsEmpty()) {
                    case 0:
                        finish();
                        return true;
                    case 1:
                        return true;
                    case 2:
                        saveItem();
                        finish();
                        return true;
                }
                return true;
            case android.R.id.home:

                if (!mItemHasChanged) {
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
        if (!mItemHasChanged) {
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
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_ORDER_EMAIL};
        return new CursorLoader(this,
                mCurrentItemUri,
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
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int imgColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int orderEmailColumnIndex = cursor.getColumnIndex(
                    InventoryEntry.COLUMN_ITEM_ORDER_EMAIL);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            orderEmailString = cursor.getString(orderEmailColumnIndex);

            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));

            if (!cursor.isNull(imgColumnIndex)) {
                byte[] itemImgByteArray = cursor.getBlob(imgColumnIndex);
                Bitmap itemImgBitmap = BitmapFactory.decodeByteArray(itemImgByteArray, 0, itemImgByteArray.length);
                mIvItem.setImageBitmap(itemImgBitmap);
            }
        }
    }

    /**
     *  Clears all info from the info fields in the case that the loader is invalidated
     *  via invalidateOptionsMenu().
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mIvItem.setImageBitmap(null);
    }

    // Dialogue to warn the user that changes will not be saved, if there were made, if they leave the editor.
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

    // Prompt the user to confirm that they want to delete this item.
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
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

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void quantityBtnClicked(boolean b) {
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantityInt = Integer.valueOf(quantityString);

        if (!b) {
            if (quantityInt == 0) {
                Toast.makeText(this, getResources().getString(R.string.tst_neg_vals),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            quantityInt = quantityInt - 1;
        } else {
            quantityInt = quantityInt + 1;
        }

        mQuantityEditText.setText(Integer.toString(quantityInt));
    }

    private void infoItemLayout(boolean b) {
        if (b) {
            setTitle(getString(R.string.editor_activity_title_new_item));
            invalidateOptionsMenu();
            LinearLayout btnView = (LinearLayout) findViewById(R.id.bottomEditBtns);
            btnView.setVisibility(View.GONE);
        } else {
            mImgHasChanged = true;
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
            Button deleteItemBtn = (Button) findViewById(R.id.deleteItemBtn);
            Button placeOrderBtn = (Button) findViewById(R.id.placeOrderBtn);
            View.OnClickListener mOnClickListenerDeleteItem = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            };
            View.OnClickListener mOnClickListenerPlaceOrder = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFields();
                    final Intent emailOrderIntent = new Intent(Intent.ACTION_SEND);

                    emailOrderIntent.setType("plain/text");
                    emailOrderIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                            orderEmailString});
                    emailOrderIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getResources().getString(R.string.supplier_order_subject) +
                                    " " + nameString);
                    emailOrderIntent.putExtra(Intent.EXTRA_TEXT,
                            getResources().getString(R.string.supplier_default_content_beginning) +
                                    " " + nameString +
                                    getResources().getString(R.string.supplier_default_content_ending));
                    Context mLocalContext = v.getContext();
                    mLocalContext.startActivity(Intent.createChooser(emailOrderIntent, "Send mail..."));
                }
            };
            deleteItemBtn.setOnClickListener(mOnClickListenerDeleteItem);
            placeOrderBtn.setOnClickListener(mOnClickListenerPlaceOrder);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                ivItemBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mIvItem.setImageBitmap(ivItemBitmap);
                mImgHasChanged = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int checkAnyFieldsEmpty() {
        getFields();

        boolean anyFieldsEmpty = (TextUtils.isEmpty(nameString) | TextUtils.isEmpty(quantityString)
                | TextUtils.isEmpty(priceString) | mImgHasChanged==false);

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) && (quantityString.equals("0")) && TextUtils.isEmpty(priceString)) {
                return RETURN_TO_CATALOG;
        } else if (anyFieldsEmpty) {
            Toast.makeText(this, getResources().getString(R.string.tst_txt_fields),Toast.LENGTH_SHORT).show();
            return EMPTY_FIELD;
        } else {
            return SAVE;
        }
    }

    private void getFields() {
        nameString = mNameEditText.getText().toString().trim();
        quantityString = mQuantityEditText.getText().toString().trim();
        priceString = mPriceEditText.getText().toString().trim();
    }

    private ContentValues putContentValues() {
        orderEmailString = getResources().getString(R.string.supplier_default_email);
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceString);

        if (mImgHasChanged && mCurrentItemUri == null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ivItemBitmap.compress(Bitmap.CompressFormat.JPEG, 5, stream);
            byte[] ivByteArray = stream.toByteArray();
            ivItemBitmap.recycle();
            values.put(InventoryEntry.COLUMN_ITEM_IMAGE, ivByteArray);
        }

        values.put(InventoryEntry.COLUMN_ITEM_ORDER_EMAIL, orderEmailString);
        return values;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private View.OnClickListener mOnClickListenerDec = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            quantityBtnClicked(false);
        }
    };

    private View.OnClickListener mOnClickListenerInc = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            quantityBtnClicked(true);
        }
    };

    private View.OnClickListener mOnClickListenerImgBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        }
    };
}