package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

//==================================================================================================
// This activity allows the user to set the difficulty of their game, from the options of Easy,
// Normal, and Hard.  The user is also able to select their theme from this page, from the options
// of Standard, Night, Western, Rainy Day, Winter, and Artsy.  Both of these choices are saved to
// the SavedPreferences of the device.  From this activity, the user can go to MainMenu
public class Settings extends AppCompatActivity {

    //==============================================================================================
    // Create all variables required for the setting of the difficulty and retrieving of
    // SharedPreferences
    private String latDifficulty;
    private String lngDifficulty;
    private final String preferencesFile = "MyPrefsFile";

    //==============================================================================================
    // The most basic of onCreates, set the content view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    //==============================================================================================
    // Access SharedPreferences and set background accordingly
    public void onStart() {
        super.onStart();

        // Acquire the required SharedPreferences information, and initialise the ImageView variable
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.S_background);

        // Set the background using the value obtained from SharedPreferences
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

    //==============================================================================================
    // Take the user to the MainMenu activity, nothing needs to be passed
    public void goToMainMenu(View view) {
        Intent intent = new Intent (this, MainMenu.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Set the lat and lng difficulty variables to a higher range, allowing the coins to be
    // collected by the user from a farther distance.  Save this choice into SharedPreferences
    public void easySelect(View view) {
        latDifficulty = "0.0007";
        lngDifficulty = "0.0007";

        // Acquire the SharedPreferences and apply the recently set lat and lng difficulties
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();

        // Inform the user of the change in difficulty
        Toast.makeText(this, "Difficulty set to Easy", Toast.LENGTH_SHORT).show();
    }

    //==============================================================================================
    // Set the lat and lng difficulty variables to a standard range, allowing the coins to be
    // collected by the user from a medium distance.  Save this choice into SharedPreferences
    public void normalSelect(View view) {
        latDifficulty = "0.0005";
        lngDifficulty = "0.0005";

        // Acquire the SharedPreferences and apply the recently set lat and lng difficulties
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();

        // Inform the user of the change in difficulty
        Toast.makeText(this, "Difficulty set to Normal", Toast.LENGTH_SHORT).show();
    }

    //==============================================================================================
    // Set the lat and lng difficulty variables to a low range, meaning the user must be much
    // closer to the coins in order to collect them.  Save this choice into SharedPreferences
    public void hardSelect(View view) {
        latDifficulty = "0.0003";
        lngDifficulty = "0.0003";

        // Acquire the SharedPreferences and apply the recently set lat and lng difficulties
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latDifficulty", latDifficulty);
        editor.putString("lngDifficulty", lngDifficulty);
        editor.apply();

        // Inform the user of the change in difficulty
        Toast.makeText(this, "Difficulty set to Hard", Toast.LENGTH_SHORT).show();
    }

    //==============================================================================================
    // Set the user's chosen background to 'Standard', and save this into SharedPreferences
    public void background1Select(View view) {
        String choice = "1";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background1);
    }

    //==============================================================================================
    // Set the user's chosen background to 'Night', and save this into SharedPreferences
    public void background2Select(View view) {
        String choice = "2";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background2);
    }

    //==============================================================================================
    // Set the user's chosen background to 'Western', and save this into SharedPreferences
    public void background3Select(View view) {
        String choice = "3";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background3);
    }

    //==============================================================================================
    // Set the user's chosen background to 'Rainy Day', and save this into SharedPreferences
    public void background4Select(View view) {
        String choice = "4";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background4);
    }

    //==============================================================================================
    // Set the user's chosen background to 'Winter', and save this into SharedPreferences
    public void background5Select(View view) {
        String choice = "5";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background5);
    }

    //==============================================================================================
    // Set the user's chosen background to 'Artsy', and save this into SharedPreferences
    public void background6Select(View view) {
        String choice = "6";

        // Acquire the SharedPreferences and apply the recently set background choice
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("backgroundPick", choice);
        editor.apply();

        // Apply the background change to the activity
        ImageView image = (ImageView) findViewById(R.id.S_background);
        image.setImageResource(R.drawable.background6);
    }

}
