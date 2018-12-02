package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

//==================================================================================================
// This activity presents the user with the main screen of the app, from which they can access the
// main sections of the app.  This activity also performs checks to ensure the most recent map is
// downloaded and whether a new day has occurred, as well as updating several variables to be either
// stored in SharedPreferences or passed in a Bundle around the app, for use in other activities.
// From this activity the user can log out and be returned to SignIn, or they can go to MapScreen,
// DepositCoins, ManageFriends, Settings, or Help
public class MainMenu extends AppCompatActivity {

    //==============================================================================================
    // Creates all required variables for connecting to Firebase and performing date checks, as well
    // as checking to ensure the most recent mapdata is downloaded and that the exchange rates in
    // SharedPreferences are correct.  The goldPass and bankPass variables must be initialised and
    // given values in this activity

    // Variables required for connection to Firebase, and the date check
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private int check = 0;
    private ArrayList<String> coinsW = new ArrayList<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private String today_check;

    // Variables required for the Log messages throughout the activity. as well as the defaults
    // required for checking the last download data and shared preferences and updating the mapdata
    private final String tag = "MainMenu";
    private String lastDownloadDate = "";
    private final String preferencesFile = "MyPrefsFile";
    private String mapdata = "";

    // Variables required for settings the various exchange rates from the mapdata, which can then
    // be saved into the SharedPreferences for use around the device
    private String shil;
    private String quid;
    private String dolr;
    private String peny;

    // Variables required for creating and filling the goldPass and bankPass variables, which store
    // the user's current gold total and the number of coins they have banked today.  These will be
    // passed to certain activities to potentially use in the future and avoid Firebase issues
    private Double goldPass;
    private int bankPass;


    //==============================================================================================
    // Sets the current user variable appropriately using FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getEmail();
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        // Obtain SharedPreferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        // Obtain the lastDownloadDate from the SharedPreferences, and create the correct Log tag
        lastDownloadDate = settings.getString("lastDownloadDate","");
        Log.d(tag ,"[onStart] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        // Obtain today's date, to be used in determining whether a new map needs downloaded, as
        // well as the mapdata stored in SharedPreferences
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        mapdata = settings.getString("mapdata", "");

        // Obtain today's data in another format which can be used for the date check against the
        // database
        today_check = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Perform a check to determine whether the last download date is the same as today's date,
        // as well as whether the mapdata is empty (so the user may have played on another device,
        // but not yet on this one)
        if ((!today.equals(lastDownloadDate)) || mapdata.equals("")) {
            // The mapdata on the device is either out of date or non-existent, so the new map
            // must be downloaded

            // Create the link required for accessing the new mapdata using the variable 'today'
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" +
                    today + "/coinzmap.geojson";

            // Perform the download asynchronously so as not to cause any lag or issues in the app's
            // performance
            AsyncTask<String, Void, String> data = new DownloadFileTask().execute(link);
            try {
                // Acquire the new mapdata
                mapdata = data.get();

                // Change the last known download date to today's date
                lastDownloadDate = today;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // Inform the user that the new map is downloading
            Toast.makeText(this, "Map downloading", Toast.LENGTH_LONG).show();

            try {
                // Create a JSONObject using the newly downloaded mapdata, and use another
                // JSONObject to extract the rates from the data, then put this into the appropriate
                // variables for each exchange rate
                JSONObject json = new JSONObject(mapdata);
                JSONObject rates = json.getJSONObject("rates");
                shil = rates.getString("SHIL");
                quid = rates.getString("QUID");
                dolr = rates.getString("DOLR");
                peny = rates.getString("PENY");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else {
            // The mapdata on the device is correct and up to date, so simply set the exchange rate
            // variables to the correct rates which have been stored in SharedPreferences
            shil = settings.getString("shil", "");
            quid = settings.getString("quid", "");
            dolr = settings.getString("dolr", "");
            peny = settings.getString("peny", "");
            //Toast.makeText(this, "Today's map has been downloaded", Toast.LENGTH_LONG).show();
        }

        // Acquire the background choice from SharedPreferences, and initialise the ImageView
        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.MM_background);

        // Set the background using the information from SharedPreferences
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
                            Toast.makeText(MainMenu.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Access Firebase to retrieve the user's current amount of gold and put this into the
            // goldPass variable, which will now be able to be passed in the Bundle between certain
            // activities which may need to make use of it
            firebaseFirestore.collection("Users").document(currentUser).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            goldPass = documentSnapshot.getDouble("Gold");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(MainMenu.this,
                                    "Failed to establish connection to DataBase, " +
                                            "please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Access Firebase to perform a date check, where the date stored in the user's Firebase
            // is compared to the actual date, in order to determine whether it is a new day.  If it
            // is not a new day then the bankPass variable can be set to today's date, whereas if it
            // is a new day then the user's database will be updated correctly and the user will
            // be taken to the DailyUpdate activity
            firebaseFirestore.collection("Users").document(currentUser)
                    .collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot current = queryDocumentSnapshots.getDocuments().get(0);
                            if(!current.getId().equals(today_check)) {
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
                                        .document(current.getId()).delete();

                                firebaseFirestore.collection("Users")
                                        .document(currentUser).collection("Limitations")
                                        .document(today_check).set(myUpdate);

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
                                            .collection("Spare Change")
                                            .document(id).set(toPut);

                                    firebaseFirestore.collection("Users")
                                            .document(currentUser).collection("Wallet")
                                            .document(id).delete();
                                }

                                // Take the user to the DailyUpdate activity
                                Intent intent = new Intent(MainMenu.this,
                                        DailyUpdate.class);
                                startActivity(intent);
                            }

                            else {
                                // Today is the same date as stored in the user's database, so this
                                // value is valid and can be retrieved and used as bankPass
                                bankPass = Integer.parseInt(current.get("Banked").toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Inform the user of error with connection
                            Toast.makeText(MainMenu.this,
                                    "Failed to retrieve DataBase info, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update the variable required for the check
        check = 1;

    }

    //==============================================================================================
    // MainMenu's onStop() method must update the SharedPreferences for the app, which allow other
    // screens to have access to the exchange rates of the currency, as well as the mapdata when
    // required.  The lastDownloadDate is also an important variable to store in SharedPreferences,
    // as it needs to be stored and checked by MainMenu in the future to ensure the newest map is
    // downloaded
    @Override
    public void onStop() {
        super.onStop();
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + lastDownloadDate);
        Log.d(tag, "[onStop] Storing mapdata as " + mapdata);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", lastDownloadDate);
        editor.putString("mapdata", mapdata);
        editor.putString("shil", shil);
        editor.putString("quid", quid);
        editor.putString("dolr", dolr);
        editor.putString("peny", peny);
        // Apply the edits!
        editor.apply();
    }

    //==============================================================================================
    // Take the user to the MapScreen activity.  MapScreen requires the mapdata to be passed to it,
    // as well as the goldPass and bankPass variables, due to the fact that MapScreen can access
    // DepositCoins which requires these variables to update its TextViews
    public void goToMapScreen(View view) {
        Intent intent = new Intent (this, MapScreen.class);
        intent.putExtra("mapdata", mapdata);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the Settings activity, nothing needs to be passed to this activity
    public void goToSettings(View view) {
        Intent intent = new Intent (this, Settings.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the DepositCoins activity, which requires the passing of the goldPass and
    // bankPass via Bundle.  This is to avoid issues with acquiring these values from Firebase in
    // the future, as MainMenu is able to reach them without problems
    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the ManageFriends activity, nothing needs to be passed to this activity
    public void goToManageFriends(View view) {
        Intent intent = new Intent (this, ManageFriends.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the Help activity, nothing needs to be passed to this activity
    public void goToHelp(View view) {
        Intent intent = new Intent (this, Help.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Returns the user to the SignIn activity, and uses the FirebaseAuth method signOut() to
    // set current user to null
    public void logOut(View view) {
        Intent intent = new Intent (this, SignIn.class);
        mAuth.signOut();
        startActivity(intent);
    }

}