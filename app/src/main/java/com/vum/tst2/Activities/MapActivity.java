package com.vum.tst2.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.vum.tst2.Models.ShopContract;
import com.vum.tst2.Models.ShopTaskContract;
import com.vum.tst2.Models.TaskContract;
import com.vum.tst2.Models.TaskDbHelper;
import com.vum.tst2.R;
import com.vum.tst2.ViewModels.MapViewModel;
import com.vum.tst2.Views.MapFragment;

import java.util.ArrayList;

public class MapActivity {

    private boolean clickNewShop;
    private TaskDbHelper mHelper;
    private MainActivity contextActivity;

    public MapActivity(TaskDbHelper dbHelper, MainActivity MA)
    {
        clickNewShop=false;
        mHelper=dbHelper;
        contextActivity=MA;
    }

    public void addShop(){
        Snackbar.make(contextActivity.mViewPager.getRootView(), R.string.add_shop_tip, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        clickNewShop=true;
    }

    public void onMapClick(LatLng point)
    {
        if(clickNewShop)
        {
            final LatLng tmpPoint = point;
            final EditText taskEditText = new EditText(contextActivity);
            AlertDialog dialog = new AlertDialog.Builder(contextActivity)
                    .setTitle(R.string.add_shop_title)
                    .setMessage(R.string.add_shop_msg)
                    .setView(taskEditText)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String task = String.valueOf(taskEditText.getText());
                            SQLiteDatabase db = mHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(ShopContract.ShopEntry.COL_SHOP_TITLE, task);
                            values.put(ShopContract.ShopEntry.COL_SHOP_LAT, tmpPoint.latitude);
                            values.put(ShopContract.ShopEntry.COL_SHOP_LNG, tmpPoint.longitude);

                            db.insertWithOnConflict(ShopContract.ShopEntry.TABLE,null,values,SQLiteDatabase.CONFLICT_REPLACE);
                            db.close();
                            updateMap();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        }
        else Snackbar.make(contextActivity.mViewPager.getRootView(), R.string.add_shop_tip2, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        clickNewShop=false;
    }

    public void onMarkerClick(Marker m)
    {
        final Marker tmpMarker = m;
        String opts[] = new String[]{"Edit name","View shop items","Delete"};
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
                .setTitle(m.getTitle())
                .setItems(opts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0) editMarker(tmpMarker);
                        else if(which==2) deleteMarker(tmpMarker);
                        else viewShopItems(tmpMarker);
                    }
                })
                .create();
        dialog.show();
    }

    public void editMarker(Marker m)
    {
        final int markerId = (int) m.getTag();

        final EditText taskEditText = new EditText(contextActivity);
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
                .setTitle(R.string.edit_shop_title)
                .setMessage(R.string.add_shop_msg)
                .setView(taskEditText)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String taskNew = String.valueOf(taskEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put(ShopContract.ShopEntry.COL_SHOP_TITLE, taskNew);
                        db.update(ShopContract.ShopEntry.TABLE,
                                values,
                                ShopContract.ShopEntry._ID + " = ?",
                                new String[]{Integer.toString(markerId)});
                        db.close();
                        updateMap();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    public void deleteMarker(Marker m)
    {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int markerId = (int) m.getTag();

        //delete shop from tasks
        db.delete(ShopTaskContract.ShopTaskEntry.TABLE,
                ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP + " = ?",
                new String[]{Integer.toString(markerId)});

        db.delete(ShopContract.ShopEntry.TABLE,
                ShopContract.ShopEntry._ID + " = ?",
                new String[]{Integer.toString(markerId)});
        db.close();
        updateMap();
    }

    public void viewShopItems(Marker m)
    {
        int markerId = (int) m.getTag();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        ArrayList<String> tasks = new ArrayList<>();
        // get all shops containing current task item
        String q = "SELECT * FROM "+ShopTaskContract.ShopTaskEntry.TABLE+" st INNER JOIN "+ TaskContract.TaskEntry.TABLE+" t ON "+
                "st."+ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK+"=t."+ TaskContract.TaskEntry._ID+
                " WHERE st."+ ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP+"=?";
        Cursor cs = db.rawQuery(q, new String[]{Integer.toString(markerId)});
        while(cs.moveToNext())
            tasks.add(cs.getString(cs.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE)));

        String opts[] = new String[tasks.size()];
        for (int i=0;i<tasks.size();i++) opts[i]=tasks.get(i);
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
                .setTitle("Items to buy at "+m.getTitle())
                .setItems(opts, null)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();
    }

    public void updateMap()
    {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<LatLng> latLngs = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = db.query(ShopContract.ShopEntry.TABLE,
                new String[]{ShopContract.ShopEntry._ID, ShopContract.ShopEntry.COL_SHOP_TITLE, ShopContract.ShopEntry.COL_SHOP_LAT, ShopContract.ShopEntry.COL_SHOP_LNG},
                null,null,null,null,null);
        while (c.moveToNext())
        {
            ids.add(c.getInt(c.getColumnIndex(ShopContract.ShopEntry._ID)));
            names.add(c.getString(c.getColumnIndex(ShopContract.ShopEntry.COL_SHOP_TITLE)));
            LatLng x = new LatLng(c.getDouble(c.getColumnIndex(ShopContract.ShopEntry.COL_SHOP_LAT)),
                    c.getDouble(c.getColumnIndex(ShopContract.ShopEntry.COL_SHOP_LNG)));
            latLngs.add(x);
        }

        MapFragment f = (MapFragment) contextActivity.mViewPager.getAdapter().instantiateItem(contextActivity.mViewPager,1);

        f.updateMap(new MapViewModel(names,latLngs,ids));
    }
}
