package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DepositCoins extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_coins);

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

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);
        TextView dValue = (TextView) findViewById(R.id.textView14);
        TextView pValue = (TextView) findViewById(R.id.textView15);
        TextView qValue = (TextView) findViewById(R.id.textView16);
        TextView sValue = (TextView) findViewById(R.id.textView17);

        String dolr = settings.getString("dolr", "Unknown");
        String peny = settings.getString("peny", "Unknown");
        String quid = settings.getString("quid", "Unknown");
        String shil = settings.getString("shil", "Unknown");

        dValue.setText("DOLR: " + dolr);
        pValue.setText("PENY: " + peny);
        qValue.setText("QUID: " + quid);
        sValue.setText("SHIL: " + shil);

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

    public void goToMapScreen(View view) {
        Intent intent = new Intent (this, MapScreen.class);
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        String mapdata = settings.getString("mapdata", "");
        intent.putExtra("mapdata", mapdata);
        startActivity(intent);
    }

    public void goToBankCoins(View view) {
        Intent intent = new Intent (this, BankCoins.class);
        startActivity(intent);
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, SendCoins.class);
        startActivity(intent);
    }

}
