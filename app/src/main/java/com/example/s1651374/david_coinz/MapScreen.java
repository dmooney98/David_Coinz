package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

//==================================================================================================
// This activity shows the user their position on a map, whilst also showing the location of all of
// the coins which they have not collected yet, shown as marker images whose colours and numbers are
// in respect to their currency and value.  The user is able to collect coins from this activity,
// which will update their database correctly.  The activity also shows the user the number of coins
// currently in their wallet.  From this activity, the user can go to MainMenu or DepositCoins
public class MapScreen extends AppCompatActivity implements OnMapReadyCallback,
        LocationEngineListener, PermissionsListener {

    //==============================================================================================
    // Create variables which allow connection to Firebase, the use of mapbox and its placement of
    // markers, and the display of the number of coins in the user's wallet.  Also be able to
    // receive and transfer goldPass and bankPass

    // Variables which allow the connection to, retrieval, and updating of data in Firebase
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String currentUser;
    private HashMap<String, Coin> theCoins = new HashMap<>();
    private CollectionReference collRef;

    // Variables which allow the Map to work and the appropriate permissions to be obtained for
    // this, as well as being able to place and remove markers correctly
    private String tag = "MapScreen";
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String mapdataString;
    private HashMap<String, Marker> markers = new HashMap<>();

    // Variables which allow for the number of coins currently in the users wallet to be displayed
    // properly
    private DecimalFormat df = new DecimalFormat("#.###");
    private TextView map_wallet_count;
    private int coinCount;

    // goldPass and bankPass variables, which must be taken and initialised in case the user decides
    // to go to DepositCoins from MapScreen
    private Double goldPass;
    private int bankPass;

    //==============================================================================================
    // Code from lectures, as well as retrieval of required information from Bundle, initialising
    // certain variables for later use, and accessing Firebase to ensure the correct number of coins
    // in the user's wallet is displayed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        // Code from lectures
        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id.mapboxMapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Access Bundle to get values for the mapdata string, as well as goldPass and bankPass for
        // potential later use in passing to DepositCoins
        Bundle bundle = getIntent().getExtras();
        mapdataString = bundle.getString("mapdata");
        goldPass = bundle.getDouble("goldPass");
        bankPass = bundle.getInt("bankPass");

        // Connect to Firebase to retrieve information on how many coins are in the user's wallet,
        // so that this can be displayed and updated correctly while the user is playing
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getEmail();
        collRef = firebaseFirestore.collection("Users").document(currentUser)
                .collection("Removed");

        firebaseFirestore.collection("Users").document(currentUser)
                .collection("Wallet").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Retrieve information on how many coins are in the user's wallet from
                        // Firebase, and update the TextView to display this
                        coinCount = queryDocumentSnapshots.size();
                        map_wallet_count = findViewById(R.id.MS_map_wallet_count);
                        map_wallet_count.setText(String.valueOf(coinCount));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Inform the user of error with connection
                        Toast.makeText(MapScreen.this,
                                "Failed to establish connection to DataBase, " +
                                        "please try again.", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    //==============================================================================================
    // Code from lectures, then parsing of the geoJSON information in order to place markers
    // correctly on the map, with useful information when the user taps on them
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        // Code from lectures
        if(mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            // Make location information available
            enableLocation();

            // Create a FeatureCollection to utilise the fromJson() method on the mapdata string,
            // then create a List of these features so that these can be iterated through to obtain
            // the required information and make markers for the map using this
            FeatureCollection featureCollection = FeatureCollection.fromJson(mapdataString);
            List<Feature> features = featureCollection.features();

            // Iterate through each of the Features obtained from the geoJSON
            for (Feature f : features) {
                if (f.geometry() instanceof Point) {
                    // Obtain the required information on the id, value, currency, and position of
                    // the feature, as we know it is a Point
                    String id = f.properties().get("id").getAsString();
                    Double value = f.properties().get("value").getAsDouble();
                    String snippet = f.properties().get("currency").getAsString();
                    LatLng latLng = new LatLng(((Point) f.geometry()).latitude(),
                            ((Point) f.geometry()).longitude());

                    // Use our determineMarker function to determine which icon the Marker should be
                    // given, based on currency and value
                    Icon icon = determineMarker(f.properties().get("currency").getAsString(),
                            f.properties().get("marker-symbol").getAsString());

                    // Utilise the database to determine if the coin has already been collected and
                    // is therefore in 'Removed'
                    collRef.document(id).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(!documentSnapshot.exists()) {
                                        // The coin has not been collected before, so create its
                                        // appropriate marker with the information retrieved about
                                        // it, and add it to the HashMap of Coins and its marker to
                                        // the HashMap of Markers
                                        Marker current = map.addMarker(new MarkerOptions()
                                                .position(latLng).title(id).snippet(snippet)
                                                .icon(icon));
                                        Coin tempCoin = new Coin(id, snippet, value);
                                        theCoins.put(id, tempCoin);
                                        markers.put(id, current);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Inform the user of error with connection
                                    Toast.makeText(MapScreen.this,
                                            "Failed to establish connection to DataBase, " +
                                                    "please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                }
            }

    }

    //==============================================================================================
    // Code from lectures
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager((this));
            permissionsManager.requestLocationPermissions(this);
        }
    }

    //==============================================================================================
    // Code from lectures
    @SuppressWarnings("MissingPermission")
    private void  initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    //==============================================================================================
    // Code from lectures
    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "map is null");
            } else {
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    //==============================================================================================
    // Code from lectures
    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    //==============================================================================================
    // Code from lectures
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    //==============================================================================================
    // Code from lectures, followed by added code which uses the user's set difficulty to reference
    // their new position against that of every Marker on the map, to determine whether or not the
    // user has moved within range to collect the coin.  The database is updated appropriately
    @Override
    public void onLocationChanged(Location location) {
        // Code from lectures
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }

        // Access SharedPreferences to retrieve the difficulty set by the user.  If the difficulty
        // has not been set, then it is by default on the distance for 'Normal'
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        double lat = Double.parseDouble(settings.getString("latDifficulty", "0.0005"));
        double lng = Double.parseDouble(settings.getString("lngDifficulty", "0.0005"));

        // Iterate through each Marker on the map
        for (Marker m: map.getMarkers()) {
            // Compare the location of the user with the location of the current Marker, to see if
            // the user is within the range set by the variables retrieved from SharedPreferences
            if ((location.getLatitude() > (m.getPosition().getLatitude() - lat)) &&
                    (location.getLatitude() < (m.getPosition().getLatitude() + lat)) &&
                    (location.getLongitude() > (m.getPosition().getLongitude() - lng)) &&
                    (location.getLongitude() < (m.getPosition().getLongitude() + lng))) {
                // The user is within range of the Marker, put the appropriate values into variables
                // for use in adding the coin to their wallet
                String id = m.getTitle();
                Coin tempCoin = theCoins.get(id);
                String currency = tempCoin.getCurrency();
                Double value = tempCoin.getValue();

                // Give the user information on the coin they collected
                Toast.makeText(this, "You found " + df.format(value) + " "
                        + currency + "!", Toast.LENGTH_LONG).show();

                // Remove the Marker associated with the collected coin from the map
                map.removeMarker(markers.get(id));

                // Create the required HashMap for adding the Coin to the user's wallet in the
                // database
                HashMap<String, Object> walletCoin = new HashMap<>();

                // Put the required values of the Coin's currency and it's value into the HashMap to
                // be stored, as the title of the document is the Coin's id
                walletCoin.put("currency", theCoins.get(id).getCurrency());
                walletCoin.put("value", theCoins.get(id).getValue());

                // Add the information to the user's database for both their wallet and their
                // 'removed'.  This is important that it is stored in two places as wallet can be
                // manipulated when banking coins or converting them to spare change, but there
                // must still be an untouched record of coins that the user has collected that
                // MapScreen can reference to determine which markers to create when the map is
                // loaded
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Wallet")
                        .document(theCoins.get(id).getId()).set(walletCoin);
                firebaseFirestore.collection("Users").document(currentUser)
                        .collection("Removed")
                        .document(theCoins.get(id).getId()).set(walletCoin);

                // Update the on-screen counter which tells the user how many coins are in their
                // wallet
                coinCount++;
                map_wallet_count.setText(String.valueOf(coinCount));
            }
        }
    }

    //==============================================================================================
    // Code from lectures, this is not used in my implementation of the app
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialogue.
    }

    //==============================================================================================
    // Code from lectures, updated with my own Toast message
    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            Toast.makeText(this, "Location is required to play!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //==============================================================================================
    // Code from lectures
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //==============================================================================================
    // Code from lectures
    @Override
    @SuppressWarnings("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    //==============================================================================================
    // Code from lectures
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    //==============================================================================================
    // Code from lectures
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    //==============================================================================================
    // Code from lectures, updated so that when onStop() is ran, location updates are cancelled, and
    // the mapdata string is put into SharedPreferences
    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        Log.d(tag, "[onStop] Storing mapdata as " + mapdataString);
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("mapdata", mapdataString);
        editor.apply();
        mapView.onStop();
    }

    //==============================================================================================
    // Code from lectures
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    //==============================================================================================
    // Code from lectures
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //==============================================================================================
    // Code from lectures
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    //==============================================================================================
    // Take the user to the MainMenu activity, nothing needs to be passed
    public void goToMainMenu(View view) {
        Intent intent = new Intent (this, MainMenu.class);
        startActivity(intent);
    }

    //==============================================================================================
    // Take the user to the DepositCoins activity, passing the goldPass and bankPass variables, as
    // these are required by DepositCoins
    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, DepositCoins.class);
        intent.putExtra("goldPass", goldPass);
        intent.putExtra("bankPass", bankPass);
        startActivity(intent);
    }

    //==============================================================================================
    // Method which determines which marker should be displayed for the Coin, depending on it's
    // currency type and value.  This was made as a separate method as, although not CPU heavy, it
    // takes up a lot of space and would clutter the earlier methods which contain other important
    // lines of code.
    public Icon determineMarker(String currency, String symbol) {
        IconFactory iconFactory = IconFactory.getInstance(MapScreen.this);
        if (currency.equals("PENY")) {
            if (symbol.equals("0")) {
                return iconFactory.fromResource(R.drawable.peny0);

            }
            else if (symbol.equals("1")) {
                return iconFactory.fromResource(R.drawable.peny1);

            }
            else if (symbol.equals("2")) {
                return iconFactory.fromResource(R.drawable.peny2);

            }
            else if (symbol.equals("3")) {
                return iconFactory.fromResource(R.drawable.peny3);

            }
            else if (symbol.equals("4")) {
                return iconFactory.fromResource(R.drawable.peny4);

            }
            else if (symbol.equals("5")) {
                return iconFactory.fromResource(R.drawable.peny5);

            }
            else if (symbol.equals("6")) {
                return iconFactory.fromResource(R.drawable.peny6);

            }
            else if (symbol.equals("7")) {
                return iconFactory.fromResource(R.drawable.peny7);

            }
            else if (symbol.equals("8")) {
                return iconFactory.fromResource(R.drawable.peny8);

            }
            else {
                return iconFactory.fromResource(R.drawable.peny9);
            }
        }
        else if (currency.equals("DOLR")) {
            if (symbol.equals("0")) {
                return iconFactory.fromResource(R.drawable.dolr0);

            }
            else if (symbol.equals("1")) {
                return iconFactory.fromResource(R.drawable.dolr1);

            }
            else if (symbol.equals("2")) {
                return iconFactory.fromResource(R.drawable.dolr2);

            }
            else if (symbol.equals("3")) {
                return iconFactory.fromResource(R.drawable.dolr3);

            }
            else if (symbol.equals("4")) {
                return iconFactory.fromResource(R.drawable.dolr4);

            }
            else if (symbol.equals("5")) {
                return iconFactory.fromResource(R.drawable.dolr5);

            }
            else if (symbol.equals("6")) {
                return iconFactory.fromResource(R.drawable.dolr6);

            }
            else if (symbol.equals("7")) {
                return iconFactory.fromResource(R.drawable.dolr7);

            }
            else if (symbol.equals("8")) {
                return iconFactory.fromResource(R.drawable.dolr8);

            }
            else {
                return iconFactory.fromResource(R.drawable.dolr9);
            }
        }
        else if (currency.equals("SHIL")) {
            if (symbol.equals("0")) {
                return iconFactory.fromResource(R.drawable.shil0);

            }
            else if (symbol.equals("1")) {
                return iconFactory.fromResource(R.drawable.shil1);

            }
            else if (symbol.equals("2")) {
                return iconFactory.fromResource(R.drawable.shil2);

            }
            else if (symbol.equals("3")) {
                return iconFactory.fromResource(R.drawable.shil3);

            }
            else if (symbol.equals("4")) {
                return iconFactory.fromResource(R.drawable.shil4);

            }
            else if (symbol.equals("5")) {
                return iconFactory.fromResource(R.drawable.shil5);

            }
            else if (symbol.equals("6")) {
                return iconFactory.fromResource(R.drawable.shil6);

            }
            else if (symbol.equals("7")) {
                return iconFactory.fromResource(R.drawable.shil7);

            }
            else if (symbol.equals("8")) {
                return iconFactory.fromResource(R.drawable.shil8);

            }
            else {
                return iconFactory.fromResource(R.drawable.shil9);
            }
        }
        else {
            if (symbol.equals("0")) {
                return iconFactory.fromResource(R.drawable.quid0);

            }
            else if (symbol.equals("1")) {
                return iconFactory.fromResource(R.drawable.quid1);

            }
            else if (symbol.equals("2")) {
                return iconFactory.fromResource(R.drawable.quid2);

            }
            else if (symbol.equals("3")) {
                return iconFactory.fromResource(R.drawable.quid3);

            }
            else if (symbol.equals("4")) {
                return iconFactory.fromResource(R.drawable.quid4);

            }
            else if (symbol.equals("5")) {
                return iconFactory.fromResource(R.drawable.quid5);

            }
            else if (symbol.equals("6")) {
                return iconFactory.fromResource(R.drawable.quid6);

            }
            else if (symbol.equals("7")) {
                return iconFactory.fromResource(R.drawable.quid7);

            }
            else if (symbol.equals("8")) {
                return iconFactory.fromResource(R.drawable.quid8);

            }
            else {
                return iconFactory.fromResource(R.drawable.quid9);
            }
        }
    }

}
