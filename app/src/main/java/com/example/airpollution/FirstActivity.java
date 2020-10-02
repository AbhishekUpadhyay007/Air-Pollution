package com.example.airpollution;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class FirstActivity extends AppCompatActivity {
    EditText cityTextView;
    Button searchButton;
    TextView aqiTextView , errorTextView;
    ImageView mainImageView;
    ProgressBar progressBar;

    WebView webView;
    String cityName;
    String databaseAqiValue;

    static int aqiValue ;

  static SQLiteDatabase database;


    public void history(View view){

        Intent intent = new Intent(getApplicationContext(),HistoryActivity.class);
        startActivity(intent);

    }

    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    public void onCity(View view){

        webView.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.VISIBLE);

        aqiTextView.setText(null);

        pollution pollution = new pollution();

        cityName = cityTextView.getText().toString();

        if(cityName.equals("")){

            Toast.makeText(this,"Please enter the name of the city",Toast.LENGTH_SHORT).show();
            aqiTextView.setText("Provide us the city name, We're happy to help");
        }else {

            progressBar.setVisibility(View.VISIBLE);

            try {

                pollution.execute("https://global.juheapi.com/aqi/v1/city?q=" + cityName + "&apikey=tMDgEvPBA1I9xJEB77bL6IStDFWRngx9");

                // executing webView * *


                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl("https://breezometer.com/air-quality-map/");

            } catch (Exception e) {

                Toast.makeText(this, "Unable to fetch details!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }
    //Hello World
    public class pollution extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection httpURLConnection;
            String pollutionData = "";
            try{
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while(data != -1){

                    char current = (char) data;
                    pollutionData += current;

                    data = reader.read();
                }

                return pollutionData;

            }catch (Exception e){

                Toast.makeText(FirstActivity.this,"City data not Available!",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return null;
            }


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String aqiString;

            try {

                JSONObject object = new JSONObject(s);

                String pollutionData = object.getString("data");

                pollutionData = "["+ pollutionData+"]";

                Log.i("data",pollutionData);

                JSONArray jsonArray = new JSONArray(pollutionData);

                progressBar.setVisibility(View.INVISIBLE);


                for (int i=0; i<jsonArray.length();i++){

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Log.i("city",jsonObject.getString("city"));
                    Log.i("AQI",jsonObject.getString("aqi"));

                    aqiValue =  Integer.parseInt(jsonObject.getString("aqi"));
                    aqiString = jsonObject.getString("aqi")+ "\n\n"+ jsonObject.getString("city");

                    SpannableStringBuilder ssb = new SpannableStringBuilder(aqiString);
                    ForegroundColorSpan fsb;
                    if(aqiValue > 60){
                        fsb = new ForegroundColorSpan(Color.RED);
                        mainImageView.setImageResource(R.drawable.polluted);
                    }else{
                        fsb = new ForegroundColorSpan(Color.GREEN);
                        mainImageView.setImageResource(R.drawable.clean);
                    }

                    ssb.setSpan(fsb,0,3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    aqiTextView.setText(ssb);

                    databaseAqiValue = ssb.toString();
                    Log.i("dtaaBase",String.valueOf(ssb));
                }
                /*HashSet<String> hashSet = new HashSet<>(history);
                sharedPreferences.edit().putStringSet("MyData", hashSet).apply();
               */

                errorTextView.setVisibility(View.VISIBLE);

                saveData(); // function for saving the data


            } catch (JSONException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(FirstActivity.this,"Unable to fetch details",Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void saveData(){

        try{

            database = getApplicationContext().openOrCreateDatabase("Student",MODE_PRIVATE,null);

            database.execSQL("CREATE TABLE IF NOT EXISTS NewHistory (AQI VARCHAR(40) , id PRIMARY KEY AUTOINCREMENT)");
            //database.execSQL("DROP TABLE USER");

            database.execSQL("INSERT INTO NewHistory (AQI) VALUES ('"+ databaseAqiValue + "')");

            Cursor c = FirstActivity.database.rawQuery("SELECT * FROM NewHistory", null);

            int aqiIndex = c.getColumnIndex("AQI");

            c.moveToFirst();

            while (c!=null){

                Log.i("DB",c.getString(aqiIndex));
                c.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        cityTextView = findViewById(R.id.cityTextView);
        searchButton= findViewById(R.id.searchButton);
        aqiTextView = findViewById(R.id.aqiTextView);
        mainImageView = findViewById(R.id.mainImageView);

        errorTextView = findViewById(R.id.errorTextView);

        webView = findViewById(R.id.webview);

        //setting up the color of progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFcc0000,
                android.graphics.PorterDuff.Mode.MULTIPLY);





    }
}
