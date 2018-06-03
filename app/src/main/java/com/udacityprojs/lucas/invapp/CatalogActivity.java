package com.udacityprojs.lucas.invapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
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

import com.udacityprojs.lucas.invapp.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

        private static final int ITEM_LOADER = 0;

        private InventoryCursorAdapter mCursorAdapter;

        @Override
        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView itemListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(
                            CatalogActivity.this, EditorActivity.class
                    );
                    Uri currentItemUri = ContentUris.withAppendedId(
                            InventoryEntry.CONTENT_URI, id
                    );
                    intent.setData(currentItemUri);
                    startActivity(intent);
                }
            });

        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    public void decreaseCount(int id, int itemQuantity){

        itemQuantity = itemQuantity - 1;

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, itemQuantity);

        Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        getApplicationContext().getContentResolver().update(
                updateUri, values,null, null
        );
    }

        private void deleteAllItems () {
        int rowsDeleted = getContentResolver().delete(
                InventoryEntry.CONTENT_URI, null, null
        );
        Log.d("CatalogActivity", rowsDeleted + " rows deleted from item database");
    }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

        @Override
        public Loader<Cursor> onCreateLoader ( int i, Bundle bundle){
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,};
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

        @Override
        public void onLoadFinished (Loader < Cursor > loader, Cursor data){
        mCursorAdapter.swapCursor(data);
    }

        @Override
        public void onLoaderReset (Loader < Cursor > loader) {
        mCursorAdapter.swapCursor(null);
    }
    }