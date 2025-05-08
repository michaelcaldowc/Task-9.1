package com.michaelcaldow.task91P;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 2;
    // Table Name
    private static final String TABLE_ITEMS = "items";
    // Items Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM_POST_TYPE = "post_type";
    private static final String KEY_ITEM_NAME = "name";
    private static final String KEY_ITEM_PHONE = "phone";
    private static final String KEY_ITEM_DESCRIPTION = "description";
    private static final String KEY_ITEM_DATE = "date";
    private static final String KEY_ITEM_LOCATION = "location";
    private static final String KEY_ITEM_LATITUDE = "latitude";
    private static final String KEY_ITEM_LONGITUDE = "longitude";

    // Singleton instance
    private static DatabaseHelper instance;
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Constructor (Private as using Singleton pattern)
    private DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ITEM_POST_TYPE + " TEXT," +
                KEY_ITEM_NAME + " TEXT," +
                KEY_ITEM_PHONE + " TEXT," +
                KEY_ITEM_DESCRIPTION + " TEXT," +
                KEY_ITEM_DATE + " TEXT," +
                KEY_ITEM_LOCATION + " TEXT," +
                KEY_ITEM_LATITUDE + " REAL," +
                KEY_ITEM_LONGITUDE + " REAL" +
                ")";

        db.execSQL(CREATE_ITEMS_TABLE);
        Log.i("DatabaseHelper", "Database table created: " + TABLE_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
            Log.w("DatabaseHelper", "Database upgraded from version " + oldVersion + " to " + newVersion + ". Table dropped and recreated.");
        }
    }

    // CRUD Operations
    public long addItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();
        long insertedId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_POST_TYPE, item.getPostType());
            values.put(KEY_ITEM_NAME, item.getName());
            values.put(KEY_ITEM_PHONE, item.getPhone());
            values.put(KEY_ITEM_DESCRIPTION, item.getDescription());
            values.put(KEY_ITEM_DATE, item.getDate());
            values.put(KEY_ITEM_LOCATION, item.getLocation());
            values.put(KEY_ITEM_LATITUDE, item.getLatitude());
            values.put(KEY_ITEM_LONGITUDE, item.getLongitude());


            insertedId = db.insertOrThrow(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
            Log.i("DatabaseHelper", "Item added successfully with ID: " + insertedId);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to add item to database", e);
        } finally {
            db.endTransaction();

        }
        return insertedId;
    }

    public Item getItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Item item = null;

        // Define the columns to retrieve
        String[] projection = {
                KEY_ITEM_ID,
                KEY_ITEM_POST_TYPE,
                KEY_ITEM_NAME,
                KEY_ITEM_PHONE,
                KEY_ITEM_DESCRIPTION,
                KEY_ITEM_DATE,
                KEY_ITEM_LOCATION,
                KEY_ITEM_LATITUDE,
                KEY_ITEM_LONGITUDE
        };

        // Define the selection criteria
        String selection = KEY_ITEM_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_ITEMS,        // Table to query
                    projection,         // Columns to return
                    selection,          // Columns for the WHERE clause
                    selectionArgs,      // Values for the WHERE clause
                    null,               // Not group the rows
                    null,               // Not filter by row groups
                    null                // Sort order
            );

            if (cursor != null && cursor.moveToFirst()) {
                long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ITEM_ID));
                String postType = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_PHONE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_DATE));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_LOCATION));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ITEM_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ITEM_LONGITUDE));

                item = new Item(itemId, postType, name, phone, description, date, location, latitude, longitude);
                Log.i("DatabaseHelper", "Item fetched successfully: " + item.getName());
            } else {
                Log.w("DatabaseHelper", "No item found with ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get item by ID from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return item;
    }


    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        String SELECT_ALL_ITEMS_QUERY = "SELECT * FROM " + TABLE_ITEMS;

        try {
            cursor = db.rawQuery(SELECT_ALL_ITEMS_QUERY, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ITEM_ID));
                    String postType = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_POST_TYPE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_PHONE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_DESCRIPTION));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_DATE));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_LOCATION));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ITEM_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ITEM_LONGITUDE));

                    Item item = new Item(itemId, postType, name, phone, description, date, location, latitude, longitude);
                    items.add(item);
                } while (cursor.moveToNext());
                Log.i("DatabaseHelper", "Fetched " + items.size() + " items successfully.");
            } else {
                Log.i("DatabaseHelper", "No items found in the database.");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get all items from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return items;
    }

   
    public int deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = 0;

        db.beginTransaction();
        try {
            String selection = KEY_ITEM_ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };

            rowsAffected = db.delete(TABLE_ITEMS, selection, selectionArgs);
            db.setTransactionSuccessful();
            if (rowsAffected > 0) {
                Log.i("DatabaseHelper", "Item deleted successfully. ID: " + id + ", Rows affected: " + rowsAffected);
            } else {
                Log.w("DatabaseHelper", "Item deletion failed or item not found. ID: " + id);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to delete item from database", e);
        } finally {
            db.endTransaction();
        }
        return rowsAffected;
    }
}
