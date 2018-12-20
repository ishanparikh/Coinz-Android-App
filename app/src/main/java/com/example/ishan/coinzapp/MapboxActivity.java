package com.example.ishan.coinzapp;

import android.annotation.SuppressLint;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import timber.log.Timber;


@SuppressWarnings("ALL")
public class MapboxActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener,View.OnClickListener,
        LocationEngineListener, PermissionsListener {

    public MapView mapView;
    public String tag = "MapboxActivity";
    public MapboxMap map;
    public PermissionsManager permissionsManager;
    public LocationEngine locationEngine;
    public Location originLocation;
    public LocationLayerPlugin locationLayerPlugin;
    // Format: YYYY/MM/DD
    public String downloadDate = "";
    // today's date
    public String date = "";
    // for storing preferences
    public  final String preferencesFile = "MyPrefsFile";
    HashMap<String,TodaysMap> todaysMapList = new HashMap<>();
    HashMap<String,TodaysMap> activatedMapList = new HashMap<>();
    HashMap<String,TodaysMap> wallet = new HashMap<>();
    DownloadFileTask urlObj = new DownloadFileTask();
    public String markerColour;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    public String ShilToGold;
    public String DolrToGold;
    public String QuidToGold;
    public String PenyToGold;
    public String usrDD;
    public double distWalked = 0;
    com.github.clans.fab.FloatingActionButton WalletIcon, HomeIcon;
    private TextView distanceCovered;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, @NonNull final String message, final Throwable throwable) {

        }
    }

    /**
    * Render Map is called whenever the Mapbox map needs to be rendered
    * This happens when:
    * 1. Coin is picked up - PickUpCoin
    * 2. When a Map is loaded for the first time a in the day
    * 3. When a user returns to the game, after having played, on that day
    * */

    public void renderMap(HashMap<String,TodaysMap> todaysMapList){
        map.clear();
        // todaysMapList contains the required list of coins to render onto map
        for (String i : todaysMapList.keySet()){

            //getting the marker colour

            String currCoin = todaysMapList.get(i).currency;
            switch (currCoin) {
                case "SHIL":
                    markerColour = "blue" + todaysMapList.get(i).symbol;
                    break;
                case "DOLR":
                    markerColour = "green" + todaysMapList.get(i).symbol;

                    break;
                case "QUID":
                    markerColour = "yellow" + todaysMapList.get(i).symbol;
                    break;
                case "PENY":
                    markerColour = "red" + todaysMapList.get(i).symbol;
                    break;
            }
            // creating marker and adding to map
            int resId = this.getResources().getIdentifier(markerColour, "drawable", this.getPackageName());
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(todaysMapList.get(i).loc.getLatitude(),todaysMapList.get(i).loc.getLongitude()))
                    .title(todaysMapList.get(i).currency.replaceAll("\"", ""))
                    .snippet(String.valueOf(todaysMapList.get(i).value))
                    .icon(IconFactory.getInstance(this).fromResource(resId))
            );
        }
        Timber.d("Number of coins left: %s", todaysMapList.size());
    }


    public void pickUpCoin(){
        double usrLat = originLocation.getLatitude();
        double usrLong = originLocation.getLongitude();
        List<String> rmID = new ArrayList<>();
        // Checking for every coin, if its within 25m radius
        for (String i : todaysMapList.keySet()){

            double markerLat = todaysMapList.get(i).loc.getLatitude();
            double markerLong = todaysMapList.get(i).loc.getLongitude();
            double phi1 = Math.toRadians(usrLat);
            double phi2 = Math.toRadians(markerLat);
            double delta = Math.toRadians(markerLong-usrLong);
            double R = 6371e3;
            double dist = Math.acos(Math.sin(phi1)* Math.sin(phi2) +
                    Math.cos(phi1) * Math.cos(phi2) * Math.cos(delta)) * R;

            if(dist <= 25){

                wallet.put(i, todaysMapList.get(i));
                rmID.add(i);
                updateWallet(i);
            }
        }
        // update todaysMapList by removing the picked up coin
        for (String i : rmID ){
            todaysMapList.remove(i);
            updateDailyCoinList(i);
            Timber.d("New size of MapList:%s", todaysMapList.size());
        }
        // re-render the map
        if(rmID.size() != 0){
        renderMap(todaysMapList);

        }
    }

    /**
     * Method is used if a user has already logged in and loaded the daliy coin list
     * Reads from firebase, Calls render map with uncollected coins
     */
    public void getAtivatedMapList(String emailID){
        db.collection("Users").document(Objects.requireNonNull(emailID)).collection("DailyCoinList")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Timber.d(document.getId() + " => " + document.getData());
                            Map loc = (Map) document.get("loc");
                            TodaysMap activated = new TodaysMap(Objects.requireNonNull(document.get("date")).toString(),
                                    Objects.requireNonNull(document.get("currency")).toString(),
                                    Objects.requireNonNull(document.get("id")).toString(),
                                    (Double) document.get("value"),
                                    new LatLng((double) (loc.get("latitude")), (double) (loc.get("longitude"))),
                                    Objects.requireNonNull(document.get("symbol")).toString());

                            activatedMapList.put(activated.id, activated);
                        }
                        renderMap(activatedMapList);

                    } else {
                        Timber.d(task.getException(), "Error getting documents: ");
                    }
                });

    }

    /**
     * Sets the exchange rates for the current day, values taken from JSON file
     * @param emailID
     * @param d2g
     * @param q2g
     * @param s2g
     * @param p2g
     */
    public void setExchangeRates(String emailID,String d2g,String q2g,String s2g,String p2g){

        HashMap<String, Object> exchange = new HashMap<>();
        exchange.put("DolrToGold", d2g);
        exchange.put("PenyToGold", p2g);
        exchange.put("QuidToGold", q2g);
        exchange.put("ShilToGold", s2g);

        db.collection("Users").document(emailID)
                .update(exchange)
                .addOnSuccessListener(aVoid -> Timber.d("Exchange rates uploaded"))
                .addOnFailureListener(e -> Timber.w(e, "Error uploading exchange rate"));
    }

    /**
     * Cruial method to check if a particular user has already downloaded the current days map for his account
     * Done on Firetore to allow users to play across devices
     * @param emailID
     */
    public void getUserDownloadDate(String emailID){

        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            usrDD = document.getString("LastDownloadDate");
                            assert usrDD != null;
                            // if new day, then
                            if(!usrDD.equals(date)){
                                // User has not played on current day, download today's map
                                renderMap(todaysMapList);
                                pushDailyCoinList();
                                HashMap<String, Object> bc = new HashMap<>();
                                bc.put("BankCounter", 0);
                                db.collection("Users").document(Objects.requireNonNull(user.getEmail()))
                                        .update(bc)
                                        .addOnSuccessListener(aVoid -> {
                                            Timber.d("Bank counter created for new day & User Download Date updated");
                                            setUserDownloadDate(emailID);
                                        })
                                        .addOnFailureListener(e -> Timber.tag(tag).w(e, "Bank counter NOT created for new day"));
                                Timber.tag(tag).w("Daily Map Downloaded" + downloadDate + "\t" + date);

                            }else {
                                // User has already been active on current day
                                getAtivatedMapList(emailID);

                            }

                        } else {
                            Timber.d("No such document");
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });
    }

    public void setUserDownloadDate(String emailID){
        HashMap setDate = new HashMap();
        setDate.put("LastDownloadDate",date);
        db.collection("Users").document(emailID)
                .update(setDate)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Timber.d("User Download Date updated successfully!"))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error updating document"));
    }

    public void pushDistanceWalked(String emailID, double distance){
        HashMap dist = new HashMap();
        dist.put("Distance", distance);
        db.collection("Users").document(emailID)
                .update(dist)
                .addOnSuccessListener((OnSuccessListener<Void>) aVoid -> Timber.d("User Distance walked updated successfully!"))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error updating document"));

    }

    public void getDistanceWalked(String emailID){
        db.collection("Users").document(emailID)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            distWalked = document.getDouble("Distance");
                            Timber.d("DocumentSnapshot data: %s", document.getData());
                        } else {
                            Timber.d("No such document");
                        }
                    } else {
                        Timber.d(task.getException(), "get failed with ");
                    }
                });
    }

    /**
     * Updates wallet when new coins are picked up
     * @param coinID
     */
    public void updateWallet(String coinID){
        //Add to FireStore
        TodaysMap newCoin = wallet.get(coinID);
        db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("Wallet").document(coinID)
                .set(newCoin, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Timber.d("Coin added to wallet, new wallet size = %s", wallet.size()))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error uploading coin to firestore"));
    }

    /**
     * Updates firestore database by removing collected coins
     * @param coinID
     */
    public void updateDailyCoinList(String coinID){

        db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("DailyCoinList").document(coinID)
                .delete()
                .addOnSuccessListener(aVoid -> Timber.d("Picked up coin removed, new size: %s", (50 - todaysMapList.size())))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error deleting document"));

    }

    /**
     * updates user's database with new day's coin list
     */
    public void pushDailyCoinList(){
        for (String i : todaysMapList.keySet()){
            TodaysMap newCoin = todaysMapList.get(i);
            db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("DailyCoinList").document(i)
                    .set(newCoin, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Timber.d("Adding coin to Daily Coin List"))
                    .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error adding document"));
        }

    }

    public void onClick(View view)
    {
        if(view == WalletIcon){
            Toast.makeText(MapboxActivity.this,"WalletIcon",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, WalletActivity.class));

        }

        if(view == HomeIcon)
        {
            Toast.makeText(MapboxActivity.this,"HomeIcon ",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MapboxActivity.this, ProfileActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new NotLoggingTree());
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_mapbox);
        distanceCovered = findViewById(R.id.distCovered);
        mapView = findViewById((R.id.mapView));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        WalletIcon = findViewById(R.id.WalletIcon);
        HomeIcon = findViewById(R.id.HomeIcon);
        HomeIcon.setOnClickListener(this);
        WalletIcon.setOnClickListener(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        getDistanceWalked(user.getEmail());
        Timber.d("Distance History is :%s", distWalked);

    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        if (mapboxMap == null) {
            Timber.d("[onMapReady] mapBox is null");
        }

            map = mapboxMap;
        if (map != null) {
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
        }

            // Make location information available
            enableLocation();
            distanceCovered.setText("Distance Covered is: \n" + Math.round(distWalked) +"m");
            String pattern = "yyyy/MM/dd";
            // setting the pattern for date
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            date = simpleDateFormat.format(new Date());
            //creating url to download map info
            String url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
            Timber.d(url);
            String mapLink = null;
            try {
                mapLink = urlObj.execute(url).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Timber.d("Failed to build mapLink");
            }

            // Get Rates from file
            try {
                JSONObject rateObj =  new JSONObject(mapLink).getJSONObject("rates");
                ShilToGold = (rateObj.get("SHIL").toString());
                DolrToGold = (rateObj.get("DOLR").toString());
                QuidToGold = (rateObj.get("QUID").toString());
                PenyToGold = (rateObj.get("PENY").toString());
                setExchangeRates(user.getEmail(),DolrToGold,QuidToGold,ShilToGold,PenyToGold);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert mapLink != null;
            FeatureCollection featureCollection = FeatureCollection.fromJson(mapLink);
            List<Feature> features = featureCollection.features();
            // Parsing JSON file for Coin infomation
            assert features != null;
            for (Feature f : features) {

                 TodaysMap today = new TodaysMap(date,
                         f.getStringProperty("currency"),
                         f.getStringProperty("id"),
                         Double.parseDouble(f.getStringProperty("value")),
                         new LatLng(((Point) Objects.requireNonNull(f.geometry())).latitude(), ((Point) Objects.requireNonNull(f.geometry())).longitude()),
                         f.getStringProperty("marker-symbol"));

                 todaysMapList.put(today.id, today);

            }
            getUserDownloadDate(user.getEmail());

     }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Timber.d("Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Timber.d("Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(3000); // preferably every 3 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        //locationEngine.addLocationEngineListener(this);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), map.getCameraPosition().zoom));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @SuppressLint({"LogNotTimber", "SetTextI18n"})
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (originLocation != null)
            {
                distWalked += originLocation.distanceTo(location);
                Log.d("MapboxActivity.java","Distance Covered = " + distWalked);
                distanceCovered.setText("Distance Covered is: \n" +  Math.round(distWalked) +"m");

            }
            originLocation = location;
            setCameraPosition(location);
            pickUpCoin();
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Present Toast/Dialogue to user to enable Location services
        Context context = getApplicationContext();
        CharSequence text = "Enable Location!";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Timber.d("[onPermissionResult] granted == %s", granted);
        if (granted) {
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Timber.d("[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null)
            locationLayerPlugin.onStart();
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
        Timber.d("[onStop] Storing lastDownloadDate of %s", downloadDate);
        downloadDate = date;
//        activityRunning = false;
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        // Apply the edits!
        editor.apply();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null)
            locationLayerPlugin.onStop();

        mapView.onStop();
        pushDistanceWalked(user.getEmail(), distWalked);
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

    @Override
    public void onMapClick(@NonNull LatLng point) {
    }


}