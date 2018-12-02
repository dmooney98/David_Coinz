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
// This activity allows the user to transfer coins from their wallet to their spare change, so that
// these coins are able to be transferred to users on their friend's list.  From this activity, the
// user can go to DepositCoins, TransferInform once a valid transfer has been completed, and
// DailyUpdate if the date check finds that the day has changed
public class SpareChange extends AppCompatActivity {

    //==============================================================================================
    // Create all variables required to connect to Firebase, transfer coins from the wallet to spare
    // change, display both the user's wallet and their spare change in separate ListViews, perform
    // the date check, and pass goldPass and bankPass for when the user returns to DepositCoins

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

    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth
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

        // Set up the values of variables from SharedPreferences and create a listener for the
        // button required to transfer coins
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.SC_background);
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Button transferButton = findViewById(R.id.SC_transferButton);
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transfer();
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

        // Set up the ListView to display the user's coins in wallet, and store these in the
        // appropriate variables for future use.  The check is used to avoid issues with ListView
        // duplicating its contents in certain conditions
        if(check == 0) {
            listViewW = (ListView) findViewById(R.id.SC_wallet);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Wallet").get()
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
                                TextView walletHelp = (TextView) findViewById(R.id.SC_walletHelp);
                                walletHelp.setText("You haven't got any coins in your wallet " +
                                        "at the moment.  Get out there and collect some more!");
                            }

                            // Remove any duplicate values from the ArrayList coins, as this can
                            // occur under certain circumstances and have duplicate coins appear in
                            // the ListView
                            HashSet<String> coinSet = new HashSet<String>();
                            coinSet.addAll(coins);
                            coins.clear();
                            coins.addAll(coinSet);

                            // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                            // coins
                            arrayAdapterW = new ArrayAdapter(SpareChange.this,
                                    R.layout.my_layout, R.id.row_layout, coins);
                            listViewW.setAdapter(arrayAdapterW);
                            listViewW.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SpareChange.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Perform the date check to determine whether the date has changed since moving to this
            // activity
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

                                // Take the user to the DailyUpdate activity
                                Intent intent = new Intent(SpareChange.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SpareChange.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Set up the ListView to display the user's coins in spare change, and store these in the
        // appropriate variables for future use.  The check is used to avoid issues with ListView
        // duplicating its contents in certain conditions
        if(check == 0) {
            listViewSC = (ListView) findViewById(R.id.SC_spare_change);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Spare Change").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot documentSnapshots) {
                            for (int i = 0; i < documentSnapshots.size(); i++) {
                                DocumentSnapshot current = documentSnapshots.getDocuments().get(i);

                                // Set up ArrayList which will be used to fill the ListView nicely
                                String change = current.get("currency").toString() +
                                        " " + current.get("value").toString();
                                changes.add(change);
                            }

                            // Display text message to fill the empty space left when the user has
                            // no coins in their wallet, or replace the message once the user moves
                            // a coin to their spare change
                            TextView changeHelp = (TextView) findViewById(R.id.SC_changeHelp);
                            if(changes.size() == 0) {
                                changeHelp.setText("You don't have any coins in Spare Change" +
                                        " just now.  Would you like to add some to send " +
                                        "to your friends?");
                            }
                            else {
                                changeHelp.setText("");
                            }

                            // Set up the ArrayAdapter for the ListView, to display the ArrayList,
                            // coins
                            arrayAdapterSC = new ArrayAdapter(SpareChange.this,
                                    R.layout.my_layout, R.id.row_layout, changes);
                            listViewSC.setAdapter(arrayAdapterSC);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(SpareChange.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update the selectedCoins ArrayList to contain the correct wallet coins that the user has
        // selected from the ListView
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

        // Update the variable required for the check
        check = 1;

    }

    //==============================================================================================
    // Transfer the coin over from the user's wallet to their spare change
    public void transfer() {
        // Set up correct variables ensure the user is only taken to TransferInform if a valid
        // transfer has occurred, rather than the user pressing the Transfer button without first
        // selecting a coin to be transferred
        int selectedCoinsSize = selectedCoins.size();
        boolean proceed = false;

        for (int i = 0; i < selectedCoins.size();) {
            // Set proceed to true so that we know that the user chose to actually transfer
            // one or more coins, rather than clicking the transfer button without selecting
            // any coins first
            proceed = true;

            // Set up required variables to add to the HashMap to move the coin to spare change
            Coin currCoin = coinMap.get(selectedCoins.get(i));
            String currentCurrency = currCoin.getCurrency();
            Double currentValue = currCoin.getValue();
            String currentId = currCoin.getId();

            // Create the required HashMap for putting the new coin into spare change
            HashMap<String, Object> toPut = new HashMap<>();

            // Populate the HashMap with the currency and value of the coin being transferred
            toPut.put("currency", currentCurrency);
            toPut.put("value", currentValue);

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

            // Put the coin into the user's spare change, using its coin id as the id for the
            // database, then delete this same coin from wallet by using its id
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Spare Change").document(currentId).set(toPut);
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Wallet").document(currentId).delete();
        }

        // This is activated when the user has made a valid transfer, and takes the user to the
        // TransferInform activity.  The number of coins transferred is passed in the Bundle to
        // allow TransferInform to display this correctly, as well as goldPass and bankPass being
        // passed so that they can be passed back to SpareChange afterwards, for when the user
        // wishes to return to DepositCoins where they are required
        if(proceed) {
            Intent intent = new Intent (this, TransferInform.class);
            intent.putExtra("coinCount", selectedCoinsSize);
            intent.putExtra("goldPass", goldPass);
            intent.putExtra("bankPass", bankPass);
            startActivity(intent);
        }
    }

    //==============================================================================================
    // Take the user to back to the DepositCoins activity, passing the goldPass and bankPass
    // variables as required
    public void goToDepositCoins(View view) {
        Intent intent = new Intent(this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
