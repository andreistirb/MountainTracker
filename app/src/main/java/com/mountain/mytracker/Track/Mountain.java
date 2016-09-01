package com.mountain.mytracker.Track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mountain.mytracker.db.DatabaseContract;
import com.mountain.mytracker.db.DatabaseHelper;

/**
 * Created by anstirb on 17.08.2016.
 */
public class Mountain {

    private String name, description, id;
    private DatabaseHelper mDatabaseHelper;

    public Mountain(){}

    public Mountain(Context context){
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public Mountain(String id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Mountain(String id, String name, String description, Context context){
        this.id = id;
        this.name = name;
        this.description = description;
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DatabaseHelper getmDatabaseHelper() {
        return mDatabaseHelper;
    }

    public void setmDatabaseHelper(DatabaseHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }

    //insert current object into database
    public void toDatabase() throws Exception{
        ContentValues row = new ContentValues();

        if (mDatabaseHelper != null){
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID, this.id);
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME, this.name);
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_DESCRIPTION, this.description);
            mDatabaseHelper.getWritableDatabase()
                    .insert(DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN,
                            null,
                            row);
            mDatabaseHelper.close();
        }
        else
            throw new Exception();

    }

    //select entry from database and populate current object fields
    public void fromDatabase(String mountainId) throws Exception{
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor c;
        SQLiteDatabase database;

        if(mDatabaseHelper != null){
            qb.setTables(DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN);
            database = mDatabaseHelper.getReadableDatabase();
            c = qb.query(database,
                    null,
                    DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID + " = ? ",
                    new String[]{mountainId},
                    null,
                    null,
                    null);
            c.moveToFirst();
            if(c.getCount() > 0){
                this.id = c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID));
                this.name = c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME));
                this.description = c.getString(c.getColumnIndex(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_DESCRIPTION));
            }
            else
                throw new Exception();
            database.close();
        }
        else
            throw new Exception();
    }

    //update entry from database
    public void updateDatabase() throws Exception{
        ContentValues row = new ContentValues();

        if (mDatabaseHelper != null){
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID, this.id);
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_NAME, this.name);
            row.put(DatabaseContract.DatabaseEntry.COL_MOUNTAIN_DESCRIPTION, this.description);
            mDatabaseHelper.getWritableDatabase()
                    .update(DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN,
                            row,
                            DatabaseContract.DatabaseEntry.COL_MOUNTAIN_ID,
                            new String[]{this.id});
            mDatabaseHelper.close();
        }
        else
            throw new Exception();
    }

    //TO-DO
    public void deleteDatabase() throws Exception{
        if(mDatabaseHelper != null){

        }
        else
            throw new Exception();
    }

    public static void deleteAllDatabase(Context context){
        DatabaseHelper staticDatabaseHelper = new DatabaseHelper(context);
        SQLiteDatabase staticDatabase;

        staticDatabase = staticDatabaseHelper.getWritableDatabase();
        staticDatabase.delete(DatabaseContract.DatabaseEntry.TABLE_MOUNTAIN,
                null, null);
        staticDatabase.close();
    }

}
