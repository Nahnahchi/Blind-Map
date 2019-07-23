package com.example.blind_map_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
//import android.support.design.widget.Snackbar;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
//import com.example.blind_map_v3.R;
//import com.mapbox.android.core.R;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.api.tilequery.MapboxTilequery;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.blind_map_v3.Constance.*;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;


/**
 * Use the LocationLayerOptions class to customize the LocationComponent's device location icon.
 */
public class MainActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {


    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Location lastLocation;
    static Location curentLocation;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    private Point originPosition;
    private Point destinationPosition;
    private ImageButton curLocationCamera;
    private Button navigationButton;
    private TextView tilequeryResponseTextView;
    private List<Feature> featureList;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private DirectionsRoute currentRout;

    ImageButton mVoiceBtn;
    ImageButton myAddress;
    ImageButton whatsNear;
    ImageButton whatsThere;

    ImageButton cancelNavigatin;

    public Location getCurentLocation() {
        return curentLocation;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        curLocationCamera = findViewById(R.id.curLocationButton);
        navigationButton = findViewById(R.id.navigationButton);
        // Check and use saved instance state in case of device rotation
//        if (savedInstanceState != null) {
//            cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
//            renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
//            lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
//            curentLocation = lastLocation;
//        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        curLocationCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(curentLocation.getLatitude(), curentLocation.getLongitude())) // Sets the new camera position
                        .zoom(18) // Sets the zoom
                        .bearing(curentLocation.getBearing()) // Rotate the camera
                        .tilt(50) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);
            }
        });

        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRout)
                        .shouldSimulateRoute(false)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this, options);
            }
        });

        mVoiceBtn = findViewById(R.id.voiceBtn);
        mVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakToMic();
            }
        });


        myAddress = findViewById(R.id.my_address);
        myAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toastMSG(address(curentLocation.getLongitude(), curentLocation.getLatitude()));
                speak(null, address(curentLocation.getLongitude(), curentLocation.getLatitude()));
            }
        });

        whatsNear = findViewById(R.id.what_near);
        whatsNear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                creatFeatureList();
            }
        });

        whatsThere = findViewById(R.id.what_there);
        whatsThere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        cancelNavigatin = findViewById(R.id.close);
        cancelNavigatin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Style style = mapboxMap.getStyle();
                style.removeLayer("click-layer-navig");
                style.removeSource(CLICK_CENTER_NAVIGATION);
                navigationMapRoute.updateRouteArrowVisibilityTo(false);
                navigationMapRoute.updateRouteVisibilityTo(false);
                navigationButton.setVisibility(View.INVISIBLE);
                cancelNavigatin.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void speak(String locale, String toSpeak) {
        new Speaker(this, locale, toSpeak);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/collos/cjxyyo0kz0g7j1cow76pb9i50"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        //addClickLayer2(style);
                        addResultLayer(style);
                        mapboxMap.addOnMapClickListener(MainActivity.this);
                    }

                });
    }


    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        Speaker.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

//        outState.putInt(SAVED_STATE_CAMERA, cameraMode);
//        outState.putInt(SAVED_STATE_RENDER, renderMode);
//        if (locationComponent != null) {
//            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
//        }

    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();
                curentLocation = location;
                if (location == null) {
                    return;
                }
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

// Toast instructing user to tap on the map

        Style style = mapboxMap.getStyle();
        addClickLayer2(style);
        if (style != null) {
// Move and display the click center layer's red marker icon to wherever the map was clicked on
            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_NAVIGATION);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }

// Use the map click location to make a Tilequery API call
            //makeTilequeryApiCall(style, point);
            destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            navigationButton.setEnabled(true);
            navigationButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.mapboxBlueDark));
            navigationButton.setVisibility(View.VISIBLE);
            cancelNavigatin.setVisibility(View.VISIBLE);
            getRoute(Point.fromLngLat(curentLocation.getLongitude(), curentLocation.getLatitude()), destinationPosition);

            return true;
        }
        toastMSG("ERRRRRRRRR");
        return false;

    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, check right user and accesss token");
                            return;
                        } else if (response.body().routes().size() == 0) {
                            Log.e(TAG, "No routes found");
                            toastMSG("No routes found");
                            return;
                        }
                        currentRout = response.body().routes().get(0);
                        if (navigationMapRoute != null) {
                            navigationMapRoute.updateRouteArrowVisibilityTo(false);
                            navigationMapRoute.updateRouteVisibilityTo(false);
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap);
                        }
                        navigationMapRoute.addRoute(currentRout);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

    private void creatFeatureList() {
        LatLng point = new LatLng(curentLocation.getLatitude(), curentLocation.getLongitude());
        Style style = mapboxMap.getStyle();
        if (style != null) {
            // Move and display the click center layer's red marker icon to wherever the map was clicked on

            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            // Use the map click location to make a Tilequery API call
            makeTilequeryApiCall(style, point);
        }
    }

    /**
     * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
     *
     * @param point the center point that the the tilequery will originate from.
     */
    private void makeTilequeryApiCall(@NonNull final Style style, @NonNull LatLng point) {
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .mapIds("mapbox.mapbox-streets-v8")
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .radius(100)
                .limit(10)
                .geometry("point")
                .dedupe(true)
                .layers("poi_label")
                .build();

        tilequery.enqueueCall(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                //tilequeryResponseTextView.setText(response.body().toJson());
                System.err.println(response.body().toString());
                System.err.println(response.body().features().get(0).getProperty("name"));
                System.err.println(response.body().features().get(0).getProperty("tilequery"));
                featureList = response.body().features();
                System.err.println(featureList.size());
                toastMSG(new NearPoints(featureList).getClosestFeatureName());
                speak(null, new NearPoints(featureList).getClosestFeatureName());

                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void creatFeatureList2() {
        LatLng point = new LatLng(curentLocation.getLatitude(), curentLocation.getLongitude());
        Style style = mapboxMap.getStyle();
        if (style != null) {
            // Move and display the click center layer's red marker icon to wherever the map was clicked on

            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            // Use the map click location to make a Tilequery API call
            makeTilequeryApiCall2(style, point);
        }
    }

    /**
     * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
     *
     * @param point the center point that the the tilequery will originate from.
     */
    private void makeTilequeryApiCall2(@NonNull final Style style, @NonNull LatLng point) {
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .mapIds("mapbox.mapbox-streets-v8")
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .radius(100)
                .limit(10)
                .geometry("point")
                .dedupe(true)
                .layers("poi_label")
                .build();

        tilequery.enqueueCall(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                featureList = response.body().features();
                //TODO GET "What's there point" method/class
                toastMSG(new NearPoints(featureList).getClosestFeatureName());
                speak(null, new NearPoints(featureList).getClosestFeatureName());

                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add a map layer which will show a marker icon where the map was clicked
     */

    //TODO DELETE
    private void addClickLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("CLICK-ICON-ID", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.green_marker));

        loadedMapStyle.addSource(new GeoJsonSource(CLICK_CENTER_GEOJSON_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[]{})));

        loadedMapStyle.addLayer(new SymbolLayer("click-layer", CLICK_CENTER_GEOJSON_SOURCE_ID).withProperties(
                iconImage("CLICK-ICON-ID"),
                iconOffset(new Float[]{0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private void addClickLayer2(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("CLICK-ICON-ID-2", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.purple_marker));

        loadedMapStyle.addSource(new GeoJsonSource(CLICK_CENTER_NAVIGATION,
                FeatureCollection.fromFeatures(new Feature[]{})));

        loadedMapStyle.addLayer(new SymbolLayer("click-layer-navig", CLICK_CENTER_NAVIGATION).withProperties(
                iconImage("CLICK-ICON-ID-2"),
                iconOffset(new Float[]{0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    /**
     * Add a map layer which will show marker icons for all of the Tilequery API results
     */
    private void addResultLayer(@NonNull Style loadedMapStyle) {
// Add the marker image to map
        loadedMapStyle.addImage("RESULT-ICON-ID", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.blue_marker));

// Retrieve GeoJSON information from the Mapbox Tilequery API
        loadedMapStyle.addSource(new GeoJsonSource(RESULT_GEOJSON_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[]{})));

        loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, RESULT_GEOJSON_SOURCE_ID).withProperties(
                iconImage("RESULT-ICON-ID"),
                iconOffset(new Float[]{0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private void toastMSG(String string) {
        Toast.makeText(
                MainActivity.this,
                string,
                Toast.LENGTH_LONG
        ).show();
    }

    private void speakToMic() {
        // intent to show speech to a text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // start intent
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // get voice input and handle it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            final String[] commandsEn = {"address", "what nearest", "what there"};
            final String[] commandsLv = {"adrese", "kas ir tuvu", "kas tur ir"};
            final String[] commandsRu = {"адрес", "что рядом", "что там"};
            String[] commands;
            switch (Locale.getDefault().toString()) {
                case "en_GB":
                case "en_US": {
                    commands = commandsEn;
                    break;
                }
                case "lv_LV": {
                    commands = commandsLv;
                    break;
                }
                case "ru_RU": {
                    commands = commandsRu;
                    break;
                }
                default: {
                    commands = null;
                    commands = commandsEn;
                    break;
                }
            }
            if (commands != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result.get(0).contains(commands[0])) {
                    Toast.makeText(getApplicationContext(), commands[0], Toast.LENGTH_SHORT).show();
                    speak(null, address(curentLocation.getLongitude(), curentLocation.getLatitude()));
                } else if (result.get(0).contains(commands[1]) || result.get(0).contains(commands[2])) {
                    Toast.makeText(getApplicationContext(), commands[1] + " " + commands[2], Toast.LENGTH_SHORT).show();
                    getPOI();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong command!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to recognize speech!", Toast.LENGTH_SHORT).show();
        }
    }

    private String address(double lon, double lat) {
        try {
            GeoCoding geoCoding = new GeoCoding(lon, lat);
            String address = geoCoding.getAddress(geoCoding.getURL());
            if (address == null) {
                final double thetaMax = 6 * Math.PI;
                final double awayStep = 0.5 / thetaMax;
                final double chord = 0.005;
                double theta = chord;
                for (; theta <= thetaMax; ) {
                    double away = awayStep * theta;
                    double around = theta + 0.0005;
                    double x = lon + Math.cos(around) * away;
                    double y = lat + Math.sin(around) * away;
                    theta += chord / away;
                    geoCoding = new GeoCoding(x, y);
                    address = geoCoding.getAddress(geoCoding.getURL());
                    if (address == null) {
                        continue;
                    }
                    break;
                }
                if (address == null && theta >= thetaMax) {
                    return "Address not found";
                }
            }

            return "Your Address " + address;

        } catch (JSONException e) {
            System.err.println(e.getMessage());
            Toast.makeText(
                    MainActivity.this,
                    "JSON " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            Toast.makeText(
                    MainActivity.this,
                    "IO" + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
        return null;
    }

    private void getPOI() {
        LatLng point = new LatLng(curentLocation.getLatitude(), curentLocation.getLongitude());
        Style style = mapboxMap.getStyle();
        if (style != null) {
            // Move and display the click center layer's red marker icon to wherever the map was clicked on

            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            // Use the map click location to make a Tilequery API call
            makeTilequeryApiCall(style, point);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.userguide:
                Toast.makeText(this, "User Guide", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, com.example.blind_map_v3.UserGuide.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }


}
