package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;


public class BankCoins extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<String> coins = new ArrayList<String>();
    private ArrayList<String> selectedCoins = new ArrayList<String>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private ListView listView;
    private String currentUser;
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private double currentGold;
    private double toBank;
    private int check;
    private Button cashInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_coins);

        currentUser = mAuth.getCurrentUser().getEmail();

        /*listView = (ListView) findViewById(R.id.coinList);
        firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String coin = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString() + " " + queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    coins.add(coin);
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(BankCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/

    }

    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        TextView goldCount = (TextView) findViewById(R.id.goldCount);
        goldCount.setText("GOLD: ");
        check = 0;
        cashInButton = findViewById(R.id.button10);
        cashInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashIn();
            }
        });

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

        dolr = Double.parseDouble(settings.getString("dolr", ""));
        peny = Double.parseDouble(settings.getString("peny", ""));
        quid = Double.parseDouble(settings.getString("quid", ""));
        shil = Double.parseDouble(settings.getString("shil", ""));

        currentUser = mAuth.getCurrentUser().getEmail();

        listView = (ListView) findViewById(R.id.coinList);
        firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String coin = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString() + " " + queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    coins.add(coin);
                    Coin myCoin = new Coin(queryDocumentSnapshots.getDocuments().get(i).getId(), queryDocumentSnapshots.getDocuments().get(i).get("currency").toString(), Double.parseDouble(queryDocumentSnapshots.getDocuments().get(i).get("value").toString()));
                    coinMap.put(coin, myCoin);
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(BankCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = ((TextView) view).getText().toString();
                if (selectedCoins.contains(temp)) {
                    selectedCoins.remove(temp);
                }
                else {
                    selectedCoins.add(temp);
                }
            }
        });
        cashIn();
        check = 1;
    }

    public void cashIn() {
        if (check != 0) {
            for (int i = 0; i < selectedCoins.size();) {
                Coin currCoin = coinMap.get(selectedCoins.get(i));
                String currentCurrency = currCoin.getCurrency();
                Double currentValue = currCoin.getValue();
                String currentId = currCoin.getId();
                toBank = 0.0;
                if (currentCurrency.equals("DOLR")) {
                    toBank = currentValue * dolr;
                } else if (currentCurrency.equals("PENY")) {
                    toBank = currentValue * peny;
                } else if (currentCurrency.equals("QUID")) {
                    toBank = currentValue * quid;
                } else if (currentCurrency.equals("SHIL")) {
                    toBank = currentValue * shil;
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                firebaseFirestore.collection("Users").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        currentGold = documentSnapshots.getDouble("Gold");
                    }
                });
                toBank = toBank + currentGold;
                HashMap<String, Double> toPut = new HashMap<>();
                toPut.put("Gold", toBank);
                firebaseFirestore.collection("Users").document(currentUser).set(toPut);
                firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();


                coins.clear();
                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);


                firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            String coin = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString() + " " + queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                            coins.add(coin);
                        }
                        //ArrayAdapter arrayAdapter = new ArrayAdapter(BankCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                        //listView.setAdapter(arrayAdapter);
                        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }
                });

            }
        }
        else {
            firebaseFirestore.collection("Users").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                    currentGold = documentSnapshots.getDouble("Gold");
                    }
            });
        }
        /*for (int i = 0; i < selectedCoins.size(); i++) {
            selectedCoins.remove(i);
        }
        Toast.makeText(this, selectedCoins.size() + "", Toast.LENGTH_LONG).show();*/
    }

    public void smallGamble(View view) {

    }

    public void bigGamble(View view) {

    }

    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        startActivity(intent);
    }

    public void goToHelp2(View view) {
        Intent intent = new Intent(this, Help2.class);
        startActivity(intent);
    }

}
