package com.udacityprojs.lucas.invapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.udacityprojs.lucas.invapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) { super(context, c, 0); }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.saleButton);

        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        String itemId = cursor.getString(idColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);

        final int itemIdInt = Integer.valueOf(itemId);
        final int itemQuantityInt = Integer.valueOf(itemQuantity);

        final CatalogActivity catalogActivity = (CatalogActivity) context;
        final Context mContext = context;

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (itemQuantityInt == 0) {
                    Toast.makeText(mContext, "Negative values prohibited",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                catalogActivity.decreaseCount(itemIdInt, itemQuantityInt);
            }
        });

        itemPrice = "$" + itemPrice + " ea.";

        nameTextView.setText(itemName);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);
    }
}