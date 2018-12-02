package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

//==================================================================================================
// This activity is used to show the user the second page of the Help section, reached either from
// Help1 or BankCoins  The only action required in this activity is to make sure that the background
// is set correctly.  From this activity the user can go to the MainMenu activity, or go to
// Help - the first page of the Help section
public class Help2 extends AppCompatActivity {

    //==============================================================================================
    // Use SharedPreferences to set up the background correctly
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help2);

        // Retrieve the background choice from SharedPreferences, and set up the ImageView to be
        // changed
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.H2_background);

        // Set the background using the variable taken from SharedPreferences
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
    // Take the user to the Help activity to view the first page of the Help section, nothing needs
    // to be passed
    public void goToHelp(View view) {
        Intent intent = new Intent (this, Help.class);
        startActivity(intent);
    }

}
