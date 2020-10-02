package com.example.airpollution;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    static ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    ListView listView;

    public void savedata(){

        arrayList.clear();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        listView = findViewById(R.id.listView);

        arrayList = new ArrayList<>();
        savedata();

        try {
            Cursor c = FirstActivity.database.rawQuery("SELECT * FROM NewHistory", null);

            int aqiIndex = c.getColumnIndex("AQI");

            c.moveToFirst();

            while (c!=null){

                arrayList.add(c.getString(aqiIndex));
                arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,arrayList);
                listView.setAdapter(arrayAdapter);
                c.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
