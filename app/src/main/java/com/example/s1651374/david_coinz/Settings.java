package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    private String latDifficulty;
    private String lngDifficulty;
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
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

    public void onStart() {
        super.onStart();

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

    public void goToMainMenu(View view) {
        Intent intent = new Intent (this, MainMenu.class);
        startActivity(intent);
    }

    public void easySelect(View view) {
        latDifficulty = "0.0007";
        lngDifficulty = "0.0007";
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();
        Toast.makeText(this, "Difficulty set to Easy", Toast.LENGTH_SHORT).show();
    }

    public void normalSelect(View view) {
        latDifficulty = "0.0005";
        lngDifficulty = "0.0005";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();
        Toast.makeText(this, "Difficulty set to Normal", Toast.LENGTH_SHORT).show();
    }

    public void hardSelect(View view) {
        latDifficulty = "0.0003";
        lngDifficulty = "0.0003";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();
        Toast.makeText(this, "Difficulty set to Hard", Toast.LENGTH_SHORT).show();
    }

    public void background1Select(View view) {
        String choice = "1";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background1);
    }

    public void background2Select(View view) {
        String choice = "2";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background2);
    }

    public void background3Select(View view) {
        String choice = "3";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background3);
    }

    public void background4Select(View view) {
        String choice = "4";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background4);
    }

    public void background5Select(View view) {
        String choice = "5";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background5);
    }

    public void background6Select(View view) {
        String choice = "6";
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();
        ImageView image = (ImageView) findViewById(R.id.background);
        image.setImageResource(R.drawable.background6);
    }

}
