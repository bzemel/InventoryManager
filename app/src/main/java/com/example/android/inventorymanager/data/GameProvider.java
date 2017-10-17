package com.example.android.inventorymanager.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.android.inventorymanager.data.GameContract.GameEntry;

/**
 * Created by benze on 9/22/2017.
 */

public class GameProvider extends ContentProvider {


    //Database helper object
    private GameDbHelper mDbHelper;

    //Whole table
    public static final int GAMES = 100;

    //One game
    public static final int GAME_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sUriMatcher.addURI(GameContract.CONTENT_AUTHORITY, GameContract.PATH_GAMES, GAMES);

        sUriMatcher.addURI(GameContract.CONTENT_AUTHORITY, GameContract.PATH_GAMES + "/#", GAME_ID);
    }

    public static final String LOG_TAG = GameProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        mDbHelper = new GameDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        // Try to match to code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                // Query on whole table
                cursor = database.query(GameEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case GAME_ID:
                // Get the ID for the game
                selection = GameEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(GameEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return insertGame(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertGame(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(GameEntry.COLUMN_GAME_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Game requires a name");
        }

        // Check that the price is valid
        double price = values.getAsDouble(GameEntry.COLUMN_GAME_PRICE);
        if (price <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }

        // Check that the quantity is valid
        Integer quantity = values.getAsInteger(GameEntry.COLUMN_GAME_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(GameEntry.TABLE_NAME, null, values);


        // If the ID is -1, then the insertion failed.
        if (id == -1) {
            return null;
        }

        //Notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        // Return uri with id
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return updateGame(uri, contentValues, selection, selectionArgs);
            case GAME_ID:
                selection = GameEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateGame(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateGame(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that the name is not null
        if (values.containsKey(GameEntry.COLUMN_GAME_NAME)) {
            String name = values.getAsString(GameEntry.COLUMN_GAME_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Game requires a name");
            }
        }

        // Check that the price is valid
        if (values.containsKey(GameEntry.COLUMN_GAME_PRICE)) {
            double price = values.getAsDouble(GameEntry.COLUMN_GAME_PRICE);
            if (price <= 0) {
                throw new IllegalArgumentException("Invalid price");
            }
        }

        // Check that the quantity is valid
        if (values.containsKey(GameEntry.COLUMN_GAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(GameEntry.COLUMN_GAME_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Invalid quantity");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(GameEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify listeners 1 or more rows have been affected
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GAMES:
                rowsDeleted = database.delete(GameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GAME_ID:
                // Delete a single row
                selection = GameEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(GameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Notify listeners if 1 or more rows deleted
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return GameEntry.CONTENT_LIST_TYPE;
            case GAME_ID:
                return GameEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
