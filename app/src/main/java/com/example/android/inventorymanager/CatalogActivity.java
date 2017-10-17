package com.example.android.inventorymanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.GameContract.GameEntry;
import com.example.android.inventorymanager.data.GameDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Initialize CursorAdapter
    GameCursorAdapter mCursorAdapter;

    //Initialize DBHelper
    GameDbHelper mDbHelper;

    //Identifies loader
    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        mDbHelper = new GameDbHelper(this);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView
        ListView gameListView = (ListView) findViewById(R.id.list);

        // 0 items in ListView
        View emptyView = findViewById(R.id.empty_view);
        gameListView.setEmptyView(emptyView);

        //Clicklistener on items
        gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);


                //form the content uri that represents the current game
                Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, id);

                //set the uri on the data field of the intent
                intent.setData(currentGameUri);

                startActivity(intent);
            }
        });

        //Setup an Adapter to create a list item for each row of game data in the Cursor
        mCursorAdapter = new GameCursorAdapter(this, null);
        gameListView.setAdapter(mCursorAdapter);


        // Prepare the loader.
        getSupportLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void saleClick(long id, int quantity) {
        mDbHelper.sellItem(id, quantity);
        mCursorAdapter.swapCursor(mDbHelper.readStock());
    }

    private void insertGame() {
        // Create a ContentValues object where column names are the keys,
        // and Catan's game attributes are the values.
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_NAME, "Catan");
        values.put(GameEntry.COLUMN_GAME_PRICE, 35.99);
        values.put(GameEntry.COLUMN_GAME_QUANTITY, 5);
        values.put(GameEntry.COLUMN_GAME_IMAGE_URL, "https://images-na.ssl-images-amazon.com/images/I/81SrTgYBDaL._SL1500_.jpg");
        values.put(GameEntry.COLUMN_GAME_SUPPLIER_URL, "5551234567");

        // Insert a new row for Catan into the provider using the ContentResolver.
        Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option
        switch (item.getItemId()) {
            // insert dummy data
            case R.id.action_insert_dummy_data:
                insertGame();
                getSupportLoaderManager().initLoader(URL_LOADER, null, this);
                return true;
            // delete all entries
            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllConfirmationDialog() {
        //AlertDialog.Builder - are you sure?
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all games
                deleteAllGames();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Cancel
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteAllGames() {
        getContentResolver().delete(GameEntry.CONTENT_URI, null, null);
        // deleted all games
        Toast.makeText(getApplicationContext(), getString(R.string.game_delete_all_successful),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Define projection
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_QUANTITY,
                GameEntry.COLUMN_GAME_IMAGE_URL,
                GameEntry.COLUMN_GAME_SUPPLIER_URL};

        return new CursorLoader(this, GameEntry.CONTENT_URI, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCursorAdapter.swapCursor(null);

    }
}
