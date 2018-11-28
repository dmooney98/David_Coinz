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

public class SpareChange extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<String> coins = new ArrayList<String>();
    private ArrayList<String> changes = new ArrayList<>();
    private ArrayList<String> selectedCoins = new ArrayList<String>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private ListView listViewW;
    private ListView listViewSC;
    private String currentUser;
    private Button transferButton;
    private int check = 0;
    private ArrayAdapter arrayAdapterW;
    private ArrayAdapter arrayAdapterSC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_change);

        currentUser = mAuth.getCurrentUser().getEmail();

    }

    public void onStart(){
        super.onStart();
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

        transferButton = findViewById(R.id.transferButton);
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transfer();
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

        if(check == 0) {
            listViewW = (ListView) findViewById(R.id.wallet);
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
                        TextView walletHelp = (TextView) findViewById(R.id.walletHelp);
                        walletHelp.setText("You haven't got any coins in your wallet at the moment.  Get out there and collect some more!");
                    }

                    HashSet<String> coinSet = new HashSet<String>();
                    coinSet.addAll(coins);
                    coins.clear();
                    coins.addAll(coinSet);

                    arrayAdapterW = new ArrayAdapter(SpareChange.this, R.layout.my_layout, R.id.row_layout, coins);
                    listViewW.setAdapter(arrayAdapterW);

                    listViewW.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            });
        }

        if(check == 0) {
            listViewSC = (ListView) findViewById(R.id.spare_change);
            firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String change = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString() + " " + queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                        changes.add(change);
                    }

                    TextView changeHelp = (TextView) findViewById(R.id.changeHelp);
                    if(changes.size() == 0) {
                        changeHelp.setText("You don't have any coins in Spare Change just now.  Would you like to add some to send to your friends?");
                    }
                    else {
                        changeHelp.setText("");
                    }

                    arrayAdapterSC = new ArrayAdapter(SpareChange.this, R.layout.my_layout, R.id.row_layout, changes);
                    listViewSC.setAdapter(arrayAdapterSC);
                }
            });
        }

        listViewW.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        check = 1;

    }

    public void transfer() {
        int selectedCoinsSize = selectedCoins.size();
        boolean proceed = false;

        for (int i = 0; i < selectedCoins.size();) {
            proceed = true;
            Coin currCoin = coinMap.get(selectedCoins.get(i));
            String currentCurrency = currCoin.getCurrency();
            Double currentValue = currCoin.getValue();
            String currentId = currCoin.getId();


            HashMap<String, Object> toPut = new HashMap<>();
            toPut.put("currency", currentCurrency);
            toPut.put("value", currentValue);

            for(int j = 0; j < coins.size(); j++) {
                if (selectedCoins.get(i).equals(coins.get(j))) {
                    coins.remove(j);
                }
            }

            coinMap.remove(selectedCoins.get(i));
            selectedCoins.remove(i);

            firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").document(currentId).set(toPut);
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();
        }

        if(proceed) {
            Intent intent = new Intent (this, TransferInform.class);
            intent.putExtra("coinCount", selectedCoinsSize);
            startActivity(intent);
        }
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        startActivity(intent);
    }

}
