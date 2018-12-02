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

//==================================================================================================
// This activity is used when the user banks one or more coins from their wallet at the BankCoins
// activity, whether by a standard, small gamble, or big gamble transaction.  This activity is used
// to inform the user whether they won or lost a gamble, as well as informing them how much gold
// they received from the transaction, and their new total.  From this activity, the user can be
// returned to BankCoins upon pressing the 'OKAY' button
public class GoldInform extends AppCompatActivity {

    //==============================================================================================
    // The only private variables which are required for this class are goldPass and bankPass, for
    // the storing and then passing back of these variables to BankCoins
    private Double goldPass;
    private int bankPass;

    //==============================================================================================
    // Set up appropriate variables using SharedPreferences and Bundles, and update the TextViews to
    // display the correct information about the transaction, and set the background
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_inform);

        // Acquire the SharedPreferences file, and initialise the TextView variables
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        TextView winLoseStandard = (TextView) findViewById(R.id.GI_winLoseStandard);
        TextView newTotal = (TextView) findViewById(R.id.GI_newTotal);
        TextView goldBanked = (TextView) findViewById(R.id.GI_goldBanked);

        // Create a DecimalFormat so that the figures involving gold can be displayed clearly to the
        // user
        DecimalFormat df = new DecimalFormat("#.###");

        // Set the background using the value obtained from SharedPreferences
        ImageView image = (ImageView) findViewById(R.id.GI_background);
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

        // Acquire the Bundle, and set up the variables correctly which take values from it, which
        // are the transaction type (Standard, Small Gamble, Big Gamble), whether the user won or
        // lost the gamble (or didn't use gamble), the user's total gold, and the profit they made
        // from the transaction
        Bundle bundle = getIntent().getExtras();
        String transactionType = bundle.getString("transactionType");
        String winLose = bundle.getString("winLose");
        double allGold = bundle.getDouble("allGold");
        double profit = bundle.getDouble("profit");

        // Set up the goldPass and bankPass variables so that they can be passed back to BankCoins
        // later in the activity
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        // Set two of the TextViews to display the correct total gold and transaction profits, using
        // the DecimalFormat created earlier
        newTotal.setText(String.valueOf(df.format(allGold)));
        goldBanked.setText(String.valueOf(df.format(profit)));

        // Use this if statement to determine whether the user participated in a gamble and, if they
        // did, whether they won or lost this gamble.  This allows us to update the corresponding
        // TextViews accordingly
        if (transactionType.equals("Standard")) {
            winLoseStandard.setText("DEPOSIT");
        } else if (transactionType.equals("SmallG")) {
            if (winLose.equals("Win")) {
                winLoseStandard.setText("YOU WIN!");
            } else {
                winLoseStandard.setText("YOU LOSE!");
            }
        } else if (transactionType.equals("SmallG")) {
            if (winLose.equals("Win")) {
                winLoseStandard.setText("YOU WIN!");
            } else {
                winLoseStandard.setText("YOU LOSE!");
            }
        }

    }

    //==============================================================================================
    // Return the user to the BankCoins activity, and pass back to this activity the goldPass and
    // bankPass variables
    public void goToBankCoins(View view) {
        Intent intent = new Intent (this, BankCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
