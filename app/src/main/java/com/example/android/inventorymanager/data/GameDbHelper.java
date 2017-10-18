package com.example.android.inventorymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventorymanager.data.GameContract.GameEntry;

/**
 * Created by benze on 9/22/2017.
 *
 * Icon made by Freepik from www.flaticon.com
 *
 */

public class GameDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";

    public static final int DATABASE_VERSION = 1;

    Context mContext;

    public GameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_GAMES_TABLE = "CREATE TABLE " + GameEntry.TABLE_NAME + " (" + GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GameEntry.COLUMN_GAME_NAME + " TEXT NOT NULL, "
                + GameEntry.COLUMN_GAME_PRICE + " DECIMAL NOT NULL, " + GameEntry.COLUMN_GAME_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " + GameEntry.COLUMN_GAME_IMAGE_URL + " TEXT, "
                + GameEntry.COLUMN_GAME_SUPPLIER_URL + " TEXT);";


        db.execSQL(SQL_CREATE_GAMES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public Cursor readStock() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_QUANTITY,
                GameEntry.COLUMN_GAME_IMAGE_URL,
                GameEntry.COLUMN_GAME_SUPPLIER_URL,
        };

        Cursor cursor = db.query(
                GameEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    public void sellItem(long itemId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        int newQuantity = 0;
        if (quantity > 0) {
            newQuantity = quantity -1;
        }
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_QUANTITY, newQuantity);
        String selection = GameEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(itemId) };
        db.update(GameEntry.TABLE_NAME,
                values, selection, selectionArgs);
    }
}
