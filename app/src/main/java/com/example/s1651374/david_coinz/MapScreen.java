package com.example.s1651374.david_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

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
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import java.util.List;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {

    private String tag = "MapScreen";
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String mapdataString;
    private FeatureCollection featureCollection;
    private List<Feature> features;
    private Marker[] markers = new Marker[50];
    private boolean[] removed = new boolean[50];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id.mapboxMapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        mapdataString = bundle.getString("mapdata");
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if(mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            // Make location information available
            enableLocation();

            featureCollection = FeatureCollection.fromJson(mapdataString);
            features = featureCollection.features();


            int i = 0;

            for (Feature f : features) {
                if (f.geometry() instanceof Point) {

                    Icon icon = determineMarker(f.properties().get("currency").getAsString(), f.properties().get("marker-symbol").getAsString());

                    String title = f.properties().get("marker-symbol").getAsString();
                    String snippet = f.properties().get("currency").getAsString();
                    LatLng latLng = new LatLng(((Point) f.geometry()).latitude(), ((Point) f.geometry()).longitude());


                    Marker current = map.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet).icon(icon));
                    markers[i] = current;
                    i++;
                    /*markers[i].title(title);
                    markers[i].snippet(snippet);
                    markers[i].icon(icon);
                    markers[i].position(latLng);
                    map.addMarker(markers[i]);
                    i++;*/

                    /*map.addMarker(
                            new MarkerOptions().position(new LatLng(
                                    ((Point) f.geometry()).latitude(),
                                    ((Point) f.geometry()).longitude()
                            )).setTitle(f.properties().get("marker-symbol").getAsString())
                            .setSnippet(f.properties().get("currency").getAsString())
                            .icon(icon)
                    );*/
                }
            }

        }
    }

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

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
        SharedPreferences settings = getSharedPreferences("MyPrefsFile",
                Context.MODE_PRIVATE);
        double lat = Double.parseDouble(settings.getString("latDifficulty", "0.0005"));
        double lng = Double.parseDouble(settings.getString("lngDifficulty", "0.0005"));
        int i = 0;
        for (Feature f : features) {
            if (f.geometry() instanceof Point) {
                if (removed[i] == false){
                    if ((location.getLatitude() > (((Point) f.geometry()).latitude() - lat)) &&
                            (location.getLatitude() < (((Point) f.geometry()).latitude() + lat)) &&
                            (location.getLongitude() > (((Point) f.geometry()).longitude() - lng)) &&
                            (location.getLongitude() < (((Point) f.geometry()).longitude() + lng))) {
                        String currency = f.properties().get("currency").getAsString();
                        String value = f.properties().get("marker-symbol").getAsString();
                        Toast.makeText(this, "You found " + value + " " + currency + "!", Toast.LENGTH_LONG).show();
                        map.removeMarker(markers[i]);
                        removed[i] = true;
                    }
                }
                i++;
            }
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
        // Present toast or dialogue.
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            //Snackbar request_location = Snackbar.make(mapView, "Location is required to play, please enable this in settings", Snackbar.LENGTH_LONG);
            //request_location.show();
            // Open a dialogue with the user
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent (this, MainMenu.class);
        startActivity(intent);
    }

    public void goToDepositCoins(View view) {
        Intent intent = new Intent (this, DepositCoins.class);
        startActivity(intent);
    }

    // Method which determines which marker should be displayed for the Coin, dependent on it's currency type and value
    // This was made as a separate method as, although not CPU heavy, it takes up a lot of space and would clutter the
    // earlier methods which contain other important lines of code.
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
