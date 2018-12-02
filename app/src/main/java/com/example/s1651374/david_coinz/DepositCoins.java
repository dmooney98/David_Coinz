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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

//==================================================================================================
// This activity presents the user with a screen showing the daily exchange rates for the four
// currencies, as well as the user's total gold and number of coins banked today.  From this
// activity, the user can go to the MainMenu, MapScreen, BankCoins, SendCoins, or SpareChange
// activities.
// If a day change occurs, they are taken to the DailyUpdate activity
public class DepositCoins extends AppCompatActivity {

    //==============================================================================================
    // Create all required variables for the date check, which involves connection to Firebase,
    // as well as having the variables set up to receive goldPass and bankPass from MainMenu, so
    // that these can be displayed correctly to the user on this screen

    // Variables required for connection to Firebase, and the date check
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private int check = 0;
    private ArrayList<String> coinsW = new ArrayList<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private String today;

    // Variables required to receive and use goldPass and bankPass from Bundle
    private Double goldPass;
    private int bankPass;

    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_coins);

        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }

    //==============================================================================================
    // Set up appropriate variables for the class using SharedPreferences and Bundles, and use
    // Firebase to perform the date check
    public void onStart() {
        super.onStart();

        // Acquire the SharedPreferences file, and initialise the Image and TextViews, so that the
        // values inside these can be updated correctly to show exchange rates and the user's gold
        // and number of coins banked today
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.DC_background);
        TextView dValue = (TextView) findViewById(R.id.DC_dolrText);
        TextView pValue = (TextView) findViewById(R.id.DC_penyText);
        TextView qValue = (TextView) findViewById(R.id.DC_quidText);
        TextView sValue = (TextView) findViewById(R.id.DC_shilText);
        TextView goldBankField = (TextView) findViewById(R.id.DC_goldPassField);
        TextView bankPassField = (TextView) findViewById(R.id.DC_bankPassField);
        DecimalFormat df = new DecimalFormat("#.###");

        // Set up the 'today' variable so that it can be used in date check
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Set up the goldPass and bankPass variables with those passed to the activity
        Bundle bundle = getIntent().getExtras();
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        // Set the goldBankField TextView to display the user's current gold
        goldBankField.setText(df.format(goldPass) + " GOLD");

        // Set the bankPassField TextView to display the user's number of coins banked today.
        // The if statement is used so that the display will be grammatically correct
        if(bankPass == 1) {
            bankPassField.setText(bankPass + " COIN BANKED TODAY");
        }
        else {
            bankPassField.setText(bankPass + " COINS BANKED TODAY");
        }

        // Obtain the exchange rates from the SharedPreferences file and put these values into their
        // respective variables
        String dolr = settings.getString("dolr", "Unknown");
        String peny = settings.getString("peny", "Unknown");
        String quid = settings.getString("quid", "Unknown");
        String shil = settings.getString("shil", "Unknown");

        // Display the exchange rates correctly in the appropriate TextViews
        dValue.setText("DOLR: " + dolr);
        pValue.setText("PENY: " + peny);
        qValue.setText("QUID: " + quid);
        sValue.setText("SHIL: " + shil);

        // Set the background using the value obtained from SharedPreferences
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

        // Perform this check to prevent issues occurring with Firebase's retrieval of information
        // from the database
        if(check == 0) {
            // Set up a HashMap of all of the coins stored in the user's wallet, as this may have to
            // be used if the date check shows that the current date does not match that on
            // Firebase
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
                                String coinW = current.get("currency").toString() + " " +
                                        current.get("value").toString();
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
                            Toast.makeText(DepositCoins.this,
                                    "Failed to establish connection to DataBase," +
                                            " please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Perform a check that the date has not changed since the user arrived on the
            // DepositCoins activity.  If it has, move coins from wallet to spare change, and go to
            // the DailyUpdate activity.  If it has not, update the local variable keeping track of
            // how many coins have been banked today
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
                                for(int i = 0; i < coinMapW.size(); i++) {
                                    // Create correct variables required for the transfer, using the
                                    // information gathered about the user's wallet coins from
                                    // earlier
                                    String key = coinsW.get(i);
                                    String id = coinMapW.get(key).getId();
                                    String currency = coinMapW.get(key).getCurrency();
                                    Double value = coinMapW.get(key).getValue();

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
                                Intent intent = new Intent(DepositCoins.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(DepositCoins.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update the variable required for the check
        check = 1;

    }

    //==============================================================================================
    // Take the user to the MainMenu activity, but goldPass and bankPass are not required to be
    // given as MainMenu creates these again anyway
    public void goToMainMenu(View view) {
        Intent intent = new Intent (this, MainMenu.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the MapScreen activity.  The mapdata, goldPass, and bankPass must be passed
    // so that MapScreen can load the map correctly, and keep the goldPass and bankPass variables in
    // case the user decides to press the 'Deposit Coins' button on MapScreen
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

    //==============================================================================================
    // Take the user to the BankCoins activity.  goldPass and bankPass must be passed to BankCoins
    // in case the user decides to return to DepositCoins without banking any coins
    public void goToBankCoins(View view) {
        Intent intent = new Intent (this, BankCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the SendCoins activity.  goldPass and bankPass must be passed as these will
    // be required by DepositCoins again when the user backs out of SendCoins
    public void goToSendCoins(View view) {
        Intent intent = new Intent (this, SendCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the SpareChange activity.  goldPass and bankPass must be passed as these
    // will be required by DepositCoins again when the user backs out of SpareChange
    public void goToSpareChange(View view) {
        Intent intent = new Intent(this, SpareChange.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

}
