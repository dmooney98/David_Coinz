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

//==================================================================================================
// This activity allows users to convert coins from their wallet into the correct value in gold.
// Three options are available when banking:
// 'Standard' - uses original exchange rate for the coins
// 'Small gamble' - 45% chance of double the transaction value, 55% chance of halving it
// 'Big gamble' - 15% of multiplying the transaction value by 5, 85% chance of dividing it by 5
// From this activity, the user can go back to the DepositCoins activity, if they press the help
// indicator they will be taken to the Help2 activity, and if they deposit 1+ coins they will be
// taken to the GoldInform activity.
// If a day change occurs, they are taken to the DailyUpdate activity
public class BankCoins extends AppCompatActivity {

    //==============================================================================================
    // Create all required variables for the conversion of the user's wallet coins into gold,
    // through standard banking and both levels of gambling

    // Variables relating to the connection to Firebase
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;

    // Variables relating to the checking and conversion of coins to gold
    private ArrayList<String> coins = new ArrayList<>();
    private ArrayList<String> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private double currentGold;
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private int banked;
    private double calc;
    private String today;
    private Double goldPass;
    private int bankPass;
    private int check = 0;

    // Variables required to set up the ListView correctly
    private ListView listView;
    private ArrayAdapter arrayAdapter;

    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_coins);

        if(mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    //==============================================================================================
    // Set up appropriate variables for the class through SharedPreferences, Bundle, and Firebase.
    // Also set up the ListView correctly
    // The warnings have been suppressed for this function as they are to do with text translation,
    // and in some areas make the code hard to read
    @SuppressWarnings("All")
    public void onStart() {
        super.onStart();

        // Set up the values of variables from SharedPreferences and create listeners for the
        // buttons required to convert coins
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.BC_background);
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // These variables are passed to avoid issues with Firebase retrieving fields
        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        Button cashInButton = findViewById(R.id.BC_bank_button);
        cashInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashIn();
            }
        });

        Button smallGambleButton = findViewById(R.id.BC_small_gamble);
        smallGambleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smallGamble();
            }
        });

        Button bigGambleButton = findViewById(R.id.BC_big_gamble);
        bigGambleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigGamble();
            }
        });

        // Set up chosen background using the value from SharedPreferences
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

        // Set up correct exchange rates
        dolr = Double.parseDouble(settings.getString("dolr", ""));
        peny = Double.parseDouble(settings.getString("peny", ""));
        quid = Double.parseDouble(settings.getString("quid", ""));
        shil = Double.parseDouble(settings.getString("shil", ""));

        // Set up the ListView to display the user's coins in wallet, and store these in the
        // appropriate variables for future use.  The check is used to avoid issues with ListView
        // duplicating its contents in certain conditions
        if(check == 0) {
            listView = (ListView) findViewById(R.id.BC_coinList);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    // Iterate over each of the coins in the user's wallet on database
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        DocumentSnapshot current = queryDocumentSnapshots.getDocuments().get(i);

                        // Set up ArrayList which will be used to fill the ListView nicely
                        String coin = current.get("currency").toString() +
                                " " + current.get("value").toString();
                        coins.add(coin);

                        // Set up HashMap of Coin objects, which use the String in the ListView as
                        // their keys.  This allows for the id of the coin to be kept for
                        // processing, without having to display it in the ListView
                        Coin myCoin = new Coin(current.getId(), current.get("currency").toString(),
                                Double.parseDouble(current.get("value").toString()));
                        coinMap.put(coin, myCoin);
                    }

                    // Display text message to fill the empty space left when the user has no coins
                    // in their wallet
                    if(coins.size() == 0) {
                        TextView coinHelp = (TextView) findViewById(R.id.BC_coinHelp);
                        coinHelp.setText("You haven't got any coins in your wallet at the moment." +
                                "  Get out there and collect some more!");
                    }

                    // Remove any duplicate values from the ArrayList coins, as this can occur under
                    // certain circumstances and have duplicate coins appear in the ListView
                    HashSet<String> coinSet = new HashSet<String>();
                    coinSet.addAll(coins);
                    coins.clear();
                    coins.addAll(coinSet);

                    // Set up the ArrayAdapter for the ListView, to display the ArrayList, coins
                    arrayAdapter = new ArrayAdapter(BankCoins.this,
                            R.layout.my_layout, R.id.row_layout, coins);
                    listView.setAdapter(arrayAdapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Inform the user of error with connection
                    Toast.makeText(BankCoins.this,
                            "Failed to establish connection to DataBase, please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Update the selectedCoins ArrayList to contain the correct coins that the user has
        // selected from the ListView
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

        // This is performed to avoid an issue with Firebase being unable to retrieve values from
        // fields upon first request
        cashIn();
        check = 1;
    }

    //==============================================================================================
    // Allow users to cash in their coins from the wallet at the standard rate
    public void cashIn() {
        // Set up correct variables to be able to keep track of the user's earnings from this
        // transaction, and to ensure the user is only taken to the GoldInform activity if they
        // actually converted one or more coins to gold
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();

        // Perform this check as proceedings should be different if this is the case when onStart()
        // accesses this method as part of the Firebase issue avoidance
        if (check != 0) {

            // Perform a check that the date has not changed since the user arrived on the BankCoins
            // activity.  If it has, move coins from wallet to spare change, and go to the
            // DailyUpdate activity.  If it has not, update the local variable keeping track of how
            // many coins have been banked today
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                // The current date matches that on the database, so the local
                                // variable can be updated to ensure the user does not go over their
                                // daily limit
                                banked = Integer.parseInt(queryDocumentSnapshots.getDocuments()
                                        .get(0).get("Banked").toString());
                            }
                            else {
                                // The current day does not match that on the database, so the date
                                // on the database should be updated and any coins in the user's
                                // wallet must be moved to spare change. Then transfer the user to
                                // the DailyUpdate activity

                                // Create the HashMap required to update the user's Limitations in
                                // the database
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                                myUpdate.put("Banked", 0);

                                // Update the user's Limitations information in the database
                                // correctly, by deleting then replacing the
                                // required information
                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Limitations")
                                        .document(queryDocumentSnapshots.getDocuments()
                                                .get(0).getId()).delete();

                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Limitations")
                                        .document(today).set(myUpdate);

                                // Move all of the user's coins from their wallet to their
                                // spare change
                                for(int i = 0; i < coinMap.size(); i++) {
                                    // Create correct variables required for the transfer, using the
                                    // information gathered about the user's wallet coins from
                                    // onStart()
                                    String key = coins.get(i);
                                    String id = coinMap.get(key).getId();
                                    String currency = coinMap.get(key).getCurrency();
                                    Double value = coinMap.get(key).getValue();

                                    // Create the HashMap required to move the coins across
                                    HashMap<String, Object> toPut = new HashMap<>();
                                    toPut.put("currency", currency);
                                    toPut.put("value", value);

                                    // Update wallet and spare change correctly by deleting the
                                    // current coin from wallet then setting it in spare change
                                    firebaseFirestore.collection("Users")
                                            .document(currentUser)
                                            .collection("Spare Change").document(id)
                                            .set(toPut);

                                    firebaseFirestore.collection("Users")
                                            .document(currentUser).collection("Wallet")
                                            .document(id).delete();
                                }

                                // Take the user to the DailyUpdate activity
                                Intent intent = new Intent(BankCoins.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(BankCoins.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            if (banked == 25) {
                // Display the correct message as the user has already deposited the maximum of 25
                // coins today
                Toast.makeText(this,
                        "You have already banked 25 coins today!", Toast.LENGTH_SHORT).show();
            }

            else if ((banked + selectedCoins.size() > 25)) {
                // This transaction would mean that the user would go over the limit of 25 coins per
                // day, display the correct message to inform them of this
                Toast.makeText(this,
                        "You can only bank 25 coins per day, please select less!",
                        Toast.LENGTH_SHORT).show();
            }

            else {
                // The transaction does not violate the 25 coin limitation, so proceed and bank each
                // of the coins the user selected.  Note that this for loop is slightly different in
                // that int i stays at zero and selectedCoins has items removed throughout the
                // transaction, bringing its size down to zero
                for (int i = 0; i < selectedCoins.size();) {
                    // Set proceed to true so that we know that the user chose to actually convert
                    // one or more coins, rather than clicking the bank button without selecting
                    // any coins first
                    proceed = true;

                    // Set up required variables to avoid the coinMap having to be referenced
                    // frequently
                    Coin currCoin = coinMap.get(selectedCoins.get(i));
                    String currentCurrency = currCoin.getCurrency();
                    Double currentValue = currCoin.getValue();
                    String currentId = currCoin.getId();

                    // Set calc to zero.  Calc will store the value of each selectedCoin after being
                    // exchanged at the correct rate, and reset to zero here for the next coin
                    calc = 0.0;

                    // Use the exchange rates obtained in onStart() to convert the coin to its
                    // correct value in gold
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

                    // Update the total variable.  The total variable is only reset at the very
                    // start of cashIn(), as it keeps track of the total amount of gold earned
                    // over all of the user's selected coins
                    total = total + calc;

                    // Access Firebase to retrieve the user's current amount of gold
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
                                    // Inform the user of error with connection
                                    Toast.makeText(BankCoins.this,
                                            "Failed to retrieve DataBase info, " +
                                                    "please try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                    // If this is the first coin of selectedCoins then set prevGold to currentGold.
                    // This is done so that later on we are able to calculate the difference between
                    // the user's gold pre and post-transaction.  This is done this way due to the
                    // issues which can occur with Firebase accessing the user's Gold field
                    if (selectedCoins.size() == sizeCount) {
                        prevGold = currentGold;
                        total = total + currentGold;
                    }

                    // Update the ArrayList, coins, as this keeps track of the coins in the users
                    // wallet and is used as the variable to display these correctly in the ListView
                    for(int j = 0; j < coins.size(); j++) {
                        if (selectedCoins.get(i).equals(coins.get(j))) {
                            coins.remove(j);
                        }
                    }

                    // Update the HashMap coinMap, as this is used to keep track of the coins in the
                    // user's wallet and allowing us to have information about the coin's id
                    coinMap.remove(selectedCoins.get(i));

                    // Remove the coin from selectedCoins, allowing the for loop to move closer
                    // towards completion
                    selectedCoins.remove(i);

                    // Delete the banked coin from the user's wallet on the database
                    firebaseFirestore.collection("Users").document(currentUser)
                            .collection("Wallet").document(currentId).delete();

                    // Increase the local variable which keeps track of the number of coins banked
                    // today
                    banked = banked + 1;

                    // Create a HashMap to update the information on the database with the number of
                    // coins banked by the user today
                    HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                    myUpdate.put("Banked", banked);

                    // Use this HashMap to update the database, by first deleting the previous
                    // number of banked coins and replacing it with the new number
                    firebaseFirestore.collection("Users").document(currentUser)
                            .collection("Limitations").document().delete();
                    firebaseFirestore.collection("Users").document(currentUser)
                            .collection("Limitations").document(today).set(myUpdate);

                }

                // Now that the for loop has ended, create the HashMap required to update the user's
                // Gold field in their database entry, using the variable 'total' which kept track
                // of the total amount of gold that the selected coins were worth
                HashMap<String, Double> toPut = new HashMap<>();
                toPut.put("Gold", total);
                firebaseFirestore.collection("Users").document(currentUser).set(toPut);
            }
        }
        else {
            // We know that check is equal to 0, meaning that this is the run of this method
            // activated from the onStart() method.  We should attempt to retrieve the user's gold
            // from the Gold field in their database entry.  This fixes the issue with Firebase
            // being unable to retrieve the value on the first attempt, which causes problems if
            // that first attempt is when a transaction is actually needing to be made
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
                            // Inform the user of error with connection
                            Toast.makeText(BankCoins.this,
                                    "Failed to retrieve DataBase info, please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // This code performs the check on the current date against that stored in the database,
            // in the exact same way as performed at the start of cashIn(). It is not put into a
            // method of its own as this caused some errors with Firebase
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                                // Date is the same as that stored in Firebase, update local banked
                                // variable
                                banked = Integer.parseInt(queryDocumentSnapshots.getDocuments()
                                        .get(0).get("Banked").toString());
                            }

                            else {
                                // Date is not the same as that stored in Firebase
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                                myUpdate.put("Banked", 0);

                                // Delete previous value in Limitaitons in Firebase
                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Limitations")
                                        .document(queryDocumentSnapshots.getDocuments()
                                                .get(0).getId()).delete();

                                // Update Limitations with new value
                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Limitations")
                                        .document(today).set(myUpdate);

                                // Update Firebase, by moving all wallet coins to spare change
                                for(int i = 0; i < coinMap.size(); i++) {
                                    String key = coins.get(i);
                                    String id = coinMap.get(key).getId();
                                    String currency = coinMap.get(key).getCurrency();
                                    Double value = coinMap.get(key).getValue();
                                    HashMap<String, Object> toPut = new HashMap<>();
                                    toPut.put("currency", currency);
                                    toPut.put("value", value);

                                    firebaseFirestore.collection("Users")
                                            .document(currentUser)
                                            .collection("Spare Change").document(id)
                                            .set(toPut);

                                    firebaseFirestore.collection("Users")
                                            .document(currentUser).collection("Wallet")
                                            .document(id).delete();
                                }

                                // Take user to DailyUpdate activity
                                Intent intent = new Intent(BankCoins.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(BankCoins.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // This is activated when the method is not being run from onStart(), as the check is not
        // equal to 0, and we also know that the user had made a legitimate transaction, as proceed
        // is true.  Update goldPass and bankPass, the variables which are passed in Bundles to
        // avoid Firebase issues, and move the user to the GoldInform activity with the appropriate
        // variables for display in this activity.  Profit is
        // calculated using total and prevGold, to work out the difference between pre and
        // post-transaction
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

    //==============================================================================================
    // This method is identical to cashIn(), with the exception of the spin variable, which is used
    // as what is needed for the gambling aspect of the transaction.  The comments in this method
    // are therefore limited to avoid clutter as the full explanation is in cashIn()
    public void smallGamble() {
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();

        // Set up the spin variable, by generating a random int between 1 and 100
        int spin = (int) Math.ceil(Math.random() * 100);

        // Perform the date check, as used in cashIn()
        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                            banked = Integer.parseInt(queryDocumentSnapshots.getDocuments()
                                    .get(0).get("Banked").toString());
                        }
                        else {
                            HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                            myUpdate.put("Banked", 0);

                            firebaseFirestore.collection("Users").document(currentUser)
                                    .collection("Limitations")
                                    .document(queryDocumentSnapshots.getDocuments()
                                            .get(0).getId()).delete();

                            firebaseFirestore.collection("Users").document(currentUser)
                                    .collection("Limitations").document(today)
                                    .set(myUpdate);

                            for(int i = 0; i < coinMap.size(); i++) {
                                String key = coins.get(i);
                                String id = coinMap.get(key).getId();
                                String currency = coinMap.get(key).getCurrency();
                                Double value = coinMap.get(key).getValue();
                                HashMap<String, Object> toPut = new HashMap<>();
                                toPut.put("currency", currency);
                                toPut.put("value", value);

                                firebaseFirestore.collection("Users")
                                        .document(currentUser)
                                        .collection("Spare Change")
                                        .document(id).set(toPut);

                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Wallet")
                                        .document(id).delete();
                            }

                            Intent intent = new Intent(BankCoins.this,
                                    DailyUpdate.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BankCoins.this,
                                "Failed to retrieve DataBase info, please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Perform checks as to whether the user has already banked 25 coins, or their transaction
        // would take them over this limit
        if (banked == 25) {
            Toast.makeText(this, "You have already banked 25 coins today!",
                    Toast.LENGTH_SHORT).show();
        }
        else if ((banked + selectedCoins.size() > 25)) {
            Toast.makeText(this, "You can only bank 25 coins per day, " +
                    "please select less!", Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < selectedCoins.size();) {
                // Set up appropriate variables
                proceed = true;
                Coin currCoin = coinMap.get(selectedCoins.get(i));
                String currentCurrency = currCoin.getCurrency();
                Double currentValue = currCoin.getValue();
                String currentId = currCoin.getId();
                calc = 0.0;

                // Perform exchange
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

                // Decide whether user won or lost the gamble, small gamble has a 45% chance of
                // success, with winning doubling the value and losing halving it
                if (spin <= 45) {
                    calc = (calc * 2);
                }
                else {
                    calc = (calc * 0.5);
                }

                // Update variable for the total transaction
                total = total + calc;

                // Retrieve user's current gold from Firebase
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
                                Toast.makeText(BankCoins.this,
                                        "Failed to retrieve DataBase info, please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                // Update prevGold if appropriate
                if (selectedCoins.size() == sizeCount) {
                    prevGold = currentGold;
                    total = total + currentGold;
                }

                // Update 'coins' correctly
                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                // Update coinMap and selectedCoins correctly
                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                // Remove the current coin from the user's wallet on Firebase
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Wallet").document(currentId).delete();

                // Update the local banked variable and create the HashMap for Firebase update, then
                // update the information stored on Firebase appropriately
                banked = banked + 1;
                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                myUpdate.put("Banked", banked);
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Limitations").document().delete();
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Limitations").document(today).set(myUpdate);
            }

            // Create the required HashMap and update the user's Gold field on Firebase correctly
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(currentUser).set(toPut);
        }

        // Enter this if statement if the user has made a valid transaction, to send the correct
        // variables to the GoldInform activity in a Bundle.  This includes information about
        // whether the user won or lost the gamble.  Then transfer the user to the GoldInform
        // activity.  Profit is calculated using total and prevGold, to work out the difference
        // between pre and post-transaction
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

    //==============================================================================================
    // This method is identical to cashIn(), with the exception of the spin variable, which is used
    // as what is needed for the gambling aspect of the transaction.  The comments in this method
    // are therefore limited to avoid clutter as the full explanation is in cashIn()
    public void bigGamble() {
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();

        // Set up the spin variable, by generating a random int between 1 and 100
        int spin = (int) Math.ceil(Math.random() * 100);

        // Perform the date check, as used in cashIn()
        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Limitations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)) {
                            banked = Integer.parseInt(queryDocumentSnapshots.
                                    getDocuments().get(0).get("Banked").toString());
                        }
                        else {
                            HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                            myUpdate.put("Banked", 0);
                            firebaseFirestore.collection("Users").document(currentUser)
                                    .collection("Limitations")
                                    .document(queryDocumentSnapshots.getDocuments()
                                            .get(0).getId()).delete();
                            firebaseFirestore.collection("Users").document(currentUser)
                                    .collection("Limitations").document(today)
                                    .set(myUpdate);
                            for(int i = 0; i < coinMap.size(); i++) {
                                String key = coins.get(i);
                                String id = coinMap.get(key).getId();
                                String currency = coinMap.get(key).getCurrency();
                                Double value = coinMap.get(key).getValue();
                                HashMap<String, Object> toPut = new HashMap<>();
                                toPut.put("currency", currency);
                                toPut.put("value", value);
                                firebaseFirestore.collection("Users")
                                        .document(currentUser)
                                        .collection("Spare Change").document(id)
                                        .set(toPut);
                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Wallet")
                                        .document(id).delete();
                            }
                            Intent intent = new Intent(BankCoins.this,
                                    DailyUpdate.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BankCoins.this,
                                "Failed to retrieve DataBase info, please try again",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Perform checks as to whether the user has already banked 25 coins, or their transaction
        // would take them over this limit
        if (banked == 25) {
            Toast.makeText(this, "You have already banked 25 coins today!",
                    Toast.LENGTH_SHORT).show();
        }
        else if ((banked + selectedCoins.size() > 25)) {
            Toast.makeText(this,
                    "You can only bank 25 coins per day, please select less!",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < selectedCoins.size();) {
                // Set up appropriate variables
                proceed = true;
                Coin currCoin = coinMap.get(selectedCoins.get(i));
                String currentCurrency = currCoin.getCurrency();
                Double currentValue = currCoin.getValue();
                String currentId = currCoin.getId();
                calc = 0.0;

                // Perform exchange
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

                // Decide whether user won or lost the gamble, big gamble has a 15% chance of
                // success, with winning multiplying the value by 5 and losing dividing it by 5
                if (spin <= 15) {
                    calc = (calc * 5);
                }
                else {
                    calc = (calc * 0.2);
                }

                // Update variable for the total transaction
                total = total + calc;

                // Retrieve user's current gold from Firebase
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
                                Toast.makeText(BankCoins.this,
                                        "Failed to retrieve DataBase info, please try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                // Update prevGold if appropriate
                if (selectedCoins.size() == sizeCount) {
                    prevGold = currentGold;
                    total = total + currentGold;
                }

                // Update 'coins' correctly
                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                // Update coinMap and selectedCoins correctly
                coinMap.remove(selectedCoins.get(i));
                selectedCoins.remove(i);

                // Remove the current coin from the user's wallet on Firebase
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Wallet").document(currentId).delete();

                // Update the local banked variable and create the HashMap for Firebase update, then
                // update the information stored on Firebase appropriately
                banked = banked + 1;
                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                myUpdate.put("Banked", banked);
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Limitations").document().delete();
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Limitations").document(today).set(myUpdate);
            }

            // Create the required HashMap and update the user's Gold field on Firebase correctly
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(currentUser).set(toPut);
        }

        // Enter this if statement if the user has made a valid transaction, to send the correct
        // variables to the GoldInform activity in a Bundle.  This includes information about
        // whether the user won or lost the gamble.  Then transfer the user to the GoldInform
        // activity.  Profit is calculated using total and prevGold, to work out the difference
        // between pre and post-transaction
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

    //==============================================================================================
    // Take the user to back to the DepositCoins activity, passing the goldPass and bankPass
    // variables as required
    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the help page from the small help button, to allow them to see how gambling
    // functions.  The goldPass and bankPass variables are not required to be passed, as the user
    // must go through the MainMenu again before getting back to the DepositCoins activity, meaning
    // that these variables are updated anyway
    public void goToHelp2(View view) {
        Intent intent = new Intent(this, Help2.class);
        startActivity(intent);
    }

}