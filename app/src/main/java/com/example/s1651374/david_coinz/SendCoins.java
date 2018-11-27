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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

public class SendCoins extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private ArrayList<String> selectedCoins = new ArrayList<String>();
    private String selectedFriend = " ";
    private ListView listViewC;
    private ListView listViewF;
    private int check = 0;
    private ArrayList<String> coins = new ArrayList<String>();
    private ArrayList<String> friends = new ArrayList<String>();
    private ArrayAdapter arrayAdapterC;
    private ArrayAdapter arrayAdapterF;
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private Button sendCoinsButton;
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private double friendGold;
    private double toBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_coins);

        currentUser = mAuth.getCurrentUser().getEmail();
    }

    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        sendCoinsButton = findViewById(R.id.button14);
        sendCoinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
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


        if(check == 0) {
            listViewC = (ListView) findViewById(R.id.coinList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String coin = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString() + " " + queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                        coins.add(coin);
                        Coin myCoin = new Coin(queryDocumentSnapshots.getDocuments().get(i).getId(), queryDocumentSnapshots.getDocuments().get(i).get("currency").toString(), Double.parseDouble(queryDocumentSnapshots.getDocuments().get(i).get("value").toString()));
                        coinMap.put(coin, myCoin);
                    }

                    if(coins.size() == 0) {
                        TextView coinHelp = (TextView) findViewById(R.id.coinHelp);
                        coinHelp.setText("You haven't got any coins in your wallet at the moment.  Get out there and collect some more!");
                    }

                    HashSet<String> coinSet = new HashSet<String>();
                    coinSet.addAll(coins);
                    coins.clear();
                    coins.addAll(coinSet);

                    arrayAdapterC = new ArrayAdapter(SendCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                    listViewC.setAdapter(arrayAdapterC);

                    listViewC.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            });
        }


        if(check == 0) {
            listViewF = (ListView) findViewById(R.id.friendList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Friends").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String friend = queryDocumentSnapshots.getDocuments().get(i).getId();
                        friends.add(friend);
                    }

                    if(friends.size() == 0) {
                        TextView friendHelp = (TextView) findViewById(R.id.friendHelp);
                        friendHelp.setText("You don't have any friends added yet.  Add some friends from the 'Manage Friends' panel in the Main Menu!");
                    }

                    arrayAdapterF = new ArrayAdapter(SendCoins.this, R.layout.my_layout, R.id.row_layout, friends);
                    listViewF.setAdapter(arrayAdapterF);

                    listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                }
            });
        }


        listViewC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        listViewF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = ((TextView) view).getText().toString();
                selectedFriend = temp;
                Toast.makeText(SendCoins.this, selectedFriend, Toast.LENGTH_SHORT).show();
            }
        });

        sendCoins();
        check = 1;
    }

    public void sendCoins() {
        double allGold = 0.0;
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit = 0.0;
        int sizeCount = selectedCoins.size();
        if (check != 0 && !selectedFriend.equals(" ")) {
            for (int i = 0; i < selectedCoins.size();) {
                proceed = true;
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
                total = total + toBank;
                firebaseFirestore.collection("Users").document(selectedFriend).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        friendGold = documentSnapshots.getDouble("Gold");
                    }
                });
                if (selectedCoins.size() == sizeCount) {
                    prevGold = friendGold;
                }
                toBank = toBank + friendGold;
                allGold = toBank;
                HashMap<String, Double> toPut = new HashMap<>();
                toPut.put("Gold", toBank);

                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                firebaseFirestore.collection("Users").document(selectedFriend).set(toPut);
                firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();
            }
        }
        else {
            for (int i = 0; i < friends.size(); i++) {
                firebaseFirestore.collection("Users").document(friends.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        friendGold = documentSnapshots.getDouble("Gold");
                    }
                });
            }
        }

        if(check!=0 && proceed) {
            profit = allGold - prevGold;
            Intent intent = new Intent (this, GoldInformF.class);
            intent.putExtra("allGold", allGold);
            intent.putExtra("profit", profit);
            startActivity(intent);
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        startActivity(intent);
    }

}
