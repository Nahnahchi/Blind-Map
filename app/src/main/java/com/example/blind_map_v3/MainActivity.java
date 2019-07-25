package com.example.blind_map_v3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.example.blind_map_v3.Constance.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


/**
 * Use the LocationLayerOptions class to customize the LocationComponent's device location icon.
 */
public class MainActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static boolean isStarted = false;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    static Location curentLocation;
    private LocationComponent locationComponent;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    private Button navigationButton;
    private List<Feature> featureList;
    private NavigationMapRoute navigationMapRoute;
    private DirectionsRoute currentRout;
    private ImageButton cancelNavigatin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_splash_screen);
        Vocabulary.setLanguage();

        /*
          Start Splash screen
         */
        if(!isStarted) {
            isStarted = true;
            new Handler().postDelayed(() -> {
                Intent i = new Intent(MainActivity.this, SplashScreen.class);
                startActivity(i);
                finish();
            }, 1);
        }

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        ImageButton curLocationCamera = findViewById(R.id.curLocationButton);
        curLocationCamera.setOnClickListener(view -> {

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(curentLocation.getLatitude(), curentLocation.getLongitude())) // Sets the new camera position
                    .zoom(18) // Sets the zoom
                    .bearing(curentLocation.getBearing()) // Rotate the camera
                    .tilt(50) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 7000);
        });

        navigationButton = findViewById(R.id.navigationButton);
        navigationButton.setOnClickListener(view -> {
            NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                    .directionsRoute(currentRout)
                    .shouldSimulateRoute(false)
                    .build();
            NavigationLauncher.startNavigation(MainActivity.this, options);
        });

        ImageButton mVoiceBtn = findViewById(R.id.voiceBtn);
        mVoiceBtn.setOnClickListener(view -> speakToMic());


        ImageButton myAddress = findViewById(R.id.my_address);
        myAddress.setOnClickListener(view -> {
            toastMSG(address(curentLocation.getLongitude(), curentLocation.getLatitude()));
            speak(null, address(curentLocation.getLongitude(), curentLocation.getLatitude()));
        });

        ImageButton whatsNear = findViewById(R.id.what_near);
        whatsNear.setOnClickListener(view -> creatFeatureList());

        ImageButton whatsThere = findViewById(R.id.what_there);
        whatsThere.setOnClickListener(view -> creatFeatureList2());

        cancelNavigatin = findViewById(R.id.close);
        cancelNavigatin.setOnClickListener(view -> {
            Style style = mapboxMap.getStyle();
            assert style != null;
            style.removeLayer("click-layer-navig");
            style.removeSource(CLICK_CENTER_NAVIGATION);
            navigationMapRoute.updateRouteArrowVisibilityTo(false);
            navigationMapRoute.updateRouteVisibilityTo(false);
            navigationButton.setVisibility(View.INVISIBLE);
            cancelNavigatin.setVisibility(View.INVISIBLE);
        });
    }



    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/collos/cjxyyo0kz0g7j1cow76pb9i50"),
                style -> {
                    enableLocationComponent(style);
                    addResultLayer(style);
                    mapboxMap.addOnMapClickListener(MainActivity.this);
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
            Timber.d(exception.getLocalizedMessage());
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
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Style style = mapboxMap.getStyle();
        assert style != null;
        style.removeLayer("click-layer-navig");
        style.removeSource(CLICK_CENTER_NAVIGATION);
        addClickLayer2(style);
        // Move and display the click center layer's red marker icon to wherever the map was clicked on
        GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_NAVIGATION);
        if (clickLocationSource != null) {
            clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        }
        // Use the map click location to make a Tilequery API call
        Point destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        navigationButton.setEnabled(true);
        navigationButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.mapboxBlueDark));
        navigationButton.setVisibility(View.VISIBLE);
        cancelNavigatin.setVisibility(View.VISIBLE);
        getRoute(Point.fromLngLat(curentLocation.getLongitude(), curentLocation.getLatitude()), destinationPosition);

        return true;

    }

    private void getRoute(Point origin, Point destination) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            Timber.e("No routes found, check right user and accesss token");
                            return;
                        } else if (response.body().routes().size() == 0) {
                            Timber.e("No routes found");
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
                    public void onFailure(@NotNull Call<DirectionsResponse> call, @NotNull Throwable t) {
                        Timber.e("Error: %s", t.getMessage());
                    }
                });
    }

    /**
     * Get nearest POI
     */

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
            public void onResponse(@NotNull Call<FeatureCollection> call, @NotNull Response<FeatureCollection> response) {
                assert response.body() != null;
                featureList = response.body().features();
                toastMSG(new NearPoints(featureList).getClosestFeatureName());
                speak(null, new NearPoints(featureList).getClosestFeatureName());
                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<FeatureCollection> call, @NotNull Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get POI using orientation
     */

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
            public void onResponse(@NotNull Call<FeatureCollection> call, @NotNull Response<FeatureCollection> response) {
                assert response.body() != null;
                featureList = response.body().features();
                assert locationComponent.getCompassEngine() != null;
                String msg = new Orientation(curentLocation,featureList,locationComponent.getCompassEngine().getLastHeading()).getOrientationPoint();
                toastMSG(msg);
                speak(null, msg);

                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

                }
            }

            @Override
            public void onFailure(@NotNull Call<FeatureCollection> call, @NotNull Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add a map layer which will show a marker icon where the map was clicked
     */

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

    public void speak(String locale, String toSpeak) {
        new Speaker(this, locale, toSpeak);
    }

    // get voice input and handle it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (Vocabulary.commands != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                assert result != null;
                if (result.get(0).contains(Vocabulary.commands[0])) {
                    speak(null, address(curentLocation.getLongitude(), curentLocation.getLatitude()));
                } else if (result.get(0).contains(Vocabulary.commands[1])) {
                    getPOI();
                } else if (result.get(0).contains(Vocabulary.commands[2])) {
                    creatFeatureList2();
                } else {
                    Toast.makeText(getApplicationContext(), Vocabulary.WRONG_COMMAND, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), Vocabulary.FAILED_TO_RECOGNIZE_SPEECH, Toast.LENGTH_SHORT).show();
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
                    return Vocabulary.ADDRESS_NOT_FOUND;
                }
            }

            return Vocabulary.YOUR_ADDRESS + address;

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
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        if (item.getItemId() == R.id.userguide) {// Toast.makeText(this, "User Guide", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, UserGuide.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
    // Permissions
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
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
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


}
