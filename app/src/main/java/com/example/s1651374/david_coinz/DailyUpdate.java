package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

//==================================================================================================
// This activity is required for when one of the date checks discovers that the current date does
// not match that which is stored for that user in the database.  This gives us a screen to inform
// the user of this change, in case they forgot, and also gives a clean way of returning the user to
// the MainMenu, which allows the new day's map to be downloaded, and goldPass and bankPass to be
// updated properly.
// From this activity the user can return to the MainMenu activity
public class DailyUpdate extends AppCompatActivity {

    //==============================================================================================
    // Initialise the page with the correct background by using SharedPreferences, as nothing else
    // has to be set up for this screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_update);
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);

        ImageView image = (ImageView) findViewById(R.id.DU_background);
        String backgroundPick = settings.getString("backgroundPick", "1");
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

    // Return the user to the MainMenu activity when they press the 'OKAY' button, allowing the new
    // map to be downloaded and the appropriate variables to be updated
    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}
