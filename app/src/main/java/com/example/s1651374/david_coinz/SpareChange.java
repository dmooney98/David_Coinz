package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class SpareChange extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<String> coins = new ArrayList<>();
    private ArrayList<String> changes = new ArrayList<>();
    private ArrayList<String> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private ListView listViewW;
    private ListView listViewSC;
    private String currentUser;
    private int check = 0;
    private ArrayAdapter arrayAdapterW;
    private ArrayAdapter arrayAdapterSC;
    private Double goldPass;
    private int bankPass;
    private String today;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_change);

        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    public void onStart(){
        super.onStart();
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Button transferButton = findViewById(R.id.transferButton);
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transfer();
            }
        });

        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");


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
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String coin = documentSnapshots.getDocuments().get(i).get("currency").toString() + " " + documentSnapshots.getDocuments().get(i).get("value").toString();
                                coins.add(coin);
                                Coin myCoin = new Coin(documentSnapshots.getDocuments().get(i).getId(), documentSnapshots.getDocuments().get(i).get("currency").toString(), Double.parseDouble(documentSnapshots.getDocuments().get(i).get("value").toString()));
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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SpareChange.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                HashMap<String, Integer> myUpdate = new HashMap<>();
                                myUpdate.put("Banked", 0);
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today).set(myUpdate);
                                for(int i = 0; i < coinMap.size(); i++) {
                                    String key = coins.get(i);
                                    String id = coinMap.get(key).getId();
                                    String currency = coinMap.get(key).getCurrency();
                                    Double value = coinMap.get(key).getValue();
                                    HashMap<String, Object> toPut = new HashMap<>();
                                    toPut.put("currency", currency);
                                    toPut.put("value", value);
                                    firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").document(id).set(toPut);
                                    firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(id).delete();
                                }
                                Intent intent = new Intent(SpareChange.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SpareChange.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if(check == 0) {
            listViewSC = (ListView) findViewById(R.id.spare_change);
            firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String change = documentSnapshots.getDocuments().get(i).get("currency").toString() + " " + documentSnapshots.getDocuments().get(i).get("value").toString();
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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SpareChange.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
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
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            startActivity(intent);
        }
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
