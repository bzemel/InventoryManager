package com.example.android.inventorymanager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventorymanager.data.GameContract.GameEntry;
import com.example.android.inventorymanager.data.GameDbHelper;

/**
 * Created by benze on 9/9/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //loader identifier
    private static final int EXISTING_GAME_LOADER = 0;

    //Initialize CursorAdapter
    GameCursorAdapter mCursorAdapter;

    //Initialize Uri
    private Uri mCurrentGameUri;

    //Game's name
    private EditText mNameEditText;

    //Games' price
    private EditText mPriceEditText;

    //Game's quantity
    private EditText mQuantityEditText;

    //Game's image url
    private EditText mImageUrlEditText;

    //Supplier phone no
    private EditText mSupplierPhoneEditText;

    //Showing the image at the url given previously
    private ImageView mGameImage;

    //Buttons to subtract and add quantity
    Button minusButton;

    Button plusButton;

    //Delete button
    Button deleteButton;

    //Order button
    Button orderButton;

    //Image Update button
    Button imageUpdate;

    //boolean for unsaved change listener
    private Boolean mGameHasChanged = false;

    private GameDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //examine intent that was used to launch this activity
        Intent intent = getIntent();
        Uri currentGameUri = intent.getData();
        mCurrentGameUri = currentGameUri;

        //Setting title of view
        if (currentGameUri == null) {
            setTitle(R.string.add_game);
            // delete button hidden
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_game);
            //Initialize loader manager
            getSupportLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);
        }

        // Find all view to read input
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mImageUrlEditText = (EditText) findViewById(R.id.edit_item_image_url);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        mGameImage = (ImageView) findViewById(R.id.image_editor);
        minusButton = (Button) findViewById(R.id.minus_button);
        plusButton = (Button) findViewById(R.id.plus_button);
        deleteButton = (Button) findViewById(R.id.delete_button);
        orderButton = (Button) findViewById(R.id.order_button);
        imageUpdate = (Button) findViewById(R.id.image_update);

        //Setting OnTouchListeners
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageUrlEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        mDbHelper = new GameDbHelper(this);

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusOneQuantity();
                mGameHasChanged = true;
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusOneQuantity();
                mGameHasChanged = true;
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        imageUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUpdate();
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String supplierPhone = PhoneNumberUtils.stripSeparators(mSupplierPhoneEditText.getText().toString().trim());
                if (PhoneNumberUtils.isGlobalPhoneNumber(supplierPhone) && supplierPhone.length() == 10) {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:" + supplierPhone));
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.phone_number_not_valid),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new game, hide the "Delete" menu item.
        if (mCurrentGameUri == null) {
            deleteButton.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // back button pressed
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // dialog warning to save on back press
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // discard
                        finish();
                    }
                };

        // unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Keepp editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void minusOneQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            mQuantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    private void plusOneQuantity() {
        String previousValueString = mQuantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        mQuantityEditText.setText(String.valueOf(previousValue + 1));
    }

    private void imageUpdate() {
        String imageUrl = mImageUrlEditText.getText().toString().trim();

        if (imageUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.image_url_empty),
                    Toast.LENGTH_SHORT).show();
        } else {
            new DownloadImageTask(mGameImage)
                    .execute(imageUrl);
            Bitmap image = ((BitmapDrawable) mGameImage.getDrawable()).getBitmap();
            mDbHelper.updateImage(GameEntry._ID, image);

        }
    }


    //Get user input from editor and save new game into database
    private void saveGame() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        int quantityInt;
        //Checking for empty value in quantity. If empty, setting to 0
        if (quantityString.isEmpty()) {
            quantityInt = 0;
        } else {
            quantityInt = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        }
        String imageUrlString = mImageUrlEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        //Checking for empty values in other fields
        if (nameString.isEmpty() || priceString.isEmpty() || imageUrlString.isEmpty() || supplierPhoneString.isEmpty() || quantityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_update_game_failed),
                    Toast.LENGTH_SHORT).show();
        } else {

            double priceDouble = Double.parseDouble(priceString);

            ContentValues values = new ContentValues();
            values.put(GameEntry.COLUMN_GAME_NAME, nameString);
            values.put(GameEntry.COLUMN_GAME_PRICE, priceDouble);
            values.put(GameEntry.COLUMN_GAME_QUANTITY, quantityInt);
            values.put(GameEntry.COLUMN_GAME_IMAGE_URL, imageUrlString);
            values.put(GameEntry.COLUMN_GAME_SUPPLIER_URL, supplierPhoneString);
            imageUpdate();

            //Add game mode
            if (mCurrentGameUri == null) {

                Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // content uri null - problem with insertion
                    Toast.makeText(this, getString(R.string.game_insert_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // insertion successful
                    Toast.makeText(this, getString(R.string.game_insert_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                //Edit Game Mode

                int rowsAffected = getContentResolver().update(mCurrentGameUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // Nothing changed
                    Toast.makeText(this, getString(R.string.editor_update_game_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Edit successful
                    Toast.makeText(this, getString(R.string.editor_update_game_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option
        switch (item.getItemId()) {
            // Save button
            case R.id.action_save:
                //Save game
                saveGame();
                return true;
            // Up button
            case android.R.id.home:
                // Go back
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Unsaved changes - warn user
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Delete
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Cancel
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the game in the database.
     */
    private void deleteGame() {
        //Only perform if this is an existing game
        if (mCurrentGameUri != null) {
            //deleting game
            int rowsAffected = getContentResolver().delete(mCurrentGameUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsAffected == 0) {
                // Error deleting game
                Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Deleted the game
                Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
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

        return new CursorLoader(this, mCurrentGameUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of game attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
            int priceColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_QUANTITY);
            int imageUrlColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_IMAGE_URL);
            int supplierUrlColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_SUPPLIER_URL);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String imageUrl = cursor.getString(imageUrlColumnIndex);
            String supplierUrl = cursor.getString(supplierUrlColumnIndex);

            // Placeholder if image url is empty
            if (TextUtils.isEmpty(imageUrl)) {
                imageUrl = this.getString(com.example.android.inventorymanager.R.string.no_image);
            }


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mImageUrlEditText.setText(imageUrl);
            mSupplierPhoneEditText.setText(supplierUrl);
            new DownloadImageTask(mGameImage)
                    .execute(imageUrl);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mImageUrlEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }
}
