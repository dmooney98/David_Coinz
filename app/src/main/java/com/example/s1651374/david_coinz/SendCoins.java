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
// This activity allows users to send coins from their spare change to other users who they have
// previously added to their friends list in ManageFriends.  From this activity, the user can go to
// DepositCoins, GoldInformF if a valid transaction is made, or to DailyUpdate if the date check
// finds that the day has changed
public class SendCoins extends AppCompatActivity {

    //==============================================================================================
    // Creates all required variables for the sending of coins from the user's spare change to one
    // of their friends, via the method of converting the coin's value in gold and adding this to
    // the selected friend's gold count

    // Variables relating to the connection to Firebase
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;

    // Variables relating to conversion of coins to gold, then sending these to the selected friend
    private ArrayList<String> selectedCoins = new ArrayList<>();
    private String selectedFriend = " ";
    private int check = 0;
    private ArrayList<String> coins = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<String> coinsW = new ArrayList<>();
    private HashMap<String, Coin> coinMap= new HashMap<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private double dolr;
    private double peny;
    private double quid;
    private double shil;
    private double friendGold;
    private String today;

    // Variables required to set up the ListView correctly
    private ListView listViewC;
    private ListView listViewF;
    private ArrayAdapter arrayAdapterC;
    private ArrayAdapter arrayAdapterF;

    // For the storing and passing back of goldPass and bankPass to DepositCoins
    private Double goldPass;
    private int bankPass;


    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_coins);

        if(mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    //==============================================================================================
    // Set up appropriate variables for the class through SharedPreferences, Bundle, and Firebase.
    // Also set up the ListViews for spare change and friends correctly
    // The warnings have been suppressed for this function as they are to do with text translation,
    // and in some areas make the code hard to read
    @SuppressWarnings("All")
    public void onStart() {
        super.onStart();

        // Set up the values of variables from SharedPreferences and create a listner for the
        // button required to transfer coins
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.SendC_background);
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Button sendCoinsButton = findViewById(R.id.SendC_sendCoinsButton);
        sendCoinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
            }
        });

        // These variables are passed to avoid issues with Firebase retrieving fields
        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

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


        // Set up the ListView to display the user's coins in spare change, and store these in the
        // appropriate variables for future use.  The check is used to avoid issues with ListView
        // duplicating its contents in certain conditions
        if(check == 0) {
            listViewC = (ListView) findViewById(R.id.SendC_coinList);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Spare Change").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            // Iterate over each of the coins in the user's wallet on database
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                DocumentSnapshot current = documentSnapshots.getDocuments().get(i);

                                // Set up ArrayList which will be used to fill the ListView nicely
                                String coin = current.get("currency").toString() +
                                        " " + current.get("value").toString();
                                coins.add(coin);

                                // Set up HashMap of Coin objects, which use the String in the
                                // ListView as their keys.  This allows for the id of the coin to be
                                // kept for processing, without having to display it in the ListView
                                Coin myCoin = new Coin(current.getId(),
                                        current.get("currency").toString(),
                                        Double.parseDouble(current.get("value").toString()));
                                coinMap.put(coin, myCoin);
                            }

                            // Display text message to fill the empty space left when the user has
                            // no coins in their wallet
                            if(coins.size() == 0) {
                                TextView coinHelp = (TextView) findViewById(R.id.SendC_coinHelp);
                                coinHelp.setText("You haven't got any coins in your Spare Change " +
                                        "at the moment.  Go and add some from the 'My Spare " +
                                        "Change' panel!");
                            }

                            // Remove any duplicate values from the ArrayList coins, as this can
                            // occur under certain circumstances and have duplicate coins appear in
                            // the ListView
                            HashSet<String> coinSet = new HashSet<>();
                            coinSet.addAll(coins);
                            coins.clear();
                            coins.addAll(coinSet);

                            // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                            // coins
                            arrayAdapterC = new ArrayAdapter(SendCoins.this,
                                    R.layout.my_layout, R.id.row_layout, coins);
                            listViewC.setAdapter(arrayAdapterC);
                            listViewC.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SendCoins.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Set up the ListView to display the user's friends, and store these in the appropriate
        // variables for future use.  The check is used to avoid issues with ListView duplicating
        // its contents in certain conditions
        if(check == 0) {
            listViewF = (ListView) findViewById(R.id.SendC_friendList);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Friends").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            // Iterate over each of the coins in the user's friends on database
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                // These variables are created to store information on the user's
                                // friends, to be used in the future and used to fill the ListView
                                String friend = documentSnapshots.getDocuments().get(i).getId();
                                friends.add(friend);
                            }

                            // Display text message to fill the empty space left when the user has
                            // users in their friends list
                            if(friends.size() == 0) {
                                TextView friendHelp =(TextView) findViewById(R.id.SendC_friendHelp);
                                friendHelp.setText("You don't have any friends added yet." +
                                        "  Add some friends from the 'Manage Friends' " +
                                        "panel in the Main Menu!");
                            }

                            // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                            // friends
                            arrayAdapterF = new ArrayAdapter(SendCoins.this,
                                    R.layout.my_layout, R.id.row_layout, friends);
                            listViewF.setAdapter(arrayAdapterF);
                            listViewF.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SendCoins.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Access Firebase to populate the HashMap of the coins in the users wallet, as these are
        // required for the date check later on
        if(check == 0) {
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            // Iterate over each of the coins in the user's wallet on database
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                DocumentSnapshot current = documentSnapshots.getDocuments().get(i);

                                // These variables are set up similarly to in BankCoins in order to
                                // store information on the user's wallet, and are used in the date
                                // checking process
                                String coinW = current.get("currency").toString() +
                                        " " + current.get("value").toString();
                                coinsW.add(coinW);
                                Coin myCoinW = new Coin(current.getId(),
                                        current.get("currency").toString(),
                                        Double.parseDouble(current.get("value").toString()));
                                coinMapW.put(coinW, myCoinW);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SendCoins.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update the selectedCoins ArrayList to contain the correct coins that the user has
        // selected from the ListView
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

        // Update the selectedFriend variable to contain the correct coins that the user has
        // selected from the ListView
        listViewF.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = ((TextView) view).getText().toString();
                selectedFriend = temp;
                // This check and use of sendCoins() is to avoid issues with Firebase retrieving the
                // selected friend's gold
                check = 0;
                sendCoins();
                check = 1;
            }
        });

        // This is performed to avoid an issue with Firebase being unable to retrieve values from
        // fields upon first request
        sendCoins();
        check = 1;
    }

    public void sendCoins() {
        // Set up correct variables to be able to keep track of the friend's earnings from this
        // transaction, and to ensure the user is only taken to the GoldInformF activity if they
        // actually sent one or more coins to a friend
        double prevGold = 0.0;
        boolean proceed = false;
        double total = 0.0;
        double profit;
        int sizeCount = selectedCoins.size();

        // Perform this check as proceedings should be different if this is the case when onStart()
        // accesses this method as part of the Firebase issue avoidance
        if (check != 0 && !selectedFriend.equals(" ")) {
            // We know that this is not part of onStart()'s special use of the function, and that
            // the user has selected a friend to send the coins to
            for (int i = 0; i < selectedCoins.size();) {
                // Set proceed to true so that we know that the user chose to actually send
                // one or more coins, rather than clicking the send coins button after selecting a
                // friend but without selecting any coins first
                proceed = true;

                // Set up required variables to avoid the coinMap having to be referenced
                // frequently
                Coin currCoin = coinMap.get(selectedCoins.get(i));
                String currentCurrency = currCoin.getCurrency();
                Double currentValue = currCoin.getValue();
                String currentId = currCoin.getId();

                // Set calc to zero.  Calc will store the value of each selectedCoin after being
                // exchanged at the correct rate, and reset to zero here for the next coin
                double calc = 0.0;

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
                // start of cashIn(), as it keeps track of the total amount of gold transferred
                // over all of the user's selected coins
                total = total + calc;

                // Access Firebase to retrieve the selected friend's current amount of gold
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
                                Toast.makeText(SendCoins.this,
                                        "Failed to retrieve DataBase info, please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                // If this is the first coin of selectedCoins then set prevGold to friendGold.
                // This is done so that later on we are able to calculate the difference between
                // the friend's gold pre and post-transaction.  This is done this way due to the
                // issues which can occur with Firebase accessing the friend's Gold field
                if (selectedCoins.size() == sizeCount) {
                    prevGold = friendGold;
                    total = total + friendGold;
                }

                // Update the ArrayList, coins, as this keeps track of the coins in the users
                // spare change and is used as the variable to display these correctly in the
                // ListView
                for(int j = 0; j < coins.size(); j++) {
                    if (selectedCoins.get(i).equals(coins.get(j))) {
                        coins.remove(j);
                    }
                }

                // Update the HashMap coinMap, as this is used to keep track of the coins in the
                // user's spare change and allowing us to have information about the coin's id
                coinMap.remove(selectedCoins.get(i));

                // Remove the coin from selectedCoins, allowing the for loop to move closer
                // towards completion
                selectedCoins.remove(i);

                // Delete the banked coin from the user's spare change on the database
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Spare Change").document(currentId).delete();
            }

            // Now that the for loop has ended, create the HashMap required to update the friend's
            // Gold field in their database entry, using the variable 'total' which kept track
            // of the total amount of gold that the transferred coins were worth
            HashMap<String, Double> toPut = new HashMap<>();
            toPut.put("Gold", total);
            firebaseFirestore.collection("Users").document(selectedFriend).set(toPut);
        }
        else if (check == 0 && !selectedFriend.equals(" ")){
            // We know that the check is equal to 0 but the user has selected a friend.  We should
            // attempt to retrieve the selected friend's gold from the Gold field in their database
            // entry. This fixes the issue with Firebase being unable to retrieve the value on the
            // first attempt, which causes problems if that first attempt is when a transfer is
            // actually needing to be made
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
                            // Inform the user of error with connection
                            Toast.makeText(SendCoins.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            // We know that check is equal to 0 and the user has not selected a friend, meaning that
            // the activity has just started and should perform the date check
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.getDocuments().get(0).getId().equals(today)){
                                // The current day does not match that on the database, so the date
                                // on the database should be updated and any coins in the user's
                                // wallet must be moved to spare change. Then transfer the user to
                                // the DailyUpdate activity

                                // Create the HashMap required to update the user's Limitations in
                                // the database
                                HashMap<String, Integer> myUpdate = new HashMap<>();
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
                                for(int i = 0; i < coinMapW.size(); i++) {
                                    String key = coinsW.get(i);
                                    String id = coinMapW.get(key).getId();
                                    String currency = coinMapW.get(key).getCurrency();
                                    Double value = coinMapW.get(key).getValue();
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

                                // Take the user to the DailyUpdate activity
                                Intent intent = new Intent(SendCoins.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SendCoins.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // This is activated when the method is not being run from onStart(), as the check is not
        // equal to 0, and we know that the user had made a legitimate transaction, as proceed
        // is true.  Move the user to the GoldInformF activity with the appropriate
        // variables for display in this activity.  Profit is calculated using total and prevGold,
        // to work out the difference between pre and post-transfer.  Pass goldPass and bankPass as
        // these will need to be returned to SendCoins afterwards for when they need to be passed to
        // DepositCoins
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

    //==============================================================================================
    // Take the user to the DepositCoins activity, passing back goldPass and bankPass in a Bundle,
    // as these are required by DepositCoins
    public void goBack(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
