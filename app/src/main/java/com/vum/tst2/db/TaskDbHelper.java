package com.vum.tst2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    public TaskDbHelper(Context context) {
        super(context, TaskContract.DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTasks = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL);";

        String createShops = "CREATE TABLE " + ShopContract.ShopEntry.TABLE + " ( " +
                ShopContract.ShopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ShopContract.ShopEntry.COL_SHOP_TITLE + " TEXT NOT NULL, " +
                ShopContract.ShopEntry.COL_SHOP_LAT + " DOUBLE NOT NULL, " +
                ShopContract.ShopEntry.COL_SHOP_LNG + " DOUBLE NOT NULL);";

        String createShopTasks = "CREATE TABLE " + ShopTaskContract.ShopTaskEntry.TABLE + " ( " +
                ShopTaskContract.ShopTaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP + " INTEGER NOT NULL, " +
                ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK + " INTEGER NOT NULL);";

        db.execSQL(createTasks);
        db.execSQL(createShops);
        db.execSQL(createShopTasks);

        ContentValues values = new ContentValues();
        values.put(ShopContract.ShopEntry.COL_SHOP_TITLE,"VUMShop");
        values.put(ShopContract.ShopEntry.COL_SHOP_LAT,43.21194485250614);
        values.put(ShopContract.ShopEntry.COL_SHOP_LNG,27.90917068719864);
        db.insert(ShopContract.ShopEntry.TABLE,null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ShopContract.ShopEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ShopTaskContract.ShopTaskEntry.TABLE);
        onCreate(db);
    }
}