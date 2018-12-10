package com.example.ishan.coinzapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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


public class MapboxActivity extends AppCompatActivity implements SensorEventListener,OnMapReadyCallback, MapboxMap.OnMapClickListener,View.OnClickListener,
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
    public  int stepCount;
    public String ShilToGold;
    public String DolrToGold;
    public String QuidToGold;
    public String PenyToGold;
    public SensorManager sensorManager;
    boolean activityRunning;
    public String usrDD;
    public double distWalked = 0;
    com.github.clans.fab.FloatingActionButton WalletIcon, HomeIcon;
    private TextView distanceCovered;

    public class NotLoggingTree extends Timber.Tree {
        @Override
        protected void log(final int priority, final String tag, final String message, final Throwable throwable) {

        }
    }


    public void renderMap(HashMap<String,TodaysMap> todaysMapList){
        map.clear();
        for (String i : todaysMapList.keySet()){

            String currCoin = todaysMapList.get(i).currency;
            //String currCoin = f.properties().get("currency").toString().replaceAll("\"", "");
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
        for (String i : rmID ){
            todaysMapList.remove(i);
            updateDailyCoinList(i);

            Timber.d("New size of MapList:%s", todaysMapList.size());
        }
        if(rmID.size() != 0){
        renderMap(todaysMapList);

        }
    }

    public void getaAtivatedMapList(String emailID){
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

    public void getUserDownloadDate(String emailID){
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();

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
                                editor.putString("DolrToGold", DolrToGold);
                                editor.putString("PenyToGold", PenyToGold);
                                editor.putString("QuidToGold", QuidToGold);
                                editor.putString("ShilToGold", ShilToGold);
                                editor.apply();
//                                setExchangeRates( emailID,DolrToGold,QuidToGold,ShilToGold,PenyToGold);

                                Timber.tag(tag).w("Daily Map Downloaded" + downloadDate + "\t" + date);

                            }else {
                                // User has already been active on current day
                                getaAtivatedMapList(emailID);

//                                renderMap(activatedMapList);
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
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        distWalked = document.getDouble("Distance");
                        Timber.d("DocumentSnapshot data: %s", document.getData());
                    } else {
                        Timber.d("No such document");
                    }
                } else {
                    Timber.d(task.getException(), "get failed with ");
                }
            }
        });
    }

    public void updateWallet(String coinID){
        //Add to FireStore
        TodaysMap newCoin = wallet.get(coinID);
        db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("Wallet").document(coinID)
                .set(newCoin, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Timber.d("Coin added to wallet, new wallet size = %s", wallet.size()))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error uploading coin to firestore"));
    }

    public void updateDailyCoinList(String coinID){

        db.collection("Users").document(Objects.requireNonNull(user.getEmail())).collection("DailyCoinList").document(coinID)
                .delete()
                .addOnSuccessListener(aVoid -> Timber.d("Picked up coin removed, new size: %s", (50 - todaysMapList.size())))
                .addOnFailureListener(e -> Timber.tag(tag).w(e, "Error deleting document"));

    }

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
//

        }

        if(view == HomeIcon)
        {
            Toast.makeText(MapboxActivity.this,"HomeIcon ",Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this, ProfileActivity.class));
            Intent intent = new Intent(MapboxActivity.this, ProfileActivity.class);
            Timber.d("Distance History is :" + distWalked);
            intent.putExtra("Distance",distWalked);
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        distanceCovered = findViewById(R.id.distCovered);
        mapView = findViewById((R.id.mapView));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        WalletIcon = findViewById(R.id.WalletIcon);
        HomeIcon = findViewById(R.id.HomeIcon);
        HomeIcon.setOnClickListener(this);
        WalletIcon.setOnClickListener(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        getDistanceWalked(user.getEmail());
        Timber.d("Distance History is :" + distWalked);

    }



    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        if (mapboxMap == null) {
            Timber.d("[onMapReady] mapBox is null");
        } else {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if(countSensor != null){
                sensorManager.registerListener(this,countSensor,SensorManager.SENSOR_DELAY_UI);

            }else {
                Toast.makeText(this, "Count sensor not available!", Toast.LENGTH_SHORT).show();
            }

            activityRunning = true;
            map = mapboxMap;
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();
            distanceCovered.setText("Distance Covered is: \n" + Math.round(distWalked) +"m");
            String pattern = "yyyy/MM/dd";
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            date = simpleDateFormat.format(new Date());
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

            // Get Rates
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

            assert features != null;
            for (Feature f : features) {

                 TodaysMap today = new TodaysMap(date,
                         f.getStringProperty("currency"),
                         f.getStringProperty("id"),
                         Double.parseDouble(f.getStringProperty("value")),
                         new LatLng(((Point) Objects.requireNonNull(f.geometry())).latitude(), ((Point) Objects.requireNonNull(f.geometry())).longitude()),
                         f.getStringProperty("marker-symbol"));

                 todaysMapList.put(today.id, today);
//                 String currCoin = f.getStringProperty("currency");
//                //String currCoin = f.properties().get("currency").toString().replaceAll("\"", "");
//                if ( currCoin.equals("SHIL")) {
//                    markerColour ="blue"+ f.getStringProperty("marker-symbol");
//                }else if(currCoin.equals("DOLR")){
//                    markerColour ="green"+f.getStringProperty("marker-symbol");;
//
//                }else if(currCoin.equals("QUID")) {
//                    markerColour = "yellow" + f.getStringProperty("marker-symbol");;
//                }else if(currCoin.equals("PENY")) {
//                    markerColour = "red" + f.getStringProperty("marker-symbol");;
//                }
//                Log.d(tag,"MarkerColour is: "+ markerColour);
//
//                int resId = this.getResources().getIdentifier(markerColour, "drawable", this.getPackageName());
//
//                if (f.geometry() instanceof Point) {
//
//                     //List<Double> coordinates = ((Point) f.geometry()).coordinates();
//                    Point pt = (Point) f.geometry();
////                    markerColour = f.properties().get("currency").toString().replaceAll("\"", "") +
////                    Integer.parseInt(f.properties().get("value").toString().replaceAll("\"", ""));
//
//
//                     map.addMarker(new MarkerOptions()
//                            .position(new LatLng(pt.latitude(), pt.longitude()))
//                            .title( f.properties().get("currency").toString().replaceAll("\"", ""))
//                            .snippet((f.properties().get("value").toString().replaceAll("\"", "")))
//                            .icon(IconFactory.getInstance(this).fromResource(resId))
//                    );
//
////                    Log.d(tag,"Displaying: "+count+ map.getMarkers().toString());
////                    count +=1;
//                }


            }
            getUserDownloadDate(user.getEmail());


//            renderMap(todaysMapList);

//            SharedPreferences settings = getSharedPreferences(preferencesFile,
//                    Context.MODE_PRIVATE);
//            // We need an Editor object to make preference changes.
//            SharedPreferences.Editor editor = settings.edit();
//
//            getUserDownloadDate(user.getEmail());
//
//            if(!downloadDate.equals(date)){
//
//                pushDailyCoinList();
//
//                HashMap<String, Object> bc = new HashMap<>();
//                bc.put("BankCounter", 0);
//                db.collection("Users").document(user.getEmail())
//                        .update(bc)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.d(tag, "Bank counter created for new day ");
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(tag, "Bank counter NOT created for new day", e);
//                            }
//                        });
//
//
//                editor.putString("DolrToGold", DolrToGold);
//                editor.putString("PenyToGold", PenyToGold);
//                editor.putString("QuidToGold", QuidToGold);
//                editor.putString("ShilToGold", ShilToGold);
//                editor.apply();
//                Log.w(tag, "Daily Map Downloaded" + downloadDate + "\t"+date );
//            }
        }
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
        activityRunning = false;
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
        //    destinationMarker = map.addMarker(new MarkerOptions().position(point));
        //    destPos = Point.fromLngLat(point.getLongitude(),point.getLatitude());
        //    origPos = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLatitude());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(activityRunning){
            stepCount = (int) event.values[ 0];
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}