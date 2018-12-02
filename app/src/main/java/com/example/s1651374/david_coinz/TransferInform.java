package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

//==================================================================================================
// This activity is used when the user transfers one or more coins from their wallet at the
// SpareChange activity.  This activity is used to inform the user how many coins they transferred
// from their wallet to their spare change.  From this activity, the user can be returned to
// SpareChange upon pressing the 'OKAY' button
public class TransferInform extends AppCompatActivity {

    //==============================================================================================
    // The only private variables which are required for this class are goldPass and bankPass, for
    // the storing and then passing back of these variables to SpareChange
    private Double goldPass;
    private int bankPass;

    //==============================================================================================
    // Set up appropriate variables using SharedPreferences and Bundles, and update the TextViews to
    // display the correct information about the transfer, and set the background
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_inform);

        // Acquire the SharedPreferences file, and initialise the TextView variable
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        TextView transferText = (TextView) findViewById(R.id.TI_tranferText);

        // Set the background using the value obtained from SharedPreferences
        ImageView image = (ImageView) findViewById(R.id.TI_background);
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

        // Set up the goldPass and bankPass variables so that they can be passed back to BankCoins
        // later in the activity, and retrieve the info on the number of coins transferred
        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");
        int coinCount = bundle.getInt("coinCount");

        // This if statement is used to make sure the display on the number of coins transferred is
        // grammatically correct
        if(coinCount == 1) {
            transferText.setText("Successfully transferred " + coinCount +
                    " coin to Spare Change.");
        }
        else {
            transferText.setText("Successfully transferred " + coinCount +
                    " coins to Spare Change.");
        }
    }

    //==============================================================================================
    // Return the user to the SpareChange activity, and pass back to this activity the goldPass and
    // bankPass variables
    public void goToSpareChange(View view) {
        Intent intent = new Intent(this, SpareChange.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }
}
