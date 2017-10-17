package com.example.android.inventorymanager;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventorymanager.data.GameContract;
import com.example.android.inventorymanager.data.GameDbHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;



public class GameCursorAdapter extends CursorAdapter {

    //Initialize instance of CatalogActivity
    private final CatalogActivity activity;

    GameDbHelper dbHelper;

    public GameCursorAdapter(CatalogActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
        activity = context;
        dbHelper = new GameDbHelper(activity);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView gameName = (TextView) view.findViewById(R.id.name_textview);
        final TextView gameQuantity = (TextView) view.findViewById(R.id.quantity_textview);
        TextView gamePrice = (TextView) view.findViewById(R.id.price_textview);
        ImageView gameImage = (ImageView) view.findViewById(R.id.game_imageview);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(GameContract.GameEntry.COLUMN_GAME_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(GameContract.GameEntry.COLUMN_GAME_QUANTITY));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(GameContract.GameEntry.COLUMN_GAME_PRICE));
        String imageURL = cursor.getString(cursor.getColumnIndexOrThrow(GameContract.GameEntry.COLUMN_GAME_IMAGE_URL));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(GameContract.GameEntry.COLUMN_GAME_IMAGE_PATH));

        final long id = cursor.getLong(getCursor().getColumnIndex(GameContract.GameEntry._ID));

        // Placeholder if image url is empty
        if (TextUtils.isEmpty(imageURL)) {
            imageURL = context.getString(com.example.android.inventorymanager.R.string.no_image);
        }

        // Populate fields with extracted properties
        gameName.setText(name);
        gameQuantity.setText(quantity + "");
        gamePrice.setText(doubleToPrice(price, '$'));
        gameImage.setImageBitmap(dbHelper.getImage(GameContract.GameEntry._ID));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.saleClick(id,
                        quantity);
            }
        });
    }


    // Convert Price-Double to String
    public static String doubleToPrice(double price, char currency){
        // Define currency to be used
        char curr = '$';
        if(currency == '€')
            curr = currency;

        // Format the string using a DecimalFormat
        Locale locale = new Locale("en", "US");
        if(curr == '€')
            locale = new Locale("de", "DE");
        DecimalFormatSymbols sym = new DecimalFormatSymbols(locale);
        sym.setGroupingSeparator('.');
        if(curr == '$')
            sym.setGroupingSeparator(',');
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        formatter.applyPattern(curr + "##,##0.00");
        formatter.setDecimalFormatSymbols(sym);

        String returnString = formatter.format(price);

        // Add space between currency-symbol and price
        returnString = returnString.substring(0, 1) + " " + returnString.substring(1, returnString.length());

        return returnString;
    }

}
