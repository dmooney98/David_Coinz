package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class MainMenu extends AppCompatActivity {

    private final String tag = "MainMenu";

    private String lastDownloadDate = "";
    private String today = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String mapdata = "";
    private String shil;
    private String quid;
    private String dolr;
    private String peny;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        if (backgroundPick.equals("1")) {
            image.setImageResource(R.drawable.background1);
        } else if (backgroundPick.equals("2")) {
            image.setImageResource(R.drawable.background2);
        } else if (backgroundPick.equals("3")) {
            image.setImageResource(R.drawable.background3);
        } else if (backgroundPick.equals("4")) {
            image.setImageResource(R.drawable.background4);
        } else if (backgroundPick.equals("5")) {
            image.setImageResource(R.drawable.background5);
        } else if (backgroundPick.equals("6")) {
            image.setImageResource(R.drawable.background6);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time tha app is run)
        lastDownloadDate = settings.getString("lastDownloadDate","");
        Log.d(tag ,"[onStart] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        mapdata = settings.getString("mapdata", "");

        if ((!today.equals(lastDownloadDate)) || mapdata.equals("")) {
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + today + "/coinzmap.geojson";
            AsyncTask<String, Void, String> data = new DownloadFileTask().execute(link);
            try {
                mapdata = data.get();

                lastDownloadDate = today;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Map downloading", Toast.LENGTH_LONG).show();
            try {
                JSONObject json = new JSONObject(mapdata);
                JSONObject rates = json.getJSONObject("rates");
                shil = rates.getString("SHIL");
                quid = rates.getString("QUID");
                dolr = rates.getString("DOLR");
                peny = rates.getString("PENY");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            shil = settings.getString("shil", "");
            quid = settings.getString("quid", "");
            dolr = settings.getString("dolr", "");
            peny = settings.getString("peny", "");
            //Toast.makeText(this, "Today's map has been downloaded", Toast.LENGTH_LONG).show();
        }

        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        if (backgroundPick.equals("1")) {
            image.setImageResource(R.drawable.background1);
        } else if (backgroundPick.equals("2")) {
            image.setImageResource(R.drawable.background2);
        } else if (backgroundPick.equals("3")) {
            image.setImageResource(R.drawable.background3);
        } else if (backgroundPick.equals("4")) {
            image.setImageResource(R.drawable.background4);
        } else if (backgroundPick.equals("5")) {
            image.setImageResource(R.drawable.background5);
        } else if (backgroundPick.equals("6")) {
            image.setImageResource(R.drawable.background6);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + lastDownloadDate);
        Log.d(tag, "[onStop] Storing mapdata as " + mapdata);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", lastDownloadDate);
        editor.putString("mapdata", mapdata);
        editor.putString("shil", shil);
        editor.putString("quid", quid);
        editor.putString("dolr", dolr);
        editor.putString("peny", peny);
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

    public void goToHelp(View view) {
        Intent intent = new Intent (this, Help.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        Intent intent = new Intent (this, SignIn.class);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        startActivity(intent);
    }

}
