package com.example.ahhear.ahhearapp;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MyRecordings extends AppCompatActivity {
    LocalDataBaseManager localDataBaseManager;
    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_recordings);
        mListView = (ListView) findViewById(R.id.my_recordings_list);
        localDataBaseManager = new LocalDataBaseManager(this, "", null, 1);
        populateListView();

    }

    private void populateListView() {
        Cursor data = localDataBaseManager.list_my_gigs();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            listData.add(data.getString(1));
            listData.add(data.getString(2));
            listData.add(data.getString(3));
            listData.add(data.getString(4));
        }
        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        mListView.setAdapter(listAdapter);
    }
}
