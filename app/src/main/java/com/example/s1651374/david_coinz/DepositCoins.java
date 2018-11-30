package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class DepositCoins extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private int check = 0;
    private ArrayList<String> coinsW = new ArrayList<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private String today;
    private Double goldPass;
    private int bankPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_coins);

        currentUser = mAuth.getCurrentUser().getEmail();

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
        TextView goldBankField = (TextView) findViewById(R.id.goldPassField);
        TextView bankPassField = (TextView) findViewById(R.id.bankPassField);
        DecimalFormat df = new DecimalFormat("#.###");

        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        goldBankField.setText(df.format(goldPass) + " GOLD");
        if(bankPass == 1) {
            bankPassField.setText(bankPass + " COIN BANKED TODAY");
        }
        else {
            bankPassField.setText(bankPass + " COINS BANKED TODAY");
        }

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

        if(check == 0) {
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String coinW = documentSnapshots.getDocuments().get(i).get("currency").toString() + " " + documentSnapshots.getDocuments().get(i).get("value").toString();
                                coinsW.add(coinW);
                                Coin myCoinW = new Coin(documentSnapshots.getDocuments().get(i).getId(), documentSnapshots.getDocuments().get(i).get("currency").toString(), Double.parseDouble(documentSnapshots.getDocuments().get(i).get("value").toString()));
                                coinMapW.put(coinW, myCoinW);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DepositCoins.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                                myUpdate.put("Banked", 0);
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today).set(myUpdate);
                                for(int i = 0; i < coinMapW.size(); i++) {
                                    String key = coinsW.get(i);
                                    String id = coinMapW.get(key).getId();
                                    String currency = coinMapW.get(key).getCurrency();
                                    Double value = coinMapW.get(key).getValue();
                                    HashMap<String, Object> toPut = new HashMap<>();
                                    toPut.put("currency", currency);
                                    toPut.put("value", value);
                                    firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").document(id).set(toPut);
                                    firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(id).delete();
                                }
                                Intent intent = new Intent(DepositCoins.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DepositCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        check = 1;

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
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToBankCoins(View view) {
        Intent intent = new Intent (this, BankCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToSendCoins(View view) {
        Intent intent = new Intent (this, SendCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToSpareChange(View view) {
        Intent intent = new Intent(this, SpareChange.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
