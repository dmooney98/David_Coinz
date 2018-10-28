package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Settings extends AppCompatActivity {

    private String latDifficulty;
    private String lngDifficulty;
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
    }

}
