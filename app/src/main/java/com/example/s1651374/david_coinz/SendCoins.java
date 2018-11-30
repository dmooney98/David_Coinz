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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SendCoins extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private ArrayList<String> selectedCoins = new ArrayList<>();
    private String selectedFriend = " ";
    private ListView listViewC;
    private ListView listViewF;
    private int check = 0;
    private ArrayList<String> coins = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<String> coinsW = new ArrayList<>();
    private ArrayAdapter arrayAdapterC;
    private ArrayAdapter arrayAdapterF;
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private Button sendCoinsButton;
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private double friendGold;
    private double calc;
    private String today;
    private Double goldPass;
    private int bankPass;

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
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        sendCoinsButton = findViewById(R.id.sendCoinsButton);
        sendCoinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
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

        dolr = Double.parseDouble(settings.getString("dolr", ""));
        peny = Double.parseDouble(settings.getString("peny", ""));
        quid = Double.parseDouble(settings.getString("quid", ""));
        shil = Double.parseDouble(settings.getString("shil", ""));


        if(check == 0) {
            listViewC = (ListView) findViewById(R.id.coinList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").get()
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
                                TextView coinHelp = (TextView) findViewById(R.id.coinHelp);
                                coinHelp.setText("You haven't got any coins in your Spare Change at the moment.  Go and add some from the 'My Spare Change' panel!");
                            }

                            HashSet<String> coinSet = new HashSet<String>();
                            coinSet.addAll(coins);
                            coins.clear();
                            coins.addAll(coinSet);

                            arrayAdapterC = new ArrayAdapter(SendCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                            listViewC.setAdapter(arrayAdapterC);

                            listViewC.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendCoins.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        if(check == 0) {
            listViewF = (ListView) findViewById(R.id.friendList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Friends").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                String friend = documentSnapshots.getDocuments().get(i).getId();
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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendCoins.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                            Toast.makeText(SendCoins.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
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
                check = 0;
                sendCoins();
                check = 1;
            }
        });

        sendCoins();
        check = 1;
    }

    public void sendCoins() {
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();
        if (check != 0 && !selectedFriend.equals(" ")) {
            for (int i = 0; i < selectedCoins.size();) {
                proceed = true;
                Coin currCoin = coinMap.get(selectedCoins.get(i));
                String currentCurrency = currCoin.getCurrency();
                Double currentValue = currCoin.getValue();
                String currentId = currCoin.getId();
                calc = 0.0;
                if (currentCurrency.equals("DOLR")) {
                    calc = currentValue * dolr;
                } else if (currentCurrency.equals("PENY")) {
                    calc = currentValue * peny;
                } else if (currentCurrency.equals("QUID")) {
                    calc = currentValue * quid;
                } else if (currentCurrency.equals("SHIL")) {
                    calc = currentValue * shil;
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                total = total + calc;

                firebaseFirestore.collection("Users").document(selectedFriend).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                friendGold = documentSnapshot.getDouble("Gold");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SendCoins.this, "Failed to retrieve DataBase info, please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                if (selectedCoins.size() == sizeCount) {
                    prevGold = friendGold;
                    total = total + friendGold;
                }

                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                firebaseFirestore.collection("Users").document(currentUser).collection("Spare Change").document(currentId).delete();
            }
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(selectedFriend).set(toPut);
        }
        else if (check == 0 && !selectedFriend.equals(" ")){
            firebaseFirestore.collection("Users").document(selectedFriend).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            friendGold = documentSnapshot.getDouble("Gold");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                HashMap<String, Integer> myUpdate = new HashMap<>();
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
                                Intent intent = new Intent(SendCoins.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SendCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if(check!=0 && proceed) {
            profit = total - prevGold;
            Intent intent = new Intent (this, GoldInformF.class);
            intent.putExtra("allGold", total);
            intent.putExtra("profit", profit);
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            startActivity(intent);
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
