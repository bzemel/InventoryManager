package com.example.android.inventorymanager.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by benze on 9/9/2017.
 */

public class GameContract {

    //Constant for inventory table
    public static final String CONTENT_AUTHORITY = "com.example.android.inventorymanager";
    //Creates content authority URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Path table name
    public static final String PATH_GAMES = "games";

    public static abstract class GameEntry implements BaseColumns {

        public static final String TABLE_NAME = "games";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_GAME_NAME = "name";
        public static final String COLUMN_GAME_PRICE = "price";
        public static final String COLUMN_GAME_QUANTITY = "quantity";
        public static final String COLUMN_GAME_IMAGE_URL = "imageurl";
        public static final String COLUMN_GAME_IMAGE_PATH = "imagename";
        public static final String COLUMN_GAME_SUPPLIER_URL = "supplierurl";

        //Constant for complete URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;


    }
}
