package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.Toast;

import java.text.Format;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class MainMenu extends AppCompatActivity {

    private final String tag = "MainMenu";

    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String mapdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time tha app is run)
        downloadDate = settings.getString("lastDownloadDate","");
        downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Log.d(tag ,"[onStart] Recalled lastDownloadDate is '" + downloadDate + "'");

        String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";
        AsyncTask<String, Void, String> data = new DownloadFileTask().execute(link);
        try {
            mapdata = data.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        // Apply the edits!
        editor.apply();
    }

    // Code for button functionalities
    public void goToMapScreen(View view) {
        Intent intent = new Intent (this, MapScreen.class);
        intent.putExtra("mapdata", mapdata);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent (this, Settings.class);
        startActivity(intent);
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, DepositCoins.class);
        startActivity(intent);
    }

}
