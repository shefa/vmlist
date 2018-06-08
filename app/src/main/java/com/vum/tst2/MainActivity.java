package com.vum.tst2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.vum.tst2.db.ShopContract;
import com.vum.tst2.db.ShopTaskContract;
import com.vum.tst2.db.TaskContract;
import com.vum.tst2.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DebugMain";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    private boolean clickNewShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        clickNewShop=false;

        mHelper = new TaskDbHelper(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        // moved this code to run after BlankFragment's initialization
        //if(mViewPager.getCurrentItem()==0) updateUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_task) {
            if(mViewPager.getCurrentItem()==0)
            {
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.add_task_title)
                        .setMessage(R.string.add_task_msg)
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
            else
            {
                Snackbar.make(mViewPager.getRootView(), R.string.add_shop_tip, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                clickNewShop=true;

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0) return BlankFragment.newInstance();
            return new MapFragment();
        }

        @Override
        public int getCount() { return 2; }
    }

    public void onMapClick(LatLng point)
    {
        if(clickNewShop)
        {
            final LatLng tmpPoint = point;
            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
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
        else Snackbar.make(mViewPager.getRootView(), R.string.add_shop_tip2, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        clickNewShop=false;
    }

    public void onMarkerClick(Marker m)
    {
        final Marker tmpMarker = m;
        String opts[] = new String[]{"Edit name","View shop items","Delete"};
        AlertDialog dialog = new AlertDialog.Builder(this)
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

        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Items to buy at "+m.getTitle())
                .setItems(opts, null)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();
    }

    public void taskOptions(View view)
    {
        final View tmpView = view;
        String opts[] = new String[]{"Edit","Assign to shop(s)","Delete"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getTaskNameFromView(view))
                .setItems(opts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: TaskOptions "+which);
                        if(which==0) editTask(tmpView);
                        else if(which==2) deleteTask(tmpView);
                        else addTaskToShop(tmpView);
                    }
                })
                .create();
        dialog.show();
    }

    public void editTask(View view)
    {
        final int taskId = getTaskIdFromView(view);

        final EditText taskEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_task_title)
                .setMessage(R.string.add_task_msg)
                .setView(taskEditText)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String taskNew = String.valueOf(taskEditText.getText());
                        SQLiteDatabase db = mHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, taskNew);
                        db.update(TaskContract.TaskEntry.TABLE,
                                values,
                                TaskContract.TaskEntry._ID + " = ?",
                                new String[]{Integer.toString(taskId)});
                        db.close();
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    String getTaskNameFromView(View view)
    {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        return String.valueOf(taskTextView.getText());
    }

    int getTaskIdFromView(View view)
    {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        final String task = String.valueOf(taskTextView.getText());
        String rawSql = "SELECT "+ TaskContract.TaskEntry._ID+" FROM "+
                TaskContract.TaskEntry.TABLE+" WHERE "+
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?";
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor ct = db.rawQuery(rawSql, new String[]{task});
        ct.moveToFirst();
        return ct.getInt(ct.getColumnIndex(TaskContract.TaskEntry._ID));
    }

    public void addTaskToShop(View view)
    {
        // Get taskId and name
        final int taskId = getTaskIdFromView(view);
        final String taskName = getTaskNameFromView(view);

        ArrayList<Integer> shopsAssigned = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();
        final Map<String, Integer> mp = new HashMap<>();

        SQLiteDatabase db = mHelper.getReadableDatabase();

        // get all shops containing current task item
        Cursor cs = db.query(ShopTaskContract.ShopTaskEntry.TABLE,
                new String[]{ShopTaskContract.ShopTaskEntry._ID, ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP,  ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK},
                ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK+" = ?",new String[]{Integer.toString(taskId)},null,null,null);
        while(cs.moveToNext())
            shopsAssigned.add(cs.getInt(cs.getColumnIndex(ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP)));
        Log.d(TAG, "addTaskToShop: shopsAssigned len="+shopsAssigned.size());
        
        // get all shops
        Cursor c = db.query(ShopContract.ShopEntry.TABLE,
                new String[]{ShopContract.ShopEntry._ID, ShopContract.ShopEntry.COL_SHOP_TITLE, ShopContract.ShopEntry.COL_SHOP_LAT, ShopContract.ShopEntry.COL_SHOP_LNG},
                null,null,null,null,null);
        while (c.moveToNext()){
            String name = c.getString(c.getColumnIndex(ShopContract.ShopEntry.COL_SHOP_TITLE));
            Integer id = c.getInt(c.getColumnIndex(ShopContract.ShopEntry._ID));
            names.add(name);
            mp.put(name,id);
        }

        final int shopCnt = names.size();
        Log.d(TAG, "addTaskToShop: shops="+shopCnt);
        
        final String n[] = new String[shopCnt];
        for(int i=0;i<shopCnt;i++) n[i]=names.get(i);

        final boolean initShops[] = new boolean[shopCnt];
        final boolean initShop[] = new boolean[shopCnt];
        for(int i=0;i<shopCnt;i++) {
            initShops[i] = shopsAssigned.contains(mp.get(n[i]));
            initShop[i]=initShops[i];
            Log.d(TAG, "addTaskToShop: initShops["+i+"]="+initShops[i]+" shopName="+n[i]+", shopId="+mp.get(n[i]));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Which shops have "+taskName+"?")
                    .setMultiChoiceItems(n, initShops,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                                }
                            })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for(int i=0;i<shopCnt;i++)
                        {
                            if(initShops[i]&&!initShop[i])
                            {
                                Log.d(TAG, "onClick: inserting");
                                ContentValues values = new ContentValues();
                                values.put(ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP, mp.get(n[i]));
                                values.put(ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK, taskId);
                                mHelper.getReadableDatabase().insertWithOnConflict(ShopTaskContract.ShopTaskEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            }
                            else if(!initShops[i]&&initShop[i])
                            {
                                Log.d(TAG, "onClick: deleting");
                                mHelper.getReadableDatabase().delete(ShopTaskContract.ShopTaskEntry.TABLE,
                                        ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK+" = ? AND "+
                                                ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_SHOP+" = ?",
                                        new String[]{Integer.toString(taskId),Integer.toString(mp.get(n[i]))});
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
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

        MapFragment f = (MapFragment) mViewPager.getAdapter().instantiateItem(mViewPager,1);
        f.updateMap(names,latLngs,ids);
    }

    public void updateUI() {
        boolean hasItems=false;
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
            hasItems=true;
        }

        BlankFragment bf = (BlankFragment) mViewPager.getAdapter().instantiateItem(mViewPager,0);
        View v = bf.getViewCustom();

        if(mTaskListView==null) mTaskListView = (ListView) v.findViewById(R.id.list_todo);


        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        TextView tl = v.findViewById(R.id.empty_text);

        if(hasItems==true) tl.setVisibility(View.INVISIBLE);
        else tl.setVisibility(View.VISIBLE);


        cursor.close();
        db.close();
    }

    public void deleteTask(View view) {
        int taskId = getTaskIdFromView(view);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        //delete it from shops
        db.delete(ShopTaskContract.ShopTaskEntry.TABLE,
                    ShopTaskContract.ShopTaskEntry.COL_SHOPTASK_TASK + " = ?",
                    new String[]{Integer.toString(taskId)});


        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{Integer.toString(taskId)});
        db.close();
        updateUI();
    }


    // Permissions, requests etc..
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (android.support.v4.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                android.support.v4.app.ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                android.support.v4.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (android.support.v4.content.ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }
}
