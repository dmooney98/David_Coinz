package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class GoldInform extends AppCompatActivity {

    private String transactionType;
    private String winLose;
    private double total;
    private double allGold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_inform);
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        TextView winLoseStandard = (TextView) findViewById(R.id.winLoseStandard);
        TextView newTotal = (TextView) findViewById(R.id.newTotal);
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
        transactionType = bundle.getString("transactionType");
        winLose = bundle.getString("winLose");
        allGold = bundle.getDouble("allGold");

        newTotal.setText(String.valueOf(df.format(allGold)));
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

    public void goToBankCoins(View view) {
        Intent intent = new Intent (this, BankCoins.class);
        startActivity(intent);
    }

}
