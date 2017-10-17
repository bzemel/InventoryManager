package com.example.android.inventorymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.android.inventorymanager.data.GameContract.GameEntry;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by benze on 9/22/2017.
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
                + GameEntry.COLUMN_GAME_PRICE + " DECIMAL NOT NULL, " + GameEntry.COLUMN_GAME_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " + GameEntry.COLUMN_GAME_IMAGE_URL + " TEXT, " + GameEntry.COLUMN_GAME_IMAGE_PATH + " TEXT, "
                + GameEntry.COLUMN_GAME_SUPPLIER_URL + " TEXT);";


        db.execSQL(SQL_CREATE_GAMES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //Updates the game image
    public void updateImage(String gameId, Bitmap image) {

        String imagePath = "";
        File storage = mContext.getDir("images", Context.MODE_PRIVATE);
        File filePath = new File(storage, gameId + ".png");
        imagePath = filePath.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (Exception e) {
            Log.i("DB", "Cannot update picture", e);
            imagePath = "";
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_IMAGE_PATH, imagePath);

        db.update(GameEntry.TABLE_NAME, values, GameEntry._ID + "=?", new String[]{String.valueOf(gameId)});
    }

    //Getting the picture from the specified row in the database
    public Bitmap getImage(String gameId) {
        String imagePath = getImagePath(gameId);

        if (imagePath == null || imagePath.length() == 0) {
            return null;
        }

        Bitmap image = BitmapFactory.decodeFile(imagePath);

        return image;
    }

    //Getting the file path of the picture from the specific game
    private String getImagePath(String gameId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(GameEntry.TABLE_NAME, null, GameEntry._ID + "=?", new String[]{String.valueOf(gameId)}, null, null, null);
        cursor.moveToNext();

        String imagePath = cursor.getString(cursor.getColumnIndex(GameEntry.COLUMN_GAME_IMAGE_PATH));

        return imagePath;
    }

    public void deleteGame(String gameId) {
        //Remove image from storage
        String imagePath = getImagePath(gameId);
        if (imagePath != null && imagePath.length() != 0) {
            File gamePath = new File(imagePath);
            gamePath.delete();
        }

        SQLiteDatabase db = getWritableDatabase();
        db.delete(GameEntry.TABLE_NAME, GameEntry._ID + "=?", new String[]{String.valueOf(gameId)});
    }

    public Cursor readStock() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_QUANTITY,
                GameEntry.COLUMN_GAME_IMAGE_URL,
                GameEntry.COLUMN_GAME_IMAGE_PATH,
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
