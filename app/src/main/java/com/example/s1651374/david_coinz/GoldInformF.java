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

public class GoldInformF extends AppCompatActivity {

    private double allGold;
    private double profit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_inform_f);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        TextView newTotal = (TextView) findViewById(R.id.newTotal);
        TextView goldBanked = (TextView) findViewById(R.id.goldBanked);
        DecimalFormat df = new DecimalFormat("#.###");

        ImageView image = (ImageView) findViewById(R.id.background);
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

        Bundle bundle = getIntent().getExtras();
        allGold = bundle.getDouble("allGold");
        profit = bundle.getDouble("profit");

        newTotal.setText(String.valueOf(df.format(allGold)));
        goldBanked.setText(String.valueOf(df.format(profit)));
    }

    public void goToSendCoins(View view) {
        Intent intent = new Intent (this, SendCoins.class);
        startActivity(intent);
    }

}