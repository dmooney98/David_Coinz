package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MainMenu extends AppCompatActivity {

    private final String tag = "MainMenu";

    private String lastDownloadDate = "";
    private String today = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String mapdata = "";
    private String shil;
    private String quid;
    private String dolr;
    private String peny;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private int check = 0;
    private ArrayList<String> coinsW = new ArrayList<>();
    private HashMap<String, Coin> coinMapW = new HashMap<>();
    private String today_check;
    private Double goldPass;
    private int bankPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        currentUser = mAuth.getCurrentUser().getEmail();

    }

    @Override
    public void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);

        // use "" as the default value (this might be the first time tha app is run)
        lastDownloadDate = settings.getString("lastDownloadDate","");
        Log.d(tag ,"[onStart] Recalled lastDownloadDate is '" + lastDownloadDate + "'");

        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        mapdata = settings.getString("mapdata", "");

        today_check = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if ((!today.equals(lastDownloadDate)) || mapdata.equals("")) {
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + today + "/coinzmap.geojson";
            AsyncTask<String, Void, String> data = new DownloadFileTask().execute(link);
            try {
                mapdata = data.get();

                lastDownloadDate = today;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Map downloading", Toast.LENGTH_LONG).show();
            try {
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
            shil = settings.getString("shil", "");
            quid = settings.getString("quid", "");
            dolr = settings.getString("dolr", "");
            peny = settings.getString("peny", "");
            //Toast.makeText(this, "Today's map has been downloaded", Toast.LENGTH_LONG).show();
        }

        String backgroundPick = settings.getString("backgroundPick", "1");
        ImageView image = (ImageView) findViewById(R.id.background);

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
                            Toast.makeText(MainMenu.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

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
                            Toast.makeText(MainMenu.this, "Failed to establish connection to DataBase, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });

            firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.getDocuments().get(0).getId().equals(today_check)) {
                                HashMap<String, Integer> myUpdate = new HashMap<String, Integer>();
                                myUpdate.put("Banked", 0);
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();
                                firebaseFirestore.collection("Users").document(currentUser).collection("Limitations").document(today_check).set(myUpdate);
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
                                Intent intent = new Intent(MainMenu.this, DailyUpdate.class);
                                startActivity(intent);
                            }
                            else {
                                bankPass = Integer.parseInt(queryDocumentSnapshots.getDocuments().get(0).get("Banked").toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainMenu.this, "Failed to retrieve DataBase info, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        check = 1;

    }

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

    // Code for button functionalities
    public void goToMapScreen(View view) {
        Intent intent = new Intent (this, MapScreen.class);
        intent.putExtra("mapdata", mapdata);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent (this, Settings.class);
        startActivity(intent);
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    public void goToManageFriends(View view) {
        Intent intent = new Intent (this, ManageFriends.class);
        startActivity(intent);
    }

    public void goToHelp(View view) {
        Intent intent = new Intent (this, Help.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        Intent intent = new Intent (this, SignIn.class);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        startActivity(intent);
    }

}
