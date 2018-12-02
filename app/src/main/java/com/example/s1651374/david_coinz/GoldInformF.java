package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

// This activity is used when the user sends one or more coins from their spare change to a selected
// friend.  This activity is used to inform the user that the transaction was successful, the amount
// of gold transferred, and their friend's new total.  From this activity, the user can be returned
// to SendCoins upon pressing the 'OKAY' button
public class GoldInformF extends AppCompatActivity {

    //==============================================================================================
    // The only private variables which are required for this class are goldPass and bankPass, for
    // the storing and then passing back of these variables to SendCoins
    private Double goldPass;
    private int bankPass;

    //==============================================================================================
    // Set up appropriate variables using SharedPreferences and Bundles, and update the TextViews to
    // display the correct information about the transaction, and set the background
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_inform_f);

        // Acquire the SharedPreferences file, and initialise the TextView variables
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        TextView newTotal = (TextView) findViewById(R.id.GIF_newTotal);
        TextView goldBanked = (TextView) findViewById(R.id.GIF_goldBanked);

        // Create a DecimalFormat so that the figures involving gold can be displayed clearly to the
        // user
        DecimalFormat df = new DecimalFormat("#.###");

        // Set the background using the value obtained from SharedPreferences
        ImageView image = (ImageView) findViewById(R.id.GIF_background);
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

        // Acquire the Bundle, and set up the variables correctly which take values from it.  These
        // are the selected friend's total gold, and the amount of gold transferred to them
        Bundle bundle = getIntent().getExtras();
        double allGold = bundle.getDouble("allGold");
        double profit = bundle.getDouble("profit");

        // Set up the goldPass and bankPass variables so that they can be passed back to SendCoins
        // later in the activity
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        // Set the correct TextViews to display the correct friend's total gold and transfer value,
        // using the DecimalFormat created earlier
        newTotal.setText(String.valueOf(df.format(allGold)));
        goldBanked.setText(String.valueOf(df.format(profit)));
    }

    //==============================================================================================
    // Return the user to the SendCoins activity, and pass back to this activity the goldPass and
    // bankPass variables
    public void goToSendCoins(View view) {
        Intent intent = new Intent (this, SendCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
