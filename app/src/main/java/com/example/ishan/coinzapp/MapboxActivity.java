package com.example.ishan.coinzapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class MapboxActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener,View.OnClickListener,
        LocationEngineListener, PermissionsListener {

    private MapView mapView;
    private String tag = "MapboxActivity";
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private Location originLocation;
    private LocationLayerPlugin locationLayerPlugin;
    private String downloadDate = ""; // Format: YYYY/MM/DD
    private String date = "";// today's date
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String mapLink;
    private String url;
    HashMap<String,TodaysMap> todaysMapList = new HashMap<String,TodaysMap>();
    public static HashMap<String,TodaysMap> wallet = new HashMap<String,TodaysMap>();
    DownloadFileTask urlObj = new DownloadFileTask();
    private String markerColour;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

//    // variables for calculating and drawing a route
//    private Point originPosition;
//    private Point destinationPosition;
//    private DirectionsRoute currentRoute;
//    private NavigationMapRoute navigationMapRoute;


    String ShilToGold;
    String DolrToGold;
    String QuidToGold;
    String PenyToGold;

    com.github.clans.fab.FloatingActionButton WalletIcon, HomeIcon;

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

//    private void getRoute(Point origin, Point destination) {
//        NavigationRoute.builder(this)
//                .accessToken(Mapbox.getAccessToken())
//                .origin(origin)
//                .destination(destination)
//                .build()
//                .getRoute(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                        // You can get the generic HTTP info about the response
//                        Log.d(tag, "Response code: " + response.code());
//                        if (response.body() == null) {
//                            Log.e(tag, "No routes found, make sure you set the right user and access token.");
//                            return;
//                        } else if (response.body().routes().size() < 1) {
//                            Log.e(tag, "No routes found");
//                            return;
//                        }
//
//                        currentRoute = response.body().routes().get(0);
//
//                        // Draw the route on the map
//                        if (navigationMapRoute != null) {
//                            navigationMapRoute.removeRoute();
//                        } else {
//                            navigationMapRoute = new NavigationMapRoute(null, mapView, map, R.style.NavigationMapRoute);
//                        }
//                        navigationMapRoute.addRoute(currentRoute);
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                        Log.e(tag, "Error: " + throwable.getMessage());
//                    }
//                });
//    }


    public void renderMap(HashMap<String,TodaysMap> todaysMapList){
        map.clear();
        for (String i : todaysMapList.keySet()){

            String currCoin = todaysMapList.get(i).currency;
            //String currCoin = f.properties().get("currency").toString().replaceAll("\"", "");
            if ( currCoin.equals("SHIL")) {
                markerColour ="blue"+ todaysMapList.get(i).symbol;
            }else if(currCoin.equals("DOLR")){
                markerColour ="green"+todaysMapList.get(i).symbol;

            }else if(currCoin.equals("QUID")) {
                markerColour = "yellow" + todaysMapList.get(i).symbol;
            }else if(currCoin.equals("PENY")) {
                markerColour = "red" + todaysMapList.get(i).symbol;
            }


            int resId = this.getResources().getIdentifier(markerColour, "drawable", this.getPackageName());
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(todaysMapList.get(i).loc.getLatitude(),todaysMapList.get(i).loc.getLongitude()))
                    .title(todaysMapList.get(i).currency.toString().replaceAll("\"", ""))
                    .snippet(String.valueOf(todaysMapList.get(i).value))
                    .icon(IconFactory.getInstance(this).fromResource(resId))
            );
        }
        Log.d(tag,"Number of coins left: "+ todaysMapList.size());
    }


    public void pickUpCoin(){
        double usrLat = originLocation.getLatitude();
        double usrLong = originLocation.getLongitude();
        List<String> rmID = new ArrayList<String>();
        Set<String> keys = todaysMapList.keySet();
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
                wallet.put(i,todaysMapList.get(i));
                rmID.add(i);
                updateWallet(i);
            }
        }
        for (String i : rmID ){
            todaysMapList.remove(i);
            updateDailyCoinList(i);

            Log.d(tag, "New size of MapList:" +todaysMapList.size());
        }
        if(rmID.size() != 0){
        renderMap(todaysMapList);

        }
    }

    public void updateWallet(String coinID){
        //Add to FireStore
        TodaysMap newCoin = wallet.get(coinID);
        db.collection("Users").document(user.getEmail()).collection("Wallet").document(coinID)
                .set(newCoin, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(tag, "Coin added to wallet, new wallet size = " + wallet.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(tag, "Error uploading coin to firestore", e);
                    }
                });
    }

    public void updateDailyCoinList(String coinID){

        db.collection("Users").document(user.getEmail()).collection("DailyCoinList").document(coinID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(tag, "Picked up coin removed, new size: " + (50-todaysMapList.size()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(tag, "Error deleting document", e);
                    }
                });

    }

    public void pushDailyCoinList(){
        for (String i : todaysMapList.keySet()){
            TodaysMap newCoin = todaysMapList.get(i);
            db.collection("Users").document(user.getEmail()).collection("DailyCoinList").document(i)
                    .set(newCoin, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(tag, "Adding coin to Daily Coin List");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(tag, "Error adding document", e);
                        }
                    });
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
            startActivity(new Intent(this, ProfileActivity.class));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_mapbox);

        mapView = findViewById((R.id.mapView));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        WalletIcon = findViewById(R.id.WalletIcon);
        HomeIcon = findViewById(R.id.HomeIcon);

        HomeIcon.setOnClickListener(this);
        WalletIcon.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

//            Toast.makeText(MapboxActivity.this,"Welcome " + name +email +photoUrl ,Toast.LENGTH_SHORT).show();
//            Log.w(tag, "Welcome " + name +"\n"+email+"\n" +photoUrl);
        }
    }



    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapBox is null");
        } else {
            map = mapboxMap;
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();
            String pattern = "yyyy/MM/dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
//            downloadDate = date;
            url = "http://homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
            Log.d(tag,url);
            mapLink = null;
            try {
                mapLink = urlObj.execute(url).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(tag, "Failed to build mapLink");
            }

            // Get Rates
            try {
                JSONObject rateObj =  new JSONObject(mapLink).getJSONObject("rates");
                ShilToGold = (rateObj.get("SHIL").toString());
                DolrToGold = (rateObj.get("DOLR").toString());
                QuidToGold = (rateObj.get("QUID").toString());
                PenyToGold = (rateObj.get("PENY").toString());


            } catch (JSONException e) {
                e.printStackTrace();
            }


            FeatureCollection featureCollection = FeatureCollection.fromJson(mapLink);
            List<Feature> features = featureCollection.features();


            // Create an Icon object for the marker to use
            // Icon icon = IconFactory.getInstance(MapboxActivity.this).fromBitmap("#ffdf00");


            for (Feature f : features) {

                 TodaysMap today = new TodaysMap(date,
                         f.getStringProperty("currency"),
                         f.getStringProperty("id"),
                         Double.parseDouble(f.getStringProperty("value")),
                         new LatLng(((Point) f.geometry()).latitude(), ((Point) f.geometry()).longitude()),
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
            renderMap(todaysMapList);
            SharedPreferences settings = getSharedPreferences(preferencesFile,
                    Context.MODE_PRIVATE);
            // We need an Editor object to make preference changes.
            SharedPreferences.Editor editor = settings.edit();

            if(downloadDate.equals(date) == false){
                pushDailyCoinList();
                editor.putString("DolrToGold", DolrToGold);
                editor.putString("PenyToGold", PenyToGold);
                editor.putString("QuidToGold", QuidToGold);
                editor.putString("ShilToGold", ShilToGold);
                editor.apply();
                Log.w(tag, "Daily Map Downloaded" + downloadDate + "\t"+date );
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
                location.getLongitude()), 13.0));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
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
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) {
            enableLocation();
        } else {
            // Open a dialogue with the user
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
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");
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
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        downloadDate = date;
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(preferencesFile,
                Context.MODE_PRIVATE);
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

}