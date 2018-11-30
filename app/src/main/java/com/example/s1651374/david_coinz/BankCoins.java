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


public class BankCoins extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<String> coins = new ArrayList<>();
    private ArrayList<String> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private ListView listView;
    private String currentUser;
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private double currentGold;
    private int banked;
    private double calc;
    private int check = 0;
    private Button cashInButton;
    private ArrayAdapter arrayAdapter;
    private String today;
    private Double goldPass;
    private int bankPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_coins);

        if(mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

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

        if(check == 0) {
            listView = (ListView) findViewById(R.id.coinList);
            firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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

                    arrayAdapter = new ArrayAdapter(BankCoins.this, R.layout.my_layout, R.id.row_layout, coins);
                    listView.setAdapter(arrayAdapter);

                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BankCoins.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }


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
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();
        if (check != 0) {
            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                banked = Integer.parseInt(queryDocumentSnapshots.getDocuments().get(0).get("Banked").toString());
                            }
                            else {
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
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
                                Intent intent = new Intent(BankCoins.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
            if (banked == 25) {
                Toast.makeText(this, "You have already banked 25 coins today!", Toast.LENGTH_SHORT).show();
            }
            else if ((banked + selectedCoins.size() > 25)) {
                Toast.makeText(this, "You can only bank 25 coins per day, please select less!", Toast.LENGTH_SHORT).show();
            }
            else {
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

                    firebaseFirestore.collection("Users").document(currentUser).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    currentGold = documentSnapshot.getDouble("Gold");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    if (selectedCoins.size() == sizeCount) {
                        prevGold = currentGold;
                        total = total + currentGold;
                    }

                    for(int j = 0; j < coins.size(); j++) {
                        if (selectedCoins.get(i).equals(coins.get(j))) {
                            coins.remove(j);
                        }
                    }

                    coinMap.remove(selectedCoins.get(i));
                    selectedCoins.remove(i);


                    firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();
                    banked = banked + 1;
                    HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                    myUpdate.put("Banked", banked);
                    firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document().delete();
                    firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today).set(myUpdate);

                }
                HashMap<String, Double> toPut = new HashMap<>();
                toPut.put("Gold", total);
                firebaseFirestore.collection("Users").document(currentUser).set(toPut);
            }
        }
        else {
            firebaseFirestore.collection("Users").document(currentUser).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            currentGold = documentSnapshot.getDouble("Gold");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                banked = Integer.parseInt(queryDocumentSnapshots.getDocuments().get(0).get("Banked").toString());
                            }
                            else {
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
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
                                Intent intent = new Intent(BankCoins.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if(check!=0 && proceed) {
            goldPass = total;
            bankPass = banked;
            profit = total - prevGold;
            Intent intent = new Intent (this, GoldInform.class);
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            intent.putExtra("transactionType", "Standard");
            intent.putExtra("winLose", "Unused");
            intent.putExtra("allGold", total);
            intent.putExtra("profit", profit);
            startActivity(intent);
        }
    }

    public void smallGamble(View view) {
        double prevGold = 0.0;
        int spin = (int) Math.ceil(Math.random() * 100);
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();
        firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                            banked = Integer.parseInt(queryDocumentSnapshots.getDocuments().get(0).get("Banked").toString());
                        }
                        else {
                            HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
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
                            Intent intent = new Intent(BankCoins.this, DailyUpdate.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
        if (banked == 25) {
            Toast.makeText(this, "You have already banked 25 coins today!", Toast.LENGTH_SHORT).show();
        }
        else if ((banked + selectedCoins.size() > 25)) {
            Toast.makeText(this, "You can only bank 25 coins per day, please select less!", Toast.LENGTH_SHORT).show();
        }
        else {
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
                if (spin <= 45) {
                    calc = (calc * 2);
                }
                else {
                    calc = (calc * 0.5);
                }
                total = total + calc;
                firebaseFirestore.collection("Users").document(currentUser).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                currentGold = documentSnapshot.getDouble("Gold");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });

                if (selectedCoins.size() == sizeCount) {
                    prevGold = currentGold;
                    total = total + currentGold;
                }



                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();
                banked = banked + 1;
                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                myUpdate.put("Banked", banked);
                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document().delete();
                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today).set(myUpdate);
            }
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(currentUser).set(toPut);
        }

        if (proceed) {
            goldPass = total;
            bankPass = banked;
            profit = total - prevGold;
            Intent intent = new Intent (this, GoldInform.class);
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            intent.putExtra("transactionType", "SmallG");
            if (spin <= 45) {
                intent.putExtra("winLose", "Win");
            }
            else {
                intent.putExtra("winLose", "Lose");
            }
            intent.putExtra("allGold", total);
            intent.putExtra("profit", profit);
            startActivity(intent);
        }

    }

    public void bigGamble(View view) {
        double prevGold = 0.0;
        int spin = (int) Math.ceil(Math.random() * 100);
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();
        firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                            banked = Integer.parseInt(queryDocumentSnapshots.getDocuments().get(0).get("Banked").toString());
                        }
                        else {
                            HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
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
                            Intent intent = new Intent(BankCoins.this, DailyUpdate.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
        if (banked == 25) {
            Toast.makeText(this, "You have already banked 25 coins today!", Toast.LENGTH_SHORT).show();
        }
        else if ((banked + selectedCoins.size() > 25)) {
            Toast.makeText(this, "You can only bank 25 coins per day, please select less!", Toast.LENGTH_SHORT).show();
        }
        else {
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
                if (spin <= 15) {
                    calc = (calc * 5);
                }
                else {
                    calc = (calc * 0.2);
                }
                total = total + calc;
                firebaseFirestore.collection("Users").document(currentUser).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                currentGold = documentSnapshot.getDouble("Gold");
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BankCoins.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                            }
                        });

                if (selectedCoins.size() == sizeCount) {
                    prevGold = currentGold;
                    total = total + currentGold;
                }

                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                firebaseFirestore.collection("Users").document(currentUser).collection("Wallet").document(currentId).delete();
                banked = banked + 1;
                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                myUpdate.put("Banked", banked);
                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document().delete();
                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today).set(myUpdate);
            }
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(currentUser).set(toPut);
        }

        if (proceed) {
            goldPass = total;
            bankPass = banked;
            profit = total - prevGold;
            Intent intent = new Intent (this, GoldInform.class);
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            intent.putExtra("transactionType", "SmallG");
            if (spin <= 15) {
                intent.putExtra("winLose", "Win");
            }
            else {
                intent.putExtra("winLose", "Lose");
            }
            intent.putExtra("allGold", total);
            intent.putExtra("profit", profit);
            startActivity(intent);
        }

    }

    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToHelp2(View view) {
        Intent intent = new Intent(this, Help2.class);
        startActivity(intent);
    }

}