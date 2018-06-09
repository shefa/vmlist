package com.vum.tst2.Activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vum.tst2.Models.ShopContract;
import com.vum.tst2.Models.ShopTaskContract;
import com.vum.tst2.Models.TaskContract;
import com.vum.tst2.Models.TaskDbHelper;
import com.vum.tst2.R;
import com.vum.tst2.Views.ListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListActivity {
    private static final String TAG = "ListActivity: ";
    private TaskDbHelper mHelper;
    private MainActivity contextActivity;

    public ListActivity(TaskDbHelper dbHelper, MainActivity MA)
    {
        mHelper=dbHelper;
        contextActivity=MA;
    }

    public void addTask()
    {
        final EditText taskEditText = new EditText(contextActivity);
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
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

    public void taskOptions(View view)
    {
        final View tmpView = view;
        String opts[] = new String[]{"Edit","Assign to shop(s)","Delete"};
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
                .setTitle(getTaskNameFromView(view))
                .setItems(opts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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

        final EditText taskEditText = new EditText(contextActivity);
        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
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

        AlertDialog dialog = new AlertDialog.Builder(contextActivity)
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

        ListFragment bf = (ListFragment) contextActivity.mViewPager.getAdapter().instantiateItem(contextActivity.mViewPager,0);
        View v = bf.getViewCustom();

        if(contextActivity.mTaskListView==null) contextActivity.mTaskListView = (ListView) v.findViewById(R.id.list_todo);


        if (contextActivity.mAdapter == null) {
            contextActivity.mAdapter = new ArrayAdapter<>(contextActivity,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            contextActivity.mTaskListView.setAdapter(contextActivity.mAdapter);
        } else {
            contextActivity.mAdapter.clear();
            contextActivity.mAdapter.addAll(taskList);
            contextActivity.mAdapter.notifyDataSetChanged();
        }

        TextView tl = v.findViewById(R.id.empty_text);

        if(hasItems) tl.setVisibility(View.INVISIBLE);
        else tl.setVisibility(View.VISIBLE);


        cursor.close();
        db.close();
    }
}
