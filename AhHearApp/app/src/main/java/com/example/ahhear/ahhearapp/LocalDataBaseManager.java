package com.example.ahhear.ahhearapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lotbs on 25/11/2017.
 */

public class LocalDataBaseManager extends SQLiteOpenHelper {

    public LocalDataBaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "AhHere.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE MY_GIGS(ID INTEGER PRIMARY KEY AUTOINCREMENT, GIGDATE TEXT, BAND TEXT, VENUE TEXT, SPL INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MY_GIGS");
        onCreate(db);
    }
    public void insert_gig_recording(String band, String venue, int spl){
        // Format the current time.
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' hh:mm:ss");
        Date currentTime_1 = new Date();
        String dateString = formatter.format(currentTime_1);
        ContentValues contentValues = new ContentValues();
        contentValues.put("GIGDATE", dateString);
        contentValues.put("BAND", band);
        contentValues.put("VENUE", venue);
        contentValues.put("SPL", spl);
        this.getWritableDatabase().insertOrThrow("MY_GIGS","", contentValues);
    }
    public Cursor list_my_gigs(){
        Cursor cursor;
        cursor = this.getReadableDatabase().rawQuery("SELECT * FROM MY_GIGS", null);
//        cursor.close();
        return cursor;
    }
}
